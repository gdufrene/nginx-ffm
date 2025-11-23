package poc;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

import nginx.core.NgGlobal;
import nginx.core.NgLog;

public class NgCore {
	
	// static final String NG_HOME = System.getenv().getOrDefault("NG_HOME", "nginx");

		final Linker linker;
		final SymbolLookup NgLib;
		// final Arena arena;
		
		final MethodHandle
			ngx_get_options,
			ngx_show_version_info,
			ngx_encode_base64,
			ngx_strerror_init,
			ngx_time_init,
			ngx_regex_init,
			ngx_log_init,
			ngx_create_pool,
			ngx_os_init,
			ngx_save_argv,
			ngx_process_options,
			ngx_crc32_table_init,
			ngx_slab_sizes_init,
			ngx_add_inherited_sockets,
			ngx_preinit_modules,
			ngx_init_cycle,
			ngx_os_status,
			ngx_get_conf,
			ngx_init_signals,
			ngx_create_pidfile,
			ngx_log_redirect_stderr,
			ngx_log_stderr,
			ngx_single_process_cycle,
			ngx_master_process_cycle,
			ngx_daemon,
			ngx_dump_config_fn,
			ngx_http_send_response,
			ngx_http_send_header,
			ngx_http_discard_request_body,
			ngx_create_temp_buffer;
		
		public NgCore(Arena arena) {
			// this.arena = arena;
			this.linker = Linker.nativeLinker();
			this.NgLib = NgGlobal.SYMBOL_LOOKUP; 
					// SymbolLookup.libraryLookup( Path.of(NG_HOME, "objs/nginx.so"), arena);
			

			ngx_get_options =  linker.downcallHandle(
				NgLib.find("ngx_get_options").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_show_version_info = null;
			/*
			ngx_show_version_info = linker.downcallHandle(
				NgLib.find("ngx_show_version").orElseThrow(),
				FunctionDescriptor.ofVoid()
			);
			*/
			
			ngx_encode_base64 = linker.downcallHandle(
				NgLib.find("ngx_encode_base64").orElseThrow(),
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS, ValueLayout.ADDRESS )
			);
			
			ngx_strerror_init = linker.downcallHandle(
				NgLib.find("ngx_strerror_init").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT )
			);
			
			ngx_time_init = linker.downcallHandle(
				NgLib.find("ngx_time_init").orElseThrow(),
				FunctionDescriptor.ofVoid()
			);
			
			ngx_regex_init = linker.downcallHandle(
				NgLib.find("ngx_regex_init").orElseThrow(),
				FunctionDescriptor.ofVoid()
			);
			
			ngx_log_init = linker.downcallHandle(
				NgLib.find("ngx_log_init").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS  )
			);
			
			ngx_create_pool = linker.downcallHandle(
				NgLib.find("ngx_create_pool").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS  )
			);
			
			ngx_os_init = linker.downcallHandle(
				NgLib.find("ngx_os_init").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_save_argv  = linker.downcallHandle(
				NgLib.find("ngx_save_argv").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_process_options = linker.downcallHandle(
				NgLib.find("ngx_process_options").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_crc32_table_init  = linker.downcallHandle(
				NgLib.find("ngx_crc32_table_init").orElseThrow(),
				FunctionDescriptor.of(ValueLayout.JAVA_INT)
			);
			
			ngx_slab_sizes_init = linker.downcallHandle(
				NgLib.find("ngx_slab_sizes_init").orElseThrow(),
				FunctionDescriptor.ofVoid()
			);
			
			ngx_add_inherited_sockets = linker.downcallHandle(
				NgLib.find("ngx_add_inherited_sockets").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_dump_config_fn = linker.downcallHandle(
				NgLib.find("ngx_dump_config_fn").orElseThrow(),
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS )
			);
				
			/*
			 * 			ngx_preinit_modules,
			ngx_init_cycle,
			ngx_os_status,
			ngx_get_conf,
			ngx_init_signals,
			ngx_create_pidfile,
			ngx_log_redirect_stderr,
			ngx_single_process_cycle;
			 */
			
			ngx_preinit_modules = linker.downcallHandle(
				NgLib.find("ngx_preinit_modules").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT )
			);
			
			ngx_init_cycle = linker.downcallHandle(
				NgLib.find("ngx_init_cycle").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.ADDRESS )
			);
			
			ngx_os_status = linker.downcallHandle(
				NgLib.find("ngx_os_status").orElseThrow(),
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS )
			);
			
			/* */
			ngx_get_conf = linker.downcallHandle(
				NgLib.find("ngx_get_conf2").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS )
			);
			/* */
			
			ngx_init_signals = linker.downcallHandle(
				NgLib.find("ngx_init_signals").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_create_pidfile = linker.downcallHandle(
				NgLib.find("ngx_create_pidfile").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS )
			);
			
			ngx_log_redirect_stderr = linker.downcallHandle(
				NgLib.find("ngx_log_redirect_stderr").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_log_stderr  = linker.downcallHandle(
				NgLib.find("ngx_log_stderr").orElseThrow(),
				FunctionDescriptor.ofVoid( ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS  )
			);
			
			ngx_single_process_cycle = linker.downcallHandle(
				NgLib.find("ngx_single_process_cycle").orElseThrow(),
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS )
			);
			
			ngx_master_process_cycle = linker.downcallHandle(
				NgLib.find("ngx_master_process_cycle").orElseThrow(),
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS )
			);
			
			ngx_daemon = linker.downcallHandle(
				NgLib.find("ngx_daemon").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS  )
			);
			
			ngx_http_send_response = linker.downcallHandle(
				NgLib.find("ngx_http_send_response").orElseThrow(),
				FunctionDescriptor.ofVoid( ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS )
			);
			
			ngx_http_send_header = linker.downcallHandle(
				NgLib.find("ngx_http_send_header").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_http_discard_request_body = linker.downcallHandle(
				NgLib.find("ngx_http_discard_request_body").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS )
			);
			
			ngx_create_temp_buffer = linker.downcallHandle(
				NgLib.find("ngx_create_temp_buf").orElseThrow(),
				FunctionDescriptor.of( ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG )
			);
		}
		
		public void initFfmHandler( NgRequestHandler handler, Arena arena ) throws Throwable {
			
			MethodHandle upcallHandler = MethodHandles.lookup().bind(
				handler,
				"handleRequest",
				MethodType.methodType(int.class, MemorySegment.class)
			);
			
			MemorySegment upcallFunc = linker.upcallStub(
				upcallHandler,
				FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS ),
				// arena
				arena
			);
			
			NgLib.findOrThrow("ngx_http_ffm_upcall")
				.reinterpret(8)
				.set( ValueLayout.ADDRESS, 0L, upcallFunc );
		}
		
		public void showVersion() throws Throwable {
			ngx_show_version_info.invokeExact();
		}
		
		StructLayout StringLayout = MemoryLayout.structLayout(
			    ValueLayout.JAVA_LONG.withName("len"),
			    ValueLayout.ADDRESS.withName("data")
			).withName("ngx_str_t");
		
		public String encodeBase64(String src) throws Throwable {
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
		
		public int strErrorInit() throws Throwable {
			return (int) ngx_strerror_init.invokeExact();
		}
		
		public void timeInit() throws Throwable {
			ngx_time_init.invokeExact();
		}
		
		public void regexInit() throws Throwable {
			ngx_regex_init.invokeExact();
		}
		
		/*
		public NgLog logInit(String prefix, String logFile) throws Throwable {
			MemorySegment logSegment = (MemorySegment) ngx_log_init.invokeExact( 
				arena.allocateFrom(prefix),
				arena.allocateFrom(logFile)
			);
			
			return new NgLog(logSegment);
		}
		*/
		
		/*
		public NgPool createPool(int size, NgLog log) throws Throwable {
			MemorySegment poolSegment = (MemorySegment) ngx_create_pool.invokeExact( 
				size,
				log.logSegment
			);
			
			return new NgPool( poolSegment );
		}
		*/
		
		public int osInit(NgLog log) throws Throwable {
			return (int) ngx_os_init.invokeExact( log.getMemorySegment() );
		}
		
		public int saveArgv(NgCycle cycle, String[] args) throws Throwable {
			Arena arena = Arena.global();
			
			MemorySegment argvSegment = arena.allocate(ValueLayout.ADDRESS, args.length);
			
			for (int i = 0; i < args.length; i++) {
				MemorySegment argSegment = arena.allocateFrom( args[i] );
				argvSegment.setAtIndex( ValueLayout.ADDRESS, i, argSegment );
			}
			
			return (int) ngx_save_argv.invokeExact(cycle.ngCycle, args.length, argvSegment );
		}
		
		public int processOptions(NgCycle cycle) throws Throwable {
			return (int) ngx_process_options.invokeExact( cycle.ngCycle );
		}
		
		public int preinitModules() throws Throwable {
			return (int) ngx_preinit_modules.invokeExact();
		}

		public Optional<NgCycle> initCycle(NgCycle cycle) throws Throwable {
			MemorySegment ms = (MemorySegment) ngx_init_cycle.invokeExact( cycle.ngCycle );
			if ( ms.address() == 0 ) {
				return Optional.empty();
			}
			cycle.ngCycle = ms.reinterpret(NgCycle.NgCycleLayout().byteSize());
			// System.out.println("After initCycle:");
			// MemUtils.dump(cycle.ngCycle);
			return Optional.of(cycle);
		}
		
		public void osStatus(NgLog log) throws Throwable {
			ngx_os_status.invokeExact( log.getMemorySegment() );
		}
		
		/*
		public NgCoreConf getCoreConfig(NgCycle cycle) throws Throwable {
			MemorySegment coreModuleSegment = NgLib.find("ngx_core_module").orElseThrow();
			NgModule ngModule = new NgModule(coreModuleSegment);
			int index = ngModule.getIndex();
			MemorySegment res = (MemorySegment) NgCycle.NgCycleLayout().varHandle( PathElement.groupElement("conf_ctx") )
				.get( cycle.ngCycle, index  );
			NgCoreConf.LAYOUT.varHandle( PathElement.groupElement("ctx") ).set( coreModuleSegment, 0L, 0L);
			// MemorySegment res = (MemorySegment) ngx_get_conf.invokeExact( cycle.ngCycle, coreModuleSegment );
			return new NgCoreConf( res );
		}
		*/
		
		
		
		public NgCoreConf getCoreConfig(NgCycle cycle) throws Throwable {
			MemorySegment coreModuleSegment = NgLib.find("ngx_core_module").orElseThrow();
			MemorySegment res = (MemorySegment) ngx_get_conf.invokeExact( cycle.ngCycle, coreModuleSegment );
			return new NgCoreConf( res );
		}

		public int initSignals(NgLog log) throws Throwable {
			return (int) ngx_init_signals.invokeExact( log.getMemorySegment() );
			
		}
		
		public int createPidFile(NgCoreConf coreConf, NgLog log) throws Throwable {
			return (int) ngx_create_pidfile.invokeExact( coreConf.getPidRef(), log.getMemorySegment() );
		}
		
		public int logRedirectStderr(NgCycle cycle) throws Throwable {
			int res = (int) ngx_log_redirect_stderr.invokeExact( cycle.ngCycle );
			NgLib.find("ngx_use_stderr")
				.orElseThrow()
				.reinterpret(4)
				.set(ValueLayout.JAVA_INT, 0L, 0);
			return res;
		}
		
		public void singleProcessCycle(NgCycle cycle) throws Throwable {
			ngx_single_process_cycle.invokeExact( cycle.ngCycle );
		}
		
		public void masterProcessCycle(NgCycle cycle) throws Throwable {
			ngx_master_process_cycle.invokeExact( cycle.ngCycle );
		}
		
		public int getOptions(String[] args) throws Throwable {
			Arena arena = Arena.global();
			
			MemorySegment argvSegment = arena.allocate(ValueLayout.ADDRESS, args.length);
			
			for (int i = 0; i < args.length; i++) {
				MemorySegment argSegment = arena.allocateFrom( args[i] );
				argvSegment.setAtIndex( ValueLayout.ADDRESS, i, argSegment );
			}
			
			return (int) ngx_get_options.invokeExact( args.length, argvSegment );
		}
		
		public int crc32TableInit() throws Throwable {
			return (int) ngx_crc32_table_init.invokeExact();
		}

		public void assignCycle(NgCycle cycle) {
			
			//SymbolLookup.libraryLookup( Path.of(NG_HOME, "nginx.so"), Arena.global())
			NgLib.findOrThrow("ngx_cycle")
				.reinterpret(8)
				.set( ValueLayout.ADDRESS, 0L, cycle.ngCycle );
		}

		public void initProcessIds() {
			ProcessHandle current = ProcessHandle.current();
			current.info().command().orElse("nginx");
			
			long pid = current.pid();
			long ppid = current.parent().map( ProcessHandle::pid ).orElse(-1L);
			
			// System.out.format("Pid %d.\n", pid);
			// System.out.format("PPid %d.\n", ppid);
			
			NgLib.findOrThrow("ngx_pid")
				.reinterpret(4)
				.set( ValueLayout.JAVA_INT, 0L, (int) pid );
			
			NgLib.findOrThrow("ngx_parent")
				.reinterpret(4)
				.set( ValueLayout.JAVA_INT, 0L, (int) ppid );
		}
		
		public int daemon(NgLog log) throws Throwable {
			int res = (int) ngx_daemon.invokeExact(log.getMemorySegment());
			
			NgLib.findOrThrow("ngx_daemonized")
				.reinterpret(4)
				.set( ValueLayout.JAVA_INT, 0L, 1 );
			
			return res;
		}

		public void slabSizesInit() throws Throwable {
			ngx_slab_sizes_init.invokeExact();
		}
		
		public void dumpConfig(NgCycle cycle) throws Throwable {
			ngx_dump_config_fn.invokeExact( cycle.ngCycle );
		}

		public void setMaxSocket(int i) {
			NgLib.findOrThrow("ngx_max_sockets")
				.reinterpret(4)
				.set( ValueLayout.JAVA_INT, 0L, i );
		}


		
		public int ngx_http_discard_request_body(MemorySegment request) {
			try {
				return (int) ngx_http_discard_request_body.invokeExact( request );
			} catch (Throwable e) {
				e.printStackTrace();
				return 500;
			}
		}
		
		public int httpSendHeader(MemorySegment request) {
			try {
				return (int) ngx_http_send_header.invokeExact( request );
			} catch (Throwable e) {
				e.printStackTrace();
				return 500;
			}
		}
		
		/*
		private final static 
			StructLayout str_t = new NgCycle.Types().str_t;
		private final static VarHandle
			STR_LEN = str_t.varHandle( PathElement.groupElement("len") ),
			STR_DATA = str_t.varHandle( PathElement.groupElement("data") );
		
		MemorySegment createNgStr(String str) {
			MemorySegment strSeg = arena.allocate(str_t);
			STR_LEN.set( strSeg, 0L, (long) str.length() );
			STR_DATA.set( strSeg, 0L, arena.allocateFrom(str) );
			return strSeg;
		}
		*/
}
