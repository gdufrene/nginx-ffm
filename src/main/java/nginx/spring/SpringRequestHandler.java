package nginx.spring;

import java.lang.foreign.MemorySegment;
import java.util.Collections;
import java.util.Enumeration;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import nginx.RequestHandler;
import nginx.core.NgCore;
import nginx.http.NgHttpRequest;
import nginx.servlet.NgRequest;
import nginx.servlet.NgResponse;
import nginx.servlet.NgServletContext;

public class SpringRequestHandler implements RequestHandler {

	private static DispatcherServlet dispatcherServlet;

	public SpringRequestHandler(Class<?> configClass) {
		var servletContext = new NgServletContext("/");
		AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
		ContextLoaderListener listener = new ContextLoaderListener(dispatcherContext, servletContext);
		dispatcherContext.register(configClass);
		dispatcherServlet = new DispatcherServlet(dispatcherContext);
		dispatcherServlet.setEnableLoggingRequestDetails(true);
		initServletConfig(servletContext);
		listener.contextInitialized(null); // we can pass null since we don't use the event object
		dispatcherContext.refresh();
	}
	
	private void initServletConfig(ServletContext servletContext) {
		try {
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
				public ServletContext getServletContext() {
					return servletContext;
				}
			} );
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize DispatcherServlet", e);
		}
	}
	
	@Override
	public int handleRequest(MemorySegment request) {
		NgRequest req = new NgRequest(request);
		
		String uri = req.getRequestURI();
		if ( !uri.startsWith("/spring") ) {
			return NgCore.NGX_DECLINED;
		}
		
		NgResponse res = new NgResponse();
		res.request = request;
		
		try {
			
			dispatcherServlet.service(req, res);			
			int rc;
			
			rc = NgHttpRequest.ngx_http_discard_request_body(request);
			if ( rc != NgCore.NGX_OK ) {
				System.out.println("ngx_http_discard_request_body() failed.");
				return rc;
			}
			
			if ( res.getStatus() == 0 ) {
				res.setStatus(200); // default to 200 if servlet did not set a status
			}
			
			// res.setContentType("text/plain"); // set default content type if not set by servlet
			rc = NgHttpRequest.ngx_http_send_header(request);
			if ( rc != NgCore.NGX_OK ) {
				System.out.println("httpSendHeader() failed.");
				return rc;
			}
			return res.flush();

		} catch (Throwable e) {
			res.setStatus(500);
			res.flush();
			
			e.printStackTrace();
			return 500;
		}
	}

}
