package nginx.core;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

public interface NgGlobal {
	
	long NULL = 0L;
	String NG_HOME_DEFAULT = "nginx";
	String NG_HOME = System.getenv().getOrDefault("NG_HOME", NG_HOME_DEFAULT);
	Linker linker = Linker.nativeLinker();
	SymbolLookup SYMBOL_LOOKUP = SymbolLookup.libraryLookup( Path.of(NG_HOME, "objs", "nginx.so"), Arena.global() );
	
	static MethodHandle downcall( String symbolName, FunctionDescriptor descriptor ) {
		return linker.downcallHandle(
			SYMBOL_LOOKUP.find(symbolName).orElseThrow(),
			descriptor
		);
	}
	
}
