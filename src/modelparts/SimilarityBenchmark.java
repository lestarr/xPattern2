package modelparts;

import java.util.Set;

import util.MyUtils;

public class SimilarityBenchmark implements Comparable<SimilarityBenchmark> {
	
	private double high;
	private double low;
	private double sumseen = 0.0;
	public double jaccard = 0.0;
	private double penalties = 1.0; // for members which are not central (case Words: Paradigms which are at the end of circle)
	private double recall = 0.0;;
	private double precision = 0.0;
	private double fscore = 0.0;
	
	private boolean lowIsLeft = true; 
	
	public boolean getLowIsLeft() {
		return lowIsLeft;
	}

	/**
	 * Saves significance of left and right part, compares it inot low() and high(). if left == low, getLowIsLeft = true; and vice versa,
	 * so one get information which was the lowest significance (conditional probability) and which is the left context. same for higher significance and right context
	 * @param l
	 * @param r
	 */
	
	public SimilarityBenchmark(Set objectsGolden, Set objectsTest) {
		getSimilarity(objectsGolden, objectsTest);
		this.jaccard = MyUtils.rdouble(sumseen / 
				(double)(objectsGolden.size() - sumseen + objectsTest.size()  ) );
	}
	
	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getFscore() {
		return fscore;
	}

	public void setFscore(double fscore) {
		this.fscore = fscore;
	}

	public double penalties() {
		return penalties;
	}

	public double high() {
	return high;
}

public double low() {
	return low;
}

public double sumseen() {
	return sumseen;
}

public double jaccard() {
	return jaccard;
}


	public void setPenalties(double penalties) {
		this.penalties = penalties;
	}

	
	private void getSimilarity(Set<Object> psetGolden, Set<Object> psetTest) {
		double sumSeen = 0.0;
		
		for(Object parname: psetGolden) {
			if(psetTest.contains(parname)) sumSeen++;
		}
		double sim = (double)sumSeen/psetGolden.size();
		sumSeen = 0.0;
		for(Object parname: psetTest) {
			if(psetGolden.contains(parname)) sumSeen++;
		}
		double sim2 = (double)sumSeen/psetTest.size();
		this.low = Math.min(sim, sim2);
		this.high = Math.max(sim, sim2);
		this.sumseen = sumSeen;
		
		this.recall = sumSeen/psetGolden.size();
		this.precision = sumSeen/psetTest.size();
		this.fscore = getFMeasure(this.recall, this.precision); //(2.0*(recall*precision))/(recall+precision);
	}
	
	public static double getFMeasure(double recall, double precision) {
		return (2.0*(recall*precision))/(recall+precision);
	}

	
	@Override
	public String toString() {
		return low+";"+high;
	}

	@Override
	public int compareTo(SimilarityBenchmark sim) {
//		if(this.low > sim.low)
//			return 1;
//		if(this.low == sim.low && this.high > sim.high)
//			return 1;
//		if(this.low == sim.low && this.high == sim.high)
//			return 0;
//		return -1;
		if(this.jaccard > sim.jaccard) return 1;
		else if(this.jaccard == sim.jaccard) return 0;
		return -1;
	}

	public static void printMeasures(SimilarityBenchmark sim, String label) {
		System.out.print(label + "\tprec\t" + sim.getPrecision());
		System.out.print("\t"+label + "\trec\t" + sim.getRecall());
		System.out.println("\t"+label + "\tfm\t" + sim.getFscore());
	}
}
