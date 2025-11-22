import org.junit.jupiter.api.Test;

import nginx.core.NgArray;
import nginx.core.NgLog;
import nginx.core.NgPool;

public class TestArray {
	
	@Test
	void test() {
		NgLog log = NgLog.ngx_log_init( "target", "nginx-test.log" ).orElseThrow();
		NgPool pool = NgPool.ngx_create_pool( 1024, log ).orElseThrow();
		
		NgArray array = NgArray.ngx_array_create( pool, 10, 4 );
	}

}
