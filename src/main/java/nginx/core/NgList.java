package nginx.core;

import static java.lang.foreign.FunctionDescriptor.of;
import static java.lang.foreign.MemoryLayout.paddingLayout;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.*;
import static nginx.core.NgGlobal.downcall;

import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.function.Function;

public interface NgList extends NgGlobal {
	
	StructLayout ngx_part_t = structLayout(
			ADDRESS.withName("elts"),
			JAVA_INT.withName("nelts"),
			paddingLayout(4),
			ADDRESS.withName("next")
		).withName("ngx_part_t");
	
	StructLayout ngx_list_t = structLayout(
			ADDRESS.withName("last"),
			ngx_part_t.withName("part"),
			JAVA_LONG.withName("size"),
			JAVA_INT.withName("nalloc"),
			paddingLayout(4),
			ADDRESS.withName("pool")
		).withName("ngx_list_t");
	
	MethodHandle
		ngx_list_push = downcall("ngx_list_push", of(ADDRESS, ADDRESS));
		
	
	/*
	 *  (comments from nginx source code)
	 *  
	 *  the iteration through the list:
	 *
	 *  part = &list.part;
	 *  data = part->elts;
	 *
	 *  for (i = 0 ;; i++) {
	 *
	 *      if (i >= part->nelts) {
	 *          if (part->next == NULL) {
	 *              break;
	 *          }
	 *
	 *          part = part->next;
	 *          data = part->elts;
	 *          i = 0;
	 *      }
	 *
	 *      ...  data[i] ...
	 *
	 *  }
	 */
	static final VarHandle
		// vhPart = ngx_list_t.varHandle( PathElement.groupElement("part") ),
		vhElts = ngx_part_t.varHandle( PathElement.groupElement("elts") ),
		vhNelts = ngx_part_t.varHandle( PathElement.groupElement("nelts") ),
		vhNext = ngx_part_t.varHandle( PathElement.groupElement("next") ),
		vhSize = ngx_list_t.varHandle( PathElement.groupElement("size") );
				
	static <T> Iterable<T> iterator( Function<MemorySegment, T> mapper, MemorySegment listSeg ) {
		long size = (long) vhSize.get(listSeg, 0L) ;
		
		return () -> new java.util.Iterator<T>() {
			
			MemorySegment part;
			MemorySegment next;
			MemorySegment elts;
			int nelts;
			int i;

			{
				part = (MemorySegment) listSeg.asSlice(ngx_list_t.byteOffset(PathElement.groupElement("part")), ngx_part_t.byteSize() );
				afterNextPart();
			}
			
			private void afterNextPart() {
				if ( part.address() != 0 ) {
					nelts = (int) vhNelts.get(part, 0L);
					elts = (MemorySegment) vhElts.get(part, 0L);
					elts = elts.reinterpret( nelts * size );
					next = (MemorySegment) vhNext.get(part, 0L);
					i = 0;
				}
			}
			
			@Override
			public boolean hasNext() {
				return i < nelts || next.address() != 0;
			}
			
			@Override
			public T next() {
				if ( i >= nelts ) {
					part = (MemorySegment) vhNext.get(part, 0L);
					if ( part.address() == 0 ) {
						throw new java.util.NoSuchElementException();
					}
					afterNextPart();
				}
				MemorySegment element = elts.asSlice( i * size, size );
				i++;
				return mapper.apply( element );
			}
		};
	}
	
	static MemorySegment ngx_list_push(MemorySegment listSeg) {
		try {
			return (MemorySegment) ngx_list_push.invokeExact( listSeg );
		} catch (Throwable e) {
			throw new RuntimeException("Unable to push to ngx_list", e);
		}
	}

}
