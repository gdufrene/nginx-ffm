package nginx.core;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;


public interface NgArray {
	
	StructLayout ngx_array_t = MemoryLayout.structLayout(
		ADDRESS.withName("elts"),
		JAVA_INT.withName("nelts"),
		MemoryLayout.paddingLayout(4),
		JAVA_LONG.withName("size"),
		JAVA_INT.withName("nalloc"),
		MemoryLayout.paddingLayout(4),
		ADDRESS.withName("pool")
	);

}

