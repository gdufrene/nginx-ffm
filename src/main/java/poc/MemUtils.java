package poc;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

public class MemUtils {

	public static void dump(MemorySegment segment) {
		ByteBuffer buffer = segment.asByteBuffer();
		System.out.println("Memory dump:");
		for (int i = 0; i < buffer.capacity(); i += 16)
			{
			System.out.printf("%08X  ", i);
			for (int j = 0; j < 16; j++) {
				if (i + j < buffer.capacity()) {
					System.out.printf("%02X ", buffer.get(i + j));
				} else {
					System.out.print("   ");
				}
			}
			System.out.print(" ");
			for (int j = 0; j < 16; j++) {
				if (i + j < buffer.capacity()) {
					byte b = buffer.get(i + j);
					if (b >= 32 && b <= 126) {
						System.out.print((char) b);
					} else {
						System.out.print(".");
					}
				}
			}
			System.out.println();
		}
	}
}
