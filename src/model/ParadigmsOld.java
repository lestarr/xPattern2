package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelparts.SemParadigm;
import modelparts.Similarity;
import modelparts.Word;
import util.ListOps;

public class ParadigmsOld {



	
	public static List<SemParadigm> simpleMerge(List<SemParadigm> pars, double simthh){
		List<SemParadigm> parsCleaned = deleteEquals(pars);
		Map<String,List<SemParadigm>> infomap = gatherRows(parsCleaned);
		List<SemParadigm> parsCombied = disambigCombineRows(infomap, simthh);
		parsCleaned = deleteEquals(parsCombied);
		System.out.println("cleaned: " + parsCleaned.size());
		infomap = gatherRows(parsCleaned);
		parsCombied = disambigCombineRows(infomap, simthh);
		parsCleaned = deleteEquals(parsCombied);
		System.out.println("cleaned: " + parsCleaned.size());

		infomap = gatherRows(parsCleaned);
		parsCombied = disambigCombineRows(infomap, simthh);
		parsCleaned = deleteEquals(parsCombied);
		System.out.println("cleaned: " + parsCleaned.size());
		infomap = gatherRows(parsCleaned);
		parsCombied = disambigCombineRows(infomap, simthh);
		parsCleaned = deleteEquals(parsCombied);
		System.out.println("cleaned: " + parsCleaned.size());

		infomap = gatherRows(parsCleaned);
		parsCombied = disambigCombineRows(infomap, simthh);
		parsCleaned = deleteEquals(parsCombied);
		System.out.println("cleaned: " + parsCleaned.size());
		infomap = gatherRows(parsCleaned);
		parsCombied = disambigCombineRows(infomap, simthh);
		parsCleaned = deleteEquals(parsCombied);
		infomap = gatherRows(parsCleaned);
		System.out.println("cleaned: " + parsCleaned.size());
		parsCombied = disambigCombineRows(infomap, simthh);
		parsCleaned = deleteEquals(parsCombied);
		System.out.println("cleaned: " + parsCleaned.size());
		return parsCleaned;
	}

	private static List<SemParadigm> disambigCombineRows(Map<String, List<SemParadigm>> map, double simthh) {
		List<SemParadigm> splist = new ArrayList<>();
		for(String w: map.keySet()) {
			List<SemParadigm> tmpParlist = map.get(w);
			for(SemParadigm sp1: tmpParlist) {
				for(SemParadigm sp2: tmpParlist) {
					Similarity sim = Similarity.getWordserSimilarity(sp1.args(), sp2.args());
					if(sim.high() > simthh) sp1.addArgs(sp2.args());
				}
			}
			List<SemParadigm> tmpParlistCleaned = deleteEquals(tmpParlist);
			if(tmpParlistCleaned.size() == 1) splist.addAll(tmpParlistCleaned);
			else {
				for(SemParadigm sp: tmpParlistCleaned) {
					splist.add(sp);
				}
				
			}
		}
		return splist;
	}

	private static Map<String, List<SemParadigm>> gatherRows(List<SemParadigm> pars) {
		Map<String, List<SemParadigm>> map = new HashMap<>();
		for(SemParadigm sp: pars) {
			Set<Word> args = sp.getCopyAllArgs();
			for(Word w: args) {
				Set<Word> argsCopy = sp.getCopyAllArgs();
				SemParadigm spnew = new SemParadigm(argsCopy);
				if(!map.containsKey(w.toString()))map.put(w.toString(), new ArrayList<SemParadigm>());
				map.get(w.toString()).add(spnew);
			}
		}
		return map;
	}

	private static List<SemParadigm> deleteEquals(List<SemParadigm> pars) {
		Map<String,SemParadigm> map = new HashMap<>();
		for(SemParadigm sp: pars)
			map.put(sp.toString(), sp);
		return ListOps.of(map.values());
	}

	public static List<SemParadigm> checkSynonyms(List<SemParadigm> pars){
		List<SemParadigm> list = new ArrayList<>();
		Map<Word,SemParadigm> infomap = getInfoMap(pars);
		for(Word keyword: infomap.keySet()) {
			SemParadigm spar = infomap.get(keyword);
			boolean areSynonyms = checkSynonymsIntern(keyword,spar,infomap);
			if(areSynonyms) {
				list.add(spar);
				spar.isSynonym = 1;
			}
		}
		return list;
	}
	private static boolean checkSynonymsIntern(Word keyword, SemParadigm spar, Map<Word, SemParadigm> infomap) {
		int initParSize = spar.args().size();
		Set<Word> initArgs = spar.argsCopy();
		for(Word w: spar.args()) {
			if(infomap.get(w) == null) return false;
			//check par size
			if(infomap.get(w).args().size() > initParSize) 
				initParSize = infomap.get(w).args().size();
			initArgs.addAll(infomap.get(w).args());
		}
		if(initArgs.size() == initParSize) 
			return true;
		return false;
	}

	private static Map<Word, SemParadigm> getInfoMap(List<SemParadigm> pars) {
		Map<Word,SemParadigm> infomap = new HashMap<>();
		for(SemParadigm spar: pars)
			infomap.put(null, spar);
		return infomap;
	}
}
