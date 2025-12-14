package nginx.core;

import static java.lang.foreign.FunctionDescriptor.of;
import static java.lang.foreign.FunctionDescriptor.ofVoid;
import static java.lang.foreign.ValueLayout.*;


import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;


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

	static NgArray ngx_array_create(NgPool pool, int n, long size) {
		return new NgArrayImpl(pool, n, size);
	}
	void ngx_array_destroy();
	void ngx_array_push();
	void ngx_array_push_n(int n);
	
	
	VarHandle eltsHandle = ngx_array_t.varHandle(
		PathElement.groupElement("elts")
	);
	
	VarHandle neltsHandle = ngx_array_t.varHandle(
		PathElement.groupElement("nelts")
	);
	
	VarHandle sizeHandle = ngx_array_t.varHandle(
		PathElement.groupElement("size")
	);
	
	VarHandle nallocHandle = ngx_array_t.varHandle(
		PathElement.groupElement("nalloc")
	);
	
	VarHandle poolHandle = ngx_array_t.varHandle(
		PathElement.groupElement("pool")
	);
	
}

class NgArrayImpl implements NgArray, NgGlobal {
	
	private static MethodHandle 
		ngx_array_create,
		ngx_array_destroy,
		ngx_array_push,
		ngx_array_push_n;
	
	static {
		ngx_array_create = linker.downcallHandle(
			SYMBOL_LOOKUP.find("ngx_array_create").orElseThrow(),
			of(ADDRESS, ADDRESS, JAVA_INT, JAVA_LONG)
		);
		
		ngx_array_destroy = linker.downcallHandle(
			SYMBOL_LOOKUP.find("ngx_array_destroy").orElseThrow(),
			ofVoid(ADDRESS)
		);
		
		ngx_array_push = linker.downcallHandle(
			SYMBOL_LOOKUP.find("ngx_array_push").orElseThrow(),
			ofVoid(ADDRESS)
		);
		
		ngx_array_push_n = linker.downcallHandle(
			SYMBOL_LOOKUP.find("ngx_array_push_n").orElseThrow(),
			ofVoid(ADDRESS, JAVA_INT)
		);
	}
	
	MemorySegment array;
	NgPool pool;

	public NgArrayImpl(NgPool pool, int n, long size) {
		try {
			array = (MemorySegment) ngx_array_create.invokeExact(pool.getSegment(), n, size);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to create ngx_array", e);
		}
	}

	@Override
	public void ngx_array_destroy() {
		try {
			ngx_array_destroy.invokeExact(array);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to destroy ngx_array", e);
		}
	}

	@Override
	public void ngx_array_push() {
		try {
			ngx_array_push.invokeExact(array);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to push ngx_array", e);
		}
	}

	@Override
	public void ngx_array_push_n(int n) {
		try {
			ngx_array_push_n.invokeExact(array, n);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to push ngx_array_n", e);
		}
	}
	
}

