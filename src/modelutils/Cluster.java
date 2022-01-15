package modelutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import model.WordSequences;
import model.Words;
import util.ListOps;
import util.MyPair;
import util.MyUtils;

public class Cluster {
	
//	public static boolean TRAIN = false;
//	public static boolean TAG = false;
	
  static public int morphParNum = 0;
  static public int maxClusterSize = 20;
	
	private String label = null;
	
	private List<Pair<String,Vector>> wordvalues = new ArrayList<>();
	private Vector centroide;
	private double sim; //how similar are the members
	private List<Pair<Cluster,Double>> bestClusters =  new ArrayList<>(1);
	public Pair<Cluster,Double> bestClustersBackup =  null;

	private double bestClusterSim = Double.MAX_VALUE;
	
	public double getBestClusterSim() {
		return this.bestClusterSim;
	}
	public double getSim() {
		return this.sim;
	}
	
	private Set<MyPair> paradigmWords = new HashSet<>();
	
	public Set<MyPair> getParadigmWords() {
	  if(paradigmWords.size() < 2) clusterSimMax = -1;
		return paradigmWords;
	}
	public  List<MyPair> getParadigmWordsSorted() {
    List<MyPair> plist = new ArrayList<>();
    plist.addAll(this.paradigmWords);
    Collections.sort(plist,Collections.reverseOrder());
    return plist;
  }

  public void addParadigmWord(MyPair w) {
      this.paradigmWords.add(w);
  }
  
  public void addParadigmWord(MyPair w, double wfreq, double freqThh) {
    if(this.paradigmWords.size() > maxClusterSize) return;
    if(this.highFreqCluster) {
      if(wfreq > freqThh)       this.paradigmWords.add(w);
    }
    else
      this.paradigmWords.add(w);
  }
  
  public void addParadigmWordOld(MyPair w, double stepForClusterThh) {
    if(this.getParadigmWords().size() < 2) {
      addPWIntern(w);
      return;
    }
	if(w.freq > this.getClusterSimThh(stepForClusterThh)) return;
	addPWIntern(w);
  }
  
	private void addPWIntern(MyPair w) {
	  if(w.freq > this.clusterSimMax) this.clusterSimMax = w.freq;
      this.paradigmWords.add(w);
  }
	
  public void clearParadigmWords() {
		this.paradigmWords.clear();
		clusterSimMax = -1;
	}

	public String morphPar = null;

  public MyPair bestCluster = null;

  private double clusterSimMax = -1;

  public boolean highFreqCluster = false;

  public String combinedToCLuster = null;

  public boolean isAndCluster = false;

  public List<MyPair> seedMembers = new ArrayList<>();
  public Map<String,Double> bestSeedClustesScore = new HashMap<>();
  public MyPair firstMpar = null;

  public double getClusterSimThh(double step) {
    if(paradigmWords.size() < 2) {
      clusterSimMax = -1;
    }
    return clusterSimMax + step;
  }
	/**
	 * main construtor. all fields can be filled from this.
	 * @param list
	 */
	public Cluster(List<Pair<String,Vector>> list) {
			for(Pair<String,Vector> p: list) {
				this.sim = 0;
				this.wordvalues.add(p);
			}
			computeCentroide();
			this.sim = computeSimilarity(this, this);
	}
	
	public Cluster(List<Pair<String,Vector>> list, Vector centroide) {
		if(list.size() < 1) {
			this.centroide = centroide;
			this.sim = 0;
		}else {
			new Cluster(list);
		}
		
	}
	
	public Cluster(Pair<String,Vector> pair) {
		this.wordvalues = ListOps.of(pair);
		computeCentroide();
		this.sim = 0;
	}
	
	public Cluster(Vector centroide) {
		this.centroide = centroide;
		this.sim = 0;
	}
	
	public int size() {
		return this.wordvalues.size();
	}
	
//	public Cluster(Cluster c, Pair<String,Vector> newMember) {
//		this.wordvalues = c.wo;
//		computeCentroide(inputwords);
//	}

	public Vector computeCentroide() {
		//EXPERIMENT FOR SIM
//		if(this.wordvalues.size() > 2) return this.centroide;
		List<Pair<String, Vector>> inputwords = this.wordvalues;
		if(inputwords == null) return this.centroide;
		int vectorlength = inputwords.get(0).getValue().size();
		Double[] newVerctor = new Double[vectorlength];
		for (int i = 0; i < vectorlength; i++) {
			double sumValues = 0.0;
			for (int j = 0; j < inputwords.size(); j++) {
				sumValues = sumValues + inputwords.get(j).getValue().get(i);
			}
			newVerctor[i] = sumValues / inputwords.size();
		}
		this.centroide = new Vector(newVerctor);
		return this.centroide;
	}
	
	public Vector addWord(String s, Vector v) {
			Pair<String,Vector> p = new Pair<>(s,v);
			if(v != null) this.sim = computeSimilarity(this, new Cluster(p));
			 this.wordvalues.add(p);
			if(v != null) computeCentroide();
			return this.centroide;
		}

	public Vector getMedianCentroide() {
		List<Pair<String, Vector>> inputwords = this.wordvalues;
		int vectorsize = inputwords.get(0).getValue().size();
		Double[] newVerctor = new Double[vectorsize];
		for (int i = 0; i < vectorsize; i++) {
			List<Double> featureValues = new ArrayList<>();
			for (int j = 0; j < inputwords.size(); j++) {
				featureValues.add(inputwords.get(j).getValue().get(i));
			}
			Collections.sort(featureValues);
			int medianIndex = featureValues.size() / 2;
			newVerctor[i] = featureValues.get(medianIndex);
		}
		return new Vector(newVerctor);
	}
	
	public Vector removeWord(String s) {
		Cluster tmpCluster = new Cluster(this.centroide);
		for(Pair<String,Vector>p: this.wordvalues) {
			if(p.getKey().equals(s)) 
				continue;
			else {
				tmpCluster.addWord(p.getKey(), p.getValue()); //as we use old CLusters Centroide here, the similarity will be slightly (or more) biased!
			}
		}
		this.wordvalues = tmpCluster.wordvalues;
		if(tmpCluster.wordvalues.size() > 0)
			computeCentroide();
		return this.centroide;
	}
	
	public Vector addCluster(Cluster c) {
//		this.sim = computeSimilarity(this.centroide, c.centroide);
		this.sim = computeSimilarity(this, c);
		//tmp: to see where cluster were added
//		Pair<String,Vector> firstAddedPair = c.wordvalues.get(0); 
//		c.wordvalues.add(0, new Pair<String,Vector>("#"+firstAddedPair.getKey(), firstAddedPair.getValue()));
//		c.wordvalues.remove(1);
		//end tmp
		for(Pair<String,Vector> p: c.wordvalues)
			if(!clusterAlreadyAdded(p, this.wordvalues))
				this.wordvalues.add(p);
		computeCentroide();
		return this.centroide;
	}
	
	private boolean clusterAlreadyAdded(Pair<String, Vector> wordClusterInCheck, List<Pair<String, Vector>> wordvalues) {
		for(Pair p: wordvalues) {
			String wordString = wordClusterInCheck.getKey();
			if(p.getKey().equals(wordString)) return true;
		}
		return false;
	}

	public Vector getCentroide() {
		return this.centroide;
	}
	
	public List<Pair<String, Vector>> getWordValues(){
		return this.wordvalues;
	}
	
	public static double computeSimilarity(Cluster c1, Cluster c2) {
		double sim = computeVectorSimilarityQuadrat(c1.centroide, c2.centroide);
		if(c1.morphPar != null && morphParNum != 0 ) {
			//PUNISHMENT FOR DIFFERENT MORPH PAR
			if(c2.morphPar == null || !c1.morphPar.equals(c2.morphPar)) {
				sim = sim + morphParNum;
			}
		}
		return sim;
	}
	
//	public static double computeSimilarity(Cluster c1, Cluster c2) {
//		double productC1C2sizes = c1.size() * c2.size();
//		double simSum = 0.0;
//		for(Pair<String,Vector> p1: c1.getWordValues()) {
//			for(Pair<String,Vector> p2: c2.getWordValues()) {
//				double sim = computeVectorSimilarity(p1.getValue(), p2.getValue());
//				simSum = simSum + sim;
//			}
//		}
//		double newClusterSim = simSum / productC1C2sizes;
//		return newClusterSim;
//	}
	
	/**
	 * smalle is better (more similar)
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double computeVectorSimilarity(Vector v1, Vector v2) {
		double sum = 0;
		for (int i = 0; i < v1.size(); i++) {
			double one = v1.get(i);
			double two = v2.get(i);
			////new similarity
//			double amountToAdd;
//			if(one > two) amountToAdd = one/two;
//			else amountToAdd = two/one;
//			sum = sum + amountToAdd;
			///// end new similarity
			
			//old similarity
			sum =  sum + Math.abs(one - two);
			//boost distance when value < 0.1 or < 0.01
//			if(Math.min(one, two) < 0.01 && Math.abs(one - two) > 0.005)
//				sum = sum + 1;
//			else if(Math.min(one, two) < 0.1 && Math.abs(one - two) > 0.05)
//				sum = sum + 1;
//			else if(Math.min(one, two) < 1 && Math.abs(one - two) > 0.5)
//				sum = sum + 1;
//			else if(Math.max(one, two) > 0.5 && Math.min(one, two) < 0.5 && Math.abs(one - two) > 0.3)
//				sum = sum + 1;
		}
		return sum;
	}
	
	public static double computeVectorSimilarityQuadrat(Vector v1, Vector v2) {
		double sum = 0;
		for (int i = 0; i < v1.size(); i++) {
			double one = v1.get(i);
			double two = v2.get(i);
			////new similarity
//			sum =  sum + Math.abs(one - two);
			double sim_computed = Math.abs(one - two);
			sim_computed = 20*(sim_computed*sim_computed);
			sum =  sum + sim_computed; //Math.abs(one - two);
		}
		return sum;
	}
	
	/**
	 * formula for cosine similarity: sum(a_i*b_i) / (sqrt(sum(a_i^2)) * sqrt(sum(b_i^2)) )
	 * smalle is better (more similar)
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double computeVectorSimilarityCos(Vector v1, Vector v2) {
		double sum = 0;
		for (int i = 0; i < v1.size(); i++) { // length of v1 == length of v2, those are vectors of parameters
			double one = v1.get(i); // value in v1
			double two = v2.get(i); // value in v2

		}
		return sum;
	}
	
	/**
	 * bigger is better (more similar)
	 * @param v
	 * @param v2
	 * @return
	 */
	public static double computeVectorSimilarityOneOne(Vector v1, Vector v2) {
		double sum = 0;
		for (int i = 0; i < v1.size(); i++) {
			double one = v1.get(i);
			double two = v2.get(i);
			if(one + two == 2.0) // means the vectors have 1 at the same idx: 1:1. Combinations 0:0, 0:1 and 1:0 are ignored
				sum =  sum + 1;
			
		}
		return sum;
	}


	public static double computeClusterSimilarityOneOne(Set<Vector> vectorset) {
		double minSim  = Double.MAX_VALUE;
		for(Vector v1: vectorset) {
			for(Vector v2: vectorset) {
				double sim = computeVectorSimilarityOneOne(v1, v2);
				if(sim < minSim) minSim = sim;
			}
		}
		return minSim;
	}

	public String toStringAll() {
		StringBuffer sb = new StringBuffer();
		for(Pair p: wordvalues)
			sb.append(p.getKey() +", ");
		sb.append("\tcluster sim: " + this.sim);
		sb.append("\t");
		for(Pair p: wordvalues)
			sb.append(p.getValue().toString() +"; ");
		return sb.toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(this.getLabel() != null) sb.append(this.getLabel()+"\t");
		sb.append(wordvalues.size() + "\t");
		for(Pair p: wordvalues)
			sb.append(p.getKey() +", ");
		return sb.toString();
	}
	
	public String toStringShort() {
		StringBuffer sb = new StringBuffer();
		for(Pair p: wordvalues)
			sb.append(p.getKey() +", ");
		return sb.toString().replaceFirst(", $", "");
	}

	public String toStringShort(int limit) {
		StringBuffer sb = new StringBuffer();
		sb.append(wordvalues.size() + "\t");
		int i = 0;
		for(Pair p: wordvalues) {
			i++;
			sb.append(p.getKey().toString() +", ");
			if(i > limit) break;
		}
		return sb.toString();
	}
	
	public List<String> toStringAlphabet() {
		List<String> words = ListOps.of();
		for(Pair<String,Vector> p: wordvalues)
			words.add(p.getKey());
		Collections.sort(words);
		return words;
	}

	public static Vector computeMergedVector(Set<Vector> cluster) {
		Vector mergedVec = cluster.iterator().next();
		for(Vector currVec: cluster) {
			for (int i = 0; i < currVec.size(); i++) {
				if(currVec.get(i) == 1.0) 
					mergedVec.set(i, 1.0);
			}
		}
		return mergedVec;
	}

	public static double computMinSimOneOne(Set<Vector> vectorsInCluster) {
		double minSim = Double.MAX_VALUE;
		for(Vector v: vectorsInCluster) {
			for(Vector v2: vectorsInCluster) {
				double newSim = computeVectorSimilarityOneOne(v, v2) ;
				if(newSim < minSim) minSim = newSim;
			}
		}
		return minSim;
	}

	public void setBestClusters(List<Pair<Cluster,Double>> bestClusters) {
		this.bestClusters = bestClusters;	
		if(bestClusters.size() > 0)
			this.bestClusterSim = bestClusters.get(0).getValue();
	}

	public List<Pair<Cluster,Double>> getBestClusters() {
		return this.bestClusters;		
	}

	public String getLabel() {
		return this.label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public List<String> getWords() {
		 List<String> clusterMembers = new ArrayList<>();
		 for(Pair<String,Vector>p: this.wordvalues){
			 clusterMembers.add(p.getKey());
		 }
		return clusterMembers;
	}

//    public static double getScaledSimMeasure(List<String> goldenFeatures, List<String> candidateFeatures) {
//      return getScaledSimMeasureIntern1(goldenFeatures, SetOps.getSet(candidateFeatures));
//    }    
    
    public static double getScaledSimMeasure(List<String> goldenFeatures, List<String> candidateFeatures) {

      Map<String,Integer> goldenMap = new HashMap<>();
      Map<String,Integer> candMap = new HashMap<>();
      for (int i = 0; i < goldenFeatures.size(); i++) {
        goldenMap.put(goldenFeatures.get(i), i);
      }
      for (int i = 0; i < candidateFeatures.size(); i++) {
        candMap.put(candidateFeatures.get(i), i);
      }
      return getScaledSimMeasureIntern2(goldenMap, candMap);
    }
	
	/**
	 * this measure reflects the importance of features which are same in golden and canidate
	 * penalty is biggest for the first feature of golden (if candidate does not have it) and smaller for each next
	 */
	public static double getScaledSimMeasureIntern1(List<String> goldenFeatures, Set<String> candidateFeatures) {
		int size = goldenFeatures.size();
		double sumPenalties = 0;
		for (int i = 0; i < size; i++) {
			String f = goldenFeatures.get(i);
			if(!candidateFeatures.contains(f) || f.startsWith(Words.DUMMY))
				sumPenalties = sumPenalties + (size-i);
		}
		return MyUtils.rdouble((double)sumPenalties/size);
	}
	
	   private static double getScaledSimMeasureIntern2(Map<String,Integer> goldenFeatures, Map<String,Integer> candidateFeatures) {
	        int size = goldenFeatures.size();
	        double sumPenalties = 0;
	        for(String gf: goldenFeatures.keySet()) {
	          double penalty;
	          if(!candidateFeatures.containsKey(gf)|| gf.startsWith(Words.DUMMY))
	            penalty = (double)(size-goldenFeatures.get(gf));
	          else {//compute how many positions candidate feature is shifted to the right or left if messed at the golden features positions in vector sorted on significance
	            int shift = Math.abs(goldenFeatures.get(gf) - candidateFeatures.get(gf));
	            penalty = shift;
	          }
              sumPenalties = sumPenalties + penalty; //(size-i);
	        }
	        return MyUtils.rdouble((double)sumPenalties/size);
	    }
	
  public double getFreqOfAllWOrds(WordSequences model) {
    double sum = 0.0;
    for(MyPair mp: this.getParadigmWords()) {
      sum = sum + model.getWord(mp.first).freq();
    }
    return sum;
  }
  
  public double getNrOfAllWOrds() {
    return (double)this.getParadigmWords().size();
  }
  public String toStringInfo() {
    return this.getLabel() + "\t" + this.getParadigmWords().size() + "\t" + this.firstMpar+ "\t" + this.getParadigmWordsSorted();
  }  
  public String toStringInfoShort() {
    return this.getLabel() + "\t" + this.getParadigmWords().size()+ "\t" + this.firstMpar + "\t" 
  + this.getParadigmWordsSorted().subList(0, Math.min(this.getParadigmWords().size(), 4));
  }
}
