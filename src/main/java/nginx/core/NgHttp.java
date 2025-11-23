package nginx.core;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.FunctionDescriptor.of;

import java.lang.foreign.StructLayout;
import java.lang.invoke.MethodHandle;

public interface NgHttp extends NgGlobal {
	
	StructLayout ngx_http_headers_out_t = structLayout(
			//headers,
			//trailers,
			paddingLayout(112), // padding ...
			JAVA_INT.withName("status"),
			paddingLayout(140), // padding ...
			JAVA_LONG.withName("content_type_len"),
			structLayout(
				JAVA_LONG.withName("len"),
				ADDRESS.withName("data")
			).withName("content_type"),
			// paddingLayout(4), // padding to align next LONG
			JAVA_LONG.withName("content_length_n")
			// ... (other fields would be defined here)
		).withName("ngx_http_headers_out_t");
	
	StructLayout ngx_http_request_t = structLayout(
			JAVA_INT.withName("signature"),
			paddingLayout(4), 
			ADDRESS.withName("connection"),
			ADDRESS.withName("ctx"),
			ADDRESS.withName("main_conf"),
			ADDRESS.withName("srv_conf"),
			ADDRESS.withName("loc_conf"),
			ADDRESS.withName("read_event_handler"),
			ADDRESS.withName("write_event_handler"),
			ADDRESS.withName("cache"), // ??
			ADDRESS.withName("upstream"),
			ADDRESS.withName("upstream_states"),
			ADDRESS.withName("pool"),
			ADDRESS.withName("header_in"),
			
			paddingLayout(312).withName("headers_in"),
			
			ngx_http_headers_out_t.withName("headers_out")
			// ... (other fields would be defined here)
		).withName("ngx_http_request_t");
	
	MethodHandle ngx_http_output_filter = linker.downcallHandle(
		SYMBOL_LOOKUP.find("ngx_http_output_filter").orElseThrow(),
		of(JAVA_INT, ADDRESS, ADDRESS)
	);

	static int ngx_http_output_filter(MemorySegment request, MemorySegment segment) {
		try {
			return (int) ngx_http_output_filter.invokeExact(request, segment);
		} catch (Throwable e) {
			e.printStackTrace();
			return 500;
		}
	}

}
