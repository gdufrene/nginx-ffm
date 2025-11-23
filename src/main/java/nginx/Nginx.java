package nginx;

import java.lang.foreign.Arena;

import nginx.core.NgGlobal;
import nginx.core.NgLog;
import nginx.core.NgPool;
import poc.NgCore;
import poc.NgCoreConf;
import poc.NgCycle;
import poc.NgRequestHandler;

public class Nginx {
	
	static final int NGX_OK          = 0;
	
	static boolean initialized = false;
	
	
	NgCore ngCore;
	
	/**
	 * Initialize Nginx and run process.
	 * <p>
	 * based on src/core/nginx.c main cycle
	 * </p>
	 */
	public synchronized void init() {
		
		if ( initialized ) return;
		
		try (Arena arena = Arena.ofConfined()) {
			ngCore = new NgCore(arena);
			
			if ( ngCore.strErrorInit() != NGX_OK ) {
				throw new RuntimeException("ngCore.strErrorInit() failed.");
			}
			
			// mocked ...
			String[] argv = new String[] {
					"nginx",
					"-c",
					"conf/nginx.conf",
					"-p",
					NgGlobal.NG_HOME,
					"-e",
					"/dev/stderr"
				};
			
			
			if ( ngCore.getOptions(argv) != NGX_OK ) {
				throw new RuntimeException("ngCore.getOptions() failed.");
			}
			
			ngCore.setMaxSocket( -1 );
			
			ngCore.timeInit();
			ngCore.regexInit();
			
			ngCore.initProcessIds( ); // assign ngx_pid, ngx_parent
			
			// NgLog log = ngCore.logInit( "/dev", "stdout" );
			NgLog log = NgLog.ngx_log_init( "/dev", "stderr" )
					.orElseThrow( () -> new RuntimeException("NgLog.ngx_log_init() failed.") );
			
			
			NgCycle cycle = new NgCycle(arena, ngCore);
			cycle.zeroFill();
			cycle.setLog(log);
			
			ngCore.assignCycle(cycle);
			
			NgPool pool = NgPool.ngx_create_pool(1024, log)
					.orElseThrow( () -> new RuntimeException("NgPool.ngx_create_pool() failed.") );
			cycle.setPool( pool );

			if ( ngCore.saveArgv( cycle, argv ) != NGX_OK ) {
				throw new RuntimeException("ngCore.saveArgv() failed.");
			}
			
			if ( ngCore.processOptions( cycle ) != NGX_OK ) {
				throw new RuntimeException("ngCore.processOptions() failed.");
			}
			
			if ( ngCore.osInit(log) != NGX_OK ) {
				throw new RuntimeException("ngCore.osInit() failed.");
			}
			
			if ( ngCore.crc32TableInit() != NGX_OK ) {
				throw new RuntimeException("ngCore.crc32TableInit() failed.");
			}
			
			ngCore.slabSizesInit();
			
			// addInheritedSockets
			
			if ( ngCore.preinitModules() != NGX_OK ) {
				throw new RuntimeException("ngCore.preinitModules() failed.");
			}
			
			// ngCore.dumpConfig( cycle );
			
			cycle = ngCore.initCycle( cycle )
					// TODO: if ngx_test_config requested log to error
					.orElseThrow( () -> new RuntimeException("ngCore.initCycle() failed.") );

			// TODO: if ngx_test_config and !ngx_quiet_mode requested log success
			// TODO: if ngx_test_config and ngx_dump_config requested dump config and return 0

			// TODO: signal handling if ngx_signal is set
			NgLog cycleLog = cycle.getLog();
			ngCore.osStatus(cycleLog);
			
			ngCore.assignCycle(cycle);
			
			NgCoreConf coreConf = ngCore.getCoreConfig(cycle);
			
			/**
			 * FFM Request Handler
			 */
			NgRequestHandler ffmHandler = new NgRequestHandler(ngCore);
			ngCore.initFfmHandler(ffmHandler, Arena.global());
			
			// NgCycle.Types.debugDumpCycleLayout( cycle.ngCycle );
			
			if ( ngCore.initSignals( cycleLog ) != NGX_OK ) {
				throw new RuntimeException("ngCore.initSignals() failed.");
			}
			
			// TODO: daemonize if needed
			
			if ( ngCore.createPidFile(coreConf, cycleLog) != NGX_OK ) {
				throw new RuntimeException("ngCore.createPidFile() failed.");
			}
			
			// TODO: redirect stderr to log file with ngCore.logRedirectStderr(cycle);
			// TODO: close log file if different from stderr
			// TODO: set ngx_use_stderr to 0
			
			// TODO: master process cycle or single process cycle
			
			// ngCore.singleProcessCycle(cycle);
			initialized = true;
			// master process will not return
			// ngCore.masterProcessCycle(cycle);
			ngCore.singleProcessCycle(cycle);
			
			System.out.println("Nginx masterProcessCycle returned ?");
		} catch (Throwable e) {
			throw new RuntimeException("Nginx initialization failed.", e);
		}
		
		
	}
	
	public static void main(String[] args) {
		Nginx nginx = new Nginx();
		nginx.init();
	}

}
