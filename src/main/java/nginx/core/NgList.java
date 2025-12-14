package nginx.core;

import static java.lang.foreign.FunctionDescriptor.of;
import static java.lang.foreign.FunctionDescriptor.ofVoid;
import static java.lang.foreign.MemoryLayout.paddingLayout;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public interface NgList {
	
	StructLayout ngx_part_t = structLayout(
			ADDRESS.withName("elts"),
			JAVA_INT.withName("nelts"),
			paddingLayout(4),
			ADDRESS.withName("next")
		);
	
	StructLayout ngx_list_t = structLayout(
			ADDRESS.withName("last"),
			ngx_part_t.withName("part"),
			JAVA_LONG.withName("size"),
			JAVA_INT.withName("nalloc"),
			paddingLayout(4),
			ADDRESS.withName("pool")
		);

}
