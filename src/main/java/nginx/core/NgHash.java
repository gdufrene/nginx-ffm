package nginx.core;

import static java.lang.foreign.FunctionDescriptor.of;
import static java.lang.foreign.MemoryLayout.paddingLayout;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public interface NgHash {
	

	StructLayout ngx_table_elt_t = structLayout(
			JAVA_LONG.withName("hash"),
			NgString.ngx_str_t.withName("key"),
			NgString.ngx_str_t.withName("value"),
			ADDRESS.withName("lowcase_key"),
			ADDRESS.withName("next")
		).withName("ngx_table_elt_t");
	

	
}
