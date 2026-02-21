package nginx.servlet;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

import org.springframework.util.StringUtils;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import nginx.core.NgBuffer;
import nginx.core.NgCore;
import nginx.core.NgHash;
import nginx.core.NgList;
import nginx.core.NgPool;
import nginx.core.NgString;
import nginx.http.NgHttpRequest;

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
		long len = (long) charsetTypeLenHandle.get(request, 0L);
		if ( len == 0 ) return null;
		MemorySegment dataSeg = (MemorySegment) charsetTypeDataHandle.get(request, 0L);
		dataSeg = dataSeg.reinterpret(len);
		byte[] bytes = new byte[(int) len];
		dataSeg.asByteBuffer().get(bytes);
		return new String(bytes, StandardCharsets.ISO_8859_1);
	}

	private static long offsetContentType = NgHttpRequest.ngx_http_request_t.byteOffset(
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

	
	VarHandle vhPool = NgHttpRequest.ngx_http_request_t.varHandle(
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
	
	private final static VarHandle 
	    charsetTypeLenHandle = NgHttpRequest.ngx_http_request_t.varHandle(
			groupElement("headers_out"),
			groupElement("charset"),
			groupElement("len")
		),
	    charsetTypeDataHandle = NgHttpRequest.ngx_http_request_t.varHandle(
			groupElement("headers_out"),
			groupElement("charset"),
			groupElement("data")
		);

	@Override
	public void setCharacterEncoding(String encoding) {
		System.out.println("setCharacterEncoding called with encoding: " + encoding);
		
		if ( encoding != null && encoding.length() > 0 ) {
			byte[] encBytes = encoding.getBytes(StandardCharsets.ISO_8859_1);
			MemorySegment seg = NgHttpRequest.allocOnPool(request, encBytes.length);
			seg.asByteBuffer().put( encBytes );
			
			charsetTypeDataHandle.set(request, 0L, seg);
			charsetTypeLenHandle.set(request, 0L, encBytes.length);
		}
	}

	private final static VarHandle contentLengthHandle = NgHttpRequest.ngx_http_request_t.varHandle(
		groupElement("headers_out"),
		groupElement("content_length_n")
	);
	@Override
	public void setContentLength(int len) {
		setContentLengthLong(len);
	}

	@Override
	public void setContentLengthLong(long len) {
		contentLengthHandle.set(request, 0L, (long) len);
	}

	private final static VarHandle contentTypeLenHandle = NgHttpRequest.ngx_http_request_t.varHandle(
			groupElement("headers_out"),
			groupElement("content_type"),
			groupElement("len")
		);
	private final static VarHandle contentTypeDataHandle = NgHttpRequest.ngx_http_request_t.varHandle(
			groupElement("headers_out"),
			groupElement("content_type"),
			groupElement("data")
		);
	private final static VarHandle contentType_LenHandle = NgHttpRequest.ngx_http_request_t.varHandle(
			groupElement("headers_out"),
			groupElement("content_type_len")
		);
	@Override
	public void setContentType(String type) {
		
		// type = "text/plain";
		
		long len = 0L;
		
		if ( type != null && type.length() > 0 ) {
			
			int i = type.indexOf(';');
			if ( i >= 0 ) {
				// log("Content-Type parameter ignored, only media type is set: " + type);
				
				int j = type.indexOf("charset=", i);
				// String params = type.substring(i).trim();
				if(j >= 0) {
					String charset = type.substring(j+8).trim();
					log("Setting character encoding from Content-Type parameter: " + charset);
					setCharacterEncoding(charset);
				}
				
				type = type.substring(0, i);
			}
			
			// FIXME: replace with a pool allocation from nginx request pool
			byte[] typeBytes = type.getBytes(StandardCharsets.ISO_8859_1);
			len = typeBytes.length;
			MemorySegment seg = NgHttpRequest.allocOnPool(request, typeBytes.length);
			seg.asByteBuffer().put( typeBytes );
			
			//Arena arena = Arena.global();
			//MemorySegment seg = arena.allocateFrom(type);
			contentTypeDataHandle.set(request, 0L, seg);
		}
		
		contentTypeLenHandle.set(request, 0L, len);
		contentType_LenHandle.set(request, 0L, len);
	}

	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub
		log("setBufferSize called with size: " + size);
	}

	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		log("getBufferSize called");
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		log("flushBuffer called");
	}

	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		log("resetBuffer called");
	}

	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		log("isCommitted called");
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		log("reset called");
	}

	@Override
	public void setLocale(Locale loc) {
		// TODO Auto-generated method stub
		log("setLocale called with locale: " + loc);
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		log("getLocale called");
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		// TODO Auto-generated method stub
		log("addCookie called with cookie: " + cookie);
	}

	@Override
	public boolean containsHeader(String name) {
		return getHeader(name) != null;
	}

	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		log("encodeURL called with url: " + url);
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		log("encodeRedirectURL called with url: " + url);
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub
		log("sendError called with status code: " + sc + " and message: " + msg);
		setStatus(sc);
		// flush();
	}

	@Override
	public void sendError(int sc) throws IOException {
		// TODO Auto-generated method stub
		log("sendError called with status code: " + sc);
		setStatus(sc);
		// flush();
	}

	@Override
	public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
		// TODO Auto-generated method stub
		log("sendRedirect called with location: " + location + ", status code: " + sc + " and clearBuffer: " + clearBuffer);
	}

	@Override
	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		log("setDateHeader called with name: " + name + " and date: " + date);
	}

	@Override
	public void addDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		log("addDateHeader called with name: " + name + " and date: " + date);
	}

	@Override
	public void setHeader(String name, String value) {
		// TODO Auto-generated method stub
		log("setHeader called with name: " + name + " and value: " + value);
		
		if (name.equalsIgnoreCase("Content-Type")) {
			setContentType(value);
			return;
		}
		
		if (name.equalsIgnoreCase("Content-Length")) {
			try {
				setContentLengthLong(Long.parseLong(value));
			} catch (NumberFormatException e) {
				log("Invalid Content-Length value: " + value);
			}
			return;
		}
		
		addHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		
		long nl = name.length();
		long vl = value == null ? 0 : value.length();
		
		if ( vl == 0 ) {
			// FIXME: delete header if value is empty
			log("Empty header value for name: " + name + ", header will not be added");
			return;
		}
		
		// Get request pool and allocate memory for header name and value
		MemorySegment kv = NgHttpRequest.allocOnPool(request, nl + vl);
		kv.asSlice(0, nl).asByteBuffer().put( name.getBytes(StandardCharsets.ISO_8859_1) );
		kv.asSlice(nl, vl).asByteBuffer().put( value.getBytes(StandardCharsets.ISO_8859_1) );
		
		MemorySegment headersList = request.asSlice(offsetHeaders, NgList.ngx_list_t.byteSize());
		MemorySegment tableEltSeg = NgList.ngx_list_push(headersList).reinterpret( NgHash.ngx_table_elt_t.byteSize() );
		
		MemorySegment keySeg = tableEltSeg.asSlice(NgHash.offsetKey, NgString.ngx_str_t.byteSize());
		NgString.strLenHandle.set(keySeg, 0L, nl);
		NgString.strDataHandle.set(keySeg, 0L, kv);
		
		MemorySegment valSeg = tableEltSeg.asSlice(NgHash.offsetValue, NgString.ngx_str_t.byteSize());
		NgString.strLenHandle.set(valSeg, 0L, vl);
		NgString.strDataHandle.set(valSeg, 0L, kv.asSlice(nl));
		
		long hash = NgHash.ngx_hash_key_lc(name);
		NgHash.vh_hash.set(tableEltSeg, 0L, hash);
		
	}

	@Override
	public void setIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		log("setIntHeader called with name: " + name + " and value: " + value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		log("addIntHeader called with name: " + name + " and value: " + value);
	}

	private final static VarHandle vhStatus = NgHttpRequest.ngx_http_request_t.varHandle(
		groupElement("headers_out"),
		groupElement("status")
	);
	@Override
	public void setStatus(int sc) {
		vhStatus.set(request, 0L, sc);
	}

	@Override
	public int getStatus() {
		return (int) vhStatus.get(request, 0L);
	}

	// r->headers_out.headers is an array of ngx_table_elt_t, which has key and value as ngx_str_t. 
	// We need to iterate over this array to find the header with the given name and return its value. 
	@Override
	public String getHeader(String name) {
		return getHeadersEnumeration(name).nextElement();
	}

	static final long offsetHeaders = NgHttpRequest.ngx_http_request_t.byteOffset(  
			PathElement.groupElement("headers_out"),
			PathElement.groupElement("headers")
	);
	
	private Enumeration<String> getHeadersEnumeration(String name) {
		long hash = NgHash.ngx_hash_key_lc(name);
		
		MemorySegment headersList = request.asSlice(offsetHeaders, NgList.ngx_list_t.byteSize());
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
				return current == null ? null : current.getValue();
			}
			
			@Override
			public boolean hasMoreElements() {
				return next != null;
			}
		};
	}
	
	@Override
	public Collection<String> getHeaders(String name) {
		var e = getHeadersEnumeration(name);		
		return Collections.list(e);
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
				return NgHttpRequest.ngx_http_output_filter( request, _out.out.getSegment() );
			} catch (Throwable e) {
				return 500;
			}
		} else {
			// no output
			return 200;
		}
	}
	
	
	public void log(String msg) {
		System.out.println(msg);
	}
	
}