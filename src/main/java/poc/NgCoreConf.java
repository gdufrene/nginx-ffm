package poc;

import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NgCoreConf {
	
	static StructLayout LAYOUT = new NgCycle.Types().core_conf_t;
	
	MemorySegment segment;
	
	public NgCoreConf(MemorySegment segment) {
		this.segment = segment.reinterpret(LAYOUT.byteSize());
		
		MemUtils.dump(this.segment);
		
		StructLayout str_t = new NgCycle.Types().str_t;
		
		BiConsumer<String, MemorySegment> printStr = (fieldName, strSeg) -> {
			// long offset = LAYOUT.byteOffset( PathElement.groupElement(fieldName) );
			// MemorySegment strSeg = segment.asSlice( offset, str_t.byteSize() );
			long len = (long) str_t.varHandle( PathElement.groupElement("len") ).get(strSeg, 0L);
			MemorySegment dataSeg = ((MemorySegment) (
					str_t
						.varHandle( PathElement.groupElement("data") )
						.get(strSeg, 0L)
					)).reinterpret(len);
			byte[] data = new byte[(int) len];
			dataSeg.asByteBuffer().get(data);
			String value = new String(data);
			System.out.println(fieldName + ": " + value);
		};
		
		MemorySegment ms = this.segment.asSlice(0x80, str_t.byteSize());
		printStr.accept("lock", ms);
		
		ms = this.segment.asSlice(0x90, str_t.byteSize());
		printStr.accept("pid", ms);
		
		ms = this.segment.asSlice(0xA0, str_t.byteSize());
		printStr.accept("oldpid", ms);
	}

	public MemorySegment getPidRef() {
		//return (MemorySegment) LAYOUT.varHandle( PathElement.groupElement("pid") ).get(segment, 0L);
		
		/* */
		long offset = LAYOUT.byteOffset( PathElement.groupElement("pid") );
		System.out.format("offset of pid: 0x%X\n", offset);
		/* */
		StructLayout str_t = new NgCycle.Types().str_t;
		MemorySegment ret = segment.asSlice( offset, str_t.byteSize() );
		
		System.out.println( "len -> " + (long) str_t.varHandle( PathElement.groupElement("len") ).get(ret, 0L) );
		
		return ret;
	}
	
	

}
