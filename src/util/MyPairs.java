package util;

public class MyPairs<L,R> implements Comparable<MyPairs<L,R>>{
	
	public L first;
	public R second;
	public double freq;
	
	public MyPairs(L f, R s){
		this.first = f;
		this.second = s;
		this.freq = 1.0;
	}
	
	public MyPairs(L f, R s, double freq){
		this.first = f;
		this.second = s;
		this.freq = freq;
	}
	
	public String toString(){
		return first.toString()+" "+second.toString()+" "+MyUtils.rdouble(freq);
	}
	
	public String toString(String sep){
		return first.toString()+sep+second.toString()+sep+freq;
	}


	@Override
	public int compareTo(MyPairs o) {
		if(this.freq > o.freq) return 1;
		if(this.freq < o.freq) return -1;
		return 0;
	}

}
