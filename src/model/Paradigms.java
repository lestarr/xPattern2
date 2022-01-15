package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelparts.SemParadigm;
import modelparts.Similarity;
import modelparts.Word;

public class Paradigms {



	
	public static Collection<Set<Word>> simpleMerge(Collection<Set<Word>> parlist2, double thh, double simthh){
		System.out.println("found: " + parlist2.size());

		Collection<Set<Word>> parsCleaned = deleteEquals(parlist2);
		System.out.println("cleaned: " + parsCleaned.size());

		Collection<Set<Word>> newpars = gatherRows(parlist2,thh,simthh);
		System.out.println("merged: " + newpars.size());

		
		return newpars;
	}

	private static Collection<Set<Word>> gatherRows(Collection<Set<Word>> parlist, double thh, double simthh) {
		List<Set<Word>> merged = new ArrayList<>();
		for(Set<Word> par: parlist) {
			checkAndAddParadigmToWord(par, merged, thh, simthh);
		}
		
		return merged;
	}

//	private static Collection<Set<Word>> gatherRows(Collection<Set<Word>> parlist2, double thh) {
//		Map<String, List<Set<Word>>> map = new HashMap<>();
//		for(Set<Word> par: parlist2) {
//			for(Word w: par) {
//				checkAndAddParadigmToWord(map, w, par, thh);
//			}
//		}
//		Collection<Set<Word>> newpars = new ArrayList<>();
//		for(List<Set<Word>> parlist: map.values()) {
//			newpars.addAll(parlist);
//		}
//		return newpars;
//	}

	private static void checkAndAddParadigmToWord(Set<Word> par, List<Set<Word>> merged, double thh, double simthh) {
		boolean wasAdded = false;
		for(Set<Word> checkedPar: merged) {
			double intersect = Similarity.getIntersect(checkedPar, par);
			Similarity sim = Similarity.getWordserSimilarity(par, checkedPar);
			if(intersect > thh && sim.high() > simthh) {
				checkedPar.addAll(par);
				wasAdded = true;
			}
		}
		if(!wasAdded)
			merged.add(par);
	}

	private static void checkAndAddParadigmToWord(Map<String, List<Set<Word>>> map, Word w, Set<Word> parToCheck, double thh) {
		if(!map.containsKey(w.toString())) map.put(w.toString(), new ArrayList<>());
		List<Set<Word>> parlist = map.get(w.toString());
		boolean wasAdded = false;
		for(Set<Word> savedpar: parlist) {
			double sim = Similarity.getIntersect(savedpar, parToCheck);
			if(sim > thh) {
				savedpar.addAll(parToCheck);
				wasAdded = true;
			}
		}
		if(!wasAdded)
			parlist.add(parToCheck);

	}

	private static Collection<Set<Word>> deleteEquals(Collection<Set<Word>> pars) {
		Map<String,Set<Word>> result = new HashMap<>();
		List<String> tmpList = new ArrayList<>();
		for(Set<Word> par: pars)
			result.put(getSortedWordsAsString(par,tmpList), par);
		return result.values();
	}

	public static String getSortedWordsAsString(Set<Word> par, List<String> tmpList) {
		tmpList.clear();
		for(Word w: par) {
			tmpList.add(w.toString());
		}
		Collections.sort(tmpList);
		return tmpList.toString();
	}

	public static List<SemParadigm> checkSynonyms(List<SemParadigm> pars){
		List<SemParadigm> list = new ArrayList<>();
		Map<Word,SemParadigm> infomap = new HashMap<>();//(pars);
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


}
