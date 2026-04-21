package nginx.core;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

import static java.lang.foreign.ValueLayout.*;
import static nginx.core.NgGlobal.downcall;

import java.lang.foreign.Arena;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

public interface NgLog extends NgGlobal {
	
	StructLayout log_t = MemoryLayout.structLayout(
		JAVA_INT.withName("log_level"),
		MemoryLayout.paddingLayout(4),
		ADDRESS.withName("file"),
		JAVA_INT.withName("connection"),
		MemoryLayout.paddingLayout(4),
		JAVA_LONG.withName("disk_full_time"),
		ADDRESS.withName("handler"),
		ADDRESS.withName("data"),
		ADDRESS.withName("writer"),
		ADDRESS.withName("wdata"),
		ADDRESS.withName("action"),
		ADDRESS.withName("next")
	);

	// ngx_log_t *ngx_log_init(u_char *prefix, u_char *error_log);
	MethodHandle ngx_log_init = downcall(
		"ngx_log_init",
		FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS)
	);
	
	static Optional<NgLog> ngx_log_init(String prefix, String error_log) {
		try {
			Arena arena = Arena.global();
			MemorySegment prefixSeg = arena.allocateFrom(prefix);
			MemorySegment errorLogSeg = arena.allocateFrom(error_log);
			MemorySegment logSeg = (MemorySegment) ngx_log_init.invokeExact(prefixSeg, errorLogSeg);
			
			return logSeg.address() == NULL ? Optional.empty() : Optional.of(new NgLogImpl(logSeg));

		} catch (Throwable e) {
			throw new RuntimeException("Unable to initialize ngx_log", e);
		}
	}
	
	MemorySegment getMemorySegment();

	static NgLog fromSegment(MemorySegment seg) {
		return new NgLogImpl(seg);
	}
}

class NgLogImpl implements NgLog {
	
	MemorySegment log;
	
	NgLogImpl(MemorySegment log) {
		this.log = log;
	}
	
	@Override
	public MemorySegment getMemorySegment() {
		return log;
	}
}