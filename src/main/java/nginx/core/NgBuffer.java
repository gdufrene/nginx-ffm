package nginx.core;

import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.util.Optional;

public interface NgBuffer extends NgGlobal {
	
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
	
	MethodHandle ngx_create_temp_buf = linker.downcallHandle(
		SYMBOL_LOOKUP.find("ngx_create_temp_buf").orElseThrow(),
		FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_LONG)
	);
	
	static Optional<NgBuffer> ngx_create_temp_buf(NgPool pool, long size) {
		try {
			MemorySegment bufSeg = (MemorySegment) ngx_create_temp_buf.invokeExact(pool.getSegment(), size);
			bufSeg = bufSeg.reinterpret(ngx_buf_t.byteSize());
			return bufSeg.address() == NULL ? Optional.empty() : Optional.of(new NgBufferImpl(bufSeg));
		} catch (Throwable e) {
			throw new RuntimeException("Unable to create temp buf", e);
		}
	}
	
	MethodHandle ngx_alloc_chain_link = linker.downcallHandle(
		SYMBOL_LOOKUP.find("ngx_alloc_chain_link").orElseThrow(),
		FunctionDescriptor.of(ADDRESS, ADDRESS)
	);
	
	
	VarHandle chainBufHandle = ngx_chain_t.varHandle( PathElement.groupElement("buf") );
	VarHandle nextHandle = ngx_chain_t.varHandle( PathElement.groupElement("next") );
	
	MethodHandle ngx_chain_end = linker.downcallHandle(
		SYMBOL_LOOKUP.find("ngx_chain_end").orElseThrow(),
		FunctionDescriptor.ofVoid(ADDRESS)
	);
	
	class NgChainLink {
		MemorySegment segment;
		NgPool pool;
		
		public NgChainLink(MemorySegment segment, NgPool pool) {
			this.segment = segment;
			this.pool = pool;
		}

		public NgChainLink setBuffer(NgBuffer buf) {
			chainBufHandle.set(segment, 0L, buf.getSegment());
			/*
			NgChainLink next = ngx_alloc_chain_link(pool).orElseThrow();
			nextHandle.set(segment, 0L, next.segment);
			*/
			return this;
		}
		
		public MemorySegment getSegment() {
			return segment;
		}

		public void end() {
			// chainBufHandle.set(segment, 0L, buf.getSegment());
			try {
				ngx_chain_end.invokeExact(segment);
			} catch (Throwable e) {
				throw new RuntimeException("Unable to set next to NULL", e);
			}
		}
	}
	
	static Optional<NgChainLink> ngx_alloc_chain_link(NgPool pool) {
		try {
			MemorySegment linkSeg = (MemorySegment) ngx_alloc_chain_link.invokeExact(pool.getSegment());
			linkSeg = linkSeg.reinterpret(ngx_chain_t.byteSize());
			linkSeg.fill((byte)0);
			return linkSeg.address() == NULL ? Optional.empty() : Optional.of(new NgChainLink(linkSeg, pool));
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
	
	VarHandle posHandle = ngx_buf_t.varHandle( PathElement.groupElement("pos") );
	VarHandle lastHandle = ngx_buf_t.varHandle( PathElement.groupElement("last") );
	VarHandle endHandle = ngx_buf_t.varHandle( PathElement.groupElement("end") );
	
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

