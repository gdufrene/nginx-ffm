# Java FFM and nginx

This project tries to use nginx web server as a dynamic native library in java.  
It uses java Foreign Function and Memory API to load and link to native code.

The general idea is to wrap Servlet API around FFM downcalls to nginx functions.  
This kind of thing has been done already in "nginx Unit", an universal web app server project. (https://unit.nginx.org/).   
But this specific project is now "archived" and java support was quite limited (java-8, old javax.servlet API) and it uses JNI to perform downcall/upcall.  
I tried to contribute to this project (https://github.com/nginx/unit/issues/869#issuecomment-2423821507), 
but I encountered some random "deadlock" when the unit java module starts jvm.

I recently dig a bit around FFM and decided to try the other way : run nginx from java.  
This attempt is also the opportunity to use java 25 and recent FFM features.  

A goal should be to use nginx as the webstack for a spring-boot 4 API :)  
But it seems a loooong road to reach that.

I had to do some changes to nginx sources.  
So, this project is based on nginx forked from master on commit c93a0c48af87bbae1568eaf110e207e435bbe0bd

Modifications available at https://github.com/gdufrene/nginx

Applied modifications to allow and ease FFM usage ...

- compile as a shared library
- expose some static method from nginx.c to use them from Java
- create a method ngx_get_conf2 to be used rather than ngx_get_conf macro
- create a method ngx_dump_config_fn to ease config dump display, based on ngx_dump_config test case from main
- expose a buffer end operation to set some bitfield from java
- added some debug log
- Create a module "ngx_ffm_module", inspired from ngx_http_static and documentation sample module

## Nginx Compilation 

Checkout repository https://github.com/gdufrene/nginx

On mac, I use `brew install pcre2 zlib` to get minimal dependencies.

```sh
./auto/configure --add-module=src/ngx_ffm_module --with-debug
make -j8
```

It generates `objs/nginx.so` library to be loaded in java.


## Java part compile and run

- Java >= 25 required.
- maven 3.9+ suggested.

You must set NG_HOME env as the place where you checkout nginx sources, a compiled `nginx.so` should be in objs after compilation.  
After compiling modified nginx, it should be in "objs" directory.

```sh
mvn compile
mkdir logs
NG_HOME=/xxx/nginx mvn exec:java
```

## What is done

Nginx java class allow to bootstrap a nginx server with configuration from conf/nginx.conf.  
In that configuration a module declaration is done : 

```
http {
	# ...
	ffm enable;
	# ...
}
```

This command is just as test to verify module loading and configuration.  
When module is loaded (during cycle_init), an handler is attached to http 'content' phase.  
This handler tries to upcall a java function (if set) to manage the http request.  

The java upcall reference is set during bootstrap in Nginx java class.

```java
NgRequestHandler ffmHandler = new NgRequestHandler(ngCore);
ngCore.initFfmHandler(ffmHandler, arena);
```

When an http request comes, the ngx_ffm_module is triggered.  
If upcall succeed, some content is added to handle the http response.  

Right now, java part is able to:
- get uri and filter requests ( fixed filter /hello right now )
- set content-type
- response status code
- get a PrintWriter and output some content

Only works in singleProcess mode (ngx_single_process_cycle).  
The fork of processes in `ngx_master_process_cycle` is maybe interfering with memory management or garbage collector, not sure.
In master mode, as soon as a memory segment is given to a new java object, it seems to be stuck (non responsive jvm).  


**Tried with tls and http/2 is ok !**

The cool thing is that nginx support tls with openssl and easy configuration for http/2 and http/3.  
In theory all the request handling can be done by nginx without any change on the java upcall.  

Compile nginx with http/2 http/3 tls :

You will require openssl lib.  
with homebrew : `brew install openssl`


```sh
./auto/configure --add-module=src/ngx_ffm_module --with-http_ssl_module --with-http_v2_module --with-http_v3_module --with-debug
make -j8
```

The resulted shared library is now ~ 1.2 Mo.

Set-up a chain of certificates to test tls :

```sh
openssl req -x509 -newkey rsa:4096 -keyout ca.key -out ca.cert.pem \
  -sha256 -days 3650 -noenc \
  -subj "/C=FR/O=BigCompany/OU=BestTeamEver/CN=My-Root-Autority" \
  -addext "authorityKeyIdentifier=keyid,issuer" \
  -addext "basicConstraints=CA:TRUE,pathlen:1" \
  -addext "keyUsage=digitalSignature,cRLSign,keyCertSign"

openssl req -new -sha256 \
	-subj "/C=FR/O=BigCompany/OU=BestTeamEver/CN=My-Intermediate" \
  -newkey rsa:4096 -keyout intermediate.key -noenc \
  -out intermediate.csr.pem 

cat<<eof > intermediate.ext
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:TRUE,pathlen:0
keyUsage=digitalSignature,cRLSign,keyCertSign
eof

openssl x509 -req -in intermediate.csr.pem \
  -CA ca.cert.pem -CAkey ca.key \
  -days 1825 -extfile intermediate.ext \
  -out intermediate.cert.pem
  
openssl req -new -sha256 \
	-subj "/C=FR/O=BigCompany/OU=BestTeamEver/CN=mbpdeguillaume-1.home" \
  -newkey rsa:4096 -noenc -keyout localhost.key \
  -out localhost.csr.pem

cat <<eof > server.ext
authorityKeyIdentifier=keyid,issuer
keyUsage=nonRepudiation,digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=@alt_names
[alt_names]
DNS.1=localhost
DNS.2=127.0.0.1
eof

openssl x509 -req -in localhost.csr.pem \
 -CA intermediate.cert.pem -CAkey intermediate.key \
 -days 395 -extfile server.ext \
 -out localhost.cert.pem

cat localhost.cert.pem intermediate.cert.pem ca.cert.pem > conf/localhost.chain.pem
chmod 600 localhost.key
mv localhost.key conf
```

Add some configuration to `nginx.conf`

```
http {

	# ...

    ssl_session_cache   shared:SSL:10m;
    ssl_session_timeout 10m;
    
    server {
        listen       8443 ssl;
        
        http2 on;
        
        server_name  localhost;
        ssl_certificate     localhost.chain.pem;
        ssl_certificate_key localhost.key;
        ssl_protocols       TLSv1.2 TLSv1.3;
        ssl_ciphers         HIGH:!aNULL:!MD5;

	    # ...
        
   }

```

Then try some calls with curl or browser :

```sh
curl -v --cacert ca.cert.pem --http2-prior-knowledge https://localhost:8443/hello
```

**Now with http/3 and curl**

My browser don't try to connect with http/3.  
My curl is not happy with http/3 neither : `curl: option --http3-only: the installed libcurl version doesn't support this`.  

So, I need to build my own curl with quic http/3 support.  

Starts by getting build dependencies `brew install libtool automake autoconf libpsl openssl libnghttp3 pkgconf`

Then build nghttp3 and curl.  
Building nghttp3 seems required to get package-config happy in curl autoconf.  

```
mkdir -p $HOME/tmp/build
cd $HOME/tmp/build
git clone https://github.com/ngtcp2/nghttp3
cd nghttp3
git submodule update --init
autoreconf -fi
mkdir target
./configure --prefix=$HOME/tmp/build/nghttp3/target --enable-lib-only
make -j8
make install
cd $home/tmp/build
git clone https://github.com/curl/curl
cd curl
autoreconf -fi
LDFLAGS="-Wl,-rpath,/opt/homebrew/lib" ./configure --with-openssl=/opt/homebrew/Cellar/openssl@3/3.6.0 --with-openssl-quic --with-nghttp3=$HOME/tmp/build/nghttp3/target
make
```

So, now you should have `$HOME/tmp/build/nginx/src/curl` binary available ! :)

I suggest `CURL=$HOME/tmp/build/nginx/src/curl`. Then try `$CURL -V`, it should give something like :

```
curl 8.18.0-DEV (aarch64-apple-darwin24.6.0) libcurl/8.18.0-DEV OpenSSL/3.6.0 zlib/1.2.12 libidn2/2.3.8 libpsl/0.21.5 nghttp2/1.67.0 nghttp3/1.14.0-DEV
Release-Date: [unreleased]
Protocols: dict file ftp ftps gopher gophers http https imap imaps ipfs ipns ldap ldaps mqtt pop3 pop3s rtsp smb smbs smtp smtps telnet tftp ws wss
Features: alt-svc AsynchDNS HSTS HTTP2 HTTP3 HTTPS-proxy IDN IPv6 Largefile libz NTLM PSL SSL threadsafe TLS-SRP UnixSockets
```

And now let's configure nginx. (thanks to https://blog.yaakov.online/http-3-with-nginx/ for this).  

```
    server {
        listen       8443 ssl;
        listen  [::]:8443 ssl;
        http2 on;

        listen       8443 quic;
        listen  [::]:8443 quic;
        http3 on;
        add_header Alt-Svc 'h3=":8443"; ma=86400';

        server_name  localhost;
        ssl_certificate     localhost.chain.pem;
        ssl_certificate_key localhost.key;
        ssl_protocols       TLSv1.2 TLSv1.3;
        ssl_ciphers         HIGH:!aNULL:!MD5;
        # ...
```

It should be enough :)  
Both http2 and http3 are now available tru 8443 with tcp (http2) and udp (http3).  
Run nginx from nginx-ffm directory : `NG_HOME=/xxx/nginx mvn exec:java`  
The run curl, from nginx source directory (for easy access to ca.cert.pem).  

```sh
$CURL -v --cacert ca.cert.pem --http3-only https://localhost:8443/hello
```

Happy http3 java server with nginx :)

```
...
* SSL certificate verified via OpenSSL.
* Established connection to localhost (127.0.0.1 port 8443) from 127.0.0.1 port 55568
* using HTTP/3
* [HTTP/3] [0] OPENED stream for https://localhost:8443/hello
* [HTTP/3] [0] [:method: GET]
* [HTTP/3] [0] [:scheme: https]
* [HTTP/3] [0] [:authority: localhost:8443]
* [HTTP/3] [0] [:path: /hello]
* [HTTP/3] [0] [user-agent: curl/8.18.0-DEV]
* [HTTP/3] [0] [accept: */*]
> GET /hello HTTP/3
...
```


 
 
## What next ?

A lot of things to do !

Right now it's just a proof of concept and a learning use case for me.  
Of course, only use this project as a pedagogical material, not as some trusted code :)

[] More cleanup around binding to nginx native parts.  
    Tried an "interface" approach to reference structs and operation available.  
    MethodHandles, VarHandles and downcalls done in some Impl classes.
    
[] Not sure around Arena management.
    Global arena should be nice for global method handlers ?
    What about Memory allocation from java ?
    If this memory is freed with a nginx pool, should be ok ?
    What about implementing an Arena around nginx pool scopes ?

[] How to Test ?
   Find something better than a fail and learn process.  
   Right now, my process is like "run server, run a request, crash and debug, try to fix, redo everything".  

[] Implement more servlet operations.
    First basic OutputStream implementation done over a ngx_buf_t.  
    Efficient I/O based on nginx pools and buffers seems to be a good idea.  
    Specific Input and OutputStreams should be inspired by https://github.com/nginx/unit/blob/master/src/java/nxt_jni_InputStream.c


## Clean up

There is many different approach to map vars and methods to native code : 

* interface with static methods  
  seems to be a good fit for global vars and methods
* interface and Impl class with segment reference  
  for major structs ?
* record that map vars  
  for short-lived data or sub-struct elements ?

## Arena management

Most of memory is based on Arena.global because nginx manage all allocated memory himself.  
The main memory life-cycle is based on a per-request pool of allocated objects freed after request handling.  

## How to test native mappings

* For struct / memory mapping:  
It should be possible to save a MemorySegment from a real request, then, in a unit test, try to load that MemorySegment and do some tests with values extracted from a mapped object.

* For request handling logic:  
Maybe a lunch of the process, then execute some request with a client and check results tru the http response ?


## More servlet operations.

I target a subset of operation that allows me to demonstrate a spring application running with nginx as servlet engine.  

From a minimalistic spring boot application, those request/response methods are called for a simple "hello world" rest controller.

* For HttpServletRequest :

```
✅ RequestFacade.getAttribute
✅ RequestFacade.getCharacterEncoding (mock to UTF-8)
✅ RequestFacade.getContentType
✅ RequestFacade.getContextPath
✅ RequestFacade.getHeaders
RequestFacade.getHttpServletMapping
✅ RequestFacade.getMethod
✅ RequestFacade.getRemoteAddr (mock to "127.0.0.1")
✅ RequestFacade.getRequestURI
✅ RequestFacade.getSession (mock: always return null)
✅ RequestFacade.getUserPrincipal (mock: null)
✅ RequestFacade.isAsyncSupported (mock to false)
✅ RequestFacade.removeAttribute
✅ RequestFacade.setAttribute
```


* For HttpServletResponse :

```
✅ ResponseFacade.addHeader
✅ ResponseFacade.containsHeader
✅ ResponseFacade.getCharacterEncoding (mock to UTF-8)
✅ ResponseFacade.getContentType
✅ ResponseFacade.getHeader
✅ ResponseFacade.getHeaders
✅ ResponseFacade.getOutputStream
✅ ResponseFacade.getStatus
✅ ResponseFacade.setContentLengthLong
```

I checked current implemented methods inside NgRequest and NgResponse wrapper.

## Use with spring-mvc

Now that a minimal set of servlet operations are implemented, I should : 

- add spring-mvc as a dependency,
- bootstrap a small annotation based context with a controller,
- initialize a servletDispatcher and delegates incoming nginx request to that dispatcher,

Maybe I can try to dispatch nginx request log with slf4j to be able to configure level and pattern with simple configuration.

Maybe I can try to implement a spring-boot autoconf that configure and start nginx and use it as web server rather than tomcat.f


## Other ideas ?

Sure, I have more ideas than the time to invests on :)

- Use nginx as the spring boot web stack, as a replacement of the traditional tomcat embedded server.
  I wonder how it could behave.  
  nginx is event-based, I wonder how virtual thread features can be used together with that.

- Expose an Api to pop some dynamic reverse proxy could be nice.  
  It can transform nginx to a kind of small API-Gateway.  
  Nginx seems really efficient in hot changes of configurations and able to handler looooots of concurrent connections with low memory consumption.  
  Seems a good fit for container deployment.  
  Well, if java is not an overkill memory consumer :)

- Trying to use graalvm native compilation of my Java module to reduce memory consumption and startup time ?
  Even if right now, it starts like a rocket.

