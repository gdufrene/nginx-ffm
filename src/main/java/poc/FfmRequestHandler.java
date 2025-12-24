package poc;

import java.io.PrintWriter;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import nginx.core.NgGlobal;
import nginx.core.NgHttp;
import nginx.servlet.NgRequest;
import nginx.servlet.NgResponse;

public class FfmRequestHandler {
	
	static final int 
		NGX_OK       =  0,
		NGX_DECLINED = -5;
	
	static final VarHandle signatureHandle = NgHttp.ngx_http_request_t.varHandle(
		PathElement.groupElement("signature")
	);
	
	public static void init() throws Throwable {
		FfmRequestHandler handler = new FfmRequestHandler();

		MethodHandle upcallHandler = MethodHandles.lookup().bind(
			handler,
			"handleRequest",
			MethodType.methodType(int.class, MemorySegment.class)
		);

		MemorySegment upcallFunc = NgGlobal.linker.upcallStub(
			upcallHandler,
			FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS.withTargetLayout(NgHttp.ngx_http_request_t) ),
			// arena
			Arena.global()
		);
		
		VarHandle ffmUpcallHandle = ValueLayout.ADDRESS.varHandle();
		ffmUpcallHandle.set(NgGlobal.SYMBOL_LOOKUP.findOrThrow("ngx_http_ffm_upcall").reinterpret(8), 0L, upcallFunc);
	}
	
	
	/*
	final VarHandle uriHandle = NgHttp.ngx_http_request_t.varHandle(
		PathElement.groupElement("uri")
	);
	*/
	
	public int handleRequest(MemorySegment reqPtr) {
		
		// reqPtr = reqPtr.reinterpret(1328);
		// NgResponse response = new NgResponse(reqPtr);
		
		NgRequest req = new NgRequest(reqPtr);
		String uri = req.getRequestURI();
		
		String contentType = req.getContentType();
		System.out.println("Request Content-Type: " + contentType);
		
		// System.out.println("Request URI address: " + uriPtr);
		// String uri = NgString.asString(uriPtr2);
		
		System.out.println("Request URI: " + uri);
		if ( !uri.startsWith("/hello") ) {
			System.out.println("Declining request...");
			return NGX_DECLINED;
		}
		
		System.out.println("Method Name: " + req.getMethod());
		
		Enumeration<String> values = req.getHeaders("Host");
		// transform Enumeration to List
		List<String> hostHeaders = Collections.list(values);
		System.out.println("headers (Host): " + hostHeaders);
		
		NgResponse response = new NgResponse();
		
		response.request = reqPtr;
		
		
		// int http = (int) signatureHandle.get(reqPtr, 0L); // "HTTP" in hex
		// int http = 1;
		// System.out.format("Handling request... %x is http %s\n", http, (http == 0x50545448 ? "YES" : "NO"));
		
		response.setStatus(201);
		response.setContentType("text/plain");
		
		try {

			PrintWriter out = response.getWriter();
			String data = "Some data from NGINX Java FFM!\n";
			int len = data.length();
			int count = 0;
			do {
				out.write( data );
				count += len;
			} while ( count < 10000 );
			// print writer flush data to nginx out chain of buffers
			out.close();
			
			// System.out.println("Discard request...");
			int rc = NgHttp.ngx_http_discard_request_body(reqPtr);
			if ( rc != NGX_OK ) {
				System.out.println("ngx_http_discard_request_body() failed.");
				return rc;
			}
			
			// System.out.println("Sending response headers...");
			rc = NgHttp.ngx_http_send_header( reqPtr );
			if ( rc != NGX_OK ) {
				System.out.println("httpSendHeader() failed.");
				return rc;
			}
			
			// System.out.println("Flushing response...");
			rc = response.flush();
			// response flush sends the out chain buffers through nginx output filters
			
			return rc;

		} catch (Throwable e) {
			e.printStackTrace();
			return 500;
		}
	}
	

}