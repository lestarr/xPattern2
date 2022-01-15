package modelparts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.MorphModel;
import model.WordSequences;
import util.MapsOps;
import util.MyPairs;

public class MorphParadigm implements Comparable<MorphParadigm>{
	
	private String label;
	private double freq = 0.0;

	private Set<Flexion> flexes; // always change sorted flexes when changing flexes
	private Map<String,Double> flexFreqMap;
	
	public Set<Word> words = new HashSet<Word>();
	public Set<String> suffs = new HashSet<>();
	
	public double score = 0.0;
	public Similarity scoreSim = null;

	public Map<String,Double> tailFlexesFreq = new HashMap<>();
	
//	public Set<MorphParadigm> rightParadigms = new HashSet<>(1);
//	public Set<MorphParadigm> leftParadigms = new HashSet<>(1);

	/**
	 * Paradigm has only sense if there is more than 1 Flexion to the same Root
	 * @param flexes
	 */
	public MorphParadigm(Set<Flexion> flexset, Map<String,Double> flexFreqMap, String label) {
		this.flexes = new HashSet<Flexion>();
		this.flexes.addAll(flexset);
		for(Flexion f: flexes)			f.addPar(this);
		this.flexFreqMap = flexFreqMap;
		this.label = label;
	}
	
	static public Map<String,Double>  getEmptyFlexFreqMap(Set<Flexion> flexset) {
		Map<String,Double>flexFreqMap = new HashMap<String, Double>();
		for(Flexion f: flexset) {
			flexFreqMap.put(f.toString(), 1.0);
		}
		return flexFreqMap;
	}
	
	public void addFlex(Flexion flex, WordSequences model) {
	     model.idx().setStopCreatingParadigms(); //after first paradigm change set stop
		this.flexes.add(flex);
	}
	
	   public void addWaitingFlex(Flexion flex, WordSequences model) {
         model.idx().setStopCreatingParadigms(); //after first paradigm change set stop
         addFlex(flex, model);
         if(this.waitingFlexWordMap == null || this.waitingFlexWordMap.isEmpty())
          return;
        //add paradigm words for new flexes!
        String flexCatName = MorphModel.getFlexLabel(flex.toString(), this.getLabel(), MorphModel.FPREF);
        if(this.waitingFlexWordMap.containsKey(flex.toString())) {
        for(Word w: this.waitingFlexWordMap.get(flex.toString())) {
          model.addCategory(flexCatName, w);
          model.addCategory(this.getLabel(), w);
        }
        }
    }
	
	public void addFlexes(Set<Flexion> flexes, WordSequences model) {
		for(Flexion flex: flexes)		addFlex(flex, model);
	}
	
//	public boolean equals(MorphParadigm par) {
//		return hasSameFlexes(getSortedFlex(par.flexes));
//	}

	public void deleteWord(Word w) {
		words.remove(w);
	}
	
	public void addWord(Word w) {
		this.words.add(w);
	}
	
	public boolean containsFlex(Flexion flex) {
		if(this.flexes.contains(flex))
			return true;
		return false;
	}

	public Set<Flexion> getFlexes() {
		return 	this.flexes;
	}
	
	public Map<String, Double> getFlexFreqMap() {
		return 	this.flexFreqMap;
	}
	
	public String getSortedFlexFreqMap() {
		return MapsOps.getSortedMapAsString(this.flexFreqMap, ", ");
	}

	public Set<Word> getWords() {
		return this.words;
	}
	


	public String toString() {
		return this.getLabel()+ " " +this.freq+" "+ this.getSortedFlex();
	}

	@Override
	public int compareTo(MorphParadigm p) {
		int value1 = this.getWords().size();
		int value2 =  p.getWords().size();
		if(value1 > value2)
			return -1;
		else if(value1 == value2)
			return 0;
		else
			return 1;
	}

	
	public MyPairs<Double,Double> overlap(MorphParadigm other) {
			double sumEqualFlexes = 0.0;
			for(Flexion f: this.flexes) {
				if(other.flexes.contains(f)) sumEqualFlexes ++;
			}
			return new MyPairs<Double,Double>(sumEqualFlexes/this.flexes.size(), sumEqualFlexes/other.flexes.size(), sumEqualFlexes);
	}
	
//	public MyPairs<Double,Double> overlapRoot(MorphParadigm other) {
//		if(this.words.size() <= other.words.size()) {
//			double sumEqualRoots = 0.0;
//			for(Root r: this.roots) {
//				if(other.roots.contains(r)) sumEqualRoots ++;
//			}
//			return new MyPairs<Double,Double>(sumEqualRoots/this.roots.size(), sumEqualRoots/other.roots.size());
//		}
//		else {
//			double sumEqualRoots = 0.0;
//			for(Root r: other.roots) {
//				if(this.roots.contains(r)) sumEqualRoots ++;
//			}
//			return new MyPairs<Double,Double>(sumEqualRoots/this.roots.size(), sumEqualRoots/other.roots.size());
//		}
//	}
	
	public static String getSortedFlex(Set<Flexion> flexes) {
		List<String> sortedFlexes= new ArrayList<>();
		for(Flexion f: flexes) {
			sortedFlexes.add(f.toString());
		}
		Collections.sort(sortedFlexes);
		return sortedFlexes.toString().replaceAll("[\\[\\]]", "");
	}
	
	public String getSortedFlex() {
		List<String> sortedFlexes= getSortedFlexList();
		return sortedFlexes.toString().replaceAll("[\\[\\]]", "");
	}
	
	   public List<String> getSortedFlexList() {
	        List<String> sortedFlexes= new ArrayList<>();
	        for(Flexion f: this.flexes) {
	            sortedFlexes.add(f.toString());
	        }
	        Collections.sort(sortedFlexes);
	        return sortedFlexes;
	    }
	
	public String getSortedFlexFirstWaiting() {
		List<String> sortedFlexes= new ArrayList<>();
		for(Flexion f: this.flexes) {
			sortedFlexes.add(f.toString());
		}
		if(firstWaiting != null) sortedFlexes.add(firstWaiting);
		Collections.sort(sortedFlexes);
		return sortedFlexes.toString().replaceAll("[\\[\\]]", "");
	}

	public String getStringWithRoots() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(getSortedFlex(flexes) + "\t");
		sbuf.append(flexes.size() + "\t");
		sbuf.append(words.size()+"\t");
		sbuf.append(words.toString());
		return sbuf.toString();
	}

//	public void setLeftParadigm(MorphParadigm mpar) {
//		this.leftParadigms.add(mpar);
//	}
//	
//	public void setRightParadigm(MorphParadigm mpar) {
//		this.rightParadigms.add(mpar);
//	}
	
	public void changeLabel(String newLabel) {
	  this.label = newLabel;
  }

  public String getLabel() {
		return this.label;
	}


	public double getFreq() {
//		if(freq < 5)
//			System.out.println("LOW FREQ get: " +freq+" "+ this.label+" " + this.getFlexes());
		return freq;
	}

	public void addFreq() {
		this.freq++;
	}

	public void addFreq(double freq2) {
		this.freq = this.freq + freq2;
	}

	public void addFlexFreqsMainTail(Map<String,Double> flexesFreq) {
		for(String k: flexesFreq.keySet()) {
			if(this.flexFreqMap.containsKey(k)) {
				MapsOps.addFreq(k, this.flexFreqMap, flexesFreq.get(k));
			} else 
				MapsOps.addFreq(k, this.tailFlexesFreq, flexesFreq.get(k));
		}
	}
	
	public void addTailFlexes(Map<String,Double> tailFlexesFreq) {
		for(String k: tailFlexesFreq.keySet()) {
			MapsOps.addFreq(k, this.tailFlexesFreq, tailFlexesFreq.get(k));
		}
	}
	
	public String getTailFlexesAsString() {
		return MapsOps.getSortedMapAsString(this.tailFlexesFreq, ", ");
	}

	public static void combine(MorphParadigm mparMain, MorphParadigm mparTest) {
		mparMain.addFreq(mparTest.getFreq());
		mparMain.addFlexFreqsMainTail(mparTest.getFlexFreqMap());
		mparMain.addFlexFreqsMainTail(mparTest.tailFlexesFreq);		
	}

	public 	Map<String,Double> waitingFlexFreqMap = new HashMap<>();;
	public 	Map<String,Set<Word>> waitingFlexWordMap = new HashMap<>();;

	public Map<String,Double> getWaitingFlexMap() {
		return this.waitingFlexFreqMap;
	}
	
	public String getSortedWaitingFlexMap() {
		return MapsOps.getSortedMapAsString(this.waitingFlexFreqMap, ", ");		
	}

	public void addWaitingFlex(String flexFromFlexPar, double freq) {
		MapsOps.addFreq(flexFromFlexPar, waitingFlexFreqMap, freq);
		this.firstWaiting = MapsOps.getFirst(waitingFlexFreqMap).first;
	}
	public void addWaitingFlexWord(String flexFromFlexPar, Word w) {
		if(!waitingFlexWordMap.containsKey(flexFromFlexPar)) waitingFlexWordMap.put(flexFromFlexPar, new HashSet<>());
		waitingFlexWordMap.get(flexFromFlexPar).add(w);
	}

	public void freqNull() {
		this.freq = 0.0;		
	}
	
	private boolean vanishing = false;
	private String firstWaiting = null;
	public boolean mostDifferentFromOtherMP = false;

	public String firstWaiting() {
		return this.firstWaiting;
	}
	
	public boolean wasVanishing() {
		return vanishing;
	}

	public void setVanishing(boolean vanishing) {
		this.vanishing = vanishing;		
	}
}
