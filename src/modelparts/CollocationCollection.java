package modelparts;

import java.util.ArrayList;
import java.util.List;

import util.MyPair;

public class CollocationCollection {
	
	private List<Collocation> strongestCollocations = new ArrayList<>(); // sim > 0.6
	private List<Collocation> strongCollocations = new ArrayList<>(); // sim > 0.3
	private List<Collocation> mediumCollocations = new ArrayList<>(); // > 0.1
	private List<Collocation> weakCollocations = new ArrayList<>(); // > 0.1, > 0.001

	public List<Collocation> getStrongestCollocations() {
		return strongestCollocations;
	}
	public List<Collocation> getStrongCollocations() {
		return strongCollocations;
	}
	public void addStrongestCollocation(Collocation strongCollocation) {
		this.strongestCollocations.add(strongCollocation);
	}
	public void addStrongCollocation(Collocation strongCollocation) {
		this.strongCollocations.add(strongCollocation);
	}
	public List<Collocation> getMediumCollocations() {
		return mediumCollocations;
	}
	public void addMediumCollocations(Collocation mediumCollocation) {
		this.mediumCollocations.add(mediumCollocation);
	}
	public List<Collocation> getWeakCollocations() {
		return weakCollocations;
	}
	public void addWeakCollocations(Collocation weakCollocation) {
		this.weakCollocations.add(weakCollocation);
	}
	public void add(MyPair bigram, Similarity sim) {
		if(sim.high() >=  0.8 && sim.high() < 1.0 && sim.low() > 0.01) addStrongestCollocation(new Collocation(bigram.first, bigram.second, sim));
		else if(sim.low() >=  0.3) addStrongCollocation(new Collocation(bigram.first, bigram.second, sim));
		else if(sim.low() >= 0.1) addMediumCollocations(new Collocation(bigram.first, bigram.second, sim));
		else if(sim.high() >= 0.1 && sim.low() >= 0.001) addWeakCollocations(new Collocation(bigram.first, bigram.second, sim));
	}

}
