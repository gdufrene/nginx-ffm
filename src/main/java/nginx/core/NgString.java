package nginx.core;

import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

public interface NgString extends NgGlobal {

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
			return "null";
		}
		seg.reinterpret(ngx_str_t.byteSize());
		// MemUtils.dump(seg);
		long len = (long) strLenHandle.get(seg, 0L);
		MemorySegment dataSeg = (MemorySegment) strDataHandle.get(seg, 0L);
		dataSeg = dataSeg.reinterpret(len);
		return StandardCharsets.UTF_8.decode( dataSeg.asByteBuffer() ).toString();
	}
	
}
