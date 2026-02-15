package nginx.core;

import static java.lang.foreign.FunctionDescriptor.of;
import static java.lang.foreign.MemoryLayout.paddingLayout;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;

public interface NgHash extends NgGlobal {
	

	StructLayout ngx_table_elt_t = structLayout(
			JAVA_LONG.withName("hash"),
			NgString.ngx_str_t.withName("key"),
			NgString.ngx_str_t.withName("value"),
			ADDRESS.withName("lowcase_key"),
			ADDRESS.withName("next")
		).withName("ngx_table_elt_t");
	
	VarHandle
		vh_hash = ngx_table_elt_t.varHandle(PathElement.groupElement("hash")),
		// vh_key = ngx_table_elt_t.varHandle(PathElement.groupElement("key")),
		// vh_value = ngx_table_elt_t.varHandle(PathElement.groupElement("value")),
		vh_next = ngx_table_elt_t.varHandle(PathElement.groupElement("next"));
	
	long offsetKey = ngx_table_elt_t.byteOffset(PathElement.groupElement("key"));
	long offsetValue = ngx_table_elt_t.byteOffset(PathElement.groupElement("value"));
	
	record NgxTableElt(MemorySegment segment) {
		public long getHash() {
			return (long) vh_hash.get(segment, 0L);
		}
		public String getKey() {
			MemorySegment keySeg = segment.asSlice( ngx_table_elt_t.byteOffset(PathElement.groupElement("key")), NgString.ngx_str_t.byteSize() );
			return NgString.asString(keySeg);
		}
		public String getValue() {
			MemorySegment valueSeg = segment.asSlice( ngx_table_elt_t.byteOffset(PathElement.groupElement("value")), NgString.ngx_str_t.byteSize() );
			return NgString.asString(valueSeg);
		}
		public NgxTableElt getNext() {
			MemorySegment nextSeg = (MemorySegment) vh_next.get(segment, 0L);
			if ( nextSeg == MemorySegment.NULL ) {
				return null;
			}
			return new NgxTableElt( nextSeg.reinterpret(ngx_table_elt_t.byteSize()) );
		}
	}
	
	
	final MethodHandle
		ngx_hash_key = linker.downcallHandle(
				SYMBOL_LOOKUP.find("ngx_hash_key").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG ),
				Linker.Option.critical(true)
			),
		ngx_hash_key_lc = linker.downcallHandle(
				SYMBOL_LOOKUP.find("ngx_hash_key_lc").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG ),
				Linker.Option.critical(true)
			);
	/*
	NgGlobal.downcallTo(
		"ngx_hash_key",
		FunctionDescriptor.of( ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG )
	);
	*/

	static long ngx_hash_key(String value) {
		try {
			byte[] bytes = value.getBytes();
			ByteBuffer buf = ByteBuffer.wrap( bytes );
			MemorySegment bufSegement = MemorySegment.ofBuffer(buf);
			
			return (long) ngx_hash_key.invokeExact( bufSegement, (long) bytes.length );
		} catch (Throwable e) {
			throw new RuntimeException("Unable to call ngx_hash_key", e);
		}
	}
	
	static long ngx_hash_key_lc(String value) {
		try {
			byte[] bytes = value.getBytes();
			ByteBuffer buf = ByteBuffer.wrap( bytes );
			MemorySegment bufSegement = MemorySegment.ofBuffer(buf);
			
			return (long) ngx_hash_key_lc.invokeExact( bufSegement, (long) bytes.length );
		} catch (Throwable e) {
			throw new RuntimeException("Unable to call ngx_hash_key_lc", e);
		}
	}
	


	/*
typedef struct {
    ngx_hash_elt_t  **buckets;
    ngx_uint_t        size;
} ngx_hash_t;
	 */
	StructLayout ngx_hash_t = structLayout(
			ADDRESS.withName("buckets"),
			JAVA_LONG.withName("size")
		).withName("ngx_hash_t");

}
