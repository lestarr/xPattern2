package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelparts.Word;
import modeltrain.MorphAnalyzer;
import util.MapsOps;
import util.MyPair;
import util.MyUtils;

public class WordsOld {
	
	
	public static double getSignifOfLeft(Word left, Word right) {
		boolean bigramExist = left.left_of.containsKey(right);
		if(!bigramExist)			return 0.0;
		
		double bigramFreq = left.left_of.get(right);
		return (bigramFreq/right.freq());
	}
	
	public static double getSignifOfRight(Word left, Word right) {
		boolean bigramExist = left.left_of.containsKey(right);
		if(!bigramExist)			return 0.0;
		
		double bigramFreq = left.left_of.get(right);
		return (bigramFreq/left.freq());
	}
	
	public static List<List<MyPair>> computeContextsWithSignif(Word w, boolean wordIsLeftOf, int howmany, boolean print) {
		HashMap<Word, Double> contexts;
		if (howmany == -1)
			howmany = Integer.MAX_VALUE;
		double wfreq = w.freq();
		if (wordIsLeftOf)
			contexts = w.left_of;
		else
			contexts = w.right_of;
		int i = 0;
		List<List<MyPair>> groups = getGroups();
		List<List<MyPair>> groupsForContext = getGroups();
		for (Word cont : contexts.keySet()) {
			i++;
			if (i > howmany)
				break;
			double freq = contexts.get(cont);
			double signifCOnt = MyUtils.rdouble(freq / wfreq);
			double signifTestWordDouble = MyUtils.rdouble(freq / cont.freq());
			addSignifToGroup(cont.toString(), signifTestWordDouble, groups);
			addSignifToGroup(cont.toString(), signifCOnt, groupsForContext);
		}
		if (print) {
			printGroups(groups);
			System.out.println("signig of CONTEXT");
			printGroups(groupsForContext);
		}
		groups.addAll(groupsForContext);
		return groups;
	}
	


	public static List<List<MyPair>> getGroups() {
		List<List<MyPair>> groups = new ArrayList<>(5);
		groups.add(0, new ArrayList<MyPair>());
		groups.add(1, new ArrayList<MyPair>());
		groups.add(2, new ArrayList<MyPair>());
		groups.add(3, new ArrayList<MyPair>());
		groups.add(4, new ArrayList<MyPair>());
		return groups;
	}

	public static void printGroups(List<List<MyPair>> groups) {
		System.out.println();
		System.out.println(groups.get(0).size() + " " + printGroup(groups.get(0)));
		System.out.println(groups.get(1).size() + " " + printGroup(groups.get(1)));
		System.out.println(groups.get(2).size() + " " + printGroup(groups.get(2)));
		System.out.println(groups.get(3).size() + " " + printGroup(groups.get(3)));
		System.out.println(groups.get(4).size() + " " + printGroup(groups.get(4)));
	}
	
	private static String printGroup(List<MyPair> list) {
		Collections.sort(list, Collections.reverseOrder());
		return list.toString();
	}

	public static void addSignifToGroup(String word, double signif, List<List<MyPair>> groups) {
		if (signif > 1.0) {
			groups.get(4).add(new MyPair(word, "", signif));
		} else if (signif > 0.9)
			groups.get(0).add(new MyPair(word, "", signif));
		else if (signif > 0.1)
			groups.get(1).add(new MyPair(word, "", signif));
		else if (signif > 0.01)
			groups.get(2).add(new MyPair(word, "", signif));
		else if (signif > 0.001)
			groups.get(3).add(new MyPair(word, "", signif));
		else
			groups.get(4).add(new MyPair(word, "", signif));
	}
	
	public static boolean isStopword(String wstring) {
		if(wstring.toLowerCase().matches("aaa|eee|zzz|doctitle")  || wstring.contains("doctitle")) return true;
		return false;
	}
	
	public static boolean isLeftWord(Word w) {
		double coefL= w.getCoef(true);
		double coefR = w.getCoef(false);
		if(coefL > coefR) return true;
		return false;
	}
	

	public static boolean isCollocation(WordSequences wsmodel, Word l, Word r, double minFreq, double thh, boolean print) {
		
		if(!l.left_of.containsKey(r)) return false;
		if(l.left_of.get(r) < minFreq) return false;
		
		double l_thh = l.left_of.get(r)/r.freq();
		double r_thh = l.left_of.get(r)/l.freq();
		
		if(print) {
			System.out.println(MyUtils.rdouble(l_thh)+ "; " + MyUtils.rdouble(r_thh) + "; frec: " + MyUtils.rdouble(l.left_of.get(r)) );
		}
		if(l_thh > thh && r_thh > thh) return true;
		return false;		
	}
	
	public static void computeRootFlexInitial(WordSequences wsmodel, LetterTokModel ltmodel, int freqTHH) {
		for(Word inputword: wsmodel.idx().getSortedWords()) {
			if(inputword.freq() < freqTHH) continue;
			List<MyPair> slicesPref = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel,
					inputword.toString(), "pref");
//			boolean isCmp = isCompound(slicesPref);

			List<MyPair> prefCuts = getRootFlexFromPREFIXSlice(inputword, slicesPref, wsmodel, false);
			List<MyPair> slicesSuf = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel,
					inputword.toString(), "suf");
			MyPair rootFlexPref = prefCuts.get(prefCuts.size() - 1);
			// boolean isCmpSuff = isCompoundSuf(slicesSuf);
			MyPair rootFlexSuf = getRootFlexFromSUFFIXSlice(inputword, slicesSuf, wsmodel, false, false);
			String suffFlex = rootFlexSuf.second.replaceAll("_", "");
			String suffRoot = rootFlexSuf.first.replaceAll("_", ""); // is not used here, redundand because of prefFlex == sufFlex, so the roots are also equal
			String prefFlex = rootFlexPref.second.replaceAll("_", "");
			String prefRoot = rootFlexPref.first.replaceAll("_", "");
			MapsOps.addStringToValueSet(prefRoot, wsmodel.idx().seenRootsNotChecked, prefFlex);
			MapsOps.addStringToValueSet(suffRoot, wsmodel.idx().seenRootsNotChecked, suffFlex);
			
			if (prefFlex.equals(""))
				prefFlex = "_";
			if (suffFlex.equals(""))
				suffFlex = "_";
			// if(!prefRoot.equals(inputword.toString()) && prefFlex.equals(suffFlex)) {
			
			inputword.prefRootFlex = new MyPair(prefRoot, prefFlex);
			inputword.sufRootFlex = new MyPair(suffRoot, suffFlex);
			
			if (prefFlex.equals(suffFlex)) {
				inputword.setRoot(prefRoot);
				inputword.setFlex(prefFlex);
				wsmodel.idx().seenRoots.add(prefRoot);
				MapsOps.addFreq(prefFlex, wsmodel.idx().seenFlexes);
//				System.out.println("SAME INIT flex\t" + prefRoot+ "\t" + prefFlex );
			}
		}
	}
	
	/**
	 * computes additional flexes and roots for cases were they were not the same but can be mapped:
	 * SAME SEEN_PREF flex	_гонщик а_ 1.0		_гонщ ика_ 0.2005
	 * SAME SEEN_SUFF flex	_котеджі в_ 1.0		_котедж ів_ 0.6141
	 * 
	 * writes found roots and flexes as seenRoots and seenFlexes
	 * 
		 * @param wsmodel
	 * @param ltmodel
	 */
	public static void collectMoreRoots(WordSequences wsmodel, int freqTHH) {
		for(Word inputword: wsmodel.idx().getSortedWords()) {
			if(inputword.freq() < freqTHH) continue;
			
			//look for other flexes, roots and for suffixes
			if(inputword.prefRootFlex == null || inputword.sufRootFlex == null) continue;
			String suffFlex = inputword.sufRootFlex.second.replaceAll("_", "");
			String suffRoot = inputword.sufRootFlex.first.replaceAll("_", "");
			String prefFlex = inputword.prefRootFlex.second.replaceAll("_", "");
			String prefRoot = inputword.prefRootFlex.first.replaceAll("_", "");
			if(prefFlex.equals("")) prefFlex = "_";
			if(suffFlex.equals("")) suffFlex = "_";

			if(inputword.getFlex() != null && prefFlex.equals(suffFlex)) {
				continue; // means: there are roots and flexes already written 
			}
			if(wsmodel.idx().seenRoots.contains(prefRoot) && wsmodel.idx().seenFlexes.containsKey(prefFlex) 
					//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
					&& (!wsmodel.idx().seenFlexes.containsKey(suffFlex) || wsmodel.idx().seenFlexes.get(prefFlex) > wsmodel.idx().seenFlexes.get(suffFlex)) ) {
				wsmodel.idx().seenRoots.add(prefRoot);
				MapsOps.addFreq(prefFlex, wsmodel.idx().seenFlexes);
//				inputword.setRoot(prefRoot);
//				inputword.setFlex(prefFlex);
//				System.out.println("SAME PREF post INIT\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString()	);
			}
			else if(wsmodel.idx().seenRoots.contains(suffRoot) && wsmodel.idx().seenFlexes.containsKey(suffFlex) 
					//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
					&& (!wsmodel.idx().seenFlexes.containsKey(prefFlex) || wsmodel.idx().seenFlexes.get(suffFlex) > wsmodel.idx().seenFlexes.get(prefFlex)) ) {
				wsmodel.idx().seenRoots.add(suffRoot);
				MapsOps.addFreq(suffFlex, wsmodel.idx().seenFlexes);
//				inputword.setRoot(suffRoot);
//				inputword.setFlex(suffFlex);
//				System.out.println("SAME SUFF post INIT\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString()			);
			}
			else if(wsmodel.idx().seenRootsNotChecked.containsKey(prefRoot) && wsmodel.idx().seenRootsNotChecked.get(prefRoot).size() > 1
					&& wsmodel.idx().seenRootsNotChecked.containsKey(suffRoot) && wsmodel.idx().seenRootsNotChecked.get(suffRoot).size() == 1) {
				wsmodel.idx().seenRoots.add(prefRoot);
				MapsOps.addFreq(prefFlex, wsmodel.idx().seenFlexes);
				System.out.println("THIS WAS ADDED PREF\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString());
			}
			else if(wsmodel.idx().seenRootsNotChecked.containsKey(suffRoot) && wsmodel.idx().seenRootsNotChecked.get(suffRoot).size() > 1
					&& wsmodel.idx().seenRootsNotChecked.containsKey(prefRoot) && wsmodel.idx().seenRootsNotChecked.get(prefRoot).size() == 1) {
				wsmodel.idx().seenRoots.add(suffRoot);
				MapsOps.addFreq(suffFlex, wsmodel.idx().seenFlexes);
				System.out.println("THIS WAS ADDED SUFF\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString());
			}
				
		}
	}
	
	public static void computeMoreRoots(WordSequences wsmodel, int freqTHH) {
		for(Word inputword: wsmodel.idx().getSortedWords()) {
			if(inputword.toString().equals("прагне"))
				System.out.println();
			if(inputword.freq() < freqTHH) continue;
			
			//look for other flexes, roots and for suffixes
			if(inputword.prefRootFlex == null || inputword.sufRootFlex == null) continue;
			String suffFlex = inputword.sufRootFlex.second.replaceAll("_", "");
			String suffRoot = inputword.sufRootFlex.first.replaceAll("_", "");
			String prefFlex = inputword.prefRootFlex.second.replaceAll("_", "");
			String prefRoot = inputword.prefRootFlex.first.replaceAll("_", "");
			if(prefFlex.equals("")) prefFlex = "_";
			if(suffFlex.equals("")) suffFlex = "_";

			if(inputword.getFlex() != null && prefFlex.equals(suffFlex)) {
				continue; // means: there are roots and flexes already written 
			}
			if(wsmodel.idx().seenRoots.contains(prefRoot) && wsmodel.idx().seenFlexes.containsKey(prefFlex) 
					//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
					&& (!wsmodel.idx().seenFlexes.containsKey(suffFlex) || wsmodel.idx().seenFlexes.get(prefFlex) > wsmodel.idx().seenFlexes.get(suffFlex)) ) {
//				wsmodel.idx().seenRoots.add(prefRoot);
//				MapsOps.addFreq(prefFlex, wsmodel.idx().seenFlexes);
				inputword.setRoot(prefRoot);
				inputword.setFlex(prefFlex);
//				System.out.println("SAME PREF post INIT\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString()	);
			}
			else if(wsmodel.idx().seenRoots.contains(suffRoot) && wsmodel.idx().seenFlexes.containsKey(suffFlex) 
					//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
					&& (!wsmodel.idx().seenFlexes.containsKey(prefFlex) || wsmodel.idx().seenFlexes.get(suffFlex) > wsmodel.idx().seenFlexes.get(prefFlex)) ) {
//				wsmodel.idx().seenRoots.add(suffRoot);
//				MapsOps.addFreq(suffFlex, wsmodel.idx().seenFlexes);
				inputword.setRoot(suffRoot);
				inputword.setFlex(suffFlex);
//				System.out.println("SAME SUFF post INIT\t" + inputword.prefRootFlex.toString()+ "\t" + inputword.sufRootFlex.toString()			);
			}
			else if(wsmodel.idx().seenRootsNotChecked.containsKey(prefRoot) && wsmodel.idx().seenRootsNotChecked.get(prefRoot).size() > 1
					&& wsmodel.idx().seenRootsNotChecked.containsKey(suffRoot) && wsmodel.idx().seenRootsNotChecked.get(suffRoot).size() == 1) {
				inputword.setRoot(prefRoot);
				inputword.setFlex(prefFlex);
			}
			else if(wsmodel.idx().seenRootsNotChecked.containsKey(suffRoot) && wsmodel.idx().seenRootsNotChecked.get(suffRoot).size() > 1
					&& wsmodel.idx().seenRootsNotChecked.containsKey(prefRoot) && wsmodel.idx().seenRootsNotChecked.get(prefRoot).size() == 1) {
				inputword.setRoot(suffRoot);
				inputword.setFlex(suffFlex);
			}
		}
	}

	/**
	 * Transitions: ними	{ими=554.0, и=2.0, ними=154.0}
	 * 
	 * writes found transitions as seenSuffixes
	 * 
	 * @param wsmodel
	 * @param ltmodel
	 */
	public static void computeRootFlexSuffixTransitions(WordSequences wsmodel, int freqTHH) {
		for(Word inputword: wsmodel.idx().getSortedWords()) {
			if(inputword.freq() < freqTHH) continue;
			if(inputword.prefRootFlex == null || inputword.sufRootFlex == null) continue;
			
			String suffFlex = inputword.sufRootFlex.second.replaceAll("_", "");
			String suffRoot = inputword.sufRootFlex.first.replaceAll("_", "");
			String prefFlex = inputword.prefRootFlex.second.replaceAll("_", "");
			String prefRoot = inputword.prefRootFlex.first.replaceAll("_", "");
			if(prefFlex.equals("")) prefFlex = "_";
			if(suffFlex.equals("")) suffFlex = "_";
			
			if(prefFlex.equals(suffFlex) ) { //for words where pref roots and suff roots were always the same
				//transitions for same flexes in suffs and prefs method
				saveSuffixTransitions(wsmodel, prefFlex, prefFlex);
				continue; // means: there are roots and flexes already written by 
			}
			if( inputword.getRoot() == null) continue;
			
			//look for other flexes, roots and for suffixes
			if( inputword.getRoot().equals(prefRoot)) {
				saveSuffixTransitions(wsmodel, prefFlex, suffFlex);
			}
			else if(inputword.getRoot().equals(suffRoot)) {
				saveSuffixTransitions(wsmodel, suffFlex, prefFlex);
			}
//			System.out.println(inputword);
		}
	}
	
	public static void computeNewRootFlex(WordSequences wsmodel, boolean useOnlySingleSuffixTransition, int freqTHH) {
		for (Word inputword : wsmodel.idx().getSortedWords()) {
			StringBuffer sbuf = new StringBuffer();
			if(!useOnlySingleSuffixTransition && (inputword.toString().equals("відповідями") || inputword.toString().equals("відповідними") || inputword.toString().equals("відповідний")|| inputword.toString().equals("відповідного") || inputword.toString().equals("метрам")) )
				System.out.println("відповідями");
			if (inputword.freq() < freqTHH)
				continue;
			if (inputword.prefRootFlex == null || inputword.sufRootFlex == null)
				continue;
			String suffFlex = inputword.sufRootFlex.second.replaceAll("_", "");
			String suffRoot = inputword.sufRootFlex.first.replaceAll("_", "");
			String prefFlex = inputword.prefRootFlex.second.replaceAll("_", "");
			String prefRoot = inputword.prefRootFlex.first.replaceAll("_", "");
			if (prefFlex.equals(""))
				prefFlex = "_";
			if (suffFlex.equals(""))
				suffFlex = "_";

			// Logik:
			// if prefFlex & sufFlex the same -> check transitions -> if possible transition
			// -> check newRoot
			// if not the same: check prefRoot -> check suffFlex transition -> if after
			// trans prefFlex - take pref
			// if not pref -> check suffRoot -> same Logik as in pref
			// if no root - check longer for transitions -> if so -> check new Root
			boolean wasCorrected = false;
			if (!prefRoot.equals(inputword.toString()) && prefFlex.equals(suffFlex) //pref == suf
					&& wsmodel.idx().seenSuffixes.containsKey(prefFlex)) {
				// try to correct suffix
				MyPair root_flex_pair = checkBestFlexFromTransitions(wsmodel, prefFlex, prefRoot, useOnlySingleSuffixTransition);

				if (root_flex_pair.first.length() != prefRoot.length()) {
					correctRootFlex(inputword, root_flex_pair);
					inputword.prefRootFlex = root_flex_pair;
					inputword.sufRootFlex = root_flex_pair;
					wasCorrected = true;
					sbuf.append("SAME CORRECTED flex\t" + inputword.prefRootFlex.toString() + ":\t" + root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
				}
				else
					sbuf.append("SAME NOT CORRECTED flex\t" + inputword.prefRootFlex.toString() + "\t"	+ root_flex_pair.toString() + "\n");
			} else if (!prefFlex.equals(suffFlex)) { //pref != suff
					MyPair root_flex_pair = checkBestFlexFromTransitions(wsmodel, prefFlex, prefRoot, useOnlySingleSuffixTransition);
					if (root_flex_pair.first.length() != prefRoot.length()) {
						String newRoot = root_flex_pair.first;
						String newFlex = root_flex_pair.second;
						if (wsmodel.idx().seenRoots.contains(newRoot) || newRoot.equals(suffRoot)) {
							wasCorrected = true;
							correctRootFlex(inputword, root_flex_pair);
							inputword.prefRootFlex = root_flex_pair;
							sbuf.append("PREF CORRECTED flex\t" + inputword.prefRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
						} else if (wsmodel.idx().seenRoots.contains(prefRoot)
								&& !wsmodel.idx().seenRoots.contains(newRoot))
							sbuf.append("PREF NOT CORRECTED flex\t" + inputword.prefRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
						else {
							wasCorrected = true;
							correctRootFlex(inputword, root_flex_pair);
							inputword.prefRootFlex = root_flex_pair;
							sbuf.append("PREF CORRECTURE\t" + inputword.prefRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
						}
					}
					else if (!wasCorrected ) { //check suffix
						root_flex_pair = checkBestFlexFromTransitions(wsmodel, suffFlex, suffRoot, useOnlySingleSuffixTransition);
						if (root_flex_pair.first.length() != suffRoot.length()) {
							String newRoot = root_flex_pair.first;
							String newFlex = root_flex_pair.second;
							if (wsmodel.idx().seenRoots.contains(newRoot) || newRoot.equals(prefRoot)) {
								wasCorrected = true;
								correctRootFlex(inputword, root_flex_pair);
								inputword.sufRootFlex = root_flex_pair;
								sbuf.append("SUFF CORRECTED flex\t" + inputword.sufRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
							} else if (wsmodel.idx().seenRoots.contains(suffRoot)
									&& !wsmodel.idx().seenRoots.contains(newRoot))
								sbuf.append("SUFF NOT CORRECTED flex\t" + inputword.prefRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
							else {
								wasCorrected = true;
								correctRootFlex(inputword, root_flex_pair);
								inputword.sufRootFlex = root_flex_pair;
								sbuf.append("SUFF CORRECTURE\t" + inputword.sufRootFlex.toString() + ":\t"	+ root_flex_pair.first + "\t" + root_flex_pair.second + "\n");
							}
						}
					}
				if (!wasCorrected) {
//					sbuf.append("STILL NOT EQUAL\t" + inputword.prefRootFlex.toString() + "\t"	+ inputword.sufRootFlex.toString() + "\n");
				}
			} else {
				sbuf.append(	"OTHERS\t" + inputword.prefRootFlex.toString() + "\t" + inputword.sufRootFlex.toString() + "\n");
			}
//			if(!wasCorrected && !suffFlex.equals(prefFlex)) { //suff != flex
//				if(wsmodel.idx().seenRoots.contains(prefRoot)  ) { //no check on flex - root check should be enough here
//					correctRootFlex(inputword, new MyPair(prefRoot, prefFlex));
//					System.out.println("NEW CORRECTED PREF\t" + inputword.prefRootFlex.toString() + "\t" + inputword.sufRootFlex.toString());
//				}
//				else if(wsmodel.idx().seenRoots.contains(suffRoot)  ) {
//					correctRootFlex(inputword, new MyPair(suffRoot, suffFlex));
//					System.out.println("NEW CORRECTED SUFF\t" + inputword.prefRootFlex.toString() + "\t" + inputword.sufRootFlex.toString());
//				}
//			}
			if(!useOnlySingleSuffixTransition)
				System.out.print(sbuf.toString());
		}
	}
	
	private static void correctRootFlex(Word inputword, MyPair root_flex_pair) {
		inputword.setRoot(root_flex_pair.first);
		inputword.setFlex(root_flex_pair.second);
	}

	/**
	 * returns suffix+flex
	 * @param wsmodel
	 * @param flex
	 * @return
	 */
	private static MyPair checkBestFlexFromTransitions(WordSequences wsmodel, String flex, String root, boolean useOnlySingleTransition) {
		Map<String,Map<String,Double>> transitions = wsmodel.idx().seenSuffixes;
		if(!transitions.containsKey(flex)) return new MyPair(root, flex);
		Map<String,Double> transitionsFound = transitions.get(flex);
		//find not possible suffix tramsformations- those, that are not suffixes of the particular word
		Set<String> butnot = new HashSet<>();
		String word = root+flex;
		for(String transitionFlex: transitionsFound.keySet()) {
			if(transitionFlex.equals("_")) transitionFlex = "";
			if(!word.endsWith(transitionFlex)) butnot.add(transitionFlex);
		}
		//retrieves most frequent transition
		String bestFlex = MapsOps.getFirstButNOT(transitionsFound, butnot).first;
		if(flex.equals("_") && bestFlex.equals("_"))
			return new MyPair(root, bestFlex);
//		if(bestFlex.equals("_")) bestFlex = ""; // BUG
		
		if(useOnlySingleTransition) {
			if( transitionsFound.size() == 1) 
				return computeNewRootFlex(flex, root, bestFlex);
			else
				return new MyPair(root, flex);
		}
		
//		boolean newRootWasSeen = wsmodel.idx().seenRoots.contains(computeNewRootFlex(flex, root, bestFlex).first); //checking if the possible new root is in seen roots
//		String savedBestFlex = bestFlex;
//		while(!newRootWasSeen  ) {
//			bestFlex = MapsOps.getFirstButNOT(transitionsFound, butnot).first;
//			butnot.add(bestFlex);
//			if(bestFlex.equals("")) //end of transition list
//				break;
//			if(bestFlex.equals("_")) bestFlex = "";
//			newRootWasSeen = wsmodel.idx().seenRoots.contains(computeNewRootFlex(flex, root, bestFlex).first);
//		}
//		if(!newRootWasSeen) bestFlex = savedBestFlex; //take statistically the best flex, if we never see the new root
		return computeNewRootFlex(flex, root, bestFlex);
	}

	private static MyPair computeNewRootFlex(String flex, String root, String bestFlex) {
		String word = root+flex;
		int bestFlexLength = bestFlex.length();
		if(bestFlex.equals("_")) bestFlexLength = 0;
		
		int flexLength = flex.length();
		if(flex.equals("_")) flexLength = 0;
		
		if(bestFlexLength >= word.length()) return  new MyPair(root, flex);
		
		if(bestFlexLength == flexLength) return new MyPair(root, bestFlex);
		else if(bestFlexLength < flexLength) {
			String suffix = flex.substring(0,(flexLength-bestFlexLength));
			String newRoot = word.substring(0, word.length() - flexLength) + suffix;
			String newFlex = bestFlex;
			return new MyPair(newRoot, newFlex);
		}else { //bestFlex is longer than suffFlex - should be seldom
			String newRoot = word.substring(0, word.length() - bestFlexLength);
			String newFlex = bestFlex;
			return new MyPair(newRoot, newFlex);
		}
	}

	private static void saveSuffixTransitions(WordSequences wsmodel, String prefferedFlex, String otherFlex) {
		Map<String,Double> preferredFreqMap = new HashMap<>();
		if(!wsmodel.idx().seenSuffixes.containsKey(otherFlex)) wsmodel.idx().seenSuffixes.put(otherFlex, preferredFreqMap);
		preferredFreqMap = wsmodel.idx().seenSuffixes.get(otherFlex);
		MapsOps.addFreq(prefferedFlex, preferredFreqMap);
		
	}

	public static MyPair getRootFlexSave(WordSequences wsmodel, LetterTokModel ltmodel, Word inputword) {
	//		if(inputword.toString().equals("компенсувати"))
			if(inputword.toString().equals("бідне"))
				System.out.println();		
			
			if(inputword.getRoot() != null && inputword.getFlex() != null) {
				System.out.println("SAME flex\t" + inputword.getRoot()+ "\t" + inputword.getFlex() );
				return new MyPair(inputword.getRoot(), inputword.getFlex());
			}
			
			List<MyPair> slicesPref = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel, inputword.toString(), "pref");
			boolean isCmp = isCompound(slicesPref);
			List<MyPair> prefCuts = getRootFlexFromPREFIXSlice(inputword, slicesPref, wsmodel, isCmp);
			MyPair rootFlexPref = prefCuts.get(prefCuts.size()-1);
			String lastCutInPREFroot = "";
			if(prefCuts.size() > 1) {
				//CUTS: _abweichen	_	1.0, _abweich	en_	0.8 -> en = lastCutInPREFroot
				int lengthDiffBetweenLastAndPrevious = prefCuts.get(prefCuts.size()-2).second.length()-rootFlexPref.second.length();
				lastCutInPREFroot = prefCuts.get(prefCuts.size()-2).second.substring(0, lengthDiffBetweenLastAndPrevious); 
			}
			//get one cut from the suffixes
			List<MyPair> slicesSuf = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel, inputword.toString(), "suf");
	//		boolean isCmpSuff = isCompoundSuf(slicesSuf);
			MyPair rootFlexSuf = getRootFlexFromSUFFIXSlice(inputword, slicesSuf, wsmodel, isCmp, false);
			String suffFlex = rootFlexSuf.second.replaceAll("_", "");
			String suffRoot = rootFlexSuf.first.replaceAll("_", "");
			String prefFlex = rootFlexPref.second.replaceAll("_", "");
			String prefRoot = rootFlexPref.first.replaceAll("_", "");
			if(prefFlex.equals("")) prefFlex = "_";
			if(suffFlex.equals("")) suffFlex = "_";
	
	
			if(!prefRoot.equals(inputword.toString()) && prefFlex.equals(suffFlex)) {
				System.out.println("SAME flex\t" + rootFlexPref.toString()+ "\t" + lastCutInPREFroot + "\t" + rootFlexSuf.toString()					);
	//			saveSuffixTransitions(wsmodel, prefFlex, suffFlex);
				return new MyPair(prefRoot, prefFlex);
			}
			//check: prefRoot in seenRoots, prefFlex.freq > suffFlex.freq in seenFlexes OR vice versa 
			else if(wsmodel.idx().seenRoots.contains(prefRoot) && wsmodel.idx().seenFlexes.containsKey(prefFlex) 
					//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
					&& (!wsmodel.idx().seenFlexes.containsKey(suffFlex) || wsmodel.idx().seenFlexes.get(prefFlex) > wsmodel.idx().seenFlexes.get(suffFlex)) ) {
				System.out.println("SAME SEEN_PREF flex\t" + rootFlexPref.toString()+ "\t" + lastCutInPREFroot + "\t" + rootFlexSuf.toString()			);
	//			saveSuffixTransitions(wsmodel, prefFlex, suffFlex);
				return new MyPair(prefRoot, prefFlex);
			}
			else if(wsmodel.idx().seenRoots.contains(suffRoot) && wsmodel.idx().seenFlexes.containsKey(suffFlex) 
					//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
					&& (!wsmodel.idx().seenFlexes.containsKey(prefFlex) || wsmodel.idx().seenFlexes.get(suffFlex) > wsmodel.idx().seenFlexes.get(prefFlex)) ) {
				System.out.println("SAME SEEN_SUFF flex\t" + rootFlexPref.toString()+ "\t" + lastCutInPREFroot + "\t" + rootFlexSuf.toString()			);
	//			saveSuffixTransitions(wsmodel, suffFlex, prefFlex);
				return new MyPair(suffRoot, suffFlex);
			}
			
			
			
			//DIFF flex	schwimm|en _ 1.0	schwimm en 1.0 --> compare last cut in root
			else if(!lastCutInPREFroot.equals("") && lastCutInPREFroot.equals(suffFlex)) {
				System.out.print("CUTS flex\t" + rootFlexPref.toString() + "\t" + lastCutInPREFroot + "\t" + rootFlexSuf.toString()			);
				if(wsmodel.idx().seenRoots.contains(suffRoot) && !wsmodel.idx().seenRoots.contains(prefRoot)) {
					MyPair sufFlex = checkBestFlexFromTransitions(wsmodel, suffFlex, inputword.toString(), false);
					System.out.println("\t" + sufFlex + "\tCONFIRMED SUFF ROOT && TRANS FLEX");
	//				return new MyPair(suffRoot, suffFlex);
				}
				else if(wsmodel.idx().seenRoots.contains(prefRoot)) {
					MyPair sufFlex = checkBestFlexFromTransitions(wsmodel, prefFlex, inputword.toString(), false);
					System.out.println("\t" + sufFlex + "\tCONFIRMED PREF ROOT && TRANS FLEX");
	//				return new MyPair(prefRoot, prefFlex);
				}
				else System.out.println();
				//return new MyPair(suffRoot, suffFlex);
			}
			//SUFF bemüh ungen 1.0	Bemühung en 1.0, when prefix flex ends with suff flex, then the middle part of prefix flex is potential suffix: ung
			//also check suffix as ROOTpart: check that possible suffix is not part of the root: rootFlexPref.freq != 1.0
			else if(!prefRoot.equals(inputword.toString()) && prefFlex.endsWith(suffFlex) 
					&& slicesPref.get(slicesPref.size() - (suffFlex.length()+2)).freq != 1.0) {
				System.out.print("SUFF flex\t" + rootFlexPref.toString() +  "\t" 
						+ prefRoot + ":" //suffix between ::
						+ prefFlex.substring(0,(prefFlex.length() - suffFlex.length())) + ":" +suffFlex
						+  "\t" + rootFlexSuf.toString()
						//+ "t" + slicesPref.toString()
						);
				if(wsmodel.idx().seenRoots.contains(suffRoot)) System.out.println("\tCONFIRMED ROOT");
				else System.out.println();
				//return new MyPair(suffRoot, suffFlex);
			}
			else if(!prefRoot.equals(inputword.toString()) ) {
				if( wsmodel.idx().seenRoots.contains(prefRoot)
	//					&&
	//					wsmodel.idx().seenFlexes.containsKey(prefFlex) 
	//					//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
	//					&& (!wsmodel.idx().seenFlexes.containsKey(suffFlex) || wsmodel.idx().seenFlexes.get(prefFlex) > wsmodel.idx().seenFlexes.get(suffFlex)) 
						) {
					System.out.println("DIFF SEEN_PREF flex\t" + rootFlexPref.toString()+ "\t" + lastCutInPREFroot + "\t" + rootFlexSuf.toString()
					//+ "t" + slicesPref.toString()
					);
					//return new MyPair(prefRoot, prefFlex); //not so good for German - Bundesliga, ukr: компенсувати
				}
				else if( wsmodel.idx().seenRoots.contains(suffRoot)
	//					&&
	//					wsmodel.idx().seenFlexes.containsKey(suffFlex) 
	//					//suffFlex not seen in SAME OR its freq is < as freq of prefFlex
	//					&& (!wsmodel.idx().seenFlexes.containsKey(prefFlex) || wsmodel.idx().seenFlexes.get(suffFlex) > wsmodel.idx().seenFlexes.get(prefFlex)) 
						) {
					System.out.println("DIFF SEEN_SUFF flex\t" + rootFlexPref.toString()+ "\t" + lastCutInPREFroot + "\t" + rootFlexSuf.toString()
					//+ "t" + slicesPref.toString()
					);
					//return new MyPair(suffRoot, suffFlex); //not so good for German - Bundesliga, ukr: компенсувати
				}
				else {
					System.out.println("DIFF flex\t" + rootFlexPref.toString() +  "\t" + rootFlexSuf.toString() 
					//+ "t" + slicesPref.toString()
					);
					return new MyPair(inputword.toString(), "_");
				}
	//			if(wsmodel.idx().seenRoots.contains(prefRoot)) System.out.println("\tCONFIRMED PREF ROOT");
	//			else if(wsmodel.idx().seenRoots.contains(suffRoot)) System.out.println("\tCONFIRMED SUFF ROOT");
	//			else System.out.println();
			}
			//end of cut from the suffixes
			
			return new MyPair(inputword.toString(), "_");
		}

	private static MyPair getRootFlexFromSUFFIXSlice(Word inputword, List<MyPair> slices, WordSequences wsmodel, boolean isCmp, boolean isCmpSuff) {
		boolean foundZeroFrequencySlice = checkSlicesForZeroFrequency(slices);
		MyPair prev = null;
		String wordWithCuts = inputword.toString();
		prev = null;
		for (int i = 0; i < Math.min(6, slices.size()); i++) {
			if(slices.size() < 5) break;
			if(i > slices.size()-3) break; //don't check more than 5 letters from the back
			MyPair mp = slices.get(i);
			if(mp.freq < 0) break; //if freq = -1 -> don't analyze potential flex - it is irrational
			if(prev == null) {
				prev = mp;
				continue;
			}
//			if(prev.freq > 0.15 && mp.freq < 0.05) { //old rule
			if(prev.freq > mp.freq && !isCmp && !isCmpSuff && !foundZeroFrequencySlice) { 
				String root = prev.first.replaceAll("_", "");
				String flex = prev.second.replaceAll("_", "");
				return prev; // add only 1 first cut from the back of the word
			}
			prev = mp;
		}
		return new MyPair(wordWithCuts, "_");
	}
	
	private static MyPair checkNextDrop(List<MyPair> slices, int i, double biggest_drop) {
		MyPair curr = slices.get(i);
		if(slices.size() > i+1) {
			MyPair next = slices.get(i+1);
			if(curr.freq > next.freq) {
				double new_biggest_drop = curr.freq - next.freq;
				if(new_biggest_drop > biggest_drop) return checkNextDrop(slices, i+1, new_biggest_drop);
				else return slices.get(i-1);
			}else return slices.get(i-1);
		}
		return slices.get(i-1);
	}

	private static List<MyPair> getRootFlexFromPREFIXSlice(Word inputword, List<MyPair> slices, WordSequences wsmodel, boolean isCmp) {
		boolean foundZeroFrequencySlice = checkSlicesForZeroFrequency(slices);
		MyPair prev = null;
		List<MyPair> cuts = new ArrayList<>();
		boolean isGoingUp = false;
		for(MyPair mp: slices) {
			if(prev == null) {
				prev = mp;
				continue;
			}
			
//			if(prev.freq > 0.59 && mp.freq < (prev.freq-0.3) && !foundZeroFrequencySlice 
			//Experiment
//			if(prev.freq > 0.59 && mp.freq < (prev.freq) && !foundZeroFrequencySlice
			if(prev.freq > 0.1 && mp.freq < (prev.freq) && !foundZeroFrequencySlice
					&& isGoingUp
					&& !isCmp
					) { //benchmark cuts 4, actual, maybe the best
				cuts.add(prev);
			}
			//check second cut
			if(prev.freq > 0.59 && mp.freq < (prev.freq) 
					&& !isGoingUp && cuts.size() > 0
					&& (cuts.get(cuts.size()-1).freq - prev.freq) < (prev.freq - mp.freq) //situation: one cut after other cut, second cut is bigger-> could be suff
					&& mp.freq < 0.1
					/*
					  	_держав	не_	0.8928571428571429
						_державн	е_	0.6
						_державне	_	0.06666666666666667
					 */
					) { 
//				System.out.println("ATTENTION SECOND CUT\t" +cuts.get(cuts.size()-1) + "\t" + prev.toString()+ "\t" + mp.toString());
				cuts.add(prev);
			}
			//end check
			
			if(prev.freq <= mp.freq) isGoingUp = true;
			else isGoingUp = false;
			prev = mp;
		}
		if(cuts.size() == 0) cuts.add(new MyPair(inputword.toString(), "_"));
		return cuts;

	}

	private static boolean isCompound(List<MyPair> slices) {
		boolean isCmp = false;
		//if last 3 slices = 1.0 --> isCompaund
		if(slices.size() > 3 && slices.get(slices.size()-1).freq == 1.0 && slices.get(slices.size()-2).freq == 1.0 && slices.get(slices.size()-3).freq == 1.0)
			isCmp = true;
		//Zentralbank
		else if(slices.size() > 4 && slices.get(slices.size()-2).freq == 1.0 && slices.get(slices.size()-3).freq == 1.0 && slices.get(slices.size()-4).freq == 1.0)
			isCmp = true;
		return isCmp;
	}
	
//	private static boolean isCompoundSuf(List<MyPair> slices) { Entwicklers -> compound -> bad
//		boolean isCmp = false;
//		//if first 4 slices = 1.0 --> isCompaund
//		//Großbank
//		 if(slices.size() > 4 
//				 && slices.get(slices.size()-1).freq == 1.0 && slices.get(slices.size()-2).freq == 1.0 && slices.get(slices.size()-3).freq == 1.0 && slices.get(slices.size()-4).freq == 1.0)
//			isCmp = true;
//		return isCmp;
//	}
	
	
	
	//this is EXPERIMENTAL, if bad --> return to old!
	public static MyPair getRootFlexNewOld(WordSequences wsmodel, LetterTokModel ltmodel, Word inputword) {
		if(inputword.toString().equals("система"))
			System.out.println();
		List<MyPair> slices= MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel, inputword.toString(), "pref"); 
		boolean foundZeroFrequencySlice = checkSlicesForZeroFrequency(slices);
		MyPair prev = null;
		String wordWithCuts = inputword.toString();
		List<Integer> cuts = new ArrayList<>();
		
		//get one cut from the suffixes
		slices = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel, inputword.toString(), "suf");
		prev = null;
		boolean foundSufficCut = false;
		for (int i = 0; i < Math.min(6, slices.size()); i++) {
			if(slices.size() < 5) break;
			if(i > slices.size()-3) break; //don't check more than 5 letters from the back
			MyPair mp = slices.get(i);
			if(mp.freq < 0) break; //if freq = -1 -> don't analyze potential flex - it is irrational
			if(prev == null) {
				prev = mp;
				continue;
			}
			if((prev.freq > 0.15 && mp.freq < 0.05) || (prev.freq > 0.2 && mp.freq < 0.1)) { 
				//System.out.println(prev + "\t" + mp);
				cuts.add(prev.second.length()-1);
				foundSufficCut = true;
				break; // add only 1 first cut from the back of the word
			}
			prev = mp;
		}
		//end of cut from the suffixes
		if(!foundSufficCut) {
			slices = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel, inputword.toString(), "pref");
		for(MyPair mp: slices) {
			if(prev == null) {
				prev = mp;
				continue;
			}
			//if(prev.freq > 0.89 && mp.freq < 0.61) { //benchmark cut 1
//			if(prev.freq > 0.79 && mp.freq < (prev.freq-0.3)) { // benchmark cuts 2
//			if(prev.freq > 0.69 && mp.freq < (prev.freq-0.3)) { //benchmark cuts 3 
			if(prev.freq > 0.59 && mp.freq < (prev.freq-0.3) && !foundZeroFrequencySlice) { //benchmark cuts 4, actual, maybe the best
				cuts.add(prev.second.length()-1);
			}
			prev = mp;
		}
		}
		Collections.sort(cuts);
		
		//put cuts into word
		for(int cut: cuts) {
			int whereToPutCut = wordWithCuts.length() - cut;
			wordWithCuts = wordWithCuts.substring(0, whereToPutCut) + "|" + wordWithCuts.substring(whereToPutCut, wordWithCuts.length());
		}
		if(wordWithCuts.endsWith("|")) wordWithCuts = wordWithCuts+"_";
		String[] sarr = wordWithCuts.split("\\|");
		String flex = "_";
		if(sarr.length > 1)
			flex =  sarr[sarr.length-1];
		if(flex.equals("")) flex = "_";
		String root = sarr.length > 1 ? wordWithCuts.substring(0,wordWithCuts.length()-flex.length()) : wordWithCuts;
		
		//save info about potential prefixes
		String[] sarrPref = wordWithCuts.split("\\|");
		if(sarrPref.length > 2) {
			String prefix = sarrPref[0];
			int prefixLength = prefix.length();
			if(ltmodel.getLang().equals("de"))			prefixLength = adjustPrefixLength(prefix);
			MyPair prefixSlice = slices.get(Math.min(prefixLength, slices.size()-1)); // get slice next to pref, check if the fall was big enough
			if(prefixSlice.freq < 0.1) { // means the difference between 0.59 and this cut was not too small
				MapsOps.addFreq(prefix, wsmodel.idx().prefixFreq);
			}
		}
//		System.out.println(root + "\t" + flex);
		return new MyPair(root,flex);
	}

	public static MyPair getRootFlexOld(WordSequences wsmodel, LetterTokModel ltmodel, Word inputword) {
		List<MyPair> slices = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel, inputword.toString(), "pref");
		boolean foundZeroFrequencySlice = checkSlicesForZeroFrequency(slices);
		MyPair prev = null;
		String wordWithCuts = inputword.toString();
		List<Integer> cuts = new ArrayList<>();
		for(MyPair mp: slices) {
			if(prev == null) {
				prev = mp;
				continue;
			}
			//if(prev.freq > 0.89 && mp.freq < 0.61) { //benchmark cut 1
//			if(prev.freq > 0.79 && mp.freq < (prev.freq-0.3)) { // benchmark cuts 2
//			if(prev.freq > 0.69 && mp.freq < (prev.freq-0.3)) { //benchmark cuts 3 
			if(prev.freq > 0.59 && mp.freq < (prev.freq-0.3) && !foundZeroFrequencySlice) { //benchmark cuts 4, actual, maybe the best
				cuts.add(prev.second.length()-1);
			}
			prev = mp;
		}
		
		//get one cut from the suffixes
		slices = MorphAnalyzer.getWordCondFreqAnalysisSubstrings(wsmodel.getLang(), ltmodel, inputword.toString(), "suf");
		prev = null;
		for (int i = 0; i < Math.min(6, slices.size()); i++) {
			if(slices.size() < 5) break;
			if(i > slices.size()-3) break; //don't check more than 5 letters from the back
			MyPair mp = slices.get(i);
			if(mp.freq < 0) break; //if freq = -1 -> don't analyze potential flex - it is irrational
			if(prev == null) {
				prev = mp;
				continue;
			}
			if(prev.freq > 0.15 && mp.freq < 0.05) { 
				//System.out.println(prev + "\t" + mp);
				cuts.add(prev.second.length()-1);
				break; // add only 1 first cut from the back of the word
			}
			prev = mp;
		}
		Collections.sort(cuts);
		
		//end of cut from the suffixes
		
		//put cuts into word
		for(int cut: cuts) {
			int whereToPutCut = wordWithCuts.length() - cut;
			wordWithCuts = wordWithCuts.substring(0, whereToPutCut) + "|" + wordWithCuts.substring(whereToPutCut, wordWithCuts.length());
		}
		if(wordWithCuts.endsWith("|")) wordWithCuts = wordWithCuts+"_";
		String[] sarr = wordWithCuts.split("\\|");
		String flex = "_";
		if(sarr.length > 1)
			flex =  sarr[sarr.length-1];
		if(flex.equals("")) flex = "_";
		String root = sarr.length > 1 ? wordWithCuts.substring(0,wordWithCuts.length()-flex.length()) : wordWithCuts;
		
		//save info about potential prefixes
		String[] sarrPref = wordWithCuts.split("\\|");
		if(sarrPref.length > 2) {
			String prefix = sarrPref[0];
			int prefixLength = prefix.length();
			if(ltmodel.getLang().equals("de"))			prefixLength = adjustPrefixLength(prefix);
			MyPair prefixSlice = slices.get(Math.min(prefixLength, slices.size()-1)); // get slice next to pref, check if the fall was big enough
			if(prefixSlice.freq < 0.1) { // means the difference between 0.59 and this cut was not too small
				MapsOps.addFreq(prefix, wsmodel.idx().prefixFreq);
			}
		}
//		System.out.println(root + "\t" + flex);
		return new MyPair(root,flex);
	}
	
	private static boolean checkSlicesForZeroFrequency(List<MyPair> slices) {
		/* checks if there were zero frequency in slices. if so - cut should not be determined, because we do not have enough statistics for those n-grams
		  _п	ідмінити_	0.14338399089154003
		_пі	дмінити_	0.10158812359744519
		_під	мінити_	0.6618521665250637
		_підм	інити_	0.010269576379974325
		_підмі	нити_	-1.0
		_підмін	ити_	-1.0
		_підміни	ти_	-1.0
		_підмінит	и_	-1.0
		_підмінити	_	-1.0
		_підмінити_		-1.0
		*/
		 for(MyPair mp: slices)
			 if(mp.freq < 0) return true;
		return false;
	}

	private static int adjustPrefixLength(String prefix) {
		prefix = prefix.replaceAll("tsch", "1").replaceAll("sch", "2").replaceAll("ch", "3");
		return prefix.length();
	}


}
