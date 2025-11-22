package poc;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

public class NgModule {
	
	static StructLayout module_t = MemoryLayout.structLayout(
		ValueLayout.JAVA_INT.withName("ctx_index"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("index"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.ADDRESS.withName("name"),
		
		ValueLayout.JAVA_INT.withName("spare0"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("spare1"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("signature"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.ADDRESS.withName("ctx"),
		ValueLayout.ADDRESS.withName("commands"),
		ValueLayout.JAVA_INT.withName("type"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.ADDRESS.withName("init_master"),
		ValueLayout.ADDRESS.withName("init_module"),
		ValueLayout.ADDRESS.withName("init_process"),
		ValueLayout.ADDRESS.withName("init_thread"),
		ValueLayout.ADDRESS.withName("exit_thread"),
		ValueLayout.ADDRESS.withName("exit_process"),
		ValueLayout.ADDRESS.withName("exit_master"),
		
		ValueLayout.ADDRESS.withName("spare_hook0"),
		ValueLayout.ADDRESS.withName("spare_hook1"),
		ValueLayout.ADDRESS.withName("spare_hook2"),
		ValueLayout.ADDRESS.withName("spare_hook3"),
		ValueLayout.ADDRESS.withName("spare_hook4"),
		ValueLayout.ADDRESS.withName("spare_hook5"),
		ValueLayout.ADDRESS.withName("spare_hook6"),
		ValueLayout.ADDRESS.withName("spare_hook7")
	);

	MemorySegment segment;
	
	public NgModule(MemorySegment segment) {
		this.segment = segment;
	}
	
	public int getIndex() {
		return (int) module_t.varHandle( PathElement.groupElement("index") ).get(segment, 0L);
	}
}
