package poc;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

import nginx.core.NgLog;
import nginx.core.NgPool;

public class NgCycle {
	
	
	static class Types {

		
		StructLayout queue_t = MemoryLayout.structLayout(
				ValueLayout.ADDRESS.withName("prev"),
				ValueLayout.ADDRESS.withName("next")
			);
		
		/*
	typedef struct {
	    void        *elts;
	    ngx_uint_t   nelts;
	    size_t       size;
	    ngx_uint_t   nalloc;
	    ngx_pool_t  *pool;
	} ngx_array_t;
		 */
		StructLayout array_t = MemoryLayout.structLayout(
			ValueLayout.ADDRESS.withName("elts"),
			ValueLayout.JAVA_INT.withName("nelts"),
			MemoryLayout.paddingLayout(4),
			ValueLayout.JAVA_INT.withName("size"),
			MemoryLayout.paddingLayout(4),
			ValueLayout.JAVA_INT.withName("nalloc"),
			MemoryLayout.paddingLayout(4),
			ValueLayout.ADDRESS.withName("pool")
		);
		
	/*
	struct ngx_rbtree_s {
	    ngx_rbtree_node_t     *root;
	    ngx_rbtree_node_t     *sentinel;
	    ngx_rbtree_insert_pt   insert;
	};
	 */
		StructLayout rbtree_t = MemoryLayout.structLayout(
			ValueLayout.ADDRESS.withName("root"),
			ValueLayout.ADDRESS.withName("sentinel"),
			ValueLayout.ADDRESS.withName("insert")
		);
	
	/*
	struct ngx_rbtree_node_s {
	    ngx_rbtree_key_t       key;
	    ngx_rbtree_node_t     *left;
	    ngx_rbtree_node_t     *right;
	    ngx_rbtree_node_t     *parent;
	    u_char                 color;
	    u_char                 data;
	};
	 */
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
		
	/*
	struct ngx_list_part_s {
	    void             *elts;
	    ngx_uint_t        nelts;
	    ngx_list_part_t  *next;
	};
	 */
		StructLayout list_part_t = MemoryLayout.structLayout(
			ValueLayout.ADDRESS.withName("elts"),
			MemoryLayout.paddingLayout(4),
			ValueLayout.JAVA_INT.withName("nelts"),
			ValueLayout.ADDRESS.withName("next")
		);
		
	/*
	typedef struct {
	    ngx_list_part_t  *last;
	    ngx_list_part_t   part;
	    size_t            size;
	    ngx_uint_t        nalloc;
	    ngx_pool_t       *pool;
	} ngx_list_t;
	 */
		StructLayout list_t = MemoryLayout.structLayout(
			ValueLayout.ADDRESS.withName("last"),
			list_part_t.withName("part"),
			MemoryLayout.paddingLayout(4),
			ValueLayout.JAVA_INT.withName("size"),
			MemoryLayout.paddingLayout(4),
			ValueLayout.JAVA_INT.withName("nalloc"),
			ValueLayout.ADDRESS.withName("pool")
		);
		
		/*
		typedef struct {
		    size_t      len;
		    u_char     *data;
		} ngx_str_t;
		*/
		StructLayout str_t = MemoryLayout.structLayout(
			ValueLayout.JAVA_INT.withName("len"),
			MemoryLayout.paddingLayout(4),
			ValueLayout.ADDRESS.withName("data")
		) //.withByteAlignment(8);
				;
		
		StructLayout cycle_t = MemoryLayout.structLayout(
			ValueLayout.ADDRESS.withName("conf_ctx"),
			
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
			
			array_t.withName("listening"),
			array_t.withName("paths"),
			
			array_t.withName("config_dump"),
			rbtree_t.withName("config_dump_rbtree"),
			rbtree_node_t.withName("config_dump_sentinel"),
			
			list_t.withName("open_files"),
			list_t.withName("shared_memory"),
			
			ValueLayout.JAVA_INT.withName("connection_n"),
			MemoryLayout.paddingLayout(4),
			ValueLayout.JAVA_INT.withName("files_n"),
			MemoryLayout.paddingLayout(4),
			
			ValueLayout.ADDRESS.withName("connections"),
			ValueLayout.ADDRESS.withName("read_events"),
			ValueLayout.ADDRESS.withName("write_events"),
			
			ValueLayout.JAVA_INT.withName("old_cycle"),
			MemoryLayout.paddingLayout(4),
			
			str_t.withName("conf_file"),
			str_t.withName("conf_param"),
			str_t.withName("conf_prefix"),
			str_t.withName("prefix"),
			str_t.withName("error_log"),
			str_t.withName("lock_file"),
			str_t.withName("hostname")
			
		);
		
		// dump cycle_t layout for debugging
		static void debugDumpCycleLayout(MemorySegment cycleSegment) {
			StructLayout cycle_t = NgCycleLayout();
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
		
		
		
		/*
typedef struct {
    ngx_flag_t                daemon;
    ngx_flag_t                master;

    ngx_msec_t                timer_resolution;
    ngx_msec_t                shutdown_timeout;

    ngx_int_t                 worker_processes;
    ngx_int_t                 debug_points;

    ngx_int_t                 rlimit_nofile;
    off_t                     rlimit_core;

    int                       priority;

    ngx_uint_t                cpu_affinity_auto;
    ngx_uint_t                cpu_affinity_n;
    ngx_cpuset_t             *cpu_affinity;

    char                     *username;
    ngx_uid_t                 user;
    ngx_gid_t                 group;

    ngx_str_t                 working_directory;
    ngx_str_t                 lock_file;

    ngx_str_t                 pid;
    ngx_str_t                 oldpid;

    ngx_array_t               env;
    char                    **environment;

    ngx_uint_t                transparent;  
} ngx_core_conf_t;
		 */
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
			
			str_t.withName("working_directory"),
			str_t.withName("lock_file"),
			
			str_t.withName("pid"),
			str_t.withName("oldpid"),
			
			array_t.withName("env"),
			ValueLayout.ADDRESS.withName("environment"),
			
			MemoryLayout.paddingLayout(4),
			ValueLayout.JAVA_INT.withName("transparent")
		);
	}
	
	static StructLayout NgCycleLayout() {
		return new NgCycle.Types().cycle_t;
	};
	
	MemorySegment ngCycle;
	NgCore ngCore;
	
	public NgCycle(Arena arena, NgCore ngCore) {
		this.ngCycle = arena.allocate( NgCycleLayout() );
		this.ngCore = ngCore;
	}
	
	static VarHandle logHandle = NgCycleLayout().varHandle(
		PathElement.groupElement("log")
	);
	
	
	public void setLog(NgLog log) {
		logHandle.set( ngCycle, 0L, log.getMemorySegment() );
		this.log = log;
	}

	public void setPool(NgPool pool) {
		NgCycleLayout().varHandle( PathElement.groupElement("pool") ).set( ngCycle, 0L, pool.getSegment() );
	}

	public MemorySegment getLogSegment() {
		return (MemorySegment) NgCycleLayout().varHandle( PathElement.groupElement("log") )
				.get( ngCycle, 0L);
	}
	
	public void zeroFill() {
		ngCycle.fill( (byte)0 );
	}

	NgLog log;

	public NgLog getLog() {
		MemorySegment seg = (MemorySegment) logHandle.get(ngCycle, 0L);
		if (seg.address() == 0) return null;
		if (seg.address() == log.getMemorySegment().address()) {
			return log;
		} else {
			return NgLog.fromSegment( seg );
		}
	}
	
}
