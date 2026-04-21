package nginx;

import java.lang.foreign.MemorySegment;

@FunctionalInterface
public interface RequestHandler {

	int handleRequest(MemorySegment request);

}
