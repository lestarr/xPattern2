package modelparts;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import model.WordSequences;

public class Flexion implements Comparable<Flexion>{
	
//	public static Set<String> seenWords = new HashSet<>(1); //no words into Flexion!!!
//	private double freq = 0; freq should be number of roots!
	private String str;
	public Set<Root> roots = new HashSet<Root>(1);

	private Set<MorphParadigm> pars = new HashSet<MorphParadigm>(1);
	
	public Flexion(String str) {
		this.str = str;
	}
	
	public Flexion(String str, Root root) {
		this.str = str;
		roots.add(root);
	}
	
	public String toString() {
		return this.str;
	}

	public void addRoot(Root root) {
		roots.add(root);
	}
	
	public Set<Root> getRoots() {
		return this.roots;
	}
	
	public void addPar(MorphParadigm par) {
		pars.add(par);
	}
	
	public Set<MorphParadigm> getPars() {
		return this.pars;
	}
	
	
	public double freq() {
		return roots.size();
	}

	@Override
	public int compareTo(Flexion f2) {
		
		return this.str.compareTo(f2.str);
	}

	@Override
	public boolean equals(Object otherFlex) {
		if(!otherFlex.getClass().equals(this.getClass())) return false;
		return this.str.equals( ((Flexion)otherFlex).str);
	}
	
    @Override
    public int hashCode() {
        return Objects.hashCode(this.str);
    }

	public String info() {
		return this.str + "\t" + getPars().toString() + "\t" + getRoots().toString();
	}
	
	public static Set<Flexion> getFlexionSetFromString(WordSequences model, Set<String> flexStringSet) {
		Set<Flexion> flexSet = new HashSet<>();
		for(String f: flexStringSet) {
			flexSet.add(model.idx().getFlex(f));
		}
		return flexSet;
	}

	public String toRealString() {
		// TODO Auto-generated method stub
		if(this.toString().equals("_"))
			return "";
		return this.toString();
	}
}
