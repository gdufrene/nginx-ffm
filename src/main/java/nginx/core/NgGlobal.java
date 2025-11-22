package nginx.core;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Path;

public interface NgGlobal {
	
	long NULL = 0L;

	String NG_HOME = System.getenv().getOrDefault("NG_HOME", "nginx/objs");
	Linker linker = Linker.nativeLinker();
	SymbolLookup SYMBOL_LOOKUP = SymbolLookup.libraryLookup( Path.of(NG_HOME, "nginx.so"), Arena.global() );
	
}
