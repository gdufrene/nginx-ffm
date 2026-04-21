package nginx.core;

import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.*;
import static nginx.core.NgGlobal.downcall;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.util.Optional;

public interface NgBuffer extends NgGlobal {
	
/*
typedef struct {
    ngx_int_t    num;
    size_t       size;
} ngx_bufs_t;
 */
	StructLayout ngx_bufs_t = structLayout(
		JAVA_INT.withName("num"),
		MemoryLayout.paddingLayout(4),
		JAVA_LONG.withName("size")
	);
	
	StructLayout ngx_buf_t = structLayout(
		ADDRESS.withName("pos"),
		ADDRESS.withName("last"),
		JAVA_LONG.withName("file_pos"),
		JAVA_LONG.withName("file_last"),
		
		ADDRESS.withName("start"),
		ADDRESS.withName("end"),
		ADDRESS.withName("tag"),
		ADDRESS.withName("file"),
		ADDRESS.withName("shadow"),
		
		JAVA_INT.withName("flags")
	);
	
	StructLayout ngx_chain_t = structLayout(
		ADDRESS.withName("buf").withTargetLayout(ngx_buf_t),
		ADDRESS.withName("next")
	);
	
	MethodHandle 
		ngx_create_temp_buf = downcall(
			"ngx_create_temp_buf", 
			FunctionDescriptor.of(ADDRESS.withTargetLayout(ngx_buf_t), ADDRESS, JAVA_LONG)
		),
		ngx_alloc_chain_link = downcall(
			"ngx_alloc_chain_link",
			FunctionDescriptor.of(ADDRESS.withTargetLayout(ngx_chain_t), ADDRESS)
		);
		
	VarHandle 
		chainBufHandle = ngx_chain_t.varHandle( groupElement("buf") ),
		nextHandle     = ngx_chain_t.varHandle( groupElement("next") );
		
	MethodHandle 
		ngx_chain_end = linker.downcallHandle(
			SYMBOL_LOOKUP.find("ngx_chain_end").orElseThrow(),
			FunctionDescriptor.ofVoid(ADDRESS)
		);
	
	static Optional<NgBuffer> ngx_create_temp_buf(NgPool pool, long size) {
		try {
			MemorySegment bufSeg = (MemorySegment) ngx_create_temp_buf.invokeExact(pool.getSegment(), size);
			return bufSeg.address() == NULL ? Optional.empty() : Optional.of(new NgBufferImpl(bufSeg));
		} catch (Throwable e) {
			throw new RuntimeException("Unable to create temp buf", e);
		}
	}
	
	static Optional<NgBuffer> ngx_calloc_buf(NgPool pool) {
		MemorySegment seg = pool.ngx_palloc(ngx_buf_t.byteSize());
		if (seg == null) {
			return Optional.empty();
		}
		return Optional.of(new NgBufferImpl(seg));
	}
	
	class NgChainLink {
		MemorySegment segment;
		
		public NgChainLink(MemorySegment segment) {
			this.segment = segment;
		}

		public NgChainLink setBuffer(NgBuffer buf) {
			chainBufHandle.set(segment, 0L, buf.getSegment());
			return this;
		}
		
		public MemorySegment getSegment() {
			return segment;
		}

		public NgChainLink end() {
			try {
				ngx_chain_end.invokeExact(segment);
				return this;
			} catch (Throwable e) {
				throw new RuntimeException("Unable to set next to NULL", e);
			}
		}
	}
	
	static Optional<NgChainLink> ngx_alloc_chain_link(NgPool pool) {
		try {
			MemorySegment linkSeg = (MemorySegment) ngx_alloc_chain_link.invokeExact(pool.getSegment());
			return linkSeg.address() == NULL ? Optional.empty() : Optional.of(new NgChainLink(linkSeg));
		} catch (Throwable e) {
			throw new RuntimeException("Unable to allocate chain link", e);
		}
	}

	long remaining();

	void write(byte[] b, int off, int toWrite);

	NgBuffer write(int b);
	
	MemorySegment getSegment();
	

}

class NgBufferImpl implements NgBuffer {
	MemorySegment segment;
	
	public NgBufferImpl(MemorySegment segment) {
		this.segment = segment;
	}
	
	final VarHandle 
		posHandle  = ngx_buf_t.varHandle( groupElement("pos") ),
		lastHandle = ngx_buf_t.varHandle( groupElement("last") ),
		endHandle  = ngx_buf_t.varHandle( groupElement("end") );
	
	public long remaining() {
		long pos = ((MemorySegment) posHandle.get(segment, 0L)).address();
		long end = ((MemorySegment) endHandle.get(segment, 0L)).address();
		return end - pos;
	}
	
	@Override
	public void write(byte[] b, int off, int toWrite) {
		// TODO Auto-generated method stub
		// b->last = ngx_cpymem(b->last, b, 3);
		ByteBuffer buf = ByteBuffer.wrap(b);
		MemorySegment last = (MemorySegment) lastHandle.get(segment, 0L);
		last = last.reinterpret(toWrite);
		last.asByteBuffer().put(buf.position(off).limit(off + toWrite));
		last = last.asSlice(toWrite, 0);
		lastHandle.set(segment, 0L, last);
	}
	
	@Override
	public NgBuffer write(int b) {
		MemorySegment last = (MemorySegment) lastHandle.get(segment, 0L);
		last = last.reinterpret(1);
		last.asByteBuffer().put((byte) (b & 0xFF));
		last.asSlice(1, 0);
		lastHandle.set(segment, 0L, last);
		return null;
	}
	
	public MemorySegment getSegment() {
		return segment;
	}
}

