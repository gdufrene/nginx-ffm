package poc;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import nginx.core.NgHttp;
import nginx.core.NgString;
import nginx.servlet.NgResponse;

public class NgRequestHandler {
	
	static final int 
		NGX_OK       =  0,
		NGX_DECLINED = -5;
	
	static final VarHandle signatureHandle = NgHttp.ngx_http_request_t.varHandle(
		PathElement.groupElement("signature")
	);
	
	NgResponse response = new NgResponse();
	
	public int handleRequest(MemorySegment reqPtr) {
		
		reqPtr = reqPtr.reinterpret(1328);
		// NgResponse response = new NgResponse(reqPtr);
		
		MemorySegment uriPtr = (MemorySegment) reqPtr.asSlice(824, NgString.ngx_str_t.byteSize());
		// System.out.println("Request URI address: " + uriPtr);
		String uri = NgString.asString(uriPtr);
		System.out.println("Request URI: " + uri);
		if ( !uri.startsWith("/hello") ) {
			System.out.println("Declining request...");
			return NGX_DECLINED;
		}
		
		
		response.request = reqPtr;
		response.ngCore = this.ngCore;
		
		
		// int http = (int) signatureHandle.get(reqPtr, 0L); // "HTTP" in hex
		// int http = 1;
		// System.out.format("Handling request... %x is http %s\n", http, (http == 0x50545448 ? "YES" : "NO"));
		
		response.setStatus(201);
		response.setContentType("text/plain");
		
		try {

			PrintWriter out = response.getWriter();
			out.write("Hello from NGINX Java FFM!\n");
			// print writer flush data to nginx out chain of buffers
			out.close();
			
			// System.out.println("Discard request...");
			int rc = ngCore.ngx_http_discard_request_body(reqPtr);
			if ( rc != NGX_OK ) {
				System.out.println("ngx_http_discard_request_body() failed.");
				return rc;
			}
			
			// System.out.println("Sending response headers...");
			rc = ngCore.httpSendHeader( reqPtr );
			if ( rc != NGX_OK ) {
				System.out.println("httpSendHeader() failed.");
				return rc;
			}
			
			// System.out.println("Flushing response...");
			rc = response.flush();
			// response flush sends the out chain buffers through nginx output filters
			
			return rc;

		} catch (Exception e) {
			e.printStackTrace();
			return 500;
		}
	}
	
	NgCore ngCore;
	
	public NgRequestHandler(NgCore ngCore) {
		this.ngCore = ngCore;
	}
	
	

}