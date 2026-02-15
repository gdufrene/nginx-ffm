package nginx.core;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Optional;

public interface NgCycle extends NgGlobal {

		
	StructLayout queue_t = MemoryLayout.structLayout(
			ValueLayout.ADDRESS.withName("prev"),
			ValueLayout.ADDRESS.withName("next")
		);

	StructLayout rbtree_t = MemoryLayout.structLayout(
		ValueLayout.ADDRESS.withName("root"),
		ValueLayout.ADDRESS.withName("sentinel"),
		ValueLayout.ADDRESS.withName("insert")
	);
	

	StructLayout rbtree_node_t = MemoryLayout.structLayout(
			MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("key"),
		ValueLayout.ADDRESS.withName("left"),
		ValueLayout.ADDRESS.withName("right"),
		ValueLayout.ADDRESS.withName("parent"),
		MemoryLayout.paddingLayout(7),
		ValueLayout.JAVA_BYTE.withName("color"),
		MemoryLayout.paddingLayout(7),
		ValueLayout.JAVA_BYTE.withName("data")
	);	


		
	StructLayout cycle_t = MemoryLayout.structLayout(
		ValueLayout.ADDRESS.withName("conf_ctx")
			.withTargetLayout(ValueLayout.ADDRESS), // list of module config context pointers
		
		ValueLayout.ADDRESS.withName("pool"),
		ValueLayout.ADDRESS.withName("log"),
		NgLog.log_t.withName("new_log"),
		
		ValueLayout.JAVA_INT.withName("log_use_stderr"),
		MemoryLayout.paddingLayout(4),
		
		ValueLayout.ADDRESS.withName("files"),
		ValueLayout.ADDRESS.withName("free_connections"),
		ValueLayout.JAVA_INT.withName("free_connection_n"),
		MemoryLayout.paddingLayout(4),
		
		
		ValueLayout.ADDRESS.withName("modules"),
		ValueLayout.JAVA_INT.withName("modules_n"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("modules_used"),
		MemoryLayout.paddingLayout(4),
		
		queue_t.withName("reusable_connections_queue"),
		ValueLayout.JAVA_INT.withName("reusable_connections_n"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_LONG.withName("connections_reuse_time"),
		
		NgArray.ngx_array_t.withName("listening"),
		NgArray.ngx_array_t.withName("paths"),
		
		NgArray.ngx_array_t.withName("config_dump"),
		rbtree_t.withName("config_dump_rbtree"),
		rbtree_node_t.withName("config_dump_sentinel"),
		
		NgList.ngx_list_t.withName("open_files"),
		NgList.ngx_list_t.withName("shared_memory"),
		
		ValueLayout.JAVA_INT.withName("connection_n"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("files_n"),
		MemoryLayout.paddingLayout(4),
		
		ValueLayout.ADDRESS.withName("connections"),
		ValueLayout.ADDRESS.withName("read_events"),
		ValueLayout.ADDRESS.withName("write_events"),
		
		ValueLayout.JAVA_INT.withName("old_cycle"),
		MemoryLayout.paddingLayout(4),
		
		NgString.ngx_str_t.withName("conf_file"),
		NgString.ngx_str_t.withName("conf_param"),
		NgString.ngx_str_t.withName("conf_prefix"),
		NgString.ngx_str_t.withName("prefix"),
		NgString.ngx_str_t.withName("error_log"),
		NgString.ngx_str_t.withName("lock_file"),
		NgString.ngx_str_t.withName("hostname")
		
	).withName("ngx_cycle_t");
	
	VarHandle 
		conf_ctxHandle = cycle_t.varHandle(PathElement.groupElement("conf_ctx")),
		modules_nHandle = cycle_t.varHandle(PathElement.groupElement("modules_n"));
		
	// dump cycle_t layout for debugging
	static void debugDumpCycleLayout(MemorySegment cycleSegment) {
		/// StructLayout cycle_t = NgCycleLayout();
		System.out.println("cycle_t layout:");
		// Iterate over the members of the struct layout
		for (MemoryLayout member : cycle_t.memberLayouts()) {
			if ( member.name().isEmpty() ) continue;
			if ( member instanceof StructLayout ) {
				continue;
			}
			String name = member.name().get();
			System.out.printf("Member: %s, Offset: %d, Size: %d\n",
				name,
				cycle_t.byteOffset(PathElement.groupElement(name)),
				member.byteSize());
			// print member type and value
			System.out.printf("  Type: %s\n", member);
			// print value
			Object value = cycle_t.varHandle(PathElement.groupElement(name)).get(cycleSegment,
					0L);
			System.out.printf("  Value: %s\n", value);
			
		}
	}
	
	StructLayout core_conf_t = MemoryLayout.structLayout(
		ValueLayout.JAVA_INT.withName("daemon"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("master"),
		MemoryLayout.paddingLayout(4),
		
		ValueLayout.JAVA_INT.withName("timer_resolution"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("shutdown_timeout"),
		MemoryLayout.paddingLayout(4),
		
		ValueLayout.JAVA_INT.withName("worker_processes"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("debug_points"),
		MemoryLayout.paddingLayout(4),
		
		ValueLayout.JAVA_INT.withName("rlimit_nofile"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_LONG.withName("rlimit_core"),
		
		ValueLayout.JAVA_INT.withName("priority"),
		MemoryLayout.paddingLayout(4),
		
		ValueLayout.JAVA_INT.withName("cpu_affinity_auto"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("cpu_affinity_n"),
		MemoryLayout.paddingLayout(4),
		/*
		ValueLayout.ADDRESS.withName("cpu_affinity"),
		// TODO: Fix memory struct layout 8 bytes over 
		*/
		
		ValueLayout.ADDRESS.withName("username"),
		ValueLayout.JAVA_INT.withName("user"),
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("group"),
		MemoryLayout.paddingLayout(4),
		
		NgString.ngx_str_t.withName("working_directory"),
		NgString.ngx_str_t.withName("lock_file"),
		
		NgString.ngx_str_t.withName("pid"),
		NgString.ngx_str_t.withName("oldpid"),
		
		NgArray.ngx_array_t.withName("env"),
		ValueLayout.ADDRESS.withName("environment"),
		
		MemoryLayout.paddingLayout(4),
		ValueLayout.JAVA_INT.withName("transparent")
	);
	
	
	MethodHandle ngx_init_cycle = NgGlobal.downcallTo(
			"ngx_init_cycle",
			FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.ADDRESS )
		);
	

	
	static NgCycle create() throws Throwable {
		MemorySegment segment = Arena.global().allocate( cycle_t.byteSize() );
		if ( segment.address() == NULL ) {
			throw new OutOfMemoryError("Failed to allocate memory for NgCycle");
		}
		return new NgCycleImpl( segment );
	}
	
	MemorySegment getSegment();
	void zeroFill();
	NgLog getLog();
	void setLog(NgLog log);
	Optional<NgCycle> initCycle() throws Throwable;
	void setPool(NgPool pool);

	MemorySegment getModule(int i);

}

class NgCycleImpl implements NgCycle {
	
	MemorySegment ngCycle;
	
	public NgCycleImpl(MemorySegment ngCycle) {
		this.ngCycle = ngCycle;
	}
	
	@Override
	public MemorySegment getSegment() {
		return ngCycle;
	}
	
	static final VarHandle 
		logHandle = cycle_t.varHandle(
				PathElement.groupElement("log")
		),
		poolHandle = cycle_t.varHandle(
				PathElement.groupElement("pool")
		);

	@Override
	public void setPool(NgPool pool) {
		poolHandle.set(ngCycle, 0L, pool.getSegment() );
	}

	public MemorySegment getLogSegment() {
		return (MemorySegment) logHandle.get(ngCycle, 0L);
	}
	
	public void zeroFill() {
		ngCycle.fill( (byte)0 );
	}

	NgLog log;

	@Override
	public NgLog getLog() {
		MemorySegment seg = (MemorySegment) logHandle.get(ngCycle, 0L);
		if (seg.address() == 0) return null;
		if (log != null && seg.address() == log.getMemorySegment().address()) {
			return log;
		} else {
			return NgLog.fromSegment( seg );
		}
	}
	
	@Override
	public void setLog(NgLog log) {
		logHandle.set(ngCycle, 0L, log.getMemorySegment() );
		this.log = log;
	}
	
	@Override
	public Optional<NgCycle> initCycle() throws Throwable {
		MemorySegment ms = (MemorySegment) ngx_init_cycle.invokeExact( ngCycle );
		if ( ms.address() == NULL ) {
			return Optional.empty();
		}
		return Optional.of( new NgCycleImpl(ms.reinterpret(NgCycle.cycle_t.byteSize())) );
	}
	
	public MemorySegment getModule(int i) {
		int modules_n = (int) cycle_t.varHandle(
				PathElement.groupElement("modules_n")
				).get( ngCycle, 0L );
		if ( i < 0 || i >= modules_n ) {
			return null;
		}
		return ((MemorySegment) cycle_t.varHandle(PathElement.groupElement("modules"))
				.get( ngCycle, 0L ))
				.reinterpret( ValueLayout.ADDRESS.byteSize() * modules_n )
				.getAtIndex( ValueLayout.ADDRESS, i )
				.reinterpret( NgModule.ngx_module_t.byteSize() );
	}
	
}
