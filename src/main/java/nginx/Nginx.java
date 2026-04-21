package nginx;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.List;

public class Nginx {
	
	public static void main(String[] args) throws Throwable {
		String NG_HOME = System.getenv("NG_HOME");
		if (NG_HOME == null) {
			System.err.println("Please set NG_HOME environment variable to the root of your nginx sources");
			System.exit(1);
		}
		var lookup = SymbolLookup.libraryLookup(Path.of(NG_HOME, "objs", "nginx.so"), Arena.global());
		var linker = Linker.nativeLinker();

		MethodHandle main = lookup.find("main")
		  .map( s -> linker.downcallHandle(s, 
		    FunctionDescriptor.of(ValueLayout.JAVA_INT, 
		     ValueLayout.JAVA_INT, ValueLayout.ADDRESS)) 
		  )
		  .orElseThrow();

		var ngArgs = List.of(
		  "nginx", "-c", "conf/nginx.conf", 
		  "-p", NG_HOME, 
		  "-e", "/dev/stdout");
		MemorySegment argsSegment = Arena.global()
		  .allocate(ValueLayout.ADDRESS, ngArgs.size());
		for (int i = 0; i < ngArgs.size(); i++) {
		  MemorySegment argSegment = Arena.global().allocateFrom(ngArgs.get(i));
		  argsSegment.set(ValueLayout.ADDRESS, i * ValueLayout.ADDRESS.byteSize(), argSegment);
		}

		int ret = (int) main.invoke(ngArgs.size(), argsSegment);
	}

}
