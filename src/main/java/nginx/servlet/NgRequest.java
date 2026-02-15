package nginx.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import nginx.core.NgHash;
import nginx.core.NgList;
import nginx.core.NgString;
import nginx.http.NgHttpRequest;

public class NgRequest implements HttpServletRequest {
	
	private final MemorySegment req;
	private final Map<String, Object> attributes = new HashMap<>();
	
	public NgRequest(MemorySegment req) {
		this.req = req;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		// FIXME get from headers_in.content_type ?
		return StandardCharsets.UTF_8.name();
	}

	@Override
	public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		
	}

	final static VarHandle vh_content_length_n = 
		NgHttpRequest.ngx_http_request_t.varHandle(
			PathElement.groupElement("headers_in"),
			PathElement.groupElement("content_length_n")
		);
	@Override
	public int getContentLength() {
		return (int) vh_content_length_n.get(req, 0L);
	}

	@Override
	public long getContentLengthLong() {
		return (long) vh_content_length_n.get(req, 0L);
	}

	final static VarHandle vh_content_type =
			NgHttpRequest.ngx_http_request_t.varHandle(
				PathElement.groupElement("headers_in"),
				PathElement.groupElement("content_type")
			);
	final long offset_key = 
		NgHash.ngx_table_elt_t.byteOffset(
			PathElement.groupElement("key")
		);
	final long offset_value = 
			NgHash.ngx_table_elt_t.byteOffset(
				PathElement.groupElement("value")
			);
	final long size_ngx_str_t = 
			NgHash.ngx_table_elt_t.byteOffset(
				PathElement.groupElement("value")
			);
	@Override
	public String getContentType() {
		
		MemorySegment msContentType = (MemorySegment) vh_content_type.get(req, 0L);
		if ( msContentType.address() == NgString.NULL ) {
			return null;
		}
		/*
		MemorySegment msKey = msContentType.asSlice( offset_key, size_ngx_str_t );
		System.out.println("Content-Type key segment:");
		MemUtils.dump(msKey);
		*/
		
		MemorySegment msValue = msContentType.asSlice( offset_value, size_ngx_str_t );
//		System.out.println("Content-Type value segment:");
//		MemUtils.dump(msValue);
		
		return NgString.asString(msValue);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getParameterValues(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// FIXME mock to localhost interface
		return "127.0.0.1";
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// FIXME mock to false
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocolRequestId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletConnection getServletConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		return getHeaders(name).nextElement();
	}

	static final long offsetHeaders = NgHttpRequest.ngx_http_request_t.byteOffset(  
			PathElement.groupElement("headers_in"),
			PathElement.groupElement("headers")
	); 
	@Override
	public Enumeration<String> getHeaders(String name) {
		long hash = NgHash.ngx_hash_key_lc(name);
		
		MemorySegment headersList = req.asSlice(offsetHeaders, NgHttpRequest.ngx_http_headers_in_t.byteSize());
		Iterable<NgHash.NgxTableElt> iterable = NgList.iterator( 
			(MemorySegment elt) -> new NgHash.NgxTableElt(elt), 
			headersList
		);
		
		final Iterator<NgHash.NgxTableElt> iterator = iterable.iterator();
		
		return new Enumeration<String>() {
			NgHash.NgxTableElt next = findNext();
			
			private NgHash.NgxTableElt findNext() {
				while( iterator.hasNext() ) {
					NgHash.NgxTableElt candidate = iterator.next();
					if ( candidate.getHash() == hash && candidate.getKey().equalsIgnoreCase(name) ) {
						return candidate;
					}
				}
				return null;
			};
			
			@Override
			public String nextElement() {
				NgHash.NgxTableElt current = next;
				next = findNext();
				return current.getValue();
			}
			
			@Override
			public boolean hasMoreElements() {
				return next != null;
			}
		};
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIntHeader(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	final static long offsetMethod = NgHttpRequest.ngx_http_request_t.byteOffset(  
			PathElement.groupElement("method_name")
	); 
	@Override
	public String getMethod() {
		return NgString.asString( req.asSlice(offsetMethod, NgString.ngx_str_t.byteSize()) );
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		// FIXME currently we don't support servlet context, so just return empty string to indicate root context
		return "";
	}

	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		// FIXME always return null (unauthenticated user)
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	private final static long offset_uri = 
		NgHttpRequest.ngx_http_request_t.byteOffset(
			PathElement.groupElement("uri")
		);
	@Override
	public String getRequestURI() {
		MemorySegment ms = (MemorySegment) req.asSlice( offset_uri, NgString.ngx_str_t.byteSize() );
		return NgString.asString(ms);
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}
	
}