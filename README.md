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
- Temp: add some nasty debug as printf 
- Create a module "ngx_ffm_module", inspired from ngx_http_static and documentation sample module

## Nginx Compilation 

Checkout repository https://github.com/gdufrene/nginx

On mac, I use `brew install pcre2 zlib` to get minimal dependencies.

```sh
./auto/configure --add-module=src/ngx_ffm_module
make
```

It generates `objs/nginx.so` library to be loaded in java.


## Java part compile and run

- Java >= 25 required.
- maven 3.9+ suggested.

You must set NG_HOME env as the place where you compiled `nginx.so`.  
After compiling modified nginx, it should be in "objs" directory.

```sh
mvn compile
mkdir logs
NG_HOME=/xxx/nginx/objs java --enable-native-access=ALL-UNNAMED -cp target/classes nginx.Nginx
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
- get a PrintWriter and output some content, limited to 4096 bytes

Only works in singleProcess mode (ngx_single_process_cycle).  
As soon as a memory segment is given to a new java object, it seems to be stuck.  
The fork of processes in `ngx_master_process_cycle` is maybe interfering with memory management or garbage collector, not sure.


**Tried with tls and http/2 is ok !**

The cool thing is that nginx support tls with openssl and easy configuration for http/2 and http/3.  
In theory all the request handling can be done by nginx without any change on the java upcall.  

Compile nginx with http/2 http/3 tls :

You will require openssl lib.  
with homebrew : `brew install openssl`


```sh
./auto/configure --add-module=src/ngx_ffm_module --with-http_ssl_module --with-http_v2_module --with-http_v3_module
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


## What next ?

A lot of things to do !

Right now it's just a proof of concept and a learning use case for me.  
Of course, only use this project as a pedagogical material, not as some trusted code :)

[ ] More cleanup around binding to nginx native parts.  
    Tried an "interface" approach to reference structs and operation available.  
    MethodHandles, VarHandles and downcalls done in some Impl classes.
    
[ ] Not sure around Arena management.
    Global arena should be nice for global method handlers ?
    What about Memory allocation from java ?
    If this memory is freed with a nginx pool, should be ok ?
    What about implementing an Arena around nginx pool scopes ?

[ ] Implement more servlet operations.
    First basic OutputStream implementation done over a ngx_buf_t.  
    Efficient I/O based on nginx pools and buffers seems to be a good idea.  
    Specific Input and OutputStreams should be inspired by https://github.com/nginx/unit/blob/master/src/java/nxt_jni_InputStream.c


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

