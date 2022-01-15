//package modelparts;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.Writer;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.SortedMap;
//
//import model.WordSequences;
//import modelutils.Cluster;
//import modelutils.Vector;
//import util.ListOps;
//import util.MapsOps;
//import util.MyPair;
//import util.MyPairs;
//import util.MyUtils;
//import util.SetOps;
//
//public class MorphParadigmsOld {
//	public Map<String,MorphParadigm> paradigms = new HashMap<>();
//	private String[] flexesSortedbyFreqInParadigms;
//	
//	
//
////	public void addParadigm(MorphParadigm mpar) {
////		paradigms.put(mpar.getSortedFlex(), mpar);
////	}
//
//	public MorphParadigm getParadigm(Set<Flexion> flexes) {
//		String sortedFlexes = MorphParadigm.getSortedFlex(flexes);
//		if(paradigms.containsKey(sortedFlexes)) return paradigms.get(sortedFlexes);
//		MorphParadigm mpar = new MorphParadigm(flexes, MorphParadigm.getEmptyFlexFreqMap(flexes));
//		paradigms.put(sortedFlexes, mpar);
//		return mpar;
//
//	}
//	
//	public void removeParadigm(Set<Flexion> flexes) {
//		String sortedFlexes = MorphParadigm.getSortedFlex(flexes);
//		if(paradigms.containsKey(sortedFlexes)) {
//			paradigms.remove(sortedFlexes);
//		}
//	}
//	
//	public void removeParadigm(MorphParadigm mpar) {
//		Set<Flexion> flexes = mpar.getFlexes();
//		removeParadigm(flexes);
//	}
//
//	public static MorphParadigmsOld buildFromRRFF(WordSequences model, boolean print, String whichTime, double flexFreqTHH) {
//		Map<String,Set<String>> rrff = getAnalyzedRootFlex(model, print, whichTime);
//		System.out.println("rrff 1 built");
//		
//		MorphParadigmsOld mpars = new MorphParadigmsOld();
//		int i = 0;
//		for(String rr: rrff.keySet()) {
//			if(rrff.get(rr).size() < 2 || rr.split("#").length < 2) continue;
//			i++;
//			Root r1 = model.idx().getRoot(rr.split("#")[0]);
//			Root r2 = model.idx().getRoot(rr.split("#")[1]);
//			Set<Flexion> fset1 = new HashSet<>();
//			Set<Flexion> fset2 = new HashSet<>();
//			for(String ff: rrff.get(rr)) {
//				if(ff.endsWith("#")) ff = ff+"_";
//				Flexion f1 = model.idx().getFlex(ff.split("#")[0], r1 ); 
//				f1.addRoot(r1);
//				Word w1 = model.getWord(r1.toString()+f1.toString().replaceAll("_", ""));
//				r1.seenWords.add(w1);
//				Flexion f2 = model.idx().getFlex(ff.split("#")[1], r2 );
//				f2.addRoot(r2);
//				Word w2 = model.getWord(r2.toString()+f2.toString().replaceAll("_", ""));
//				r2.seenWords.add(w2);
//				//add only flexes with reasonable freq, avoid seldom flexes, seldom flexes is not sth we look for, because we need robust patterns here
//				if(f1.freq() > flexFreqTHH || f1.toString().matches("die|der|das|dem|den|des"))
//					fset1.add(f1);
//				if(f2.freq() > flexFreqTHH || f2.toString().matches("die|der|das|dem|den|des"))
//					fset2.add(f2);			
//			}
//			
////			if(fset1.size() < 2 || fset2.size() < 2) continue;
//			
//			MorphParadigm mparLeft  = mpars.getParadigm(fset1);
//			MorphParadigm mparRight = mpars.getParadigm(fset2);
//
//				mparLeft.addRoot(r1);
////				mparLeft.setRightParadigm(mparRight);
//				mparRight.addRoot(r2);
////				mparRight.setLeftParadigm(mparLeft);
//			//if(i%1000 == 0) { 				System.out.println("size of mpars: " + mpars.paradigms.size());			}
//			if(mpars.paradigms.size()> 5000) break;
//		}
//		
//		return mpars;
//	}
//	
//
//	/**
//	 * writes rrff and ffrr - matrices with neighbors roots and flexes as key-values pairs and vice versa
//	 * @param wsmodel
//	 * @param print
//	 * @param ffrr 
//	 * @param rrff 
//	 */
//	private static Map<String, Set<String>> getAnalyzedRootFlexTest(WordSequences wsmodel, boolean print, String whichTime) {
//		int i = 0;
//		int freq3 = 0;
//		Map<String,Set<String>> rrff = new HashMap<>();
//		Map<String,Set<String>> ffrr = new HashMap<>();
//		for(Word w: wsmodel.idx().getSortedWords()) {
//			if(w.toString().equals("Entwickler"))
//				System.out.println();
//			if(w.freq() < 3) continue;
//			freq3++;
//			if(w.getRoot() == null || w.getFlex() == null) continue;
//			i++;
//			if(i%100000 == 0) System.out.println("processed again: "+ i);
//			String thisroot = w.getRoot().toString();
//			if(wsmodel.idx().prefixFreq.containsKey(thisroot) && wsmodel.idx().prefixFreq.get(thisroot) > 2) continue; // means: do'nt take the possible prefix into paradimg
//			String thisflex = w.getFlex().toString();
//			Set<Word> rights = w.left_of.keySet();
////			for(Word wr: rights) {
////				if(wr.getRoot() == null || wr.getFlex() == null) continue;
////				if(!WordSequences.isCollocation(wsmodel, w, wr, 0, 0.001, false))					continue;
//				String wrRoot = thisroot;//wr.getRoot().toString();
//				if(wsmodel.idx().prefixFreq.containsKey(wrRoot) && wsmodel.idx().prefixFreq.get(wrRoot) > 2) continue; // means: do'nt take the possible prefix into paradimg
////				if(w.left_of.get(wr) < 2) continue; // exclude seldom bigrams: freq < 2, this is very restrictiv to the paradigms - factor 10 less!
//				
//				String wrFlex = thisflex; //wr.getFlex().toString();
//				String rr = thisroot+"#"+wrRoot;
//				String ff = thisflex+"#"+wrFlex;
//				
//				if(!rrff.containsKey(rr)) rrff.put(rr, new HashSet<>());
//				rrff.get(rr).add(ff);
//				
//				if(!ffrr.containsKey(ff)) ffrr.put(ff, new HashSet<>());
//				ffrr.get(ff).add(rr);
////			}
//			//experiment
//			Set<String> articles = new HashSet<>();
//			articles.add("der");articles.add("die");articles.add("das");articles.add("des");articles.add("dem");articles.add("den");
//			if(w.toString().equals("Entwickler"))
//				System.out.println();
//			for(Word wl: w.right_of.keySet()) {
//				if(articles.contains(wl.toString())) {
//					wl.setFlex(wl.toString());
//					wl.setRoot("");
//					rr = ""+"#"+thisroot;
//					ff = wl.toString()+"#"+thisflex;
//					if(!rrff.containsKey(rr)) rrff.put(rr, new HashSet<>());
//					rrff.get(rr).add(ff);
//					
//					if(!ffrr.containsKey(ff)) ffrr.put(ff, new HashSet<>());
//					ffrr.get(ff).add(rr);
//				}
//			}
//		}
//		
//
//		
//		//delete all paradigm candidats with freq == 1
//		for(String rr: rrff.keySet()) {
//			Iterator<String> it = rrff.get(rr).iterator();
//			 while (it.hasNext()) {
//				 String ff = it.next();
//				 if(ffrr.get(ff).size() == 1) {
//					 it.remove();
//					 continue;
//				 }
//
//			 }
//		}
//		
//		if(print) {
//			for(String rr: rrff.keySet()) {
//				if(rrff.get(rr).size() < 2) continue;
//				for(String ff: rrff.get(rr))
//					System.out.println(rr+"\t"+ff);
//				System.out.println();
//			}
//			System.out.println();			System.out.println();			System.out.println();
//			Map<String,Double> ff_freq = new HashMap<>();
//			for(String ff: ffrr.keySet()) {
//				if(ffrr.get(ff).size() < 10 ) continue;
//				ff_freq.put(ff, (double)ffrr.get(ff).size());
//			}
//			SortedMap<String,Double> sorted_distr = MapsOps.returnSortedMap(ff_freq);
//			for(String flexes: sorted_distr.keySet()) {
//				Map<String,Double> freq_distribution = new HashMap<>();
//				Set<String>roots = ffrr.get(flexes);
//				for(String root: roots) {
//					Set<String> flexesFromRoot = rrff.get(root);
//					MapsOps.addSubParadigToDistribution2(flexesFromRoot, freq_distribution);
//				}
//				MapsOps.printSortedMap(freq_distribution, null,5);
//				break;
//			}
//		} 
//		else {
//			try {
//			String fileString = "out/paradigm/" + wsmodel.getLang() + "-rrff-1-SuffixPreferred-"+whichTime + ".txt";
//			File newFile = new File(fileString);
//			Writer out;
//			if(!newFile.exists()) {
//				out = MyUtils.getWriter(fileString);
//			}else
//				out = MyUtils.getWriter(fileString + "2");
//			for(String rr: rrff.keySet()) {
//				if(rrff.get(rr).size() < 2) continue;
//				for(String ff: rrff.get(rr))
//					out.write(rr+"\t"+ff+"\n");
//				out.write("\n");
//			}
//			
//			out.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		System.out.println("words with freq > 2:\t" + freq3);
//		System.out.println("words with root/flex:\t" + i);
//		System.out.println("%:\t" + (double)((double)i/(double)freq3));
//		return rrff;
//	}
//	
//
//	/**
//	 * writes rrff and ffrr - matrices with neighbors roots and flexes as key-values pairs and vice versa
//	 * @param wsmodel
//	 * @param print
//	 * @param ffrr 
//	 * @param rrff 
//	 */
//	private static Map<String, Set<String>> getAnalyzedRootFlex(WordSequences wsmodel, boolean print, String whichTime) {
//		int i = 0;
//		int freq3 = 0;
//		Map<String,Set<String>> rrff = new HashMap<>();
//		Map<String,Set<String>> ffrr = new HashMap<>();
//		for(Word w: wsmodel.idx().getSortedWords()) {
//			if(w.toString().equals("Entwickler"))
//				System.out.println();
//			if(w.freq() < 3) continue;
//			freq3++;
//			if(w.getRoot() == null || w.getFlex() == null) continue;
//			i++;
//			if(i%100000 == 0) System.out.println("processed again: "+ i);
//			String thisroot = w.getRoot().toString();
//			if(wsmodel.idx().prefixFreq.containsKey(thisroot) && wsmodel.idx().prefixFreq.get(thisroot) > 2) continue; // means: do'nt take the possible prefix into paradimg
//			String thisflex = w.getFlex().toString();
//			Set<Word> rights = w.left_of.keySet();
//			for(Word wr: rights) {
//				if(wr.getRoot() == null || wr.getFlex() == null) continue;
////				if(!WordSequences.isCollocation(wsmodel, w, wr, 0, 0.001, false))					continue;
//				String wrRoot = wr.getRoot().toString();
//				if(wsmodel.idx().prefixFreq.containsKey(wrRoot) && wsmodel.idx().prefixFreq.get(wrRoot) > 2) continue; // means: do'nt take the possible prefix into paradimg
////				if(w.left_of.get(wr) < 2) continue; // exclude seldom bigrams: freq < 2, this is very restrictiv to the paradigms - factor 10 less!
//				
//				String wrFlex = wr.getFlex().toString();
//				String rr = thisroot+"#"+wrRoot;
//				String ff = thisflex+"#"+wrFlex;
//				
//				if(!rrff.containsKey(rr)) rrff.put(rr, new HashSet<>());
//				rrff.get(rr).add(ff);
//				
//				if(!ffrr.containsKey(ff)) ffrr.put(ff, new HashSet<>());
//				ffrr.get(ff).add(rr);
//			}
//			//experiment
//			Set<String> articles = new HashSet<>();
//			articles.add("der");articles.add("die");articles.add("das");articles.add("des");articles.add("dem");articles.add("den");
//			if(w.toString().equals("Entwickler"))
//				System.out.println();
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
//		}
//		
//
//		
//		//delete all paradigm candidats with freq == 1
//		for(String rr: rrff.keySet()) {
//			Iterator<String> it = rrff.get(rr).iterator();
//			 while (it.hasNext()) {
//				 String ff = it.next();
//				 if(ffrr.get(ff).size() == 1) {
//					 it.remove();
//					 continue;
//				 }
//
//			 }
//		}
//		
//		if(print) {
//			for(String rr: rrff.keySet()) {
//				if(rrff.get(rr).size() < 2) continue;
//				for(String ff: rrff.get(rr))
//					System.out.println(rr+"\t"+ff);
//				System.out.println();
//			}
//			System.out.println();			System.out.println();			System.out.println();
//			Map<String,Double> ff_freq = new HashMap<>();
//			for(String ff: ffrr.keySet()) {
//				if(ffrr.get(ff).size() < 10 ) continue;
//				ff_freq.put(ff, (double)ffrr.get(ff).size());
//			}
//			SortedMap<String,Double> sorted_distr = MapsOps.returnSortedMap(ff_freq);
//			for(String flexes: sorted_distr.keySet()) {
//				Map<String,Double> freq_distribution = new HashMap<>();
//				Set<String>roots = ffrr.get(flexes);
//				for(String root: roots) {
//					Set<String> flexesFromRoot = rrff.get(root);
//					MapsOps.addSubParadigToDistribution2(flexesFromRoot, freq_distribution);
//				}
//				MapsOps.printSortedMap(freq_distribution, null,5);
//				break;
//			}
//		} 
//		else {
//			try {
//			String fileString = "out/paradigm/" + wsmodel.getLang() + "-rrff-1-SuffixPreferred-"+whichTime + ".txt";
//			File newFile = new File(fileString);
//			Writer out;
//			if(!newFile.exists()) {
//				out = MyUtils.getWriter(fileString);
//			}else
//				out = MyUtils.getWriter(fileString + "2");
//			for(String rr: rrff.keySet()) {
//				if(rrff.get(rr).size() < 2) continue;
//				for(String ff: rrff.get(rr))
//					out.write(rr+"\t"+ff+"\n");
//				out.write("\n");
//			}
//			
//			out.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		System.out.println("words with freq > 2:\t" + freq3);
//		System.out.println("words with root/flex:\t" + i);
//		System.out.println("%:\t" + (double)((double)i/(double)freq3));
//		wsmodel.idx().rrff = rrff;
//		return rrff;
//	}
//
//	//compare freq of flex with and without suffix
//	private boolean checkSuffixes(String suff, MorphParadigm mpar, WordSequences model) {
//		Root someRoot = mpar.roots.iterator().next();
//		for(Flexion f: mpar.getFlexes()) {
//			String newFlexString = f.toString().substring(suff.length(), f.toString().length());
//			if(newFlexString.equals("")) newFlexString = "_";
//			Flexion newFlex = model.idx().getFlex(newFlexString, someRoot);
//			if(newFlex.freq() / 2 > f.freq())
//				continue;
//			else return false;
//		}
//		return true;
//	}
//
//	private boolean checkSuffixes2(String suff, MorphParadigm mpar, Map<String, MorphParadigm> paradigms) {
//		//get the flex paradigm with suff, get the flex paradigm without suf. check if paradigms contain new paradigm
//		//if so check if old paradigm and new paradigm share same left or right paradigm
//		//if so - return true
//		//eg: внутрішн іми (справами) внутрішн іх не має бути змінено через бібліотека ми
//		// квадрат ний квадрат ного має бути змінено
//		
//		String flexStringOld = mpar.getSortedFlex();
//		if(flexStringOld.contains("іми"))
//			System.out.println(flexStringOld);
//		String flexStringNew = flexStringOld.replaceFirst("^"+suff, "").replaceAll(", "+suff, ", ");
//		if(flexStringNew.contains(", , ")) flexStringNew = "_, " + flexStringNew.replace(", , ", ", ");
//		if(flexStringNew.startsWith(",")) flexStringNew = "_"+flexStringNew;
//		
////		if(paradigms.containsKey(flexStringNew)) {
////			MorphParadigm mparNew = paradigms.get(flexStringNew);
////			if(mpar.leftParadigms.contains(mparNew) )
////				return true;
////			if(mpar.rightParadigms.contains(mparNew))
////				return true;
////			Set<MorphParadigm> leftParadigmsNew = paradigms.get(flexStringNew).leftParadigms;
////			for(MorphParadigm mp: leftParadigmsNew) {
////				if(mpar.leftParadigms.contains(mp))
////					return true;
////			}
////			Set<MorphParadigm> rightParadigmsNew = paradigms.get(flexStringNew).rightParadigms;
////			for(MorphParadigm mp: rightParadigmsNew) {
////				if(mpar.rightParadigms.contains(mp))
////					return true;
////			}
////		}
//		return false;
//	}
//
//	public Map<String,Set<String>> findSuffixes(WordSequences model) {
//		Map<String,Double> seensuf = new HashMap<>();
//		Map<String,Set<String>> suffToFlex = new HashMap<>();
//
//		Map<String,Set<MorphParadigm>> seensuffPar = new HashMap<>();
//		for(MorphParadigm mpar: paradigms.values()) {
//			if(mpar.getFlexes().size() < 2) continue;
//			String suff = getSuffixes(mpar.getFlexes());
//			if(suff.length() > 0) {
//				if(checkSuffixes(suff, mpar, model) && checkSuffixes2(suff, mpar, paradigms) ) {
//					MapsOps.addFreq(suff, seensuf, 1.0);
//					if(!seensuffPar.containsKey(suff)) seensuffPar.put(suff, new HashSet<>());
//					seensuffPar.get(suff).add(mpar);
//					
//					//for cleaning of other flexes (in mixed paradigms)
//					if(!suffToFlex.containsKey(suff)) suffToFlex.put(suff, new HashSet<>());
//					for(Flexion f: mpar.getFlexes()) 
//						suffToFlex.get(suff).add(f.toString());
//				}
//
//			}
//		}
//		//cleanSuffixes(model, seensuf, seensuffPar);
//		System.out.println(seensuf.toString());
//		return suffToFlex;
//	}
//
////	private static boolean checkSuffixes3(String suff, MorphParadigm mpar, WordSequences model) {
////		if(suff.equals("им") ) {
////			System.out.println();
////			System.out.println(model.idx().seenRoots.contains("земельн"));
////			for(Flexion f: mpar.getFlexes()) {
////				System.out.println(f);
////			}
////		}
////		return true;
////	}
//
//	public void cleanSuffixes(WordSequences model, Map<String, Double> seensuf,
//			Map<String, Set<MorphParadigm>> seensuffPar) {
//		//clean paradigms, change Flexes and Roots
//		Set<Word> changedWords = new HashSet<>();
//
//		for(String suff: seensuffPar.keySet()) {
//			if(!seensuf.containsKey(suff) || seensuf.get(suff) < 3) 
//				continue; // dont do it for seldom suffixes
////			for(Flexion flex: seensufFlex.get(suff)) {
////				Set<MorphParadigm> thisPars = flex.getPars();
//				Set<MorphParadigm> suffParadigms = seensuffPar.get(suff);
//				for(MorphParadigm mpar: suffParadigms) {
//					Set<Root> roots = mpar.getRoots();
//					for(Root root: roots) {
////						Set<MorphParadigm> rootParadigms = root.getParadigms();
////						boolean foundBiggerParadigmNotInSuff = false;
////						for(MorphParadigm rootPar: rootParadigms) {
////							if(!suffParadigms.contains(rootPar)) {
////								foundBiggerParadigmNotInSuff = true;
////								break;
////							}
////						}
////						if(foundBiggerParadigmNotInSuff) continue;
//						root.seenflexes.removeAll(mpar.getFlexes());
//						root.seensuffs.add(suff);
//						Set<Word> words = root.seenWords;
//						for(Word w: words) {
//							//root.seenWords.remove(w); // because root will change сусід --> сусідн and the old root will  not have this word any more, but maybe some other words still
//							if(!changedWords.contains(w) && w.getFlex() != null && w.getFlex().toString().startsWith(suff) ) {
//								changeFlexRoot(w, suff, model);
//								changedWords.add(w);
//							}
//						}
//					}
//					cleanParadigm( mpar, model);
//
//				}
//
////			}
//		}
//	}
//
//
//
//	private void cleanParadigm(MorphParadigm mpar, WordSequences model) {
//		
//		//clean flexes from roots
//		for(Root r: mpar.roots) {
//			r.seenflexes.removeAll(mpar.getFlexes());
//			//clean par from roots
//			//r.getParadigms().remove(mpar);
//		}
//		
//		//clean roots from flexes
//		for(Flexion flex: mpar.getFlexes()) {
//			flex.roots.removeAll(mpar.getRoots());
//			//clean par from flexes
//			flex.getPars().remove(mpar);
//		}
//		
//		//delete paradigm
//		this.removeParadigm(mpar.getFlexes());
//		
//		
//	}
//
//	private void changeFlexRoot(Word w, String suff, WordSequences model) {
//		Root root = model.idx().getRoot(w.getRoot());
//		Root newRoot = model.idx().getRoot(root.toString()+suff);
//		w.setRoot(newRoot.toString());
//		Flexion flex = model.idx().getFlex(w.getFlex());
////		flex.seensuffs.add(suff);
//
//		Flexion newFlex = model.idx().getFlex(flex.toString().substring(suff.length()), newRoot);
//		w.setFlex(newFlex.toString());
////		newFlex.seensuffs.add(suff);
//		
//		//System.out.println("changed:\t" + suff + "\t" + newRoot + "\t" + newFlex.toString());
//	}
//
//	private String getSuffixes(Set<Flexion> flexes) {
//		StringBuffer sufBuffer = new StringBuffer();
//		int min = Integer.MAX_VALUE;
//		for(Flexion f: flexes) min = Math.min(min, f.toString().length());
//		boolean allHaveSameChar = true;
//		String flex1 = flexes.iterator().next().toString();
//
//		for (int i = 0; i < min; i++) {
//			char charInFirstFlex = flex1.charAt(i);
//			if(charInFirstFlex == '_') break;
//			for(Flexion flex: flexes) {
//				if(flex.toString().charAt(i) == charInFirstFlex) continue;
//				else {
//					allHaveSameChar = false;
//					break;
//				}
//			}
//			if(allHaveSameChar) sufBuffer.append(charInFirstFlex);
//					
//			else break;
//		}
//		return sufBuffer.toString();
//	}
//
//	public void printParadigms(Writer out) throws IOException {
//		System.out.println("PRINTING PARADIGMS:");
//		System.out.println(paradigms.size());
//		for(String flexesString: paradigms.keySet()) {
//			MorphParadigm mpar = paradigms.get(flexesString);
//			if(mpar.roots.size() < 2 || mpar.getFlexes().size() < 2) continue;
//			String parStringWithRoots = mpar.getStringWithRoots();
//			if(out != null)
//				out.write(parStringWithRoots + "n");
//			else
//				System.out.println(parStringWithRoots);
//		}
//		System.out.println(paradigms.size());
//	}
//
//	//combine flexes and roots from 2 paradigms, if they have same flexes and roots, min = 2
//	public void combineParadigms1(WordSequences model) {
//		List<MorphParadigm> mparlist = ListOps.of(paradigms.values());
//		Set<MorphParadigm> seen = new HashSet<>();
//		Set<MorphParadigm> saveNew = new HashSet<>();
//		for (int i = 0; i < mparlist.size(); i++) {
//			MorphParadigm mpar1 = mparlist.get(i);
//			if(mpar1 == null) continue;
//			if(seen.contains(mpar1)) continue;
//			for (int j = i+1; j < mparlist.size(); j++) {
//				MorphParadigm mpar2 = mparlist.get(j);
//				if(mpar2 == null) continue;
//				if(seen.contains(mpar2)) continue;
//				Set<Flexion> sameFlexes = getIntersect(mpar1.getFlexes(), mpar2.getFlexes());
//				if(sameFlexes.size() < 2) continue;
//				Set<Root> sameRoots = getIntersect(mpar1.roots, mpar2.roots); 
//				if(sameRoots.size() < 2) continue;
//				sameFlexes.addAll(mpar1.getFlexes());
//				sameFlexes.addAll(mpar2.getFlexes());
//				mpar1.roots.removeAll(sameRoots);
//				mpar2.roots.removeAll(sameRoots);
//				MorphParadigm newmpar = this.getParadigm(sameFlexes);
//				newmpar.roots.addAll(sameRoots);
//				newmpar.roots.addAll(mpar1.roots);
//				newmpar.roots.addAll(mpar2.roots);
////				this.removeParadigm(mpar1);
////				this.removeParadigm(mpar2);
////				paradigms.put( MorphParadigm.getSortedFlex(newmpar.getFlexes()), newmpar);
//				if(!saveNew.contains(newmpar))				System.out.println("new par: " + newmpar.getStringWithRoots());
//				seen.add(mpar1);
//				seen.add(mpar2);
//				saveNew.add(newmpar);
////				System.out.println("1 par: " + mpar1.getStringWithRoots());
////				System.out.println("2 par: " + mpar2.getStringWithRoots());
//			}
//		}
//		paradigms = new HashMap<>();
//		for(MorphParadigm newmpar: saveNew) {
//			paradigms.put( MorphParadigm.getSortedFlex(newmpar.getFlexes()), newmpar);
//		}
//		
//	}
//	
//	//combine flexes and roots from 2 paradigms, if they have same flexes and roots, min = 2
//	public void combineParadigms2(WordSequences model, double thh) {
//		List<MorphParadigm> mparlist = ListOps.of(paradigms.values());
//		Set<MorphParadigm> wasAdded = new HashSet<>();
//		for (int i = 0; i < mparlist.size(); i++) {
//			MorphParadigm mpar1 = mparlist.get(i);
//			
////			if(mpar1.getFlexes().size() == 1) continue;
//			
//			if(mpar1 == null) continue;
//			double maxFoundRoots = 0;
//			List<MorphParadigm> savedMostSimilarPar = new ArrayList<>();
//			for (int j = 0; j < mparlist.size(); j++) {
//				MorphParadigm mpar2 = mparlist.get(j);
//				if(mpar2 == null) continue;
//				if(mpar1.getSortedFlex().equals(mpar2.getSortedFlex())) continue;
//				MyPairs<Double,Double> overlap = mpar1.overlap(mpar2);
//				if(overlap.first >= thh) { // means all the flex from mpar1 are in mpar2
//					if(mpar1.getFlexes().size() == 1 || overlap.freq > 2) { //if 1 flex in testParadigm or if min 2 flexes were in the other
//					double overlapRootsMpar1 = mpar1.overlapRoot(mpar2).first;
//					if(overlapRootsMpar1 == maxFoundRoots) savedMostSimilarPar.add(mpar2); 
//						else if(overlapRootsMpar1 > maxFoundRoots) {
//							savedMostSimilarPar = new ArrayList<>();
//							savedMostSimilarPar.add(mpar2);
//							maxFoundRoots = overlapRootsMpar1;
//						}
//					}
//				}
//			}
//			System.out.println("for par:\t" + mpar1.getStringWithRoots());
//			if(savedMostSimilarPar.size() != 0) {
//				for(MorphParadigm mpIter: savedMostSimilarPar) {
//					MorphParadigm mp = paradigms.get(mpIter.getSortedFlex());
//					mp.addFlexes(mpar1.getFlexes());
//					mp.roots.addAll(mpar1.roots);
//					System.out.println("found most similar:\t" + mp.getStringWithRoots());
//					wasAdded.add(mpar1);
//				}
//				
//			}
//			else
//				System.out.println("found nothing");
//		}
//		Set<String> parStrings = new HashSet<>();
//		for(String s: paradigms.keySet()) {
//			parStrings.add(s);
//		}
//		for(String parString: parStrings) {
//			if(wasAdded.contains(paradigms.get(parString)) ) paradigms.remove(parString);
//		}
//		System.out.println("left paradigms: " + paradigms.size());
//		
//	}
//	
//	
//	public void combineParadigms3() {
//		List<String[]> flexParadigmTable = getParadigmRows();
//		
//		//print it:
//		for(String[] row: flexParadigmTable) {
//			for(String s: row) {
//				if(s != null)
//					System.out.print(s+"\t");
//				else
//					System.out.print(""+"\t");
//			}
//			System.out.println();
//		}
//		
//	}
//	
//	public void combineParadigms4() {
//		List<String[]> flexParadigmTable = getParadigmRows();
//		//print it:
//				for(String[] row: flexParadigmTable) {
//					for(String s: row) {
//						if(s != null)
//							System.out.print(s+"\t");
//						else
//							System.out.print(""+"\t");
//					}
//					System.out.println();
//				}
//		Map<String,Vector> parVectors = getParadigmVectors(flexParadigmTable);
//		
//		Map<String, Set<String>> clusterCenterToVectorsMap = collectClusters(parVectors);
//		
//		System.out.println(clusterCenterToVectorsMap.toString());
//	}
//	
//	public Map<String, Map<String, Double>> combineParadigms5(boolean printStarRain) {
//		List<String[]> flexParadigmTable = getParadigmRows();
//		//print it:
//		if(printStarRain) {
//				for(String[] row: flexParadigmTable) {
//					for(String s: row) {
//						if(s != null)
//							System.out.print(s+"\t");
//						else
//							System.out.print(""+"\t");
//					}
//					System.out.println();
//				}
//		}
//		Map<String,Vector> parVectors = getParadigmVectors(flexParadigmTable);
//		
//		Map<String, Set<String>> clusterCenterToVectorsMap = collectClusters2(parVectors, false, flexParadigmTable.get(0));
//		
//		Map<String,List<MyPair>> denseParadigmsWithFreq = printSortedAndTransponiert(clusterCenterToVectorsMap, parVectors);
//		
//		//System.out.println(clusterCenterToVectorsMap.toString());
//		
//		
//		// TOUR 2
//		
//		Map<String, Map<String,Double>> clusterCenterToFlexFreqsMap = collectClustersTour2(denseParadigmsWithFreq, true, printStarRain);
//		
////		for(String id: clusterCenterToFlexFreqsMap.keySet()) {
////			System.out.print(id + "\t" );
////			MapsOps.printSortedMap(clusterCenterToFlexFreqsMap.get(id), null, 100, 0, true , "\t");
////		}
//		
//		return clusterCenterToFlexFreqsMap;
//	}
//
//
//
//	private Map<String,List<MyPair>> printSortedAndTransponiert(Map<String, Set<String>> clusterCenterToVectorsMap, Map<String, Vector> parVectors) {
//		Map<String,List<MyPair>> transList = new HashMap<>();
//		int mostLongParadimg = 0;
//		for(String clusterID: clusterCenterToVectorsMap.keySet()) {
//			List<MyPair> list = new ArrayList<>();
//			for(String id: clusterCenterToVectorsMap.get(clusterID)) {
//				list.add(new MyPair(id, "", Cluster.computeVectorSimilarityOneOne(parVectors.get(clusterID), parVectors.get(id))) );
//			}
//			mostLongParadimg = list.size() > mostLongParadimg ? list.size() : mostLongParadimg;
//			
//			transList.put(clusterID, list);
//			
//		}
////		for (int i = 0; i < mostLongParadimg; i++) {
////			for(List<MyPair> p: transList.values()){
////				if(p.size() <= i || p.get(i) == null) {
////					System.out.print("\t\t");
////					continue;
////				}
////				System.out.print(p.get(i).first + "\t" + p.get(i).freq + "\t");
////			}
////			System.out.println();
////		}
//		return transList;
//	}
//
//	
//	private Map<String, Map<String,Double>> collectClustersTour2(Map<String,List<MyPair>> clusterCenterToVectorsMap, 
//			boolean useSeen, boolean print) {
//		//delete non frequent tail in paradigms
//		Map<String, Map<String,Double>> clusterCenterToFlexFreqMap = deleteNonFrequentTail(clusterCenterToVectorsMap, print);
//		System.out.println("CLUSTER NUMBER FIRST: " + clusterCenterToFlexFreqMap.size());
//		Map<String, Map<String,Double>> outputMap = getCombinedClusters(  clusterCenterToFlexFreqMap, print);
//
//		System.out.println("CLUSTER NUMBER SECOND: " + outputMap.size());
//		outputMap = getCombinedClusters(  outputMap, print);
//
//		System.out.println("CLUSTER NUMBER THIRD: " + outputMap.size());
//		outputMap = getCombinedClusters(  outputMap, print);
//		
//		System.out.println("CLUSTER NUMBER FOURTH: " + outputMap.size());
//		outputMap = getCombinedClusters(  outputMap, print);
//		
//		System.out.println("CLUSTER NUMBER FIFTH: " + outputMap.size());
//		outputMap = getCombinedClusters(  outputMap, print);
//		
//		for(String s: outputMap.keySet()) System.out.println(s + "\t" + outputMap.get(s));
//		System.out.println();
//			
//		return outputMap;
//	}
//
//	private Map<String, Map<String, Double>> getCombinedClusters( Map<String, Map<String, Double>> clusterCenterToFlexFreqMap, boolean print) {
//		Map<String, Map<String,Double>> outputMap = new HashMap<>();
//		Set<String> seen = new HashSet<>();
//		Set<String> wasMerged = new HashSet<>();
//
//		for(String id: clusterCenterToFlexFreqMap.keySet()) {
//			if(seen.contains(id)) continue;
//			Map<String,Double> map1 = clusterCenterToFlexFreqMap.get(id);
////			map1 = MapsOps.getFirstEntriesString(map1, 10, 0);
//			seen.add(id);
//			for(String id2: clusterCenterToFlexFreqMap.keySet()) {
//				if(id.equals(id2)) continue;
//				Map<String,Double> map2 =  clusterCenterToFlexFreqMap.get(id2);
////				map2 = MapsOps.getFirstEntriesString(map2, 10, 0);
//				MyPairs<Double, Double> sim;
//				if(map1.size() < map2.size()) sim = overlap(map1.keySet(), map2.keySet());
//				else 	sim = overlap(map2.keySet(), map1.keySet());
//
//				if(combineMorphClusters(sim, map1, map2)) {
////				if(sim.first > 0.6) {
////					if(sim.first - sim.second < 0.11 || sim.second > 0.7 || Math.abs(map1.size() - map2.size()) == 1.0) {// the difference no more than 20%
//						//if(print)System.out.println("COMBINING: " +id + "\tand\t" +id2 + "\t"+ map1.toString() + "\tand\t" + map2.toString());
//						//merge clusters
//						seen.add(id2);
//						wasMerged.add(id2);
//						wasMerged.add(id);
//						Map<String,Double> mergedMap;
//	
//						if(outputMap.containsKey(id)) mergedMap = outputMap.get(id);
//						else {
//							mergedMap = new HashMap<>();
//							for(String flex: map1.keySet())	MapsOps.addFreq(flex, mergedMap, map1.get(flex));
//							outputMap.put(id, mergedMap);
//						}
//						for(String flex: map2.keySet())	MapsOps.addFreq(flex, mergedMap, map2.get(flex));
//						seen.add(id2);
//						
////					}
//				}
//			}
//			if(!wasMerged.contains(id)) 
//				outputMap.put(id, map1);
//		}
//		//if(print)System.out.println();
//		return outputMap;
//	}
//	
//	
//	private Map<String, Map<String, Double>> getCombinedClustersGood( Map<String, Map<String, Double>> clusterCenterToFlexFreqMap) {
//		Map<String, Map<String,Double>> outputMap = new HashMap<>();
//		Set<String> seen = new HashSet<>();
//
//		for(String id: clusterCenterToFlexFreqMap.keySet()) {
//			if(seen.contains(id)) continue;
//			Map<String,Double> map1 = clusterCenterToFlexFreqMap.get(id);
//
//			seen.add(id);
//			for(String id2: clusterCenterToFlexFreqMap.keySet()) {
//				if(id.equals(id2)) continue;
//				Map<String,Double> map2 =  clusterCenterToFlexFreqMap.get(id2);
//
//				MyPairs<Double, Double> sim;
//				if(map1.size() < map2.size()) sim = overlap(map1.keySet(), map2.keySet());
//				else 	sim = overlap(map2.keySet(), map1.keySet());
//
//				if(combineMorphClusters(sim, map1, map2)) {
//							//System.out.println("COMBINING: " +id + "\tand\t" +id2 + "\t"+ map1.toString() + "\tand\t" + map2.toString());
//						//merge clusters
//						seen.add(id2);
//						Map<String,Double> mergedMap;
//						if(outputMap.containsKey(id)) mergedMap = outputMap.get(id);
//						else {
//							mergedMap = new HashMap<>();
//							for(String flex: map1.keySet())	MapsOps.addFreq(flex, mergedMap, map1.get(flex));
//							outputMap.put(id, mergedMap);
//						}
//						for(String flex: map2.keySet())	MapsOps.addFreq(flex, mergedMap, map2.get(flex));
//						seen.add(id2);
//						
////					}
//				}
//			}
//		}
//		//System.out.println();
//		return outputMap;
//	}
//
//
//
//
//	/**
//	 * Collects vectors of flexes which were seen together in the paradigms
//	 * @param parVectors
//	 * @param useSeen
//	 * @param flexSortedByFreq
//	 * @return
//	 */
//	private Map<String, Set<String>> collectClusters2(Map<String, Vector> parVectors, boolean useSeen, String[] flexSortedByFreq) {
//		Map<String,Set<String>> clusterCenterToVectorNamesMap = new HashMap<>();
//		Set<String> seen  = new HashSet<>();
//
//		List<String> flexSortedByFreqReversed = new ArrayList<>();
//		for (int i = flexSortedByFreq.length-1; i >= 0 ; i--) {
//			flexSortedByFreqReversed.add(flexSortedByFreq[i]);
//		}
//			//start with the least frequent flexes
//		for(String vectorId: flexSortedByFreqReversed) {
//			if(useSeen && seen.contains(vectorId)) continue;
//			Vector v = parVectors.get(vectorId);
//			List<Double> fullList = new ArrayList<>();
//			for (int i = 0; i < v.size(); i++) {
//				fullList.add(1.0);
//			}
//			Double minSim = 1.0;
//			
//
//			for(String vectorId2: flexSortedByFreq) { //parVectors.keySet()
//				if(vectorId.equals(vectorId2)) continue;
//				Vector v2 = parVectors.get(vectorId2);
//				double sim = Cluster.computeVectorSimilarityOneOne(v, v2);
//				if(sim > minSim) {
//					if(!clusterCenterToVectorNamesMap.containsKey(vectorId)){
//						clusterCenterToVectorNamesMap.put(vectorId, new HashSet<>());
//						clusterCenterToVectorNamesMap.get(vectorId).add(vectorId);
//						seen.add(vectorId);
//					}
//					clusterCenterToVectorNamesMap.get(vectorId).add(vectorId2);
//					seen.add(vectorId2);
//				}
//
//			}
//			
//		}
//		
//		return clusterCenterToVectorNamesMap;
//	}
//
//	
//	private Map<String, Set<String>> collectClusters(Map<String, Vector> parVectors) {
//		
//		Map<String,Set<String>> clusterCenterToVectorNamesMap = new HashMap<>();
//		Set<String> seen  = new HashSet<>();
//		for(String vectorIdx: parVectors.keySet()) {
//			if(seen.contains(vectorIdx)) continue;
//			Vector v = parVectors.get(vectorIdx);
//			Double maxSim = 0.0;
//			String saveClosestVectorID = null;
//			for(String vectorIdx2: parVectors.keySet()) {
//				if(vectorIdx.equals(vectorIdx2)) continue;
//				Vector v2 = parVectors.get(vectorIdx2);
//				double sim = Cluster.computeVectorSimilarityOneOne(v, v2);
//				if(sim > maxSim) {
//					maxSim = sim; //reset the most closest Vector
//					saveClosestVectorID = vectorIdx2;
//				}
//			}
//			clusterCenterToVectorNamesMap.put(vectorIdx, new HashSet<>());
//			clusterCenterToVectorNamesMap.get(vectorIdx).add(vectorIdx);
//			clusterCenterToVectorNamesMap.get(vectorIdx).add(saveClosestVectorID);
//			seen.add(vectorIdx);
//			seen.add(saveClosestVectorID);
//			collectClusterRecursively(vectorIdx, clusterCenterToVectorNamesMap, seen, parVectors, null, null);
//		}
//		return clusterCenterToVectorNamesMap;
//	}
//
//	private void collectClusterRecursively(String clusterCenter, Map<String, Set<String>> clusterCenterToVectorNamesMap,
//			Set<String> seen, Map<String, Vector> parVectors, Vector mergedClusterVector, Set<String> checkedVectors) {
//		Set<String> vectorNamessInCluster = clusterCenterToVectorNamesMap.get(clusterCenter);
//		Set<Vector> vectorsInCluster = getVectorSet(vectorNamessInCluster, parVectors);
//		if(mergedClusterVector == null) mergedClusterVector = Cluster.computeMergedVector(vectorsInCluster);
//		//go through all vectors unless in cluster already and find new members: 
//		// best similarity with merged vector, no Null sim with any of cluster vectors!
//		double maxSim = 0.0;
//		Vector savedVector = null;
//		String savedName = null;
//		if( checkedVectors  == null) checkedVectors = new HashSet<>();
//		
//		for(String vectorName: parVectors.keySet()) {
//			//continue if vectorName is already in cluster or was tested for this cluster
//			if(vectorNamessInCluster.contains(vectorName) || checkedVectors.contains(vectorName)) continue;
//			Vector testVector = parVectors.get(vectorName);
//			double newSim = Cluster.computeVectorSimilarityOneOne(mergedClusterVector, testVector);
//			if(newSim > maxSim) {
//				savedVector = testVector; 
//				maxSim = newSim; 
//				savedName = vectorName;
//			}
//		}
//		//case best vector found
//		if(savedVector != null) {
//			checkedVectors.add(savedName);
//			if(hasNoNull(savedVector, vectorsInCluster)) {
//				vectorsInCluster.add(savedVector);
//				seen.add(savedName);
//				clusterCenterToVectorNamesMap.get(clusterCenter).add(savedName);
//				mergedClusterVector = Cluster.computeMergedVector(SetOps.of(mergedClusterVector, savedVector));
//				collectClusterRecursively(clusterCenter, clusterCenterToVectorNamesMap, seen, parVectors, mergedClusterVector, checkedVectors);
//			}else {
//				collectClusterRecursively(clusterCenter, clusterCenterToVectorNamesMap, seen, parVectors, mergedClusterVector, checkedVectors);
//			}
//		}
//		//case not found - break recursion!
//		else {
//			return;
//		}
//	}
//
//	private boolean hasNoNull(Vector savedVector, Set<Vector> vectorsInCluster) {
//		double minsim = Cluster.computMinSimOneOne(vectorsInCluster);
//		for(Vector v: vectorsInCluster)
//			if(Cluster.computeVectorSimilarityOneOne(savedVector, v) < minsim/2) return false;
//		return true;
//	}
//
//	private Set<Vector> getVectorSet(Set<String> vectorNameset, Map<String, Vector> parVectors) {
//		Set<Vector> vset = new HashSet<>();
//		for(String vectorname: vectorNameset) {
//			vset.add(parVectors.get(vectorname));
//		}
//		return vset;
//	}
//
//
//
//	private Map<String, Vector> getParadigmVectors(List<String[]> flexParadigmTable) {
//		Map<String,Vector> vectorMap = new HashMap<>();
//
//		if(flexParadigmTable.size() < 2) return vectorMap;
//		String[] firstRow = flexParadigmTable.get(0);
//		for (int i = 0; i < firstRow.length; i++) {
//			
//			List<Double> vectorlist = new ArrayList<>();
//
//			for (int rowidx = 1; rowidx < flexParadigmTable.size(); rowidx++) {
//				String[] row = flexParadigmTable.get(rowidx);
//				Double value = row[i] == null ? 0.0 : 1.0;
//				vectorlist.add(value);
//			}
//			Vector v = new Vector(vectorlist);
//			vectorMap.put(firstRow[i], v);
//		}
//		return vectorMap;
//	}
//
//	private static <T> Set<T> getIntersect(Set<T> elem1, Set<T> elem2) {		
//		Set<T> newset = new HashSet<>();
//		for(T e1: elem1) if(elem2.contains(e1)) newset.add(e1);
//		for(T e2: elem2) if(elem1.contains(e2)) newset.add(e2);
//		return newset;
//	}
//	
//	public static <T> MyPairs<Double, Double> overlap(Set<T> oneSet, Set<T> secondSet) {
//			double sumEqualFlexes = 0.0;
//			for(T e1: oneSet) {
//				if(secondSet.contains(e1)) sumEqualFlexes ++;
//			}
//			return new MyPairs<Double, Double>(sumEqualFlexes/oneSet.size(), sumEqualFlexes/secondSet.size(), sumEqualFlexes);
//	}
//
//	public static Set<String> findAmbigFlexes(WordSequences wsmodel) {
//		Set<String> ambigFlexes = new HashSet<>();
//		
//		Map<String, Set<String>> rrff = wsmodel.idx().rrff;
//		for(String rr: rrff.keySet()) {
//			boolean allEqual = true;
//			Set<String> someEqualFlexes = new HashSet<>();
//			for(String ff: rrff.get(rr)) {
//				if(ff.split("#").length < 2) continue;
//				String f1 = ff.split("#")[0]; 
//				String f2 = ff.split("#")[1];
//				if(f1.equals(f2)) someEqualFlexes.add(f1);
//				else allEqual = false;
//			}
//			if(!allEqual) ambigFlexes.addAll(someEqualFlexes);
//		}
//		return ambigFlexes;
// 	}
//
//	public static void computeFlexionFreqs(WordSequences wsmodel){
//		//statistics for flexes
//		wsmodel.idx().seenFlexes = new HashMap<>();
//		for(Word w: wsmodel.idx().getSortedWords()) {
//			if(w.getFlex() == null) continue;
//			String f = w.getFlex();
//			MapsOps.addFreq(f, wsmodel.idx().seenFlexes);
//		}
//	}
//	
//	public static int computeFlexFreqAndGetTHH(WordSequences wsmodel, int percent) {
//		computeFlexionFreqs(wsmodel);
//		Set<String> butnot = new HashSet<>();
//		butnot.add("_");
//		MyPair mostFrequentFlexNotZero = MapsOps.getFirstButNOT(wsmodel.idx().seenFlexes, butnot);
//		return (int)(mostFrequentFlexNotZero.freq / percent); 
//	}
//	
//	// more new than other functions here
//	
//	
//	public Map<String, Map<String, Double>> computeGeneralParadigmsOld(boolean printStarRain) {
//
//		Map<String,Vector> parVectors = getFlexToParadigmVectors(printStarRain);
//		
//		Map<String, Set<String>> clusterCenterToVectorsMap = collectFlexClustersSeenTogetherInWordParadigm(parVectors);
//		
//		Map<String,List<MyPair>> denseParadigmsWithFreq = computeFlexSimAsHowOftenSeenWithOtherFlexInWordParadigm(clusterCenterToVectorsMap, parVectors);
//		
//		Map<String, Map<String,Double>> clusterCenterToFlexFreqsMap = combineClustersOld(denseParadigmsWithFreq, true, printStarRain);
//		
//		return clusterCenterToFlexFreqsMap;
//	}
//	
//	
//	
//	private List<String[]> getParadigmRows() {
//		double thh = 10;
//		Map<String,Double> flexFreqInParadigms = new HashMap<>();
//		for(String s: paradigms.keySet()) {
//			MorphParadigm mp = paradigms.get(s);
//			for(Flexion f: mp.getFlexes()) {
//				MapsOps.addFreq(f.toString(), flexFreqInParadigms);
//			}
//		}
//		String mostFreqFlex = MapsOps.getSortedMapString(flexFreqInParadigms).firstKey();
//		double biggestFlexFreq = flexFreqInParadigms.get(mostFreqFlex) ;
//		thh = 1.0 * biggestFlexFreq / 200.0;
//		
////		MapsOps.printSortedMap(flexFreqInParadigms, null, 100, true);
//		Map<String,Integer> flexIndexMap = new HashMap<>();
//		int i = 0;
//		for(String s: MapsOps.getSortedMapString(flexFreqInParadigms).keySet()) {
//			if(flexFreqInParadigms.get(s) < thh) continue;
//			flexIndexMap.put(s, i);
//			i++;
//			
//		}
//		List<String[]> flexParadigmTable = new ArrayList<>();
//		String[] flexNames = new String[flexIndexMap.size()];
//		for(String f: flexIndexMap.keySet()) {
//			flexNames[flexIndexMap.get(f)] = f;
//		}
//		flexParadigmTable.add(flexNames);
//		this.flexesSortedbyFreqInParadigms  = flexNames;
//		
//		for(String s: paradigms.keySet()) {
//			MorphParadigm mp = paradigms.get(s);
//			String[] row =  new String[flexIndexMap.size()];
//			boolean thereWasNonFrequentFLex = false;
//			if(mp.getFlexes().size() < 2) continue;
//			for(Flexion f: mp.getFlexes()) {
//				if(!flexFreqInParadigms.containsKey(f.toString()) || flexFreqInParadigms.get(f.toString()) < thh){
//					thereWasNonFrequentFLex = true;
//					break;
//				}
//				row[flexIndexMap.get(f.toString())] = f.toString();
//			}
//			if(!thereWasNonFrequentFLex)
//				flexParadigmTable.add(row);
//	
//		}
//		return flexParadigmTable;
//	}
//	
//	private Map<String, Vector> getFlexToParadigmVectors(boolean printStarRain) {
//		
//		//this part is only for visualisation for research if necessary
//		List<String[]> flexParadigmTable = getParadigmRows();
//		//print it:
//		if(printStarRain) {
//				for(String[] row: flexParadigmTable) {
//					for(String s: row) {
//						if(s != null)
//							System.out.print(s+"\t");
//						else
//							System.out.print(""+"\t");
//					}
//					System.out.println();
//				}
//		}
//		// end visualization part
//		
//		//now collect Vectors: binary Vector is combination of flexes seen with a flex together in one paradigm = 1, other non seen flexes are marked with 0
//		Map<String,Vector> vectorMap = new HashMap<>();
//
//		if(flexParadigmTable.size() < 2) return vectorMap;
//		String[] firstRow = flexParadigmTable.get(0);
//		for (int i = 0; i < firstRow.length; i++) {
//			
//			List<Double> vectorlist = new ArrayList<>();
//
//			for (int rowidx = 1; rowidx < flexParadigmTable.size(); rowidx++) {
//				String[] row = flexParadigmTable.get(rowidx);
//				Double value = row[i] == null ? 0.0 : 1.0;
//				vectorlist.add(value);
//			}
//			Vector v = new Vector(vectorlist);
//			vectorMap.put(firstRow[i], v);
//		}
//		return vectorMap;
//	}
//	
//	private Map<String,List<MyPair>> computeFlexSimAsHowOftenSeenWithOtherFlexInWordParadigm(Map<String, Set<String>> clusterCenterToVectorsMap, Map<String, Vector> parVectors) {
//		Map<String,List<MyPair>> transList = new HashMap<>();
//		int mostLongParadimg = 0;
//		for(String clusterID: clusterCenterToVectorsMap.keySet()) {
//			List<MyPair> list = new ArrayList<>();
//			for(String id: clusterCenterToVectorsMap.get(clusterID)) {
//				list.add(new MyPair(id, "", Cluster.computeVectorSimilarityOneOne(parVectors.get(clusterID), parVectors.get(id))) );
//			}
//			mostLongParadimg = list.size() > mostLongParadimg ? list.size() : mostLongParadimg;
//			
//			transList.put(clusterID, list);
//		}
//		return transList;
//	}
//	
//	/**
//	 * Collects vectors of flexes which were seen together in the paradigms
//	 */
//	private Map<String, Set<String>> collectFlexClustersSeenTogetherInWordParadigm(Map<String, Vector> parVectors) {
//		Map<String,Set<String>> clusterCenterToVectorNamesMap = new HashMap<>();
//		if(this.flexesSortedbyFreqInParadigms == null) return clusterCenterToVectorNamesMap;
//
//		List<String> flexSortedByFreqReversed = new ArrayList<>();
//		for (int i = this.flexesSortedbyFreqInParadigms.length-1; i >= 0 ; i--) {
//			flexSortedByFreqReversed.add( this.flexesSortedbyFreqInParadigms[i]);
//		}
//			//start with the least frequent flexes
//		for(String vectorId: flexSortedByFreqReversed) {
//			Vector v = parVectors.get(vectorId);
//			
//			double minSim = 1.0;
//			for(String vectorId2:  this.flexesSortedbyFreqInParadigms) { //parVectors.keySet()
//				if(vectorId.equals(vectorId2)) continue;
//				Vector v2 = parVectors.get(vectorId2);
//				double sim = Cluster.computeVectorSimilarityOneOne(v, v2);
//				if(sim > minSim) {
//					if(!clusterCenterToVectorNamesMap.containsKey(vectorId))
//						clusterCenterToVectorNamesMap.put(vectorId, new HashSet<>());
//					clusterCenterToVectorNamesMap.get(vectorId).add(vectorId);
//					clusterCenterToVectorNamesMap.get(vectorId).add(vectorId2);
//				}
//			}
//		}
//		return clusterCenterToVectorNamesMap;
//	}
//	
//	public Map<String, Map<String,Double>> combineClustersOld(Map<String,List<MyPair>> clusterCenterToVectorsMap, 
//			boolean useSeen, boolean print) {
//		//delete non frequent tail in paradigms
//		Map<String, Map<String,Double>> clusterCenterToFlexFreqMap = deleteNonFrequentTail(clusterCenterToVectorsMap, print);
//		System.out.println("CLUSTER NUMBER FIRST: " + clusterCenterToFlexFreqMap.size());
//		Map<String, Map<String,Double>> outputMap = getCombinedClustersOld(  clusterCenterToFlexFreqMap, print);
//
//		System.out.println("CLUSTER NUMBER SECOND: " + outputMap.size());
//		outputMap = getCombinedClustersOld(  outputMap, print);
//
//		System.out.println("CLUSTER NUMBER THIRD: " + outputMap.size());
//		outputMap = getCombinedClustersOld(  outputMap, print);
//		
//		System.out.println("CLUSTER NUMBER FOURTH: " + outputMap.size());
//		outputMap = getCombinedClustersOld(  outputMap, print);
//		
//		System.out.println("CLUSTER NUMBER FIFTH: " + outputMap.size());
//		outputMap = getCombinedClustersOld(  outputMap, print);
//		
//		for(String s: outputMap.keySet()) System.out.println(s + "\t" + outputMap.get(s));
//		System.out.println();
//			
//		return outputMap;
//	}
//	
//	private Map<String, Map<String,Double>> deleteNonFrequentTail(Map<String, List<MyPair>> clusterCenterToVectorsMap, boolean print) {
//		Map<String, Map<String,Double>> clusterCenterToFlexFreqMap = new HashMap<>();
//		for(String id: clusterCenterToVectorsMap.keySet()) {
//			Map<String,Double> map1 = new HashMap<>();
//			for(MyPair p: clusterCenterToVectorsMap.get(id)) {
//				map1.put(p.first, p.freq);
//			}
//			if(print)System.err.println("map1 before: " + map1.keySet());
//			double biggestFreqInMap1 = MapsOps.getFirst(map1).freq;
//			Set<String> keyset = new HashSet<>();
//			for(String k: map1.keySet()) keyset.add(k);
//			for(String key: keyset) {
//				double freq = map1.get(key);
//				if(freq < biggestFreqInMap1 / 10) map1.remove(key); //delete the non frequent flexes from paradigm
//			}
//			clusterCenterToFlexFreqMap.put(id, map1);
//			if(print)System.err.println("map1 after: " + map1.keySet());
//
//		}
//		return clusterCenterToFlexFreqMap;
//	}
//	
//	public static Map<String, Map<String, Double>> getCombinedClustersOld( Map<String, Map<String, Double>> clusterCenterToFlexFreqMap, boolean print) {
//			Map<String, Map<String,Double>> outputMap = new HashMap<>();
//			Set<String> seen = new HashSet<>();
//			Set<String> wasMerged = new HashSet<>();
//	
//			for(String id: clusterCenterToFlexFreqMap.keySet()) {
//				if(seen.contains(id)) continue;
//				Map<String,Double> map1 = clusterCenterToFlexFreqMap.get(id);
//	//			map1 = MapsOps.getFirstEntriesString(map1, 10, 0);
//				seen.add(id);
//				for(String id2: clusterCenterToFlexFreqMap.keySet()) {
//					if(id.equals(id2)) continue;
//					Map<String,Double> map2 =  clusterCenterToFlexFreqMap.get(id2);
//	//				map2 = MapsOps.getFirstEntriesString(map2, 10, 0);
//					MyPairs<Double, Double> sim;
//					Map<String,Double> map1cut = mapCutNonFrequentTail(map1);
//					Map<String,Double> map2cut = mapCutNonFrequentTail(map2);
//	
//					//do comparisson only on most frequent flexes from the paradigm, if the paradigm is long >= 8 flexes
//					if(map1cut.size() < map2cut.size()) sim = overlap(map1cut.keySet(), map2cut.keySet());
//					else 	sim = overlap(map2cut.keySet(), map1cut.keySet());
//	
//					if(combineMorphClusters(sim, map1, map2)) {
//	//				if(sim.first > 0.6) {
//	//					if(sim.first - sim.second < 0.11 || sim.second > 0.7 || Math.abs(map1.size() - map2.size()) == 1.0) {// the difference no more than 20%
//							//if(print)System.out.println("COMBINING: " +id + "\tand\t" +id2 + "\t"+ map1.toString() + "\tand\t" + map2.toString());
//							//merge clusters
//							seen.add(id2);
//							wasMerged.add(id2);
//							wasMerged.add(id);
//							if(print)
//								System.out.println("COMBINED PARS\t" + MapsOps.getSortedByKey(map1) + "\tAND\t" + MapsOps.getSortedByKey(map2));
//							Map<String,Double> mergedMap;
//		
//							if(outputMap.containsKey(id)) mergedMap = outputMap.get(id);
//							else {
//								mergedMap = new HashMap<>();
//								for(String flex: map1.keySet())	MapsOps.addFreq(flex, mergedMap, map1.get(flex));
//								outputMap.put(id, mergedMap);
//							}
//							for(String flex: map2.keySet())	MapsOps.addFreq(flex, mergedMap, map2.get(flex));
//							seen.add(id2);
//							
//	//					}
//					}
//				}
//				if(!wasMerged.contains(id)) 
//					outputMap.put(id, map1);
//			}
//			//if(print)System.out.println();
//			return outputMap;
//		}
//
//
//	
//	private static Map<String, Double> mapCutNonFrequentTail(Map<String, Double> map) {
//		if(map.size() < 8) return map;
//		SortedMap<String,Double> mapSortedByFreqs = MapsOps.getSortedMapString(map);
//		int size = mapSortedByFreqs.size();
//		Map<String,Double> bestFlexMap = new HashMap<>();
//		int i = 0;
//		double mostFreqFlex = MapsOps.getFirst(map).freq;
//		int stop = size / 2; // the half
//		stop = (int) (mostFreqFlex / 100);
//		for (Iterator<String> iter = mapSortedByFreqs.keySet().iterator(); iter.hasNext();) {
//			if (i > stop)
//				break;
//			i++;
//			String key = (String) iter.next();
//			bestFlexMap.put(key, mapSortedByFreqs.get(key));
//		}
//		return bestFlexMap;
//	}
//	
//	private static boolean combineMorphClusters(MyPairs<Double, Double> sim, Map<String, Double> map1,
//			Map<String, Double> map2) {
//		if(sim.first < 0.4 && sim.second < 0.4) return false;
//		if(map1.size() <= 2 || map2.size() <= 2) {
//			if(sim.first > 0.8 && sim.second > 0.8) return true;
//		}
//		else if(map1.size() <= 4 || map2.size() <= 4) {
//			if(sim.first > 0.7 && sim.second > 0.7) return true;
//		}
//		else if(map1.size() <= 6 || map2.size() <= 6) {
//			if(sim.first > 0.6 && sim.second > 0.6) return true;
//		}
//		else if(map1.size() <= 8 || map2.size() <= 8) {
//			if(sim.first > 0.5 && sim.second > 0.5) return true;
//		}
//		else if(map1.size() > 8 && map2.size() > 8) {
//			if(sim.first > 0.5 && sim.second > 0.5) return true;
//		}
//		return false;
//	}
//	
//	private boolean combineMorphClustersOld(MyPairs<Double, Double> sim, Map<String, Double> map1,
//			Map<String, Double> map2) {
//		if(map1.size() < 5) {
//			if(sim.first > 0.7 && sim.second > 0.7) return true;
//		}
//		else 
//		if(sim.first > 0.6) {
//			if(sim.first - sim.second < 0.11 || sim.second > 0.7 || Math.abs(map1.size() - map2.size()) == 1.0) {// the difference no more than 20%
//				return true;
//			}
//		}
//		return false;
//	}
//
//}
