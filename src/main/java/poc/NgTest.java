package poc;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.MemoryLayout.*;

import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.nio.charset.StandardCharsets;

public class NgTest {

	static StructLayout ngx_str_t = structLayout(
			JAVA_LONG.withName("len"),
			ADDRESS.withName("data")
		);
	
	static StructLayout ngx_http_request_t = structLayout(
			JAVA_INT.withName("signature"),
			paddingLayout(4), 
			
			ADDRESS.withName("connection").withTargetLayout(ngx_str_t),
			ngx_str_t.withName("uri")
		);

		final VarHandle strLenHandle = 
				MethodHandles.insertCoordinates(
						ngx_str_t.varHandle( PathElement.groupElement("len") ), 1, 0L);

		final VarHandle strDataHandle = 
				MethodHandles.insertCoordinates(
						ngx_str_t.varHandle( PathElement.groupElement("data") ) , 1, 0L);



		String asString(MemorySegment seg) {
			if ( seg == MemorySegment.NULL ) {
				return "null";
			}
			// seg.reinterpret(ngx_str_t.byteSize());
			// MemUtils.dump(seg);
			long len = (long) strLenHandle.get(seg);
			MemorySegment dataSeg = (MemorySegment) strDataHandle.get(seg);
			dataSeg = dataSeg.reinterpret(len);
			return StandardCharsets.UTF_8.decode( dataSeg.asByteBuffer() ).toString();
		}
		
		MemorySegment asString(String value) {
			byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
			MemorySegment data = Arena.global().allocate(bytes.length);
			data.asByteBuffer().put(bytes);
			
			MemorySegment str = Arena.global().allocate(ngx_str_t);
			str.set(JAVA_LONG, 0L, bytes.length);
			str.set(ADDRESS, 8L, data);

			return str;
		}
		
		MemoryLayout uriHandle = ngx_http_request_t.select( PathElement.groupElement("uri") );
		
		final VarHandle connectionHandle = 
				MethodHandles.insertCoordinates(
						ngx_http_request_t.varHandle( PathElement.groupElement("connection") ), 1, 0L);
		final MethodHandle connectionHandleStringFilter;
		NgTest() {
			try {
				connectionHandleStringFilter = MethodHandles.filterReturnValue(
						connectionHandle.toMethodHandle(AccessMode.GET),
						//MethodHandles.insertArguments(
						//	MethodHandles.lookup()
						//		.findVirtual(NgTest.class, "asString", MethodType.methodType(MemorySegment.class, String.class)),
						//	1, this),
						MethodHandles.insertArguments(
							MethodHandles.lookup()
								.findVirtual(NgTest.class, "asString", MethodType.methodType(String.class, MemorySegment.class)),
							0, this)
					);
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		 

		void handle(MemorySegment request) throws Throwable {
			
			// how to get connection as a java string ?
			MemorySegment connStr = (MemorySegment) connectionHandle.get(request);
			String conn = asString( connStr );
			System.out.println("Connection string: " + conn);

			String str =  (String) connectionHandleStringFilter.invokeExact(request);
			System.out.println("Connection string: " + str);
			// how to get uri as a java string ?

		}
		
		public static void main(String[] args) throws Throwable {
			
			Arena arena = Arena.global();
			MemorySegment request = arena.allocate(ngx_http_request_t);
			request.set(JAVA_INT, 0L, 0x50545448); // "HTTP" signature
			
			MemorySegment connStr = arena.allocate(16L);
			connStr.asByteBuffer().put("hello world !!!!".getBytes());
			MemorySegment str = arena.allocate(ngx_str_t);
			str.set(JAVA_LONG, 0L, 16L);
			str.set(ADDRESS, 8L, connStr);
			request.set(ADDRESS, 8L, str);
			
			MemorySegment uriStr = request.asSlice(16L, ngx_str_t.byteSize());
			str.set(JAVA_LONG, 0L, 16L);
			str.set(ADDRESS, 8L, connStr);
			
			new NgTest().handle( request );
		}
		
}
