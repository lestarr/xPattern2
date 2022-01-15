package modelparts;

import java.util.HashMap;

import model.WordSequences;

public abstract class LangStructure {
	
	private int id;
	protected String label; 
	
	public LangStructure(int id, String label) {
		this.id = id;
		this.label = label;
	}
	
	
	private double freq = 0;
	
	public double freq() {
		return freq;
	}
	public void addFreq() {
		this.freq = this.freq + 1;
	}
	
	public void addFreq(double freqvalue) {
		this.freq = this.freq + freqvalue;
	}
	
	public HashMap<Object,Double> left_of;
	public HashMap<Object,Double> right_of;

  public void tag(WordSequences model, int howmany, double thh) {
    // TODO Auto-generated method stub
    
  }

}
