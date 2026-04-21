package nginx.core;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

import static java.lang.foreign.ValueLayout.*;
import static nginx.core.NgGlobal.downcall;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.FunctionDescriptor.*;

public interface NgPool {

	static Optional<NgPool> ngx_create_pool(long size, NgLog log) {
		return NgPoolImpl.create(size, log);
	}
	static NgPool fromSegment(MemorySegment seg) {
		return new NgPoolImpl(seg);
	}

	MemorySegment getSegment();
	
	MemorySegment ngx_palloc(long size); 
	
}

class NgPoolImpl implements NgPool, NgGlobal {
	
	static final MethodHandle 
		ngx_create_pool,
		ngx_palloc;
	
	static {
		ngx_create_pool = downcall(
			"ngx_create_pool",
			of(ADDRESS, JAVA_LONG, ADDRESS)
		);
		ngx_palloc = downcall(
			"ngx_palloc",
			of(ADDRESS, ADDRESS, JAVA_LONG)
		);
	}
	
	MemorySegment pool;
	
	NgPoolImpl(MemorySegment pool) {
		this.pool = pool;
	}
	
	static Optional<NgPool> create(long size, NgLog log) {
		try {
			MemorySegment seg = (MemorySegment) ngx_create_pool.invokeExact(size, log.getMemorySegment());
			return seg.address() == NULL ? Optional.empty() : Optional.of(new NgPoolImpl(seg));
		} catch (Throwable e) {
			throw new RuntimeException("Unable to create ngx_pool", e);
		}
	}
	
	@Override
	public MemorySegment getSegment() {
		return pool;
	}
	
	public MemorySegment ngx_palloc(long size) {
		try {
			MemorySegment mem = (MemorySegment) ngx_palloc.invokeExact(pool, size);
			return mem.address() == NULL ? null : mem.reinterpret(size);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to allocate memory from ngx_pool", e);
		}
	}
}