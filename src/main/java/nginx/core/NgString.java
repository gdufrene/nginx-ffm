package nginx.core;

import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

public interface NgString extends NgGlobal {
	
	NgString NULL_STRING = new NgString() {};

	StructLayout ngx_str_t = structLayout(
		JAVA_LONG.withName("len"),
		ADDRESS.withName("data")
	);
	
	StructLayout ngx_keyval_t = structLayout(
		ngx_str_t.withName("key"),
		ngx_str_t.withName("value")
	);
	
	VarHandle 
		vhStrLen  = ngx_str_t.varHandle( groupElement("len") ),
		vhStrData = ngx_str_t.varHandle( groupElement("data") );
	
	static String asString(MemorySegment seg) {
		if ( seg.address() == NULL ) {
			return null;
		}
		long len = (long) vhStrLen.get(seg, 0L);
		if ( len == 0 ) return "";
		MemorySegment dataSeg = (MemorySegment) vhStrData.get(seg, 0L);
		dataSeg = dataSeg.reinterpret(len);
		return StandardCharsets.UTF_8.decode( dataSeg.asByteBuffer() ).toString();
	}
	
}
