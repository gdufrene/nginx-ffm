# Java FFM and nginx

This branch contains demo code shown at Devoxx France 2026.

Some git tag "devoxx_#" gives you the code for each step of this presentation. So you should start with `git checkout devoxx_1` then continue with each one.

## Demo of Arena / MemorySegment

start jshell and run ...

```java
var global = Arena.global();
var seg = global.allocate(10_000);
seg.set(ValueLayout.JAVA_LONG, 0, 123456L);
var hex = HexFormat.ofDelimiter(" ").withUpperCase();
var arr = new byte[16];
seg.asByteBuffer().get(arr);
hex.formatHex(arr);
global.close();
var confined = Arena.ofConfined();
seg = confined.allocate(10_000);
seg.set(ValueLayout.JAVA_DOUBLE, 0, 123.456);
confined.close();
seg.get(ValueLayout.JAVA_DOUBLE, 0);
```

## Demo of Symbol lookup, jextract and downcall

Get my fork of nginx.

`git clone https://github.com/gdufrene/nginx-ffm`

In that directory, checkout tag `step3`, configure and compile.

```bash
./auto/configure && make -j8
NG_HOME=$(pwd)
```

Generate usefull java source for this demo, compile.  
(replace /opt/homebrew/include if required for libraries headers)

```bash
jextract -I src \
-I src/core \
-I src/event \
-I src/http \
-I src/mail \
-I src/misc \
-I src/os \
-I src/os/unix \
-I src/stream \
-I /opt/homebrew/include \
-I objs \
--include-struct ngx_cycle_s \
--include-struct ngx_log_s \
--include-struct ngx_queue_s \
--include-struct ngx_rbtree_s \
--include-struct ngx_rbtree_node_s \
--include-typedef ngx_str_t \
--header-class-name ngx_core \
-t nginx \
--output objs/src/java \
src/core/ngx_cycle.h src/core/ngx_string.h
javac -sourcepath objs/src/java objs/src/java/nginx/ngx_str_t.java
```

Start a jshell 

`jshell --class-path objs/src/java`

and run ...

```java
import nginx.ngx_str_t;

long ngx_base64_encoded_length(long len) {
  return  (((len + 2) / 3) * 4);
}

var lookup = SymbolLookup.libraryLookup(Path.of("objs", "nginx.so"), Arena.global());
var linker = Linker.nativeLinker();
var arena = Arena.ofConfined();

var ngx_cycle = lookup.find("ngx_cycle").orElseThrow();

String s = "hello world";
long len = s.length();
MemorySegment src = ngx_str_t.allocate(arena);
ngx_str_t.len(src, len);
ngx_str_t.data(src, arena.allocateFrom(s));

MemorySegment dst = ngx_str_t.allocate(arena);
long dst_len = ngx_base64_encoded_length(len);
MemorySegment dstData = arena.allocate(dst_len); 
ngx_str_t.data(dst, dstData);

final MethodHandle ngx_encode_base64 = lookup.find("ngx_encode_base64")
  .map( seg -> linker.downcallHandle(seg, 
    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
  ))
  .orElseThrow();
ngx_encode_base64.invoke(dst, src);

String encoded = StandardCharsets.ISO_8859_1
  .decode( dstData.asByteBuffer() ).toString();
```

## Start nginx from java

One file to compile:

```bash
javac -d target/classes src/main/java/nginx/Nginx.java
```

replace NG_HOME path to nginx sources, run:

```bash
NG_HOME=/path/to/nginx java --enable-native-access=ALL-UNNAMED -cp target/classes nginx.Nginx
```

open http://localhost:8000/ 
hit Ctrl+C in console to stop nginx.

## Handle requests in java

Some interfaces define nginx structs, one interface per header file.  
Each interface contains one or many structs, var handlers, method handlers and sometimes wrap structs in a java object.

Different design choices can be done to map data and call functions ...
 * Lonely interface with only structs definitions , (such as NgArray)
 * Interface with varHandles and static methods , (such as NgString)
 * Neutral interface with an Implementation class , (such as NgPool)
 * Interface with inner class , (NgChainLink in NgBuffer)
 * Interface with embedded record , (NgxTableElt in NgHash)