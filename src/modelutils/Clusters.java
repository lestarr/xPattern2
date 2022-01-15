package modelutils;

import javafx.util.Pair;
import model.WordSequences;

public class Clusters {
	
	public static boolean accepts(Cluster c1, Cluster c2, WordSequences model) {
		Vector thisVector = c1.getCentroide();
		Vector v2 = c2.getCentroide();
		if(thisVector.size() != v2.size()) return false;
		int sumStrikes = 0;
		int sumBigStrikes = 0;
		for (int i = 0; i < thisVector.size(); i++) {
			double val1 = thisVector.get(i);
			double val2 = v2.get(i);
			
			double diff = val1 - val2;
			if(Math.abs(diff) > 0.2) 
				sumStrikes = sumStrikes + 1;
			if(val1 > 0.8 || val2 > 0.8 || val1 < 0.2 || val2 < 0.2) {
				if(Math.abs(diff) > 0.1) 				
					sumBigStrikes = sumBigStrikes + 1;
			}
				//return false; // the coef > 0.9 and < 0.1 it means a very strong trend, so if the difference is big --> clusters should not be combined
			//}
		}
		
		
		if(sumStrikes > 5 || sumBigStrikes > 2) return false;
		double sim = Cluster.computeSimilarity(c1, c2);
		if(sim < 1.0) return true;
//		if(this.sim > 0.0 && c2.sim > 0.0) { //for not 1 word clusters
//			if(sim / this.sim > 4.0 || sim / c2.sim > 4.0) // if new similarity differs a lot from the old ones
//				return false;
//		}
		double freq1 = getClusterMemberFreq(c1, model);
		double freq2 = getClusterMemberFreq(c2, model);
		
		if(freq1 / freq2 > 10.0 || freq2/freq1 > 10.0) return false; //not allowed cluster for very frequent members with less frequent
		
		return true;
	}
	
	
	private static double getClusterMemberFreq(Cluster c, WordSequences model) {
		double freqSum = 0;
		for(Pair<String,Vector> p: c.getWordValues()) {
			freqSum = freqSum + model.getWord(p.getKey()).freq();
		}
		return (double)(freqSum / c.size());
	}

}
