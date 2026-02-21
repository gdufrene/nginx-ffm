package poc;

import java.io.PrintWriter;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletConfig;
import nginx.core.NgGlobal;
import nginx.http.NgHttpRequest;
import nginx.servlet.NgRequest;
import nginx.servlet.NgResponse;
import nginx.servlet.NgServletContext;

public class FfmRequestHandler {
	
	static final int 
		NGX_OK       =  0,
		NGX_DECLINED = -5;
	
	static final VarHandle signatureHandle = NgHttpRequest.ngx_http_request_t.varHandle(
		PathElement.groupElement("signature")
	);
	
	private static DispatcherServlet dispatcherServlet;
	
	public static void init() throws Throwable {
		FfmRequestHandler handler = new FfmRequestHandler();

		MethodHandle upcallHandler = MethodHandles.lookup().bind(
			handler,
			"handleToSpring", // or handleRequest
			MethodType.methodType(int.class, MemorySegment.class)
		);

		MemorySegment upcallFunc = NgGlobal.linker.upcallStub(
			upcallHandler,
			FunctionDescriptor.of( ValueLayout.JAVA_INT, ValueLayout.ADDRESS.withTargetLayout(NgHttpRequest.ngx_http_request_t) ),
			// arena
			Arena.global()
		);
		
		/*
		 * 
 *      // Create the 'root' Spring application context
 *      AnnotationConfigWebApplicationContext rootContext =
 *        new AnnotationConfigWebApplicationContext();
 *      rootContext.register(AppConfig.class);
 *
 *      // Manage the lifecycle of the root application context
 *      container.addListener(new ContextLoaderListener(rootContext));
 *
 *      // Create the dispatcher servlet's Spring application context
 *      AnnotationConfigWebApplicationContext dispatcherContext =
 *        new AnnotationConfigWebApplicationContext();
 *      dispatcherContext.register(DispatcherConfig.class)
		 * 
		 */
		

		
		
		
		var servletContext = 
				new NgServletContext("/"); // or any context path you want
				/*
				(jakarta.servlet.ServletContext) Proxy.newProxyInstance(
				FfmRequestHandler.class.getClassLoader(),
				new Class[] {jakarta.servlet.ServletContext.class},
				(proxy, method, args) -> {
					System.out.println("ServletContext method called: " 
							+ method.getName() 
							+ (args == null || args.length == 0 ? "()" : List.of(args)) );
					return null;
				});
				*/
		
		// initialize Spring DispatcherServlet
		/*
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(AppConfig.class);
		*/
		
		
		AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
		ContextLoaderListener listener = new ContextLoaderListener(dispatcherContext, servletContext);
		dispatcherContext.register(AppConfig.class);
		// dispatcherContext.setParent(rootContext);
		/*
		dispatcherContext.setServletConfig( new ServletConfig() {
			@Override
			public String getServletName() {
				return "dispatcher";
			}
			@Override
			public String getInitParameter(String name) {
				return null;
			}
			@Override
			public Enumeration<String> getInitParameterNames() {
				return Collections.emptyEnumeration();
			}
			@Override
			public jakarta.servlet.ServletContext getServletContext() {
				return servletContext;
			}
		} );
		dispatcherContext.setServletContext( servletContext  );
		*/
		
		

		dispatcherServlet = new DispatcherServlet(dispatcherContext);
		dispatcherServlet.setEnableLoggingRequestDetails(true);
		// dispatcherServlet.set

		dispatcherServlet.init( new ServletConfig() {
			@Override
			public String getServletName() {
				return "dispatcher";
			}
			@Override
			public String getInitParameter(String name) {
				return null;
			}
			@Override
			public Enumeration<String> getInitParameterNames() {
				return Collections.emptyEnumeration();
			}
			@Override
			public jakarta.servlet.ServletContext getServletContext() {
				return servletContext;
			}
		} );
		listener.contextInitialized(null); // we can pass null since we don't use the event object
		dispatcherContext.refresh();
		
		
		// dispatcherServlet = dispatcherContext.getBean(DispatcherServlet.class);
		if ( dispatcherServlet == null ) {
			throw new RuntimeException("Failed to initialize DispatcherServlet");
		}
		
		VarHandle ffmUpcallHandle = ValueLayout.ADDRESS.varHandle();
		ffmUpcallHandle.set(NgGlobal.SYMBOL_LOOKUP.findOrThrow("ngx_http_ffm_upcall").reinterpret(8), 0L, upcallFunc);
	}
	
	
	/*
	final VarHandle uriHandle = NgHttp.ngx_http_request_t.varHandle(
		PathElement.groupElement("uri")
	);
	*/
	
	/**
	 * wraps the request pointer into a NgRequest and dispatches it to Spring DispatcherServlet
	 * the response is then sent back to NGINX through the NgResponse object
	 * 
	 * @param reqPtr
	 * @return the return code to NGINX, NGX_OK for success, NGX_DECLINED to decline the request, or other error codes
	 */
	public int handleToSpring(MemorySegment reqPtr) {
		
		NgRequest req = new NgRequest(reqPtr);
		
		String uri = req.getRequestURI();
		if ( !uri.startsWith("/hello") ) {
			System.out.println("Declining request...");
			return NGX_DECLINED;
		}
		
		NgResponse res = new NgResponse();
		res.request = reqPtr;
		
		try {
			
			dispatcherServlet.service(req, res);
			
			int rc;
			
			// System.out.println("Discard request...");
			/* */
			rc = NgHttpRequest.ngx_http_discard_request_body(reqPtr);
			if ( rc != NGX_OK ) {
				System.out.println("ngx_http_discard_request_body() failed.");
				return rc;
			}
			/* */
			
			if ( res.getStatus() == 0 ) {
				res.setStatus(200); // default to 200 if servlet did not set a status
			}
			
			// res.setContentType("text/plain"); // set default content type if not set by servlet
			
			// System.out.println("Sending response headers...");
			rc = NgHttpRequest.ngx_http_send_header(reqPtr);
			if ( rc != NGX_OK ) {
				System.out.println("httpSendHeader() failed.");
				return rc;
			}
			
			rc = res.flush();
			
			return rc == NGX_OK ? res.getStatus() : 500;

		} catch (Throwable e) {
			res.setStatus(500);
			res.flush();
			
			e.printStackTrace();
			return 500;
		}
		
		
	}
	
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
		
		System.out.println("Response content type: " + response.getContentType());
		
		response.addHeader("X-FFM", "Hello from Java FFM!");
		System.out.println("Contains header (X-FFM): " + response.containsHeader("X-FFM"));
		response.getHeaders("X-FFM").forEach( (v) -> System.out.println("Response headers X-FFM: " + v) );
		System.out.println("Response first header X-FFM: " + response.getHeader("X-FFM") );
		System.out.println("Response charset: " + response.getCharacterEncoding() );
		System.out.println("Response status: " + response.getStatus() );
		
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
			int rc = NgHttpRequest.ngx_http_discard_request_body(reqPtr);
			if ( rc != NGX_OK ) {
				System.out.println("ngx_http_discard_request_body() failed.");
				return rc;
			}
			
			// System.out.println("Sending response headers...");
			rc = NgHttpRequest.ngx_http_send_header( reqPtr );
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