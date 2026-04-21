
void main() {	
	var confined = Arena.ofConfined();
	var seg = confined.allocate(10_000);
	seg.set(ValueLayout.JAVA_DOUBLE, 0, 123.456);
	confined.close();
	seg.get(ValueLayout.JAVA_DOUBLE, 0);
}