
void main() {
	var global = Arena.global();
	
	var seg = global.allocate(10_000);
	seg.set(ValueLayout.JAVA_LONG, 0, 1234567891234567899L);
	
	var hex = HexFormat.ofDelimiter(" ").withUpperCase();
	var arr = new byte[16];
	seg.asByteBuffer().get(arr);
	IO.println( hex.formatHex(arr) );

	
	var confined = Arena.ofConfined();
	seg = confined.allocate(10_000);
	seg.set(ValueLayout.JAVA_DOUBLE, 0, 123.456);
	confined.close();
	// seg.get(ValueLayout.JAVA_DOUBLE, 0);
}