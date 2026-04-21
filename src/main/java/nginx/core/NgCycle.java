package nginx.core;

import static nginx.core.NgGlobal.downcall;
import static nginx.core.NgString.ngx_str_t;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Optional;

public interface NgCycle extends NgGlobal {

	StructLayout queue_t = MemoryLayout.structLayout(
		ADDRESS.withName("prev"), 
		ADDRESS.withName("next")
	);

	StructLayout rbtree_t = MemoryLayout.structLayout(
		ADDRESS.withName("root"), 
		ADDRESS.withName("sentinel"),
		ADDRESS.withName("insert")
	);

	StructLayout rbtree_node_t = MemoryLayout.structLayout(
		MemoryLayout.paddingLayout(4), 
		JAVA_INT.withName("key"),
		ADDRESS.withName("left"), 
		ADDRESS.withName("right"), 
		ADDRESS.withName("parent"),
		MemoryLayout.paddingLayout(7), 
		JAVA_BYTE.withName("color"), 
		MemoryLayout.paddingLayout(7),
		JAVA_BYTE.withName("data")
	);

	StructLayout cycle_t = MemoryLayout.structLayout(
		ADDRESS.withName("conf_ctx").withTargetLayout(ADDRESS), 
		ADDRESS.withName("pool"), 
		ADDRESS.withName("log"), 
		NgLog.log_t.withName("new_log"),

		JAVA_INT.withName("log_use_stderr"), 
		MemoryLayout.paddingLayout(4),

		ADDRESS.withName("files"), 
		ADDRESS.withName("free_connections"),
		JAVA_INT.withName("free_connection_n"), 
		MemoryLayout.paddingLayout(4),

		ADDRESS.withName("modules"), 
		JAVA_INT.withName("modules_n"),
		MemoryLayout.paddingLayout(4), 
		JAVA_INT.withName("modules_used"), 
		MemoryLayout.paddingLayout(4),

		queue_t.withName("reusable_connections_queue"), 
		JAVA_INT.withName("reusable_connections_n"),
		MemoryLayout.paddingLayout(4), 
		JAVA_LONG.withName("connections_reuse_time"),

		NgArray.ngx_array_t.withName("listening"), 
		NgArray.ngx_array_t.withName("paths"),

		NgArray.ngx_array_t.withName("config_dump"), 
		rbtree_t.withName("config_dump_rbtree"),
		rbtree_node_t.withName("config_dump_sentinel"),

		NgList.ngx_list_t.withName("open_files"), 
		NgList.ngx_list_t.withName("shared_memory"),

		JAVA_INT.withName("connection_n"), 
		MemoryLayout.paddingLayout(4),
		JAVA_INT.withName("files_n"), 
		MemoryLayout.paddingLayout(4),

		ADDRESS.withName("connections"), 
		ADDRESS.withName("read_events"),
		ADDRESS.withName("write_events"),

		JAVA_INT.withName("old_cycle"), 
		MemoryLayout.paddingLayout(4),

		ngx_str_t.withName("conf_file"), 
		ngx_str_t.withName("conf_param"),
		ngx_str_t.withName("conf_prefix"), 
		ngx_str_t.withName("prefix"),
		ngx_str_t.withName("error_log"), 
		ngx_str_t.withName("lock_file"),
		ngx_str_t.withName("hostname")

	).withName("ngx_cycle_t");

	VarHandle 
		conf_ctxHandle  = cycle_t.varHandle(groupElement("conf_ctx")),
		modules_nHandle = cycle_t.varHandle(groupElement("modules_n"));

	// dump cycle_t layout for debugging
	static void debugDumpCycleLayout(MemorySegment cycleSegment) {
		/// StructLayout cycle_t = NgCycleLayout();
		System.out.println("cycle_t layout:");
		// Iterate over the members of the struct layout
		for (MemoryLayout member : cycle_t.memberLayouts()) {
			if (member.name().isEmpty())
				continue;
			if (member instanceof StructLayout) {
				continue;
			}
			String name = member.name().get();
			System.out.printf("Member: %s, Offset: %d, Size: %d\n", name,
					cycle_t.byteOffset(groupElement(name)), member.byteSize());
			// print member type and value
			System.out.printf("  Type: %s\n", member);
			// print value
			Object value = cycle_t.varHandle(groupElement(name)).get(cycleSegment, 0L);
			System.out.printf("  Value: %s\n", value);

		}
	}

	StructLayout core_conf_t = MemoryLayout.structLayout(JAVA_INT.withName("daemon"),
		MemoryLayout.paddingLayout(4), 
		JAVA_INT.withName("master"), 
		MemoryLayout.paddingLayout(4),

		JAVA_INT.withName("timer_resolution"), 
		MemoryLayout.paddingLayout(4),
		JAVA_INT.withName("shutdown_timeout"), 
		MemoryLayout.paddingLayout(4),

		JAVA_INT.withName("worker_processes"), 
		MemoryLayout.paddingLayout(4),
		JAVA_INT.withName("debug_points"), 
		MemoryLayout.paddingLayout(4),

		JAVA_INT.withName("rlimit_nofile"), 
		MemoryLayout.paddingLayout(4),
		JAVA_LONG.withName("rlimit_core"),

		JAVA_INT.withName("priority"), 
		MemoryLayout.paddingLayout(4),

		JAVA_INT.withName("cpu_affinity_auto"), 
		MemoryLayout.paddingLayout(4),
		JAVA_INT.withName("cpu_affinity_n"), 
		MemoryLayout.paddingLayout(4),
		/*
		 * ADDRESS.withName("cpu_affinity"), // TODO: Fix memory struct
		 * layout 8 bytes over
		 */

		ADDRESS.withName("username"), 
		JAVA_INT.withName("user"),
		MemoryLayout.paddingLayout(4), 
		JAVA_INT.withName("group"), 
		MemoryLayout.paddingLayout(4),

		ngx_str_t.withName("working_directory"), 
		ngx_str_t.withName("lock_file"),

		ngx_str_t.withName("pid"), 
		ngx_str_t.withName("oldpid"),

		NgArray.ngx_array_t.withName("env"), 
		ADDRESS.withName("environment"),

		MemoryLayout.paddingLayout(4), 
		JAVA_INT.withName("transparent")
	);

	MethodHandle ngx_init_cycle = downcall(
		"ngx_init_cycle",
		FunctionDescriptor.of(ADDRESS.withTargetLayout(cycle_t), ADDRESS)
	);

	static NgCycle create() throws Throwable {
		MemorySegment segment = Arena.global().allocate(cycle_t.byteSize());
		if (segment.address() == NULL) {
			throw new OutOfMemoryError("Failed to allocate memory for NgCycle");
		}
		return new NgCycleImpl(segment);
	}

	MemorySegment getSegment();

	void zeroFill();

	NgLog getLog();

	void setLog(NgLog log);

	Optional<NgCycle> initCycle() throws Throwable;

	void setPool(NgPool pool);

	// MemorySegment getModule(int i);

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
		logHandle  = cycle_t.varHandle(groupElement("log")),
		poolHandle = cycle_t.varHandle(groupElement("pool"));

	@Override
	public void setPool(NgPool pool) {
		poolHandle.set(ngCycle, 0L, pool.getSegment());
	}

	public MemorySegment getLogSegment() {
		return (MemorySegment) logHandle.get(ngCycle, 0L);
	}

	public void zeroFill() {
		ngCycle.fill((byte) 0);
	}

	NgLog log;

	@Override
	public NgLog getLog() {
		MemorySegment seg = (MemorySegment) logHandle.get(ngCycle, 0L);
		if (seg.address() == 0)
			return null;
		if (log != null && seg.address() == log.getMemorySegment().address()) {
			return log;
		} else {
			return NgLog.fromSegment(seg);
		}
	}

	@Override
	public void setLog(NgLog log) {
		logHandle.set(ngCycle, 0L, log.getMemorySegment());
		this.log = log;
	}

	@Override
	public Optional<NgCycle> initCycle() throws Throwable {
		MemorySegment ms = (MemorySegment) ngx_init_cycle.invokeExact(ngCycle);
		if (ms.address() == NULL) {
			return Optional.empty();
		}
		return Optional.of(new NgCycleImpl(ms));
	}

}
