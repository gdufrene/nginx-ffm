import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;

import ngx.ngx_str_t;

void main() throws Throwable {	
	
	var arena = Arena.ofConfined();
	
	var src = ngx_str_t.allocate(arena);
	var dst = ngx_str_t.allocate(arena);
	
	var data = arena.allocateFrom("Hello devoxx");
	ngx_str_t.data(src, data);
	ngx_str_t.len(src, data.byteSize()-1);
	
	long dstLen = data.byteSize() * 4 / 3;
	var dstData = arena.allocate(dstLen);
	ngx_str_t.data(dst, dstData);
	
	String NG_HOME = System.getProperty("user.home") + "/devoxx/nginx";
	var lookup = SymbolLookup.libraryLookup( Path.of(NG_HOME, "objs", "nginx.so"), arena);
	var linker = Linker.nativeLinker();
	
	MethodHandle ngx_encode_base64 = lookup.find("ngx_encode_base64")
			  .map( seg -> linker.downcallHandle(seg, 
			    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
			  ))
			  .orElseThrow();
	ngx_encode_base64.invoke(dst, src);
	String encoded = StandardCharsets.ISO_8859_1
	  .decode( dstData.asByteBuffer() ).toString();
	System.out.println("Encoded: " + encoded);

	
}