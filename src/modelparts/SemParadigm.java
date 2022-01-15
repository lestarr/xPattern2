package modelparts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.ListOps;

public class SemParadigm {
	
	private Set<Word> args;
//	public Map<Word,Similarity> argsMap = new HashMap<>(); //from the given word
	/**
	 * -1 = not checked; 0 = not synonym; 1 = synonym par: Stockwerk	Etage	Geschoss
	 */
	public int isSynonym = -1;
	public String label = null;
	public SemParadigm(Set<Word> inputArgs) {
		this.args = inputArgs;
	}

	public Set<Word> args() {
		return args;
	}
	
	public Set<Word> argsCopy() {
		Set<Word> set = new HashSet<>();
		set.addAll(args);
		return set;
	}

	public Set<Word> getCopyAllArgs() {
		Set<Word> set = new HashSet<>();
		set.addAll(args);
		return set;
	}
	
	public void addArg(Word arg) {
		this.args.add(arg);
	}	
	public void addArgs(Set<Word> args) {
		this.args.addAll(args);
	}
	
	
	public String toString() {

		List<Word> alist = ListOps.of(args);
		Collections.sort(alist,new Comparator<Word>() {
	        @Override
	        public int compare(Word o1, Word o2) {
	            return o1.toString().compareTo(o2.toString());
	        }
	    });
		return alist.toString();
	}


	public static List<SemParadigm> getParsFromList(Collection<Set<Word>> parlist) {
		List<SemParadigm> splist = new ArrayList<>();
		for(Set<Word> wset: parlist) {
				SemParadigm sp = new SemParadigm(wset);
				splist.add(sp);
		}
		return splist;
	}

	
}
