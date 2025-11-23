package nginx.servlet;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import nginx.core.NgBuffer;
import nginx.core.NgHttp;
import nginx.core.NgPool;
import poc.NgCore;

public class NgResponse implements HttpServletResponse {
	
	public MemorySegment request;
	public NgCore ngCore;
	
	public NgResponse() {
		// this.request = request;
	}
	
	NgResponse(MemorySegment request) {
		this.request = request;
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	
	private NgServletOutputStream _out;
	
	class NgServletOutputStream extends ServletOutputStream {
		NgPool pool;
		NgBuffer.NgChainLink chain; 
		NgBuffer.NgChainLink out; 
		NgBuffer buf; 
		
		NgServletOutputStream() {
			pool = getPool();
			chain = NgBuffer.ngx_alloc_chain_link( pool ).orElseThrow();
			out = chain;
			buf = NgBuffer.ngx_create_temp_buf( pool, 4096 ).orElseThrow();
			chain.setBuffer( buf );
		}
		
		NgBuffer.NgChainLink chainNext() {
			return out;
		}
		
		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
		}
		
		@Override
		/**
		 * Override for efficiency
		 */
		public void write(byte[] b, int off, int len) throws IOException {
			int todo = len;
			long remaining = buf.remaining();
			while ( todo > 0 ) {
				System.out.println("todo=" + todo + ", remaining=" + remaining);
				if ( remaining == 0 ) {
					/*
					buf = NgBuffer.ngx_create_temp_buf( pool, 4096 ).orElseThrow();
					chain = chain.setBuffer( buf );
					remaining = buf.remaining();
					*/
					// TODO: limit amount of data to buffer length right now. todo Next : allocate new chain link
					return;
				}
				int toWrite = (int) Math.min( todo, remaining );
				buf.write( b, off, toWrite );
				off += toWrite;
				todo -= toWrite;
			}
		}

		@Override
		public void write(int b) throws IOException {
			long remaining = buf.remaining();
			if ( remaining == 0 ) {
				buf = NgBuffer.ngx_create_temp_buf( pool, 4096 ).orElseThrow();
				chain = chain.setBuffer( buf );
			}
			buf = buf.write( b );
		}
		
		@Override
		public void flush() throws IOException {
			chain.end( out );
		}

	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (_out == null) {
			_out = new NgServletOutputStream();
		}
		return _out;
	}

	
	VarHandle vhPool = NgHttp.ngx_http_request_t.varHandle(
		groupElement("pool")
	);
	private NgPool getPool() {
		MemorySegment poolSeg = (MemorySegment) vhPool.get(request, 0L);
		System.out.println("Pool segment: " + poolSeg);
		return NgPool.fromSegment(poolSeg);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(getOutputStream());
	}

	@Override
	public void setCharacterEncoding(String encoding) {
		// TODO Auto-generated method stub
		
	}

	private final static VarHandle contentLengthHandle = NgHttp.ngx_http_request_t.varHandle(
		groupElement("headers_out"),
		groupElement("content_length_n")
	);
	@Override
	public void setContentLength(int len) {
		contentLengthHandle.set(request, 0L, (long) len);
	}

	@Override
	public void setContentLengthLong(long len) {
		contentLengthHandle.set(request, 0L, (long) len);
	}

	private final static VarHandle contentTypeLenHandle = NgHttp.ngx_http_request_t.varHandle(
			groupElement("headers_out"),
			groupElement("content_type"),
			groupElement("len")
		);
	private final static VarHandle contentTypeDataHandle = NgHttp.ngx_http_request_t.varHandle(
			groupElement("headers_out"),
			groupElement("content_type"),
			groupElement("data")
		);
	private final static VarHandle contentType_LenHandle = NgHttp.ngx_http_request_t.varHandle(
			groupElement("headers_out"),
			groupElement("content_type_len")
		);
	@Override
	public void setContentType(String type) {
		Arena arena = Arena.ofAuto();
		MemorySegment seg = arena.allocateFrom(type);
		contentTypeDataHandle.set(request, 0L, seg);
		long len = seg.byteSize()-1;
		contentTypeLenHandle.set(request, 0L, len);
		contentType_LenHandle.set(request, 0L, len);
	}

	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocale(Locale loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsHeader(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendError(int sc) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeader(String name, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addHeader(String name, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		
	}

	private final static VarHandle vhStatus = NgHttp.ngx_http_request_t.varHandle(
		groupElement("headers_out"),
		groupElement("status")
	);
	@Override
	public void setStatus(int sc) {
		vhStatus.set(request, 0L, sc);
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public int flush() {
		if ( _out != null ) {
			try {
				_out.flush();
			} catch (IOException e) {
				throw new RuntimeException("Unable to flush response output stream", e);
			}
			return NgHttp.ngx_http_output_filter( request, _out.out.getSegment() );
		} else {
			// no output
			return 200;
		}
	}
	
}