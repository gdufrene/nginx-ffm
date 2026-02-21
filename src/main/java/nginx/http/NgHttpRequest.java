package nginx.http;

import static java.lang.foreign.FunctionDescriptor.of;
import static java.lang.foreign.MemoryLayout.paddingLayout;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.rmi.UnexpectedException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import nginx.core.NgGlobal;
import nginx.core.NgHash;
import nginx.core.NgList;
import nginx.core.NgPool;
import nginx.core.NgString;

public interface NgHttpRequest extends NgGlobal {
	
	int NGX_HTTP_LC_HEADER_LEN = 32;
	
	StructLayout ngx_http_headers_out_t = structLayout(
			NgList.ngx_list_t.withName("headers"),
			NgList.ngx_list_t.withName("trailers"),
			
			JAVA_INT.withName("status"),
			paddingLayout(4),
			NgString.ngx_str_t.withName("status_line"),
			
			paddingLayout(120), // padding ...
			
			JAVA_LONG.withName("content_type_len"),
			NgString.ngx_str_t.withName("content_type"),
			NgString.ngx_str_t.withName("charset"),
			ADDRESS.withName("content_type_lowcase"),
			JAVA_LONG.withName("content_type_hash"),
			
			JAVA_LONG.withName("content_length_n"),
			JAVA_LONG.withName("content_offset"),
			JAVA_LONG.withName("date_time"),
			JAVA_LONG.withName("last_modified_time")
		).withName("ngx_http_headers_out_t");
	
	StructLayout ngx_http_headers_in_t = structLayout(
			NgList.ngx_list_t.withName("headers"),
			ADDRESS.withName("host"),
			ADDRESS.withName("connection"),
			ADDRESS.withName("if_modified_since"),
			ADDRESS.withName("if_unmodified_since"),
			ADDRESS.withName("if_match"),
			ADDRESS.withName("if_none_match"),
			ADDRESS.withName("user_agent"),
			ADDRESS.withName("referer"),
			ADDRESS.withName("content_length"),
			ADDRESS.withName("content_range"),
			ADDRESS.withName("content_type").withTargetLayout(NgHash.ngx_table_elt_t),
			
			ADDRESS.withName("range"),
			ADDRESS.withName("if_range"),
			
			ADDRESS.withName("transfer_encoding"),
			ADDRESS.withName("te"),
			ADDRESS.withName("expect"),
			ADDRESS.withName("upgrade"),
			
			ADDRESS.withName("accept_encoding"),
			ADDRESS.withName("via"),
			
			ADDRESS.withName("authorization"),
			ADDRESS.withName("keep_alive"),
			ADDRESS.withName("x_forwarded_for"),
			// ADDRESS.withName("x_real_ip"), 		// NGX_HTTP_REALIP
			// ADDRESS.withName("accept"), 			// NGX_HTTP_HEADERS
			// ADDRESS.withName("accept_language"), // NGX_HTTP_HEADERS
			/* DAV diabled */
			ADDRESS.withName("cookie"),
			NgString.ngx_str_t.withName("user"),
			NgString.ngx_str_t.withName("passwd"),
			NgString.ngx_str_t.withName("server"),
			JAVA_LONG.withName("content_length_n"),
			JAVA_LONG.withName("keep_alive_n"),
			JAVA_LONG.withName("flags")

		).withName("ngx_http_headers_in_t");
	
	StructLayout ngx_http_request_body_t = structLayout(
			ADDRESS.withName("temp_file"),
			ADDRESS.withName("bufs"),
			ADDRESS.withName("buf"),
			JAVA_LONG.withName("rest"),
			JAVA_LONG.withName("received"),
			ADDRESS.withName("free"),
			ADDRESS.withName("busy"),
			ADDRESS.withName("chunked"),
			ADDRESS.withName("post_handler"),
			JAVA_LONG.withName("flags")
		).withName("ngx_http_request_body_t");
	
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
			
			ADDRESS.withName("cache"), // NGX_HTTP_CACHE
			
			ADDRESS.withName("upstream"),
			ADDRESS.withName("upstream_states"),
			
			ADDRESS.withName("pool"),
			ADDRESS.withName("header_in"), 

			// offset: 104
			ngx_http_headers_in_t.withName("headers_in"),
			ngx_http_headers_out_t.withName("headers_out"),
			
			ADDRESS.withName("request_body").withTargetLayout(ngx_http_request_body_t),
			
			JAVA_LONG.withName("lingering_time"),
			JAVA_LONG.withName("start_sec"),
			JAVA_LONG.withName("start_msec"),
			
			JAVA_LONG.withName("method"),
			JAVA_LONG.withName("http_version"),
			// paddingLayout(4),
			
			NgString.ngx_str_t.withName("request_line"),
			NgString.ngx_str_t.withName("uri"),
			NgString.ngx_str_t.withName("args"),
			NgString.ngx_str_t.withName("exten"),
			NgString.ngx_str_t.withName("unparsed_uri"),
			
			NgString.ngx_str_t.withName("method_name"),
			NgString.ngx_str_t.withName("http_protocol"),
			NgString.ngx_str_t.withName("schema"),
			
			ADDRESS.withName("out"),
			ADDRESS.withName("main"),
			ADDRESS.withName("parent"),
			ADDRESS.withName("postponed"),
			ADDRESS.withName("post_subrequest"),
			ADDRESS.withName("posted_requests"),
			
			JAVA_INT.withName("phase_handler"),
			paddingLayout(4),
			ADDRESS.withName("content_handler"),
			JAVA_INT.withName("access_code"),
			paddingLayout(4),
			
			ADDRESS.withName("variables"),
			
			JAVA_INT.withName("ncaptures"),
			paddingLayout(4),
			ADDRESS.withName("captures"),
			ADDRESS.withName("capture_data"),
			
			JAVA_LONG.withName("limit_rate"),
			JAVA_LONG.withName("limit_rate_after"),
			
			JAVA_LONG.withName("header_size"),
			
			JAVA_LONG.withName("request_length"),
			
			JAVA_INT.withName("err_status"),
			paddingLayout(4),
			
			ADDRESS.withName("http_connection"),
			ADDRESS.withName("stream"),
			ADDRESS.withName("v3_parse"),
			
			ADDRESS.withName("log_handler"),
			ADDRESS.withName("cleanup"),
			
			JAVA_INT.withName("port"),
			
			/* padding for flags ... */
			paddingLayout(32),
			
			JAVA_INT.withName("state"),
			
			JAVA_INT.withName("header_hash"),
			JAVA_INT.withName("lowcase_index"),
			MemoryLayout.sequenceLayout(NGX_HTTP_LC_HEADER_LEN, ValueLayout.JAVA_BYTE).withName("lowcase_header"),
			//paddingLayout(4),
			
			ADDRESS.withName("header_name_start"),
			ADDRESS.withName("header_name_end"),
			ADDRESS.withName("header_start"),
			ADDRESS.withName("header_end"),
			
			ADDRESS.withName("uri_start"),
			ADDRESS.withName("uri_end"),
			ADDRESS.withName("uri_ext"),
			ADDRESS.withName("args_start"),
			ADDRESS.withName("request_start"),
			ADDRESS.withName("request_end"),
			ADDRESS.withName("method_end"),
			ADDRESS.withName("schema_start"),
			ADDRESS.withName("schema_end"),
			ADDRESS.withName("host_start"),
			ADDRESS.withName("host_end"),
			
			JAVA_CHAR.withName("http_minor"),
			JAVA_CHAR.withName("http_major"),
			paddingLayout(4)
			
			
		).withName("ngx_http_request_t");
	
	MethodHandle 
		  ngx_http_output_filter = NgGlobal.downcallTo("ngx_http_output_filter", of(JAVA_INT, ADDRESS, ADDRESS))
		, ngx_http_send_header = NgGlobal.downcallTo("ngx_http_send_header", of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS ))
		, ngx_http_discard_request_body = NgGlobal.downcallTo("ngx_http_discard_request_body", of( JAVA_INT, ADDRESS ))
		;
	
	VarHandle
		vhPool = ngx_http_request_t.varHandle(PathElement.groupElement("pool"));
	
	

	static int ngx_http_output_filter(MemorySegment request, MemorySegment segment) throws Throwable {
		return (int) ngx_http_output_filter.invokeExact(request, segment);
	}
	
	static int ngx_http_send_header(MemorySegment segment) throws Throwable {
		return (int) ngx_http_send_header.invokeExact(segment);
	}
	
	static int ngx_http_discard_request_body(MemorySegment segment) throws Throwable {
		return (int) ngx_http_discard_request_body.invokeExact(segment);
	}
	
	static MemorySegment allocOnPool(MemorySegment request, long size) {
		NgPool pool = NgPool.fromSegment( (MemorySegment) vhPool.get(request, 0L) );
		return pool.ngx_palloc(size);
	}
	
	public MemorySegment getMemorySegment();


	/* */
	public static void main(String[] args) {
		BiConsumer<StructLayout, Long> sizing = (layout, expected) -> {
				long size = layout.byteSize();
				System.out.format("%s size(%s) = %d\n",
						size == expected ? "✅" : "❌",
						layout.name().get(), 
						size
				);
		};
		
		sizing.accept(NgHttpRequest.ngx_http_request_t, 1328L);
		sizing.accept(NgHttpRequest.ngx_http_headers_in_t, 312L);
		sizing.accept(NgHttpRequest.ngx_http_headers_out_t, 344L);
		sizing.accept(NgHttpRequest.ngx_http_request_body_t, 80L);
		
		sizing.accept(NgList.ngx_list_t, 56L);

		System.out.println();
		System.out.println("NgHttp.headers_in.content_type offset: " + NgHttpRequest.ngx_http_request_t.byteOffset(PathElement.groupElement("headers_in"), PathElement.groupElement("content_type")) );
		System.out.println("offset(ngx_http_request_t.headers_out.content_length_n) : " + NgHttpRequest.ngx_http_request_t.byteOffset(PathElement.groupElement("headers_out"), PathElement.groupElement("content_length_n")) );
		System.out.println("NgHttp.method_name offset: " + NgHttpRequest.ngx_http_request_t.byteOffset(PathElement.groupElement("method_name")) );
	}
	/* */
}