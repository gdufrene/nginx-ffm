package nginx;

import java.lang.foreign.Arena;

import nginx.core.NgCore;
import nginx.core.NgCoreConf;
import nginx.core.NgCycle;
import nginx.core.NgGlobal;
import nginx.core.NgLog;
import nginx.core.NgPool;
import poc.FfmRequestHandler;

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
		
		try {
			// ngCore = new NgCore(arena);
			
			if ( NgCore.strErrorInit() != NGX_OK ) {
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
			
			
			if ( NgCore.getOptions(argv) != NGX_OK ) {
				throw new RuntimeException("ngCore.getOptions() failed.");
			}
			
			NgCore.setMaxSocket( -1 );
			
			NgCore.timeInit();
			NgCore.regexInit();
			
			NgCore.initProcessIds( ); // assign ngx_pid, ngx_parent
			
			// NgLog log = ngCore.logInit( "/dev", "stdout" );
			NgLog log = NgLog.ngx_log_init( "/dev", "stderr" )
					.orElseThrow( () -> new RuntimeException("NgLog.ngx_log_init() failed.") );
			
			// #if (NGX_OPENSSL) how to detect ? search for OPENSSL_VERSION_TEXT symbol ?
			if ( NgCore.ngx_ssl_init(log) != NGX_OK ) {
				throw new RuntimeException("ngCore.ngx_ssl_init() failed.");
			}
			
			
			NgCycle cycle = NgCycle.create();
			cycle.zeroFill();
			cycle.setLog(log);
			
			NgCore.assignCycle(cycle);
			
			NgPool pool = NgPool.ngx_create_pool(1024, log)
					.orElseThrow( () -> new RuntimeException("NgPool.ngx_create_pool() failed.") );
			cycle.setPool( pool );

			if ( NgCore.saveArgv( cycle, argv ) != NGX_OK ) {
				throw new RuntimeException("ngCore.saveArgv() failed.");
			}
			
			if ( NgCore.processOptions( cycle ) != NGX_OK ) {
				throw new RuntimeException("ngCore.processOptions() failed.");
			}
			
			if ( NgCore.osInit(log) != NGX_OK ) {
				throw new RuntimeException("ngCore.osInit() failed.");
			}
			
			if ( NgCore.crc32TableInit() != NGX_OK ) {
				throw new RuntimeException("ngCore.crc32TableInit() failed.");
			}
			
			NgCore.slabSizesInit();
			
			// addInheritedSockets
			
			if ( NgCore.preinitModules() != NGX_OK ) {
				throw new RuntimeException("ngCore.preinitModules() failed.");
			}
			
			// ngCore.dumpConfig( cycle );
			
			cycle = cycle.initCycle( )
					// TODO: if ngx_test_config requested log to error
					.orElseThrow( () -> new RuntimeException("ngCore.initCycle() failed.") );

			// TODO: if ngx_test_config and !ngx_quiet_mode requested log success
			// TODO: if ngx_test_config and ngx_dump_config requested dump config and return 0

			// TODO: signal handling if ngx_signal is set
			NgLog cycleLog = cycle.getLog();
			NgCore.osStatus(cycleLog);
			
			NgCore.assignCycle(cycle);
			
			NgCoreConf coreConf = NgCore.getCoreConfig(cycle);
			
			/**
			 * FFM Request Handler
			 */
			FfmRequestHandler.init();
			
			// NgCycle.Types.debugDumpCycleLayout( cycle.ngCycle );
			
			if ( NgCore.initSignals( cycleLog ) != NGX_OK ) {
				throw new RuntimeException("ngCore.initSignals() failed.");
			}
			
			// TODO: daemonize if needed
			
			if ( NgCore.createPidFile(coreConf, cycleLog) != NGX_OK ) {
				throw new RuntimeException("ngCore.createPidFile() failed.");
			}
			
			// TODO: redirect stderr to log file with ngCore.logRedirectStderr(cycle);
			// TODO: close log file if different from stderr
			// TODO: set ngx_use_stderr to 0
			
			// TODO: master process cycle or single process cycle
			
			// ngCore.singleProcessCycle(cycle);
			initialized = true;
			// process cycle will not return
			// ngCore.masterProcessCycle(cycle);
			NgCore.singleProcessCycle(cycle);
			
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
