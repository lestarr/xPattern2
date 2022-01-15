package modelparts;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import model.WordSequences;
import model.Words;
import util.ListOps;
import util.MapsOps;
import util.MyPair;
import util.MyPairs;
import util.MyUtils;

public class MorphParadigms {
	public Map<String,MorphParadigm> paradigms = new HashMap<>();
	private String[] flexesSortedbyFreqInParadigms = null;

	public MorphParadigm getParadigm(Set<Flexion> flexes, WordSequences model) {
		return getParadigm(flexes, model,null);
	}
	
	public MorphParadigm getParadigm(Set<Flexion> flexes, WordSequences model, String label) {
		String sortedFlexes = MorphParadigm.getSortedFlex(flexes);
		if(paradigms.containsKey(sortedFlexes)) {
			MorphParadigm mpar = paradigms.get(sortedFlexes);
			mpar.addFreq();
			return mpar;
		}
		MorphParadigm mpar = model.idx().getNewMorphParadigm(flexes, MorphParadigm.getEmptyFlexFreqMap(flexes),null);
		mpar.addFreq();
		if(label == null) label = model.idx().getMparID();
		paradigms.put(sortedFlexes, mpar);
		return mpar;

	}
	
	public MorphParadigm getParadigm(Set<Flexion> flexes, Map<String,Double> flexFreqs, WordSequences model) {
		String sortedFlexes = MorphParadigm.getSortedFlex(flexes);
		if(paradigms.containsKey(sortedFlexes)) {
			MorphParadigm mpar = paradigms.get(sortedFlexes);
			for(String f: mpar.getFlexFreqMap().keySet()) {
				if(flexFreqs.containsKey(f))
					MapsOps.addFreq(f, mpar.getFlexFreqMap(), flexFreqs.get(f));
			}
			mpar.addFreq();
			return mpar;
		}
		MorphParadigm mpar = model.idx().getNewMorphParadigm(flexes, MorphParadigm.getEmptyFlexFreqMap(flexes),null);
		paradigms.put(sortedFlexes, mpar);
		return mpar;

	}
	
	public void removeParadigm(Set<Flexion> flexes) {
		String sortedFlexes = MorphParadigm.getSortedFlex(flexes);
		if(paradigms.containsKey(sortedFlexes)) {
			paradigms.remove(sortedFlexes);
		}
	}


	public static void buildFromRRFF(WordSequences model, boolean print, String whichTime, double flexFreqTHH) {
		Map<String,Set<String>> rrff = getAnalyzedRootFlex(model, print, whichTime);
		System.out.println("rrff 1 built");
		System.out.println("RRFF size1: " + rrff.size());
		//experiment
		rrff = getRRFFcombinedOnRoot(rrff);
		printRRFF(rrff, model, "-second");

		//end
		System.out.println("RRFF size2: " + rrff.size());
		MorphParadigms mpars = new MorphParadigms();
		int i = 0;
		for(String rr: rrff.keySet()) {
			if(rrff.get(rr).size() < 2 || rr.split("#").length < 2) continue;
			i++;
			Root r1 = model.idx().getRoot(rr.split("#")[0]);
			Root r2 = model.idx().getRoot(rr.split("#")[1]);
			Set<Flexion> fset1 = new HashSet<>();
			Set<Flexion> fset2 = new HashSet<>();
			for(String ff: rrff.get(rr)) {
				if(ff.endsWith("#")) ff = ff+"_";
				Flexion f1 = model.idx().getFlex(ff.split("#")[0], r1 ); 
				f1.addRoot(r1);
				Word w1 = model.getWord(r1.toString()+f1.toString().replaceAll("_", ""));
				r1.seenWords.add(w1);
				Flexion f2 = model.idx().getFlex(ff.split("#")[1], r2 );
				f2.addRoot(r2);
				Word w2 = model.getWord(r2.toString()+f2.toString().replaceAll("_", ""));
				r2.seenWords.add(w2);
				//add only flexes with reasonable freq, avoid seldom flexes, seldom flexes is not sth we look for, because we need robust patterns here
//				if(f1.freq() > flexFreqTHH || f1.toString().matches("die|der|das|dem|den|des"))
				if(f1.freq() > flexFreqTHH || r1.toString().equals("root"))
					fset1.add(f1);
				if(f2.freq() > flexFreqTHH || r2.toString().equals("root"))
//				if(f2.freq() > flexFreqTHH || f2.toString().matches("die|der|das|dem|den|des"))
					fset2.add(f2);			
			}
			
			if(fset1.size() < 2 && fset2.size() < 2) continue;

			MorphParadigm mparLeft  = mpars.getParadigm(fset1, model);
			MorphParadigm mparRight = mpars.getParadigm(fset2, model);

//				mparLeft.addRoot(r1);
//				mparLeft.setRightParadigm(mparRight);
//				mparRight.addRoot(r2);
//				mparRight.setLeftParadigm(mparLeft);
			//if(i%1000 == 0) { 				System.out.println("size of mpars: " + mpars.paradigms.size());			}
			if(mpars.paradigms.size()> 5000) break;
		}
		System.out.println("size before combine: " + model.idx().getMPlabels().size());
	}
	
	private static Map<String, Set<String>> getRRFFcombinedOnRoot(Map<String, Set<String>> rrff) {
		Map<String, Set<String>> rrffNew = new HashMap<>();
	
		for(String rr: rrff.keySet()) {
			if(rrff.get(rr).size() < 2 || rr.split("#").length < 2) continue;
			String r1 = rr.split("#")[0];
			String r2 = rr.split("#")[1];
			Set<String> fset1 = new HashSet<>();
			Set<String> fset2 = new HashSet<>();
			for(String ff: rrff.get(rr)) {
				if(ff.endsWith("#")) ff = ff+"_";
				String f1 = ff.split("#")[0]; 
				String f2 = ff.split("#")[1];
				fset1.add(f1+"#"+f1);
				fset2.add(f2+"#"+f2);		
			}
			
			//experiment: remove paradigms which were build only from 1 word with no flex: "_" -- not very good results: impact small, some good flexes get lost
//			if(fset1.size() == 1 && fset1.iterator().next().toString().equals("_#_")) continue;
//			if(fset2.size() == 1 && fset2.iterator().next().toString().equals("_#_")) continue;
			
			String rr1 = r1+"#"+r1; // just to match the same format
			String rr2 = r2+"#"+r2;
			if(!rrffNew.containsKey(rr1)) rrffNew.put(rr1, new HashSet<>());
			if(!rrffNew.containsKey(rr2)) rrffNew.put(rr2, new HashSet<>());
			
			rrffNew.get(rr1).addAll(fset1);
			rrffNew.get(rr2).addAll(fset2);
			
			//experiment
//			if(fset1.size() == 1 || fset2.size() == 1)
//				System.out.println("MONOFLEX\t" + rr + "\t" + rrff.get(rr));
		}
		
		return rrffNew;
	}

	/**
	 * writes rrff and ffrr - matrices with neighbors roots and flexes as key-values pairs and vice versa
	 */
	private static Map<String, Set<String>> getAnalyzedRootFlex(WordSequences wsmodel, boolean print, String whichTime) {
		int i = 0;
		int freq3 = 0;
		double freqOfAND5percent = wsmodel.getFreqOfAnd() / 20.0;
		Map<String,Set<String>> rrff = new HashMap<>();
		Map<String,Set<String>> ffrr = new HashMap<>();
		for(Word w: wsmodel.idx().getSortedWords()) {
			if(w.freq() < 3) continue;
			freq3++;
			if(w.getRoot() == null || w.getFlex() == null) continue;
			
			//too small roots --> forbidden: un|d, і|_, й|ого, йо|му etc.
//			if(w.getRoot().length() < 2) continue; //--> also not so good, some good flexes get lost
			
			i++;
			if(i%100000 == 0) System.out.println("processed again: "+ i);
			String thisroot = w.getRoot().toString();
			if(wsmodel.idx().prefixFreq.containsKey(thisroot) && wsmodel.idx().prefixFreq.get(thisroot) > 2) continue; // means: do'nt take the possible prefix into paradimg
			String thisflex = w.getFlex().toString();
			Set<Word> rights = w.left_of.keySet();
			for(Word wr: rights) {
				if(wr.getRoot() == null || wr.getFlex() == null) continue;
//				if(!WordSequences.isCollocation(wsmodel, w, wr, 0, 0.001, false))					continue;
				String wrRoot = wr.getRoot().toString();
				if(wsmodel.idx().prefixFreq.containsKey(wrRoot) && wsmodel.idx().prefixFreq.get(wrRoot) > 2) continue; // means: do'nt take the possible prefix into paradimg
//				if(w.left_of.get(wr) < 2) continue; // exclude seldom bigrams: freq < 2, this is very restrictiv to the paradigms - factor 10 less!
				
				String wrFlex = wr.getFlex().toString();
				String rr = thisroot+"#"+wrRoot;
				String ff = thisflex+"#"+wrFlex;
				
				if(!rrff.containsKey(rr)) rrff.put(rr, new HashSet<>());
				rrff.get(rr).add(ff);
				
				if(!ffrr.containsKey(ff)) ffrr.put(ff, new HashSet<>());
				ffrr.get(ff).add(rr);
			}
			//experiment
//			Set<String> articles = new HashSet<>();
//			articles.add("der");articles.add("die");articles.add("das");articles.add("des");articles.add("dem");articles.add("den");
//			for(Word wl: w.right_of.keySet()) {
//				if(articles.contains(wl.toString())) {
//					wl.setFlex(wl.toString());
//					wl.setRoot("");
//					String rr = ""+"#"+thisroot;
//					String ff = wl.toString()+"#"+thisflex;
//					if(!rrff.containsKey(rr)) rrff.put(rr, new HashSet<>());
//					rrff.get(rr).add(ff);
//					
//					if(!ffrr.containsKey(ff)) ffrr.put(ff, new HashSet<>());
//					ffrr.get(ff).add(rr);
//				}
//			}
			
			Set<String> signifWords = Words.getMostSignificantLeftWord(w); //get only the first word
			for(String signif: signifWords) {
				if(wsmodel.getWord(signif).freq() < freqOfAND5percent) 
					continue; //take here only frequent words

				String root = "root";
//				wordSignif.setFlex(signif);
//				wordSignif.setRoot(root);
				String rr = root+"#"+thisroot;
				String ff = signif+"#"+thisflex;
				if(!rrff.containsKey(rr)) rrff.put(rr, new HashSet<>());
				rrff.get(rr).add(ff);
					
				if(!ffrr.containsKey(ff)) ffrr.put(ff, new HashSet<>());
				ffrr.get(ff).add(rr);
			}
		}
		
		//delete all paradigm candidats with freq == 1
		for(String rr: rrff.keySet()) {
			Iterator<String> it = rrff.get(rr).iterator();
			 while (it.hasNext()) {
				 String ff = it.next();
				 if(ffrr.get(ff).size() == 1) {
					 it.remove();
					 continue;
				 }

			 }
		}
		printRRFF(rrff, wsmodel, whichTime);
		System.out.println("words with freq > 2:\t" + freq3);
		System.out.println("words with root/flex:\t" + i);
		System.out.println("%:\t" + (double)((double)i/(double)freq3));
		
		
		wsmodel.idx().rrff = rrff;
		return rrff;
	}

	private static void printRRFF(Map<String, Set<String>> rrff, WordSequences wsmodel, String whichTime) {

			try {
			String fileString = "out/paradigm/" + wsmodel.getLang() + "-rrff-1-SuffixPreferred-"+whichTime + ".txt";
			File newFile = new File(fileString);
			Writer out;
			if(!newFile.exists()) {
				out = MyUtils.getWriter(fileString);
			}else
				out = MyUtils.getWriter(fileString + "2");
			for(String rr: rrff.keySet()) {
				if(rrff.get(rr).size() < 2) continue;
				for(String ff: rrff.get(rr))
					out.write(rr+"\t"+ff+"\n");
				out.write("\n");
			}
			
			out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
	}

	public static void combineBestParadigms( WordSequences model, boolean print) {
		Map<String, Map<String,Double>> outputMap = new HashMap<>();
		Set<String> seen = new HashSet<>();
		Set<String> wasMerged = new HashSet<>();

		for(String id: model.idx().getMPlabels()) {
			if(seen.contains(id)) continue;
			Map<String,Double> map1 = model.idx().getMorphParadigm(id).getFlexFreqMap();
			seen.add(id);
			for(String id2: model.idx().getMPlabels()) {
				if(id.equals(id2)) continue;
				Map<String,Double> map2 =  model.idx().getMorphParadigm(id2).getFlexFreqMap();
				MyPairs<Double, Double> sim;
				Map<String,Double> map1cut = mapCutNonFrequentTail(map1);
				Map<String,Double> map2cut = mapCutNonFrequentTail(map2);

				//do comparisson only on most frequent flexes from the paradigm, if the paradigm is long >= 8 flexes
				if(map1cut.size() < map2cut.size()) sim = overlap(map1cut.keySet(), map2cut.keySet());
				else 	sim = overlap(map2cut.keySet(), map1cut.keySet());

				if(combineMorphClusters(sim, map1, map2) && compareSizeOfMaps(map1, map2) ) {
						//merge clusters
						seen.add(id2);
						wasMerged.add(id2);
						wasMerged.add(id);
						if(print)
							System.out.println("COMBINED PARS\t" + MapsOps.getSortedByKey(map1) + "\tAND\t" + MapsOps.getSortedByKey(map2));
						Map<String,Double> mergedMap;
	
						if(outputMap.containsKey(id)) mergedMap = outputMap.get(id);
						else {
							mergedMap = new HashMap<>();
							for(String flex: map1.keySet())	MapsOps.addFreq(flex, mergedMap, map1.get(flex));
							outputMap.put(id, mergedMap);
						}
						for(String flex: map2.keySet())	MapsOps.addFreq(flex, mergedMap, map2.get(flex));
						seen.add(id2);
				}
			}
			if(!wasMerged.contains(id)) 
				outputMap.put(id, map1);
		}
		List<MorphParadigm> newMpars = new ArrayList<>();
		for(String mparTMPname: outputMap.keySet()) {
			Map<String,Double> flexFreqs = outputMap.get(mparTMPname);
			Set<Flexion> flexSet = new HashSet<>();
			for(String f: flexFreqs.keySet()) {
				flexSet.add(model.idx().getFlex(f));
			}
			MorphParadigm mpar = model.idx().getNewMorphParadigm(flexSet, flexFreqs, null);
			newMpars.add(mpar);
		}
	}
	
	
	private static boolean compareSizeOfMaps(Map<String, Double> map1, Map<String, Double> map2) {
		return ((double)(Math.min(map1.size(), map2.size())) / (double)(Math.max(map1.size(), map2.size())) ) >= 0.6;
	}

	private static Map<String, Double> mapCutNonFrequentTail(Map<String, Double> map) {
//		if(map.size() < 8) return map;
		SortedMap<String,Double> mapSortedByFreqs = MapsOps.getSortedMapString(map);
		int size = mapSortedByFreqs.size();
		Map<String,Double> bestFlexMap = new HashMap<>();
		double mostFreqFlex = MapsOps.getFirst(map).freq;
		int stop = size / 2; // the half
		stop = (int) (mostFreqFlex / 10);
		for (Iterator<String> iter = mapSortedByFreqs.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			double freq = mapSortedByFreqs.get(key);
			if (freq < stop)
				break;
			bestFlexMap.put(key, freq);
			
			
		}
		return bestFlexMap;
	}

	private static boolean combineMorphClusters(MyPairs<Double, Double> sim, Map<String, Double> map1,
			Map<String, Double> map2) {
		if(sim.first < 0.4 && sim.second < 0.4) return false;
		if(map1.size() <= 2 || map2.size() <= 2) {
			if(sim.first > 0.8 && sim.second > 0.8) return true;
		}
		else if(map1.size() <= 4 || map2.size() <= 4) {
			if(sim.first > 0.7 && sim.second > 0.7) return true;
		}
		else if(map1.size() <= 6 || map2.size() <= 6) {
			if(sim.first > 0.6 && sim.second > 0.6) return true;
		}
		else if(map1.size() <= 8 || map2.size() <= 8) {
			if(sim.first > 0.5 && sim.second > 0.5) return true;
		}
		else if(map1.size() > 8 && map2.size() > 8) {
			if(sim.first > 0.5 && sim.second > 0.5) return true;
		}
		return false;
	}
	

	public static <T> MyPairs<Double, Double> overlap(Set<T> oneSet, Set<T> secondSet) {
		double sumEqualFlexes = 0.0;
		for(T e1: oneSet) {
			if(secondSet.contains(e1)) sumEqualFlexes ++;
		}
		return new MyPairs<Double, Double>(sumEqualFlexes/oneSet.size(), sumEqualFlexes/secondSet.size(), sumEqualFlexes);
	}
	
	public static int computeFlexFreqAndGetTHH(WordSequences wsmodel, int percent) {
		computeFlexionFreqs(wsmodel);
		return getFreqTHH(wsmodel.idx().seenFlexes, percent);
	}
	
	public static int getFreqTHH(Map<String, Double> seenFlexes, int percent) {
		Set<String> butnot = new HashSet<>();
		butnot.add("_");
		MyPair mostFrequentFlexNotZero = MapsOps.getFirstButNOT(seenFlexes, butnot);
		return (int) Math.max(1, (mostFrequentFlexNotZero.freq / percent)); 
//		return (int) (mostFrequentFlexNotZero.freq / percent); 

	}

	public static void computeFlexionFreqs(WordSequences wsmodel){
		//statistics for flexes
		wsmodel.idx().seenFlexes = new HashMap<>();
		for(Word w: wsmodel.idx().getSortedWords()) {
			if(w.getFlex() == null) continue;
			String f = w.getFlex();
			MapsOps.addFreq(f, wsmodel.idx().seenFlexes);
		}
	}
	
	public static Map<String,Double> findAmbigFlexes(WordSequences wsmodel) {
		Map<String,Double> ambigFlexes = new HashMap<>();
		
		Map<String, Set<String>> rrff = wsmodel.idx().rrff;
		for(String rr: rrff.keySet()) {
			boolean allEqual = true;
			Set<String> someEqualFlexes = new HashSet<>();
			for(String ff: rrff.get(rr)) {
				if(ff.split("#").length < 2) continue;
				String f1 = ff.split("#")[0]; 
				String f2 = ff.split("#")[1];
				if(f1.equals(f2)) {
						someEqualFlexes.add(f1);
				}
				else allEqual = false;
			}
			if(!allEqual) {
				for(String equalFlex: someEqualFlexes) {
					MapsOps.addFreq(equalFlex, ambigFlexes);
				}
			}
		}
		return ambigFlexes;
 	}
	
	
	

	public static void outputAmbigFlexes(WordSequences wsmodel) {
		//AMBIGE
				System.out.println();
				
				Map<String,Double> ambigFlexes = MorphParadigms.findAmbigFlexes(wsmodel);
				for(String mpstring: wsmodel.idx().getMPlabels()) {
					MorphParadigm mpar = wsmodel.idx().getMorphParadigm(mpstring);
					if(mpar == null) continue;	
					System.out.print("BEST + AMBIG\t");
					for (String flex : mpar.getFlexFreqMap().keySet()) {
						double freq = mpar.getFlexFreqMap().get(flex);
						if(ambigFlexes.containsKey(flex)) flex = flex+"_ambig";
						System.out.print(flex + "=" + freq + ", ");
					}
					System.out.println();
				}
				
//				System.out.println("ambig flexes from MorphModel");
//				MapsOps.printSortedMap(ambigFlexes, null);		
	}

	public static void outputMorphPars(WordSequences wsmodel) {
		for(String mpstring: wsmodel.idx().getMPlabels()) {
			MorphParadigm mpar = wsmodel.idx().getMorphParadigm(mpstring);
			if(mpar == null) continue;		
			if(mpar.getFlexes().size() == 0) continue;
			System.out.print("BEST\t");
			List<String> flexes = ListOps.of(mpar.getFlexFreqMap().keySet());
			Collections.sort(flexes);
			for(String flex: flexes) {
				double freq = mpar.getFlexFreqMap().get(flex);
				System.out.print(flex +"="+freq+", ");
			}
			System.out.println();
		}		
	}

	public List<MorphParadigm> toList(int freqthh) {
		List<MorphParadigm> mparsList = new ArrayList<MorphParadigm>();
		for(MorphParadigm mpar2: this.paradigms.values())
			if(mpar2.getFreq() > freqthh)
				mparsList.add(mpar2);
		return mparsList;
	}

}
