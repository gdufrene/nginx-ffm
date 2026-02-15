package nginx.core;

import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout.PathElement;
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
	
	VarHandle strLenHandle = ngx_str_t.varHandle( PathElement.groupElement("len") );
	VarHandle strDataHandle = ngx_str_t.varHandle( PathElement.groupElement("data") );
	
	static String asString(MemorySegment seg) {
		if ( seg.address() == NULL ) {
			return null;
		}
		// seg.reinterpret(ngx_str_t.byteSize());
		// MemUtils.dump(seg);
		long len = (long) strLenHandle.get(seg, 0L);
		if ( len == 0 ) return "";
		MemorySegment dataSeg = (MemorySegment) strDataHandle.get(seg, 0L);
		dataSeg = dataSeg.reinterpret(len);
		return StandardCharsets.UTF_8.decode( dataSeg.asByteBuffer() ).toString();
	}
	
//	static void set(MemorySegment seg, String string) {
//		byte[] bytes = string.getBytes( StandardCharsets.UTF_8 );
//		strLenHandle.set(seg, 0L, (long)bytes.length);
//		seg.asByteBuffer().position( (int) ngx_str_t.byteOffset(PathElement.groupElement("data")) ).put(bytes);
//	}
	
}
