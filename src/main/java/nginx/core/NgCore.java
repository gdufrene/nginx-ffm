package nginx.core;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

import poc.FfmRequestHandler;

public interface NgCore extends NgGlobal {

		final MethodHandle
			ngx_get_options = NgGlobal.downcallTo(
				"ngx_get_options",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			),
			ngx_encode_base64 = NgGlobal.downcallTo(
				"ngx_encode_base64",
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS, ValueLayout.ADDRESS )
			),
			ngx_strerror_init = NgGlobal.downcallTo(
				"ngx_strerror_init",
				FunctionDescriptor.of( ValueLayout.JAVA_INT )
			),
			ngx_time_init = NgGlobal.downcallTo(
				"ngx_time_init",
				FunctionDescriptor.ofVoid()
			),
			ngx_regex_init = NgGlobal.downcallTo(
				"ngx_regex_init",
				FunctionDescriptor.ofVoid()
			),
			ngx_log_init = NgGlobal.downcallTo(
				"ngx_log_init",
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS  )
			),
			ngx_create_pool = NgGlobal.downcallTo(
				"ngx_create_pool",
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS  )
			),
			ngx_os_init = NgGlobal.downcallTo(
				"ngx_os_init",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			),
			ngx_save_argv = NgGlobal.downcallTo(
				"ngx_save_argv",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			),
			ngx_process_options = NgGlobal.downcallTo(
				"ngx_process_options",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			),
			ngx_crc32_table_init = NgGlobal.downcallTo(
				"ngx_crc32_table_init",
				FunctionDescriptor.of( ValueLayout.JAVA_INT )
			),
			ngx_slab_sizes_init = NgGlobal.downcallTo(
				"ngx_slab_sizes_init",
				FunctionDescriptor.ofVoid()
			),
			ngx_add_inherited_sockets = NgGlobal.downcallTo(
				"ngx_add_inherited_sockets",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			),
			ngx_preinit_modules = NgGlobal.downcallTo(
				"ngx_preinit_modules",
				FunctionDescriptor.of( ValueLayout.JAVA_INT )
			),
			ngx_os_status = NgGlobal.downcallTo(
				"ngx_os_status",
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS )
			),
			ngx_get_conf = NgGlobal.downcallTo(
				"ngx_get_conf2",
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS )
			),
			ngx_init_signals = NgGlobal.downcallTo(
				"ngx_init_signals",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			),
			ngx_create_pidfile = NgGlobal.downcallTo(
				"ngx_create_pidfile",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS )
			),
			ngx_log_redirect_stderr = NgGlobal.downcallTo(
				"ngx_log_redirect_stderr",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			),
			ngx_log_stderr = NgGlobal.downcallTo(
				"ngx_log_stderr",
				FunctionDescriptor.ofVoid( ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS  )
			),
			ngx_single_process_cycle = NgGlobal.downcallTo(
				"ngx_single_process_cycle",
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS )
			),
			ngx_master_process_cycle = NgGlobal.downcallTo(
				"ngx_master_process_cycle",
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS )
			),
			ngx_daemon = NgGlobal.downcallTo(
				"ngx_daemon",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS  )
			),
			ngx_dump_config_fn = NgGlobal.downcallTo(
				"ngx_dump_config_fn",
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS )
			),
			ngx_create_temp_buffer = NgGlobal.downcallTo(
				"ngx_create_temp_buf",
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG )
			),
			ngx_ssl_init = NgGlobal.downcallTo(
				"ngx_ssl_init",
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);	

		StructLayout StringLayout = MemoryLayout.structLayout(
			    ValueLayout.JAVA_LONG.withName("len"),
			    ValueLayout.ADDRESS.withName("data")
			).withName("ngx_str_t");
		
		static String encodeBase64(String src) throws Throwable {
			var len = StringLayout.varHandle(PathElement.groupElement("len"));
			//var LEN = MethodHandles.insertCoordinates(len, 1, 0L);
			
			var data = StringLayout.varHandle(PathElement.groupElement("data"));
			//var DATA = MethodHandles.insertCoordinates(data, 1, 0L);
			
			// byte[] srcBytes = src.getBytes();

			try (Arena arena = Arena.ofConfined()) {
				MemorySegment srcData = arena.allocateFrom(src);
				MemorySegment srcLayout = arena.allocate(StringLayout);
				// LEN.set(srcLayout, src.length());
				len.set(srcLayout, 0, (int) srcData.byteSize());
				
				// DATA.set(srcLayout, srcData);
				data.set(srcLayout, 0, srcData);
				
				MemorySegment dstLayout = arena.allocate(StringLayout);
				// DATA.set(dstLayout, arena.allocate(4 * ((src.length() / 3) + 1) + 1));
				data.set(dstLayout, 0, arena.allocate(200) );
				
				// MemorySegment ms = arena.allocateFrom(src);
				ngx_encode_base64.invokeExact( dstLayout, srcLayout );
				//.get(dstLayout);
				// int dstLen = (int) LEN.get(dstLayout);
				long dstLen = (long) len.get(dstLayout, 0L);
				// MemorySegment b64data = (MemorySegment) DATA.get(dstLayout);
				MemorySegment b64data = (MemorySegment) data.get(dstLayout, 0L);
				return new String( b64data.reinterpret(dstLen).toArray(ValueLayout.JAVA_BYTE) );
			}
			
			// return result.reinterpret(Integer.MAX_VALUE).getString(0);
		}
		
		static int strErrorInit() throws Throwable {
			return (int) ngx_strerror_init.invokeExact();
		}
		
		static void timeInit() throws Throwable {
			ngx_time_init.invokeExact();
		}
		
		static void regexInit() throws Throwable {
			ngx_regex_init.invokeExact();
		}
		
		static int osInit(NgLog log) throws Throwable {
			return (int) ngx_os_init.invokeExact( log.getMemorySegment() );
		}
		
		static int ngx_ssl_init(NgLog log) throws Throwable {
			return (int) ngx_ssl_init.invokeExact( log.getMemorySegment() );
		}
		
		static int saveArgv(NgCycle cycle, String[] args) throws Throwable {
			Arena arena = Arena.global();
			
			MemorySegment argvSegment = arena.allocate(ValueLayout.ADDRESS, args.length);
			
			for (int i = 0; i < args.length; i++) {
				MemorySegment argSegment = arena.allocateFrom( args[i] );
				argvSegment.setAtIndex( ValueLayout.ADDRESS, i, argSegment );
			}
			
			return (int) ngx_save_argv.invokeExact(cycle.getSegment(), args.length, argvSegment );
		}
		
		static int processOptions(NgCycle cycle) throws Throwable {
			return (int) ngx_process_options.invokeExact( cycle.getSegment() );
		}
		
		static int preinitModules() throws Throwable {
			return (int) ngx_preinit_modules.invokeExact();
		}


		
		static void osStatus(NgLog log) throws Throwable {
			ngx_os_status.invokeExact( log.getMemorySegment() );
		}
		
		
		static NgCoreConf getCoreConfig(NgCycle cycle) throws Throwable {
			MemorySegment coreModuleSegment = SYMBOL_LOOKUP.find("ngx_core_module").orElseThrow();
			MemorySegment res = (MemorySegment) ngx_get_conf.invokeExact( cycle.getSegment(), coreModuleSegment );
			return new NgCoreConf( res );
		}

		static int initSignals(NgLog log) throws Throwable {
			return (int) ngx_init_signals.invokeExact( log.getMemorySegment() );
			
		}
		
		static int createPidFile(NgCoreConf coreConf, NgLog log) throws Throwable {
			return (int) ngx_create_pidfile.invokeExact( coreConf.getPidRef(), log.getMemorySegment() );
		}
		
		static int logRedirectStderr(NgCycle cycle) throws Throwable {
			int res = (int) ngx_log_redirect_stderr.invokeExact( cycle.getSegment() );
			SYMBOL_LOOKUP.find("ngx_use_stderr")
				.orElseThrow()
				.reinterpret(4)
				.set(ValueLayout.JAVA_INT, 0L, 0);
			return res;
		}
		
		static void singleProcessCycle(NgCycle cycle) throws Throwable {
			ngx_single_process_cycle.invokeExact( cycle.getSegment() );
		}
		
		static void masterProcessCycle(NgCycle cycle) throws Throwable {
			ngx_master_process_cycle.invokeExact( cycle.getSegment() );
		}
		
		static int getOptions(String[] args) throws Throwable {
			Arena arena = Arena.global();
			
			MemorySegment argvSegment = arena.allocate(ValueLayout.ADDRESS, args.length);
			
			for (int i = 0; i < args.length; i++) {
				MemorySegment argSegment = arena.allocateFrom( args[i] );
				argvSegment.setAtIndex( ValueLayout.ADDRESS, i, argSegment );
			}
			
			return (int) ngx_get_options.invokeExact( args.length, argvSegment );
		}
		
		static int crc32TableInit() throws Throwable {
			return (int) ngx_crc32_table_init.invokeExact();
		}

		static void assignCycle(NgCycle cycle) {
			SYMBOL_LOOKUP.findOrThrow("ngx_cycle")
				.reinterpret(8)
				.set( ValueLayout.ADDRESS, 0L, cycle.getSegment() );
		}
		
		static NgCycle currentCycle() {
			MemorySegment cyclePtr = SYMBOL_LOOKUP.findOrThrow("ngx_cycle")
				.reinterpret(8L)
				.get( ValueLayout.ADDRESS, 0L )
				.reinterpret( NgCycle.cycle_t.byteSize() );
			return new NgCycleImpl( cyclePtr );
		}

		static void initProcessIds() {
			ProcessHandle current = ProcessHandle.current();
			current.info().command().orElse("nginx");
			
			long pid = current.pid();
			long ppid = current.parent().map( ProcessHandle::pid ).orElse(-1L);
			
			SYMBOL_LOOKUP.findOrThrow("ngx_pid")
				.reinterpret(4)
				.set( ValueLayout.JAVA_INT, 0L, (int) pid );
			
			SYMBOL_LOOKUP.findOrThrow("ngx_parent")
				.reinterpret(4)
				.set( ValueLayout.JAVA_INT, 0L, (int) ppid );
		}
		
		static int daemon(NgLog log) throws Throwable {
			int res = (int) ngx_daemon.invokeExact(log.getMemorySegment());
			
			SYMBOL_LOOKUP.findOrThrow("ngx_daemonized")
				.reinterpret(4)
				.set( ValueLayout.JAVA_INT, 0L, 1 );
			
			return res;
		}

		static void slabSizesInit() throws Throwable {
			ngx_slab_sizes_init.invokeExact();
		}
		
		static void dumpConfig(NgCycle cycle) throws Throwable {
			ngx_dump_config_fn.invokeExact( cycle.getSegment() );
		}

		static void setMaxSocket(int i) {
			SYMBOL_LOOKUP.findOrThrow("ngx_max_sockets")
				.reinterpret(4)
				.set( ValueLayout.JAVA_INT, 0L, i );
		}

}
