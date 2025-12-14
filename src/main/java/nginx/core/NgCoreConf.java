package nginx.core;

import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

public class NgCoreConf {
	
	static StructLayout LAYOUT = NgCycle.core_conf_t;
	
	MemorySegment segment;
	
	public NgCoreConf(MemorySegment segment) {
		this.segment = segment.reinterpret(LAYOUT.byteSize());
	}

	public MemorySegment getPidRef() {
		long offset = LAYOUT.byteOffset( PathElement.groupElement("pid") );
		MemorySegment ret = segment.asSlice( offset, NgString.ngx_str_t.byteSize() );		
		return ret;
	}
	
	

}
