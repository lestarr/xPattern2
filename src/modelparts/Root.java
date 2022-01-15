package modelparts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import model.WordSequences;
import util.MapsOps;

public class Root implements Comparable<Root>{
	
	private String str;
	public Set<Flexion> seenflexes = new HashSet<Flexion>(1);
	public Set<String> seensuffs = new HashSet<String>(1);

	//private Set<MorphParadigm> par = new HashSet<MorphParadigm>(1);
	public Set<Word> seenWords = new HashSet<>(1);
	
	public Map<String,Double> trainedflexes = null;
	public Map<String, Double> tailflexes = null;
	
	public Root(String str) {
		this.str = str;
	}
	
	public String toString() {
		return this.str;
	}
	
	public String info() {
		return this.str + "\t" + seenflexes.toString() + "\t" + seensuffs.toString();
	}
	
//	public void setParadigm(ParadigmMorph par) {
//		if(this.par == null)
//			this.par = par;
//		else{
//			changeParadigm(par);
//		}
//	}

//	/**
//	 * Change paradigm only if they are different. Change par means that this root has to be deleted from the previous paradigm
//	 * @param par
//	 */
//	private void changeParadigm(ParadigmMorph par2) {
//		if(!this.par.equals(par2)) {
//			this.par.deleteRoot(this);
//			this.par = par2;
//		}
//	}
	
//	public void addParadigm(MorphParadigm p) {
//		this.par.add(p);
//	}
//	
//	public Set<MorphParadigm> getParadigms() {
//		return this.par;
//	}
	
	public void addFlex(Flexion f) {
		this.seenflexes.add(f);
	}
	
	public Set<Flexion> getFlexes() {
		return this.seenflexes;
	}
	
	public Set<String> getFlexesString() {
		Set<String> set = new HashSet<>();
		for(Flexion f: seenflexes) set.add(f.toString());
		return set;
	}

	@Override
	public int compareTo(Root r) {
		return this.str.compareTo(r.str);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!other.getClass().equals(this.getClass())) return false;
		return this.str.equals( ((Root)other).str);
	}
	
    @Override
    public int hashCode() {
        return Objects.hashCode(this.str);
    }

	public Set<Word> getWords() {
		return this.seenWords;
	}

	public MorphParadigm getMainParadigm(WordSequences model) {
		Map<String,Double> parScores = new HashMap<>();
		for(Flexion f: this.seenflexes) {
			Word w = model.getWord(this.toString()+f.toString());
			MorphParadigm mp = w.getMorphParadigm();
			if(mp != null) MapsOps.addFreq(mp.getLabel(), parScores);
		}
		String bestMP = MapsOps.getFirst(parScores).first;
		if(bestMP.length() > 0) return model.idx().getMorphParadigm(bestMP);
		
		return null;
	}



}
