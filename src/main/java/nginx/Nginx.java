package nginx;

import static nginx.core.NgGlobal.downcall;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;

import app.SpringApp;
import nginx.core.NgBuffer;
import nginx.core.NgCore;
import nginx.core.NgGlobal;
import nginx.core.NgPool;
import nginx.http.NgHttpRequest;
import nginx.spring.SpringRequestHandler;

public class Nginx  {
	
	// The java handler to say hello !
	public static int sayHello(MemorySegment request) {
		try {
			byte[] response = "Hello devoxx 2026 from java !".getBytes(StandardCharsets.UTF_8);
			int rc;

			rc = NgHttpRequest.ngx_http_discard_request_body(request);
			if (rc != NgCore.NGX_OK) {
		        return rc;
		    }

			// request.set(ValueLayout.JAVA_INT, 528L, 200);
			NgHttpRequest.vhResponseStatus.set(request, 0L, 200);

			// request.set(ValueLayout.JAVA_LONG, 728L, response.length);
			NgHttpRequest.vhResponseContentLength.set(request, 0L, (long) response.length);

			rc = NgHttpRequest.ngx_http_send_header(request);
			if (rc == NgCore.NGX_ERROR || rc > NgCore.NGX_OK /* || r->header_only */) {
		        return rc;
		    }

			NgPool requestPool = NgPool.fromSegment((MemorySegment) NgHttpRequest.vhPool.get(request, 0L));
			NgBuffer buf = NgBuffer.ngx_create_temp_buf(requestPool, 1024).orElseThrow();

			buf.write(response, 0, response.length);
			NgBuffer.NgChainLink out = NgBuffer.ngx_alloc_chain_link(requestPool)
					.orElseThrow()
					.setBuffer(buf)
					.end();

			return NgHttpRequest.ngx_http_output_filter(request, out.getSegment());
		} catch (Throwable e) {
			e.printStackTrace();
			return NgCore.NGX_ERROR;
		}
	}

	public static void main(String[] args) {
		Nginx nginx = new Nginx();
		RequestHandler handler = new SpringRequestHandler(SpringApp.class);
		nginx.start(handler);
	}

	public void start(RequestHandler handler) {
		assignJavaHandler(handler);		
		callNginxMain();
	}

	private void assignJavaHandler(RequestHandler handler) {
		try {
			FunctionDescriptor handlerFd = FunctionDescriptor.of(
				ValueLayout.JAVA_INT, 
				ValueLayout.ADDRESS.withTargetLayout(NgHttpRequest.ngx_http_request_t)
			);
			MethodHandle handlerMh = MethodHandles.lookup().bind(
				handler, 
				"handleRequest", 
				handlerFd.toMethodType()
			);
			MemorySegment handlerSegment = NgGlobal.linker.upcallStub(handlerMh, handlerFd, Arena.global());
			NgGlobal.SYMBOL_LOOKUP
				.find("java_handler")
				.orElseThrow()
				.reinterpret(ValueLayout.ADDRESS.byteSize())
				.set(ValueLayout.ADDRESS, 0L, handlerSegment);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to set java_handler", e);
		}
	}

	private void callNginxMain() {
		MethodHandle main = downcall(
			"main", 
			FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
		);

		var ngArgs = List.of(
				"nginx", "-c", "conf/nginx.conf", 
				"-p", NgGlobal.NG_HOME, 
				"-e", "/dev/stdout");
		MemorySegment argsSegment = Arena.global()
				.allocate(ValueLayout.ADDRESS, ngArgs.size());
		for (int i = 0; i < ngArgs.size(); i++) {
			MemorySegment argSegment = Arena.global().allocateFrom(ngArgs.get(i));
			argsSegment.set(ValueLayout.ADDRESS, i * ValueLayout.ADDRESS.byteSize(), argSegment);
		}

		try {
			main.invoke(ngArgs.size(), argsSegment);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to start nginx", e);
		}
	}

}
