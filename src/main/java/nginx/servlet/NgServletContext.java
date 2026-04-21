package nginx.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

public class NgServletContext implements ServletContext {
	
	private final String contextPath;
	
	public NgServletContext(String contextPath) {
		this.contextPath = contextPath;
	}
	
	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public ServletContext getContext(String uripath) {
		return this; // for simplicity, return the same context for any path
	}

	@Override
	public int getMajorVersion() {
		return 3;
	}

	@Override
	public int getMinorVersion() {
		return 1;
	}

	@Override
	public int getEffectiveMajorVersion() {
		return 3;
	}

	@Override
	public int getEffectiveMinorVersion() {
		return 1;
	}

	@Override
	public String getMimeType(String file) {
		return "plain/text";
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		// TODO Auto-generated method stub
		log("getResourcePaths called with path: " + path);
		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		// TODO Auto-generated method stub
		log("getResource called with path: " + path);
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		log("getResourceAsStream called with path: " + path);
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		log("getRequestDispatcher called with path: " + path);
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		log("getNamedDispatcher called with name: " + name);
		return null;
	}

	@Override
	public void log(String msg) {
		System.out.println(msg);
	}

	@Override
	public void log(String message, Throwable throwable) {
		throwable.printStackTrace();
		System.out.println(message);
	}

	@Override
	public String getRealPath(String path) {
		log("getRealPath called with path: " + path);
		return null;
	}

	@Override
	public String getServerInfo() {
		return "Nginx-FFM/1.0";
	}

	HashMap<String, String> initParameters = new HashMap<>();
	
	@Override
	public String getInitParameter(String name) {
		return initParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return new Enumeration<String>() {
			private final List<String> params = new ArrayList<>( initParameters.keySet() );
			private int index = 0;
			
			@Override
			public boolean hasMoreElements() {
				return index < params.size();
			}

			@Override
			public String nextElement() {
				return params.get(index++);
			}
		};
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		if ( initParameters.containsKey(name) ) {
			return false; // already set
		}
		initParameters.put(name, value);
		return true;
	}
	
	HashMap<String, Object> attribute = new HashMap<>();

	@Override
	public Object getAttribute(String name) {
		return attribute.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new Enumeration<String>() {
			private final List<String> params = new ArrayList<>( attribute.keySet() );
			private int index = 0;
			
			@Override
			public boolean hasMoreElements() {
				return index < params.size();
			}

			@Override
			public String nextElement() {
				return params.get(index++);
			}
		};
	}

	@Override
	public void setAttribute(String name, Object object) {
		attribute.put(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		attribute.remove(name);
	}

	@Override
	public String getServletContextName() {
		return "nginx-ffm";
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public Dynamic addJspFile(String servletName, String jspFile) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName,
			Class<? extends Filter> filterClass) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		throw new UnsupportedOperationException("addServlet by class name is not supported");
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		// TODO Auto-generated method stub
		log("getSessionCookieConfig called");
		return null;
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		// TODO Auto-generated method stub
		log("setSessionTrackingModes called with modes: " + sessionTrackingModes);
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		// TODO Auto-generated method stub
		log("getDefaultSessionTrackingModes called");
		return null;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		// TODO Auto-generated method stub
		log("getEffectiveSessionTrackingModes called");
		return null;
	}

	@Override
	public void addListener(String className) {
		// TODO Auto-generated method stub
		log("addListener called with className: " + className);
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		// TODO Auto-generated method stub
		log("addListener called with instance of: " + t.getClass().getName());
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		// TODO Auto-generated method stub
		log("addListener called with class: " + listenerClass.getName());
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		// TODO Auto-generated method stub
		log("createListener called with class: " + clazz.getName());
		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		log("getJspConfigDescriptor called");
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		log("getClassLoader called");
		return null;
	}

	@Override
	public void declareRoles(String... roleNames) {
		// TODO Auto-generated method stub
		log("declareRoles called with roles: " + String.join(", ", roleNames));
	}

	@Override
	public String getVirtualServerName() {
		// TODO Auto-generated method stub
		log("getVirtualServerName called");
		return null;
	}

	@Override
	public int getSessionTimeout() {
		// TODO Auto-generated method stub
		log("getSessionTimeout called");
		return 0;
	}

	@Override
	public void setSessionTimeout(int sessionTimeout) {
		// TODO Auto-generated method stub
		log("setSessionTimeout called with timeout: " + sessionTimeout);
		
	}

	@Override
	public String getRequestCharacterEncoding() {
		// TODO Auto-generated method stub
		log("getRequestCharacterEncoding called");
		return null;
	}

	@Override
	public void setRequestCharacterEncoding(String encoding) {
		// TODO Auto-generated method stub
		log("setRequestCharacterEncoding called with encoding: " + encoding);
	}

	@Override
	public String getResponseCharacterEncoding() {
		// TODO Auto-generated method stub
		log("getResponseCharacterEncoding called");
		return null;
	}

	@Override
	public void setResponseCharacterEncoding(String encoding) {
		// TODO Auto-generated method stub
		log("setResponseCharacterEncoding called with encoding: " + encoding);
		
	}
	
	

}
