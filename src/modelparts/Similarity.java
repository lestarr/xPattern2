package modelparts;

import java.util.Set;

public class Similarity implements Comparable<Similarity> {
	
	private double high;
	private double low;
	
	public double left;
	public double right;
	
	/**
	 * Saves significance of left and right part, compares it inot low() and high(). if left == low, getLowIsLeft = true; and vice versa,
	 * so one get information which was the lowest significance (conditional probability) and which is the left context. same for higher significance and right context
	 * @param l
	 * @param r
	 */
	public Similarity(double l, double r) {
		this.left = l;
		this.right = r;
		this.low = Math.min(l, r);
		this.high = Math.max(l, r);
	}
	

	public double high() {
		return high;
	}

	public double low() {
		return low;
	}

	@Override
	public String toString() {
		return left+";"+right;
	}

	@Override
	public int compareTo(Similarity sim) {
		if(this.low > sim.low)
			return 1;
		if(this.low == sim.low && this.high > sim.high)
			return 1;
		if(this.low == sim.low && this.high == sim.high)
			return 0;
		return -1;
	}
	
	public static Similarity getWordserSimilarity(Set<Word> set1, Set<Word> set2) {
		if(set1.size() == 0 || set2.size() == 0) return new Similarity(0.0, 0.0);
		double intersect =  0.0;
		for(Word w: set1) {
			if(set2.contains(w)) intersect++;
		}
		return new Similarity((intersect / set1.size()), (intersect/set2.size()));
	}

	public static double getIntersect(Set<Word> set1, Set<Word> set2) {
		if(set1.size() == 0 || set2.size() == 0) return 0.0;
		double intersect =  0.0;
		for(Word w: set1) {
			if(set2.contains(w)) intersect++;
		}
		return intersect;
	}
}
