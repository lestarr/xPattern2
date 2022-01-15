package utils;

public class MyException extends Exception {
	
	private String info;
	private Object o;
	
	public MyException(String info, Object o) {
		this.info = info;
		this.o = o;
	}
	
	public void info() {
		System.err.println(info + ":\t" + o.toString());
	}

}
