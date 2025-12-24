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
import nginx.core.NgCore;
import nginx.core.NgHttp;
import nginx.core.NgPool;
import nginx.core.NgString;

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

	private static long offsetContentType = NgHttp.ngx_http_request_t.byteOffset(
		groupElement("headers_out"),
		groupElement("content_type")
	);
	@Override
	public String getContentType() {
		MemorySegment ctSeg = request.asSlice(offsetContentType, NgString.ngx_str_t.byteSize());
		if ( ctSeg == MemorySegment.NULL ) {
			return null;
		}
		return NgString.asString( ctSeg );
	}

	
	private NgServletOutputStream _out;
	
	class NgServletOutputStream extends ServletOutputStream {
		NgPool pool;
		NgBuffer.NgChainLink chain; 
		NgBuffer.NgChainLink out; 
		NgBuffer buf; 
		
		NgServletOutputStream() {
			pool = getPool();
			chain = chainNext();
			// chain = NgBuffer.ngx_alloc_chain_link( pool ).orElseThrow();
			out = chain;
			
		}
		
		NgBuffer.NgChainLink chainNext() {
			MemorySegment previousNext = MemorySegment.NULL;
			if (chain != null) {
				previousNext = (MemorySegment) NgBuffer.nextHandle.get( chain.getSegment(), 0L );
				previousNext.reinterpret(8L);
			}
			NgBuffer.NgChainLink res = NgBuffer.ngx_alloc_chain_link( pool ).orElseThrow();
			this.buf = NgBuffer.ngx_create_temp_buf( pool, 2048 ).orElseThrow();
			res.setBuffer( this.buf );
			if ( previousNext != MemorySegment.NULL ) {
				System.out.println("Linking previous next to new chain segment " + res.getSegment());
				NgBuffer.nextHandle.set( chain.getSegment(), 0L, res.getSegment() );
			}
			return res;
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
			while ( todo > 0 ) {
				long remaining = buf.remaining();
				int toWrite = (int) Math.min( todo, remaining );
				buf.write( b, off, toWrite );
				
				off += toWrite;
				todo -= toWrite;
				remaining -= toWrite;

				if ( remaining == 0 ) {
					chain = chainNext();
					remaining = buf.remaining();
				}
			}
		}

		@Override
		public void write(int b) throws IOException {
			long remaining = buf.remaining();
			if ( remaining == 0 ) {
				chain = chainNext();
			}
			buf = buf.write( b );
		}
		
		@Override
		public void close() throws IOException {
			chain.end();
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
				_out.close();
			} catch (IOException e) {
				throw new RuntimeException("Unable to close response output stream", e);
			}
			try {
				return NgHttp.ngx_http_output_filter( request, _out.out.getSegment() );
			} catch (Throwable e) {
				return 500;
			}
		} else {
			// no output
			return 200;
		}
	}
	
}