package modelparts;

import java.util.ArrayList;
import java.util.List;

public class Collocation implements Comparable<Collocation>{
	
	public String left;
	public String right;
	public Similarity sim;
	private double maxSignif;
	private double freq;
	public Collocation(String l, String r, Similarity sim) {
		this.left = l;
		this.right = r;
		this.sim = sim;
		this.maxSignif = sim.high();
		this.setFreq(0.0);
	}
	
	public String toString() {
		return left + " " + right + " " + sim.toString();
	}

	public double maxSignif() {
		return maxSignif;
	}

	@Override
	public int compareTo(Collocation coll) {
		return Double.compare(this.sim.low(), coll.sim.low())*(-1);
	}

	public double getFreq() {
		return freq;
	}

	public void setFreq(double freq) {
		this.freq = freq;
	}

	public static List<Collocation> getSubCollocs(List<Collocation> colls, double thh) {
		List<Collocation> collsNew = new ArrayList<Collocation>();
		for(Collocation c: colls) {
			if(c.sim.high() > thh)
				collsNew.add(c);
		}
		return collsNew;
	}


}
