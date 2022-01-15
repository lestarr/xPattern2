package modeltrain;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import model.SyntModel;
import model.WordSequences;
import model.Words;
import modelparts.Collocation;
import modelparts.MorphParadigm;
import modelparts.Word;
import modelutils.Cluster;
import modelutils.Vector;
import processText.mainModels.BuildWordSequenceModel;
import processText.mainModels.BuildWordSequenceModelDE;
import processText.mainModels.BuildWordSequenceModelUKR;
import util.ListOps;
import util.MyPair;
import util.MyUtils;

public class SyntParTrain {
	
//	static Writer outClusters = MyUtils.getWriter("out/cluster/clusterCentroide2.txt");
	private static double freqOfAND = 0;


	public static List<Cluster> trainClusters(WordSequences wsmodel, int max, int start, boolean print, String splitterType) throws IOException {
		
		freqOfAND = wsmodel.getFreqOfAnd();
		List<Pair<String, Vector>> wordsValuesList = getWordsValuesList(wsmodel, max, start, splitterType, false);	
		
		double minSim = Double.MAX_VALUE;
		minSim = 1.6;
		List<Cluster> output = analyzeWordListToCluster(wordsValuesList, wsmodel, print, splitterType);  
		
//		System.out.println("\n\n\nSTATS");
//		output = tagWordsToClusters(output, wordsValuesList,start,  minSim, wsmodel);
//		System.out.println(start);
//		System.out.println(start+max);
//		int analyzedWords = wordsValuesList.size();
//		System.out.println(analyzedWords);
//		wordsValuesList.addAll(getSliceOfWordvalues(wsmodel, max, start+max));
//		System.out.println(wordsValuesList.size());
//		output = tagWordsToClusters(output,wordsValuesList, analyzedWords, minSim, wsmodel);
//		CLusterUtils.analyseClusterConsistence(output, wsmodel);
		
		//print out cluster vectors
//		for(Cluster c: output)
//			System.out.println(c.getSim() + "\t" + c.getCentroide() + "\t" + c.toString() );
		
		return output;
	}


	public static List<Pair<String, Vector>> getWordsValuesList(WordSequences wsmodel, int max, int start
			, String splitterOnly, boolean tagOnlyWordsWithoutCluster) {
		List<Pair<String,Vector>> wordsValuesList = new ArrayList<>();
		List<Word> wordsToCluster = getMostFrequestWordsForTraining(wsmodel, max, start, splitterOnly);


		for( Word w: wordsToCluster) {
			if(tagOnlyWordsWithoutCluster && w.getCluster() != null) continue;
//			if(Cluster.morphParNum > 0 && w.morphLabel == null) continue;
//			Vector values = new Vector(collectFeaturesSmall(w, wsmodel));
			Vector values;
			if(splitterOnly.startsWith(SyntModel.SPLITTER_PREFIX))
				values = new Vector(collectFeaturesSplitter(w, wsmodel));
			else
				values = new Vector(collectFeatures(w, wsmodel));
//				values = new Vector(collectFeatures(w, wsmodel));
			wordsValuesList.add(new Pair(w.toString(), values));
//			System.out.println(w.toString() + "\t" + values.toString());
		}
		return wordsValuesList;
	}

	//FallBackCluster: for Words which were not added to any of the existing clusters
	public static List<Cluster> tagWordsToClusters(WordSequences model, List<Pair<String, Vector>> wordsValuesList, double minSim, boolean useFallbackCluster) {
		
		List<Cluster> inputClusters = ListOps.of(model.idx().syntPars().values());
		Cluster fallBackCluster = new Cluster(new Pair("dummy", inputClusters.get(0).getWordValues().get(0).getValue()));
		for(Pair<String,Vector> p: wordsValuesList) {
			boolean passedCheck = false;
			Cluster wordInCheckCluster = new Cluster(p);
			wordInCheckCluster.morphPar = model.getWord(p.getKey()).getMorphLabelNotNull();
			List<Pair<Cluster,Double>> bestClusters = computeBestClusters(inputClusters, Double.MAX_VALUE, -1, wordInCheckCluster);
			wordInCheckCluster.setBestClusters(bestClusters);
			boolean clusterWasAdded = false;
			for (int i = 0; i < bestClusters.size(); i++) {
				double sim = bestClusters.get(i).getValue();
				Cluster bestCluster = bestClusters.get(i).getKey();
				sim = checkFlexionForMPar(model.getWord(p.getKey()), sim, bestCluster, model);

				if(sim <= minSim) 
					passedCheck = true;
//				else System.out.println("NO min SIM: " + p.getKey() + "\t" +bestClusters.get(i).getValue()+ "\t" + bestClusters.get(i) );
//				if(passedCheck && !checkNeighbours(p.getKey(), bestCluster,model, sim))
//					passedCheck = false;
				if(passedCheck) {
					bestCluster.addCluster(new Cluster(p));
					clusterWasAdded = true;
					//add cluster to word
					model.getWord(p.getKey()).setCluster(bestCluster);
					break;
				}
			}
			if(!clusterWasAdded) {
				//try to add to a best word cluster
				List<Pair<Cluster, Double>> bwordPairs = findBestWordCluster(p, wordsValuesList, minSim);
				Pair<Cluster,Double> bestWordClusterPair = bwordPairs.size() > 0 ? bwordPairs.get(0) : null;
//				System.out.println("BWC: " + p.getKey() + "\t"+ bestWordClusterPair);
				if(bestWordClusterPair != null && bestWordClusterPair.getValue() < minSim) {
					Cluster bestWordCluster = model.getWord(bestWordClusterPair.getKey().getWordValues().get(0).getKey()).getCluster();
//					System.out.println("BWC2: " + bestWordCluster);
					if(bestWordCluster != null){
						bestWordCluster.addCluster(new Cluster(p));
						clusterWasAdded = true;
					} 
//					else System.out.println("WHOLE best word list:\t" + bwordPairs.toString());
				}
			}
			if(!clusterWasAdded && useFallbackCluster) {
					//make a new Cluster
				Cluster newCluster = new Cluster(p);
				fallBackCluster.addCluster(newCluster);

			}
		}
		if(useFallbackCluster)		inputClusters.add(fallBackCluster);
		
		System.out.println("\n\n\n");
		for(Cluster c: inputClusters) {
			System.out.println(MyUtils.rdouble(c.getSim()) + "\t" + c.toString());
			System.out.print("FLEX WORDS: \t"  );
			for(String w: c.getWords()) {
				if(model.getWord(w).clusterWithFlex)
					System.out.print(w+"\t");
			}
			System.out.println();
		}
		System.out.println("SIZE: " + inputClusters.size());
		return inputClusters;
	}
	
	private static double checkFlexionForMPar(Word word, double sim, Cluster cluster, WordSequences model) {
		if(Cluster.morphParNum == 0) return sim;
		if(sim < Cluster.morphParNum) return sim;
		if(word.getFlex() == null || word.getFlex().equals("_")) return sim;
		if(cluster.morphPar == null || cluster.morphPar.equals(Word.M_ZERO)) return sim;
		MorphParadigm mp = model.getWord(cluster.morphPar).getMorphParadigm();
		String flex = word.getFlex();
		if(mp.getFlexes().contains(model.idx().getFlex(flex)) && Cluster.morphParNum > 0) {
			//remove punishment, if a word has flex which is contained in the clusters paradigm
			sim = sim - Cluster.morphParNum;
			word.clusterWithFlex = true;
		}
		return sim;
	}


	private static List<Pair<Cluster, Double>> findBestWordCluster(Pair<String, Vector> p, 
			List<Pair<String, Vector>> wordsValues, double minSim) {
		List<Cluster> clusterList = new ArrayList<>();
		for(Pair p1: wordsValues) {
			clusterList.add(new Cluster(p1));
		}
		List<Pair<Cluster, Double>> bestClusters = computeBestClusters(clusterList, minSim, -1, new Cluster(p));
		return bestClusters;
	}
	
	public static List<Double> collectFeaturesSplitter(Word w, WordSequences model) {
		double sumLeftwords = 0;
		double sumRihtwords = 0;
		double sumSplitter = 0;
		double sumUnknowns = 0;
		double sumWords = 0;
		
		//new features
		double sumPredicats2 = 0;
		double sumAdj = 0;
		double sumContrAdj = 0;
		
		//new features experiment
		double sumAddFeature4 = 0;
		
		List<Double> list = new ArrayList<Double>();
		for(Word contextWord: w.left_of.keySet()) {
			double thisBigramFreq = w.left_of.get(contextWord);
			//check only significant
			if((w.left_of.get(contextWord) / contextWord.freq()) < 0.001 && (w.left_of.get(contextWord) / w.freq()) < 0.001) continue;
			
			sumWords = sumWords + thisBigramFreq;
			if(Words.isLeftWord(contextWord)) sumLeftwords = sumLeftwords +  thisBigramFreq;
			else sumRihtwords = sumRihtwords +  thisBigramFreq;
			//how often this word is followed by sth like article or preposition or connector 
			if(contextWord.getCoef(true) > 0.95 && contextWord.getCoef(false) > 0.95) sumSplitter = sumSplitter +  thisBigramFreq;
			
			else if(contextWord.getCoef(true) > 0.5 && contextWord.getCoef(false) > 0.5) sumPredicats2 = sumPredicats2 + thisBigramFreq;
			//maybe ADJ
			if(contextWord.getCoef(true) > 0.6 && contextWord.getCoef(false) < 0.4) sumAdj = sumAdj +  thisBigramFreq;
			if(contextWord.getCoef(false) > 0.6 && contextWord.getCoef(true) < 0.4) sumContrAdj = sumContrAdj +  thisBigramFreq;

			//get context word coef to the left (right coef)
			if(contextWord.getCoef(true) < 0.2 && contextWord.getCoef(false) < 0.2) sumUnknowns = sumUnknowns + thisBigramFreq;
//			else if(contextWord.getCoef(true) < 0.5 && contextWord.getCoef(false) < 0.5) sumAddFeature4 = sumAddFeature4 + thisBigramFreq;
		}
		double neighSize = (double)(w.left_of.size() + w.right_of.size());
		double neighbourCountCoefLeft = (double)w.left_of.size()/neighSize; //(double)((double)w.left_of.size()/(double)w.right_of.size());
		double neighbourCountCoefRight = (double)w.right_of.size()/neighSize;
		List<Collocation> colistleft = Words.getWordCollocations(false, w, 0.001, 5, 0.001,10);
		double averageLeft = 0.0;
		if(colistleft.size()>0) 
			averageLeft = colistleft.stream().mapToDouble(c->c.sim.left).average().getAsDouble();
		List<Collocation> colistr = Words.getWordCollocations(true, w, 0.001, 5, 0.001,10);
		double averageR = 0.0;
		if(colistr.size()>0) 
		averageR = colistr.stream().mapToDouble(c->c.sim.right).average().getAsDouble();
		list.add(averageLeft+averageR);
//		list.add(averageLeft+averageR);

		
//		list.add(averageR);
//		list.add(neighbourCountCoefLeft);
//		list.add(neighbourCountCoefRight);
		list.add(w.getCoef(true));
		list.add(w.getCoef(false));
		
		//		list.add(w.isLeftCollocWord(0.01));

//		//if word is Splitter
		
		list.add(MyUtils.rdouble((double)sumLeftwords / sumWords ) );
		list.add(MyUtils.rdouble((double)sumRihtwords / sumWords ) );
		list.add(MyUtils.rdouble((double)sumSplitter / sumWords ) );
		
		// right context is Predicat (not necessarily Splitter)
//		list.add(MyUtils.rdouble((double)sumPredicats2 / sumWords ) );
		//maybe adj
		list.add(MyUtils.rdouble((double)sumAdj / sumWords ) );
		//list.add(MyUtils.rdouble((double)sumContrAdj / sumWords ) );
					
		double valueUnkLeft = MyUtils.rdouble((double)sumUnknowns / sumWords ) ;
		
		//contexts to the Left
		sumLeftwords = 0;
		sumRihtwords = 0;
		sumSplitter = 0;
		 sumPredicats2 = 0;
		 sumAdj = 0;
		 sumContrAdj = 0;
		sumUnknowns = 0;
		sumWords = 0;
		
		//new feat exp
		sumAddFeature4 = 0;
		for(Word contextWord: w.right_of.keySet()) {
			//check only significant
			if((w.right_of.get(contextWord) / contextWord.freq()) < 0.001 && (w.right_of.get(contextWord) / w.freq()) < 0.001) continue;
//			if((w.right_of.get(contextWord) / contextWord.freq()) < 0.001) continue;
			
			double countBigrams = w.right_of.get(contextWord);
			sumWords = sumWords + countBigrams;
			if(Words.isLeftWord(contextWord)) sumLeftwords = sumLeftwords + countBigrams;
			else if(!Words.isLeftWord(contextWord)) sumRihtwords = sumRihtwords +  countBigrams;
			//how often this word follows sth like article or preposition or connector 
			if(contextWord.getCoef(true) > 0.95 && contextWord.getCoef(false) > 0.95) sumSplitter = sumSplitter +  countBigrams;
			
			else if(contextWord.getCoef(true) > 0.5 && contextWord.getCoef(false) > 0.5) sumPredicats2 = sumPredicats2 + countBigrams;
			//maybe ADJ
			if(contextWord.getCoef(true) > 0.6 && contextWord.getCoef(false) < 0.4) sumAdj = sumAdj +  countBigrams;
			if(contextWord.getCoef(false) > 0.6 && contextWord.getCoef(true) < 0.4) sumContrAdj = sumContrAdj +  countBigrams;

			//get context word coef to the right (left coef of the context word)			
			if(contextWord.getCoef(true) < 0.2 && contextWord.getCoef(false) < 0.2) sumUnknowns = sumUnknowns + countBigrams;
			else if(contextWord.getCoef(true) < 0.5 && contextWord.getCoef(false) < 0.5) sumAddFeature4 = sumAddFeature4 + countBigrams;

		}
		list.add(MyUtils.rdouble((double)sumLeftwords / sumWords ) );
		list.add(MyUtils.rdouble((double)sumRihtwords / sumWords ) );
		list.add(MyUtils.rdouble((double)sumSplitter / sumWords ) );
		
		
		list.add(valueUnkLeft); //unknowns left
		list.add(MyUtils.rdouble((double)sumUnknowns / sumWords ) ); //unknowns right

		// right context is Predicat (not necessarily Splitter)
//		list.add(MyUtils.rdouble((double)sumPredicats2 / sumWords ) );

		//maybe adj
		list.add(MyUtils.rdouble((double)sumAdj / sumWords ) );
		//		list.add(MyUtils.rdouble((double)sumContrAdj / sumWords ) );
					

		return list;

	}

	public static List<Double> collectFeaturesSmall(Word w, WordSequences model) {
		List<Double> list = new ArrayList<Double>();
		if(w.isSplitterLeftRight(freqOfAND ))
			list.add(1.0);
		else list.add(0.0);
		list.add(Words.getCollocation(model.getWord(Words.SPLITTER_WORD_LABEL), w).sim.left);
		list.add(Words.getCollocation(w, model.getWord(Words.SPLITTER_WORD_LABEL)).sim.right);
		list.add(w.getCoef(true));
		list.add(w.getCoef(false));
		list.add(w.isLeftCollocWord(0.01));
		
		return list;
	}
	
	//collect features
/* isLeftWord	isRightWord 
 * LContextIsLeft	LContextIsRight	
 * LContextIsSplitter	LContextIsPred	LContextIsAdj	LContextIsContrAdj	LContextLeftLowRigtLow
 * RContextIsLeft	RContextIsRight	RContextIsSplitter	
 * LConIsUnknown	RConIsUnknown	
 * RContextIsPred	RContextIsAdj	RContextIsContrAdj	RContextLeftLowRigtLow
 * LConHowBigIsGroup9	LConNoCon	LConBiggestGroup	RConHowBigIsGroup9	RConNoCon	RConBiggestGroup
 */
//	isLeftWord	isRightWord LConIsLeft	LConIsRight	LConIsSplitter	LConIsPred	LConIsAdj	LConIsContrAdj	LConLeftLowRigtLow	RConIsLeft	RConIsRight	RConIsSplitter	LConIsUnknown	RConIsUnknown	RConIsPred	RConIsAdj	RConIsContrAdj	RConLeftLowRigtLow	LConHowBigIsGroup9	LConNoCon	LConBiggestGroup	RConHowBigIsGroup9	RConNoCon	RConBiggestGroup
		public static List<Double> collectFeatures(Word w, WordSequences model) {
			double sumLeftwords = 0;
			double sumRihtwords = 0;
			double sumSplitter = 0;
			double sumUnknowns = 0;
			double sumWords = 0;
			
			//new features
			//double sumPredicats = 0;
			double sumPredicats2 = 0;
			double sumAdj = 0;
			double sumContrAdj = 0;
			
			//new features experiment
			double sumAddFeature4 = 0;
			
			List<Double> list = new ArrayList<Double>();
			for(Word contextWord: w.left_of.keySet()) {
				double thisBigramFreq = w.left_of.get(contextWord);
				//check only significant
				if((w.left_of.get(contextWord) / contextWord.freq()) < 0.001 && (w.left_of.get(contextWord) / w.freq()) < 0.001) continue;
//				if((w.left_of.get(contextWord) / contextWord.freq()) < 0.001) continue;
				sumWords = sumWords + thisBigramFreq;
				if(Words.isLeftWord(contextWord)) sumLeftwords = sumLeftwords +  thisBigramFreq;
				else sumRihtwords = sumRihtwords +  thisBigramFreq;
				//how often this word is followed by sth like article or preposition or connector 
				if(contextWord.getCoef(true) > 0.95 && contextWord.getCoef(false) > 0.95) sumSplitter = sumSplitter +  thisBigramFreq;
				
				//else if(contextWord.getCoef(true) > 0.8 && contextWord.getCoef(false) > 0.5) sumPredicats = sumPredicats + countBigramsLeft;
				else if(contextWord.getCoef(true) > 0.5 && contextWord.getCoef(false) > 0.5) sumPredicats2 = sumPredicats2 + thisBigramFreq;
				//maybe ADJ
				if(contextWord.getCoef(true) > 0.6 && contextWord.getCoef(false) < 0.4) sumAdj = sumAdj +  thisBigramFreq;
				if(contextWord.getCoef(false) > 0.6 && contextWord.getCoef(true) < 0.4) sumContrAdj = sumContrAdj +  thisBigramFreq;

				//get context word coef to the left (right coef)
				if(contextWord.getCoef(true) < 0.2 && contextWord.getCoef(false) < 0.2) sumUnknowns = sumUnknowns + thisBigramFreq;
				else if(contextWord.getCoef(true) < 0.5 && contextWord.getCoef(false) < 0.5) sumAddFeature4 = sumAddFeature4 + thisBigramFreq;
			}
			list.add(w.getCoef(true));
			list.add(w.getCoef(false));
			
			list.add(w.isLeftCollocWord(0.01));

//			//if word is Splitter
			
			list.add(MyUtils.rdouble((double)sumLeftwords / sumWords ) );
			list.add(MyUtils.rdouble((double)sumRihtwords / sumWords ) );
			list.add(MyUtils.rdouble((double)sumSplitter / sumWords ) );
			
			// right context is Predicat (not necessarily Splitter)
			list.add(MyUtils.rdouble((double)sumPredicats2 / sumWords ) );

			//maybe adj
			list.add(MyUtils.rdouble((double)sumAdj / sumWords ) );
			list.add(MyUtils.rdouble((double)sumContrAdj / sumWords ) );
			
			//new feat exp
			list.add(MyUtils.rdouble((double)sumAddFeature4 / sumWords ) );

			double valueUnkLeft = MyUtils.rdouble((double)sumUnknowns / sumWords ) ;
			
			//contexts to the Left
			sumLeftwords = 0;
			sumRihtwords = 0;
			sumSplitter = 0;
//			 sumPredicats = 0;
			 sumPredicats2 = 0;
			 sumAdj = 0;
			 sumContrAdj = 0;
			sumUnknowns = 0;
			sumWords = 0;
			
			//new feat exp
			sumAddFeature4 = 0;
			for(Word contextWord: w.right_of.keySet()) {
				//check only significant
				if((w.right_of.get(contextWord) / contextWord.freq()) < 0.001 && (w.right_of.get(contextWord) / w.freq()) < 0.001) continue;
//				if((w.right_of.get(contextWord) / contextWord.freq()) < 0.001) continue;
				
				double countBigrams = w.right_of.get(contextWord);
				sumWords = sumWords + countBigrams;
				if(Words.isLeftWord(contextWord)) sumLeftwords = sumLeftwords + countBigrams;
				else if(!Words.isLeftWord(contextWord)) sumRihtwords = sumRihtwords +  countBigrams;
				//how often this word follows sth like article or preposition or connector 
				if(contextWord.getCoef(true) > 0.95 && contextWord.getCoef(false) > 0.95) sumSplitter = sumSplitter +  countBigrams;
				
				else if(contextWord.getCoef(true) > 0.5 && contextWord.getCoef(false) > 0.5) sumPredicats2 = sumPredicats2 + countBigrams;
				//maybe ADJ
				if(contextWord.getCoef(true) > 0.6 && contextWord.getCoef(false) < 0.4) sumAdj = sumAdj +  countBigrams;
				if(contextWord.getCoef(false) > 0.6 && contextWord.getCoef(true) < 0.4) sumContrAdj = sumContrAdj +  countBigrams;

				//get context word coef to the right (left coef of the context word)			
				if(contextWord.getCoef(true) < 0.2 && contextWord.getCoef(false) < 0.2) sumUnknowns = sumUnknowns + countBigrams;
				else if(contextWord.getCoef(true) < 0.5 && contextWord.getCoef(false) < 0.5) sumAddFeature4 = sumAddFeature4 + countBigrams;

			}
			list.add(MyUtils.rdouble((double)sumLeftwords / sumWords ) );
			list.add(MyUtils.rdouble((double)sumRihtwords / sumWords ) );
			list.add(MyUtils.rdouble((double)sumSplitter / sumWords ) );
			
			
			list.add(valueUnkLeft); //unknowns left
			list.add(MyUtils.rdouble((double)sumUnknowns / sumWords ) ); //unknowns right
			
			// right context is Predicat (not necessarily Splitter)
			list.add(MyUtils.rdouble((double)sumPredicats2 / sumWords ) );

			//maybe adj
			list.add(MyUtils.rdouble((double)sumAdj / sumWords ) );
			list.add(MyUtils.rdouble((double)sumContrAdj / sumWords ) );

			list.add(MyUtils.rdouble((double)sumAddFeature4 / sumWords ) ); 

			
//			ukr: nearest sims kaputt: evtl. letzte Features weglassen
//			new: recompute clusters after some iterations (or after each)
			
//			1. add difference between 0.1 und 0.8 wieder > 0.5 done
//			2. similarity wieder auf 1.5, auch kleinere testen!
//			3. new feature: word_left oder word_right aber nicht mit 1/0
//			4. freq of word is a feature: check wona i pomahaje - tried, bad feature
//			5. a usual context of a word 1 is highly unlikely for the word 2: wona, pomahaje: win pomahaje - ok!, win wona - highly unlikely! - this is kind of tricky,
			// because usual context words - and, the, etc will be for most words - means need complicated heuristic
			
//			6. BUT - new feature - if two words are significant contexts to each other - they cannot be in same cluster!
			
			
			List<List<MyPair>> groups = Words.computeContextsWithSignif(w, true, Integer.MAX_VALUE, false);
			double rightContextCounts = w.left_of.size();
		
			if(groups.size() == 10 && rightContextCounts != 0) {
				list.add(groups.get(9).size()/rightContextCounts); //only the last group of the context stats
			} else list.add(0.0);
			
			double max = 0.0;
			double maxValue = 0.0;
			//find in which group are the most context words
			for (int i = 0; i < 4; i++) {
				if(groups.get(i).size() >= maxValue) {
					max = (double)i;
					maxValue = groups.get(i).size();
				}					
			}
			list.add((double)max/2.0);
			
			//same for if the word is RIGHT context
				groups = Words.computeContextsWithSignif(w, false, Integer.MAX_VALUE, false);
				double lefttContextCounts = w.right_of.size();
				if(groups.size() == 10 && lefttContextCounts != 0) {
					list.add(groups.get(9).size()/lefttContextCounts); //only the last group of the context stats
				} else list.add(0.0);
				
				max = 0.0;
				maxValue = 0.0;
				//find in which group are the most context words
				for (int i = 0; i < 4; i++) {
					if(groups.get(i).size() >= maxValue) {
						max = (double)i;
						maxValue = groups.get(i).size();
					}					
				}
				list.add((double)max/2.0);
				
				if(w.isSplitterLeftRight(freqOfAND ))
					list.add(1.0);
				else list.add(0.0);
				list.add(Words.getCollocation(model.getWord("s_splitter"), w).sim.left);
				list.add(Words.getCollocation(w, model.getWord("s_splitter")).sim.right);
			
				list.add(w.getVarOfAll(true, model.idx().words.size())) ; 
				list.add(w.getVarOfAll(false, model.idx().words.size())) ;
				
			return list;

		}
		
		//collect features
		/* isLeftWord	isRightWord 
		 * LContextIsLeft	LContextIsRight	
		 * LContextIsSplitter	LContextIsPred	LContextIsAdj	LContextIsContrAdj	LContextLeftLowRigtLow
		 * RContextIsLeft	RContextIsRight	RContextIsSplitter	
		 * LConIsUnknown	RConIsUnknown	
		 * RContextIsPred	RContextIsAdj	RContextIsContrAdj	RContextLeftLowRigtLow
		 * LConHowBigIsGroup9	LConNoCon	LConBiggestGroup	RConHowBigIsGroup9	RConNoCon	RConBiggestGroup
		 */
//			isLeftWord	isRightWord LConIsLeft	LConIsRight	LConIsSplitter	LConIsPred	LConIsAdj	LConIsContrAdj	LConLeftLowRigtLow	RConIsLeft	RConIsRight	RConIsSplitter	LConIsUnknown	RConIsUnknown	RConIsPred	RConIsAdj	RConIsContrAdj	RConLeftLowRigtLow	LConHowBigIsGroup9	LConNoCon	LConBiggestGroup	RConHowBigIsGroup9	RConNoCon	RConBiggestGroup
				public static List<Double> collectFeaturesOld(Word w) {
					double sumLeftwords = 0;
					double sumRihtwords = 0;
					double sumSplitter = 0;
					double sumUnknowns = 0;
					double sumWords = 0;
					
					//new features
					//double sumPredicats = 0;
					double sumPredicats2 = 0;
					double sumAdj = 0;
					double sumContrAdj = 0;
					
					//new features experiment
					double sumAddFeature4 = 0;
					
					List<Double> list = new ArrayList<Double>();
					for(Word contextWord: w.left_of.keySet()) {
						double thisBigramFreq = w.left_of.get(contextWord);
						//check only significant
						if((w.left_of.get(contextWord) / contextWord.freq()) < 0.001 && (w.left_of.get(contextWord) / w.freq()) < 0.001) continue;
//						if((w.left_of.get(contextWord) / contextWord.freq()) < 0.001) continue;
						sumWords = sumWords + thisBigramFreq;
						if(Words.isLeftWord(contextWord)) sumLeftwords = sumLeftwords +  thisBigramFreq;
						else sumRihtwords = sumRihtwords +  thisBigramFreq;
						//how often this word is followed by sth like article or preposition or connector 
						if(contextWord.getCoef(true) > 0.95 && contextWord.getCoef(false) > 0.95) sumSplitter = sumSplitter +  thisBigramFreq;
						
						//else if(contextWord.getCoef(true) > 0.8 && contextWord.getCoef(false) > 0.5) sumPredicats = sumPredicats + countBigramsLeft;
						else if(contextWord.getCoef(true) > 0.5 && contextWord.getCoef(false) > 0.5) sumPredicats2 = sumPredicats2 + thisBigramFreq;
						//maybe ADJ
						if(contextWord.getCoef(true) > 0.6 && contextWord.getCoef(false) < 0.4) sumAdj = sumAdj +  thisBigramFreq;
						if(contextWord.getCoef(false) > 0.6 && contextWord.getCoef(true) < 0.4) sumContrAdj = sumContrAdj +  thisBigramFreq;

						//get context word coef to the left (right coef)
						if(contextWord.getCoef(true) < 0.2 && contextWord.getCoef(false) < 0.2) sumUnknowns = sumUnknowns + thisBigramFreq;
						else if(contextWord.getCoef(true) < 0.5 && contextWord.getCoef(false) < 0.5) sumAddFeature4 = sumAddFeature4 + thisBigramFreq;
					}
					list.add(w.getCoef(true));
					list.add(w.getCoef(false));
//					//if word is Splitter
					
					list.add(MyUtils.rdouble((double)sumLeftwords / sumWords ) );
					list.add(MyUtils.rdouble((double)sumRihtwords / sumWords ) );
					list.add(MyUtils.rdouble((double)sumSplitter / sumWords ) );
					
					// right context is Predicat (not necessarily Splitter)
					list.add(MyUtils.rdouble((double)sumPredicats2 / sumWords ) );

					//maybe adj
					list.add(MyUtils.rdouble((double)sumAdj / sumWords ) );
					list.add(MyUtils.rdouble((double)sumContrAdj / sumWords ) );
					
					//new feat exp
					list.add(MyUtils.rdouble((double)sumAddFeature4 / sumWords ) );

					double valueUnkLeft = MyUtils.rdouble((double)sumUnknowns / sumWords ) ;
					
					//contexts to the Left
					sumLeftwords = 0;
					sumRihtwords = 0;
					sumSplitter = 0;
//					 sumPredicats = 0;
					 sumPredicats2 = 0;
					 sumAdj = 0;
					 sumContrAdj = 0;
					sumUnknowns = 0;
					sumWords = 0;
					
					//new feat exp
					sumAddFeature4 = 0;
					for(Word contextWord: w.right_of.keySet()) {
						//check only significant
						if((w.right_of.get(contextWord) / contextWord.freq()) < 0.001 && (w.right_of.get(contextWord) / w.freq()) < 0.001) continue;
//						if((w.right_of.get(contextWord) / contextWord.freq()) < 0.001) continue;
						
						double countBigrams = w.right_of.get(contextWord);
						sumWords = sumWords + countBigrams;
						if(Words.isLeftWord(contextWord)) sumLeftwords = sumLeftwords + countBigrams;
						else if(!Words.isLeftWord(contextWord)) sumRihtwords = sumRihtwords +  countBigrams;
						//how often this word follows sth like article or preposition or connector 
						if(contextWord.getCoef(true) > 0.95 && contextWord.getCoef(false) > 0.95) sumSplitter = sumSplitter +  countBigrams;
						
						else if(contextWord.getCoef(true) > 0.5 && contextWord.getCoef(false) > 0.5) sumPredicats2 = sumPredicats2 + countBigrams;
						//maybe ADJ
						if(contextWord.getCoef(true) > 0.6 && contextWord.getCoef(false) < 0.4) sumAdj = sumAdj +  countBigrams;
						if(contextWord.getCoef(false) > 0.6 && contextWord.getCoef(true) < 0.4) sumContrAdj = sumContrAdj +  countBigrams;

						//get context word coef to the right (left coef of the context word)			
						if(contextWord.getCoef(true) < 0.2 && contextWord.getCoef(false) < 0.2) sumUnknowns = sumUnknowns + countBigrams;
						else if(contextWord.getCoef(true) < 0.5 && contextWord.getCoef(false) < 0.5) sumAddFeature4 = sumAddFeature4 + countBigrams;

					}
					list.add(MyUtils.rdouble((double)sumLeftwords / sumWords ) );
					list.add(MyUtils.rdouble((double)sumRihtwords / sumWords ) );
					list.add(MyUtils.rdouble((double)sumSplitter / sumWords ) );
					
					
					list.add(valueUnkLeft); //unknowns left
					list.add(MyUtils.rdouble((double)sumUnknowns / sumWords ) ); //unknowns right
					
					// right context is Predicat (not necessarily Splitter)
					list.add(MyUtils.rdouble((double)sumPredicats2 / sumWords ) );

					//maybe adj
					list.add(MyUtils.rdouble((double)sumAdj / sumWords ) );
					list.add(MyUtils.rdouble((double)sumContrAdj / sumWords ) );

					list.add(MyUtils.rdouble((double)sumAddFeature4 / sumWords ) ); 

					
//					ukr: nearest sims kaputt: evtl. letzte Features weglassen
//					new: recompute clusters after some iterations (or after each)
					
//					1. add difference between 0.1 und 0.8 wieder > 0.5 done
//					2. similarity wieder auf 1.5, auch kleinere testen!
//					3. new feature: word_left oder word_right aber nicht mit 1/0
//					4. freq of word is a feature: check wona i pomahaje - tried, bad feature
//					5. a usual context of a word 1 is highly unlikely for the word 2: wona, pomahaje: win pomahaje - ok!, win wona - highly unlikely! - this is kind of tricky,
					// because usual context words - and, the, etc will be for most words - means need complicated heuristic
					
//					6. BUT - new feature - if two words are significant contexts to each other - they cannot be in same cluster!
					
					
					List<List<MyPair>> groups = Words.computeContextsWithSignif(w, true, Integer.MAX_VALUE, false);
					double rightContextCounts = w.left_of.size();
				
					if(groups.size() == 10 && rightContextCounts != 0) {
						list.add(groups.get(9).size()/rightContextCounts); //only the last group of the context stats
					} else list.add(0.0);
					
					double max = 0.0;
					double maxValue = 0.0;
					//find in which group are the most context words
					for (int i = 0; i < 4; i++) {
						if(groups.get(i).size() >= maxValue) {
							max = (double)i;
							maxValue = groups.get(i).size();
						}					
					}
					list.add((double)max/2.0);
					
					//same for if the word is RIGHT context
						groups = Words.computeContextsWithSignif(w, false, Integer.MAX_VALUE, false);
						double lefttContextCounts = w.right_of.size();
						if(groups.size() == 10 && lefttContextCounts != 0) {
							list.add(groups.get(9).size()/lefttContextCounts); //only the last group of the context stats
						} else list.add(0.0);
						
						max = 0.0;
						maxValue = 0.0;
						//find in which group are the most context words
						for (int i = 0; i < 4; i++) {
							if(groups.get(i).size() >= maxValue) {
								max = (double)i;
								maxValue = groups.get(i).size();
							}					
						}
						list.add((double)max/2.0);
					
					return list;

				}
		
		public static List<Word> getMostFrequestWordsForTraining(WordSequences wsmodel, int max, int start, String splitterOnly) {
			List<Word> wordsToCluster = ListOps.of();
			int i = 0;
			max = start + max;
			for(Word w: wsmodel.idx().getSortedWords()) {
		         if(w.toString().contains("_")) continue;
//				if(w.freq() >2000) continue;
				//clusters only splitters
				if(splitterOnly.equals(SyntModel.SPLITTER_LEFTRIGHT)) {
					if(!w.isSplitterLeftRight(wsmodel.getFreqOfAnd()) || w.toString().matches("[bmsf]_.+")) continue;
				}
				if(splitterOnly.equals(SyntModel.SPLITTER_PRED_LEFT)) {
					if(!w.isPredicativeLeft(wsmodel.getFreqOfAnd()) || w.toString().matches("[bmsf]_.+")) continue;
				}
				if(splitterOnly.equals(SyntModel.SPLITTER_PRED_RIGHT)) {
					if(!w.isPredicativeRight(wsmodel.getFreqOfAnd()) || w.toString().matches("[bmsf]_.+")) continue;
				}
				i++;
				if(i < start) continue;

				if(Words.isStopword(w.toString())) continue; //AAA, EEE; ZZZ, doctitle
				// filter out uppercase words at the beginning of the sentence
				if(!w.toString().equals(w.toString().toLowerCase()) 
						&& wsmodel.idx().words.containsKey(w.toString().toLowerCase())
						&& w.freq() < wsmodel.getWord(w.toString().toLowerCase()).freq()) 
					continue; 
				//if only one uppercase letter: A, O
				if(w.toString().matches("(\\p{Lu})|[mbsp]_.+")) continue; 
				wordsToCluster.add(w);
//				if(i>max-20 ) System.out.println(w.toString() + "\t" + w.freq());
				if(i > max) break;
			}
			System.out.println(wordsToCluster);
			return wordsToCluster;
		}
		
		/**
		 * Cluster = ListOfPair, cluster add = cluster1MeanVector - cluster2MeanVector.
		 * ClusterMeanVector = (a1+a2+a3) / 3 and so on
		 * @param wordsValues
		 * @return
		 * @throws IOException 
		 */
		
		static List<Cluster> analyzeWordListToCluster(List<Pair<String,Vector>> wordsValues, WordSequences model, 
				boolean print, String splitteronly) throws IOException {
			List<Cluster> clusterList = new ArrayList<>();
			for(Pair<String,Vector> p: wordsValues) {
				Cluster c = new Cluster(p);
				if(Cluster.morphParNum != 0) //add morph par as feature punishment later
					c.morphPar = model.getWord(p.getKey()).getMorphLabelNotNull();
				clusterList.add(c);
			}
			double initialSim = 0.6;
			double simStep = 0.1;
//			setBestClusters(clusterList, 2.0, false);
			setBestClusters(clusterList, 1.0, false);
			
			List<Cluster> newClusterList = fillClusters(clusterList, 1);
			//write clusters with new members
			if(print) {
				for(Cluster c: newClusterList) {
					System.out.println(c.toString());
				}
				System.out.println("Runde 1: " + newClusterList.size());
			}

			if(!splitteronly.equals(SyntModel.SPLITTER_PRED_LEFT))
				newClusterList = doSomeMoreRounds(newClusterList, print, splitteronly);
//			doSomeMoreRounds2(newClusterList, initialSim, simStep);

			
			return newClusterList;
		}

		/**
				 * Cluster = ListOfPair, cluster add = cluster1MeanVector - cluster2MeanVector.
				 * ClusterMeanVector = (a1+a2+a3) / 3 and so on
				 * @param wordsValues
				 * @return
				 * @throws IOException 
				 */
		@Deprecated
				static List<List<Cluster>> analyzeWordListToCluster(List<Pair<String,Vector>> wordsValues, WordSequences wsmodel, 
						double inputMinClusterSim) throws IOException {
					List<List<Cluster>> output = new ArrayList<>();
					Set<Integer> increased = new HashSet<>(); //contains Cluster, which got new members
					Set<Integer> wasAdded = new HashSet<>(); //contains Clusters which were added to the increased
		
					//init clusterlist
					List<Cluster> clusterList = new ArrayList<>();
					
					for(Pair<String,Vector> p: wordsValues)
						clusterList.add(new Cluster(p));
					
					int iteration = 0;
					int lastIteration = 6;
					double minSim = Double.MAX_VALUE;;
					double minMaxSim = 0.0;
					//ii = iterations
					while (iteration < lastIteration) {
		
						List<Cluster> clusterListNew = new ArrayList<>();
						// get all clusters into new list
						for (Cluster c : clusterList)
							clusterListNew.add(c);
		
						increased = new HashSet<>();
						wasAdded = new HashSet<>();
		
						minSim = Double.MAX_VALUE;
						minMaxSim = 0.0;
						for (int i = 0; i < clusterListNew.size(); i++) {
							if (increased.contains(i))
								continue; // means some previous cluster already found this cluster as its nearest
							int bestSimilarityCluster = -1;
							int secondBestSimilarityCluster = -1;
							double minNewSimToClusterSim = Double.MAX_VALUE;
							double minClusterSim = inputMinClusterSim ; //Double.MAX_VALUE;
							Cluster clusterInCheck = clusterListNew.get(i);
							for (int j = 0; j < clusterListNew.size(); j++) {
								if (i == j)
									continue;
								Cluster basisCluster = clusterListNew.get(j);
								if (wasAdded.contains(j))
									continue; // means this cluster is already in another one, dont check it again
								double sim = Cluster.computeSimilarity(clusterInCheck, basisCluster);
								double clusterAcceptsSim = 1.5;
		//						if (iteration > 3)						clusterAcceptsSim = 2.0;
		
		//						boolean basisCLusterAccepts = clusterAccepts(clusterInCheck, basisCluster, sim, clusterAcceptsSim,
		//								iteration, wsmodel);
								boolean basisCLusterAccepts = true;
								minSim = Math.min(minSim, sim);
								if (sim < minClusterSim) {
									minMaxSim = Math.max(minMaxSim, sim);
								}
		
								if (basisCLusterAccepts && sim < minClusterSim) {
									minClusterSim = sim;
									bestSimilarityCluster = j;
								}
								// just for exploring tests
								else if (sim < minNewSimToClusterSim) {
									// look which clusterAcceptsSim would accept this cluster
									minNewSimToClusterSim = Math.min(minNewSimToClusterSim,
											Math.max(sim / basisCluster.getSim(), sim / clusterInCheck.getSim()));
									secondBestSimilarityCluster = j;
								}
								// ende tests exploring
							}
							if (bestSimilarityCluster != -1) {
		//						System.out
		//								.println("MIN Similarity: " + minClusterSim + "\t für Cluster: " + clusterInCheck.toString()
		//										+ "\t into cluster:  " + clusterListNew.get(bestSimilarityCluster).toString());
		
								// means add clusterInCheck to the found nearest cluster
								// System.out.println(clusterListNew.get(bestSimilarityCluster));
								// System.out.println(clusterInCheck);
								clusterListNew.get(bestSimilarityCluster).addCluster(clusterInCheck);
								increased.add(bestSimilarityCluster);
								wasAdded.add(i);
							} else if(secondBestSimilarityCluster > -1){
								increased.add(i);
		//						System.out.println("CLUSTER NOT ADDED: " + clusterInCheck.toString()
		//								+ "\t best clusterAcceptsSim would be: " + MyUtils.rdouble(minNewSimToClusterSim)
		//								+ clusterListNew.get(secondBestSimilarityCluster) != null ? clusterListNew.get(secondBestSimilarityCluster).toString():"");
							}
							
						}
						// do output for iteration(circle)
						// save clusters which numbers are in added (means this clusters got other
						// clusters as nearest)
						clusterList = new ArrayList<>();
						for (int i = 0; i < clusterListNew.size(); i++) {
							if (increased.contains(i))
								clusterList.add(clusterListNew.get(i));
						}
						clusterList = recomputeClusters(clusterList);
						
						doIterationOutput(clusterList, minSim, minMaxSim, iteration, iteration == lastIteration-1, wsmodel);
						
		//				recomputeClusters(clusterList,iteration);
						
						
						
						iteration++;
						output.add(clusterList);
//						outputClusterCentroides(outClusters, clusterList);
					}
					return output;
				}


		private static List<Cluster> doSomeMoreRounds(List<Cluster> newClusterList, boolean print, String splitteronly) {
			//2 round
			int stop = 50;
			//make new sim matrix for clusters with several members
			if(print)System.out.println("\n\n\nRunde 2\n\n\n");
			double thhMain = 1.0;
			newClusterList = trainClusters(newClusterList, thhMain, 2, print);
			if(print)System.out.println("Runde 2: " + newClusterList.size());
			//3 round
			//make new sim matrix for clusters with several members
			int round = 3;
			while(true) {
				int oldClusterNumber = newClusterList.size();
				newClusterList = moreRoundsRoutine(newClusterList, thhMain, round, print); // !!! produktiv für splitters
				int newCnumber = newClusterList.size();
				round++;
				double newthh = thhMain;
				while(round < 20 && newCnumber > 2 && newCnumber == oldClusterNumber) {
					double step = splitteronly.startsWith(SyntModel.SPLITTER_PREFIX) ? 0.05 : 0.2;
 					newthh = newthh + step;
					newClusterList = moreRoundsRoutine(newClusterList, newthh, round, print);
					newCnumber = newClusterList.size();
					round++;
					if(splitteronly.startsWith(SyntModel.SPLITTER_PREFIX)) break;
				}
				if(round > 20 || newCnumber < 3 || newCnumber == oldClusterNumber)
					break;
			}

//			//4 round
//			//make new sim matrix for clusters with several members
//			round = 4;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print); // !!! produktiv für splitters
//
//			//5 round
//			//make new sim matrix for clusters with several members
//			round = 5;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//
//			//6 round
//			//make new sim matrix for clusters with several members
//			round = 6;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 7;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 8;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//
//			round = 9;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 10;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 11;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 12;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 13;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 14;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 15;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			
//			round = 16;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			round = 17;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			round = 18;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			round = 19;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
//			round = 20;
//			newClusterList = moreRoundsRoutine(newClusterList, 1.0, round, print);
			
			return newClusterList;

		}
		private static List<Cluster> moreRoundsRoutine(List<Cluster> inClusterList,double minSim,  int round, boolean print) {
			//System.out.println("\n\n\nRunde "  + round + "\n\n\n");
			if(inClusterList.size() < 3) return inClusterList;
			List<Cluster> oldClusterList = saveClusters(inClusterList);
			List<Cluster> newClusterList = trainClusters(inClusterList, minSim, round, print);
			System.out.println("Runde " + round + ": " + newClusterList.size());
			if(newClusterList.size() < 2) 
				return oldClusterList;
			return newClusterList;
		}
		private static List<Cluster> saveClusters(List<Cluster> inClusterList) {
			List<Cluster> oldClusterList = new ArrayList<>();
			for(Cluster c: inClusterList) {
				List<Pair<String,Vector>> newlist = new ArrayList<>();
				for(Pair<String,Vector> p: c.getWordValues()) newlist.add(p);
				Cluster saved = new Cluster(newlist);
				oldClusterList.add(saved);
			}
			return oldClusterList;
		}


		private static List<Cluster> trainClusters(List<Cluster> newClusterList, double thh, int roundNumber, boolean print) {

			setBestClusters(newClusterList, thh, false);
			//add best cluster which don't have better sim and combined similarity < 1.0
			//filter out clusters which were not added
			newClusterList = fillClusters(newClusterList, roundNumber);	
			if(print) 	for(Cluster c: newClusterList) 	System.out.println(MyUtils.rdouble(c.getSim()) + "\t" + c.toString()
			+ "\t" + c.getBestClusters().toString());

			return newClusterList;
		}
		
		private static List<Cluster> fillClusters(List<Cluster> clusterList, int roundNumber) {
			Collections.sort(clusterList, (c1, c2) -> Double.compare(c1.getBestClusterSim(),c2.getBestClusterSim() ) );
			List<Cluster> newClusterList = new ArrayList<>();
			Map<Cluster,Cluster> wereAdded = new HashMap<>();
			for(Cluster currentCluster: clusterList) {
				if(wereAdded.containsKey(currentCluster)) continue;
				List<Pair<Cluster,Double>> best = currentCluster.getBestClusters();
				boolean newMemberFound = false;
				//save clusters without best
//				if(best.size() == 0 && currentCluster.getSim() > 0) newMemberFound = true; //means has no best but is longer than 1 member
				for(Pair<Cluster,Double> p: best) {
					Cluster bestC = p.getKey();
//					if(wereAdded.containsKey(bestC)) continue;
					double currentBestSim = p.getValue();
					double bestSimOfbestCluster = bestC.getBestClusters().size() != 0 ? bestC.getBestClusters().get(0).getValue() : Double.MAX_VALUE; //checks if the bestC has a better similarity with other cluster
					if(bestSimOfbestCluster >= currentBestSim) {
						currentCluster.addCluster(bestC);
						newMemberFound = true;
						wereAdded.put(bestC, currentCluster);
//						if(roundNumber > 1) 
							break;
					}else if( !newMemberFound && wereAdded.containsKey(bestC) && roundNumber < 2){
						newMemberFound = false;
						wereAdded.get(bestC).addCluster(currentCluster);
						wereAdded.put(currentCluster, wereAdded.get(bestC));
						break;
					}
				}
				if(newMemberFound) {
					wereAdded.put(currentCluster,currentCluster);
					newClusterList.add(currentCluster);
				}
//				else if(clusterList.size() < 51 && !wereAdded.containsKey(currentCluster))
//				else if(currentCluster.size() > roundNumber)
//					newClusterList.add(currentCluster);
				
				
			}
			for(Cluster c: clusterList) { //sammelt fertige Klassen
				if(!wereAdded.containsKey(c) && c.getWordValues().size() > 1)
					newClusterList.add(c);
			}

			return newClusterList;
		}
		private static void setBestClusters(List<Cluster> cluster_index,
				double simthh, boolean print) {
			// init sim_matrix
			for (int idx_1 = 0; idx_1 < cluster_index.size(); idx_1++) {
				Cluster current = cluster_index.get(idx_1);
				List<Pair<Cluster, Double>> bestClusters = computeBestClusters(cluster_index, simthh, idx_1, current);
				current.setBestClusters(bestClusters);
				if(print)
				System.out.println(current.toString() + "\tBEST:\t" + bestClusters.toString());
//				if(bestClusters.size() == 0 && current.bestClustersBackup != null)
//					System.out.println(current.toString() + "\tBEST BACKUP:\t" + current.bestClustersBackup.toString());
					
			}
		}
		public static List<Pair<Cluster, Double>> computeBestClusters(List<Cluster> cluster_index, double simthh,
				int idx_1, Cluster current) {
			Map<Double,Cluster> sim_cluster_matrix = getSimMatrix(cluster_index, current, idx_1);
			List<Pair<Cluster,Double>> bestClusters = getBestClusters(sim_cluster_matrix, simthh, 10, current);
			return bestClusters;
		}
		
		private static Map<Double,Cluster> getSimMatrix(List<Cluster> cluster_index, Cluster current, int idx_of_current){
			Map<Double,Cluster> sim_cluster_matrix = new HashMap<>();
			
			
			for (int idx_2 = 0; idx_2 < cluster_index.size(); idx_2++) {
				if(idx_of_current == idx_2) continue;
				Cluster clusterToTest = cluster_index.get(idx_2);
				if(clusterToTest.toString().equals(current.toString())) continue;
				double sim = Cluster.computeSimilarity(current, clusterToTest);
				if(sim_cluster_matrix.containsKey(sim))
					sim = getSlightlyDifferentKexForMatrix(sim);
				sim_cluster_matrix.put(sim, clusterToTest); //getWordValues().get(0).getKey())
			}
			return sim_cluster_matrix;
		}
		
		private static List<Pair<Cluster,Double>> getBestClusters(Map<Double,Cluster> sim_cluster_matrix, 
				 double simthh, int howmany, Cluster current) {
			List<Double> simList = ListOps.of(sim_cluster_matrix.keySet());
			Collections.sort(simList);
			List<Pair<Cluster,Double>> bestClusters = new ArrayList<>();
			for (int i = 0; i < Math.min(howmany, simList.size()); i++) {
				double sim = simList.get(i);
				if(sim > simthh ) {
					current.bestClustersBackup = new Pair(sim_cluster_matrix.get(sim),sim);
					break; //if best cluster similarity > thh, add only one best cluster
				}
				Cluster bestCluster = sim_cluster_matrix.get(sim); 
				bestClusters.add(new Pair<Cluster, Double>(bestCluster,sim));
			}
			return bestClusters;
		}
		
		public static double getSlightlyDifferentKexForMatrix(double sim) {

			return Double.parseDouble(Double.toString(sim)+"1");
		}


		private static List<Cluster> recomputeClusters(List<Cluster> clusterList) {
			
			List<Cluster> targetClusterList = new ArrayList<Cluster>();
			for(Cluster c: clusterList) targetClusterList.add(new Cluster(c.getWordValues(), c.getCentroide()));
			
			for (int i = 0; i < clusterList.size(); i++) {
				Cluster clusterInCheck = clusterList.get(i);
				int bestClusterInt = -1;
				for(Pair<String,Vector> pairInCheck: clusterInCheck.getWordValues()) {
					String wInCheck = pairInCheck.getKey();
					Vector vectorInCheck = pairInCheck.getValue();
					double minSim = clusterInCheck.getSim();

					for (int j = 0; j < clusterList.size(); j++) {
						Cluster currentCLuster = clusterList.get(j);
						double newSim = Cluster.computeSimilarity(new Cluster(pairInCheck), currentCLuster);
						if (newSim < minSim) {
							minSim = newSim;
							bestClusterInt = j;
						}
					}
					if (bestClusterInt > -1) {
						if (bestClusterInt != i){
							//add to the new cluster, remove in old
							targetClusterList.get(bestClusterInt).addWord(wInCheck, vectorInCheck);
							targetClusterList.get(i).removeWord(wInCheck);
//							System.out.println("MOVED:\t" + wInCheck + "\t-->:\t" + targetClusterList.get(bestClusterInt).toString() + 
//									"\tFROM:\t" + targetClusterList.get(i).toString());
						}
					}
//					else 	System.out.println("WRONG: "+ clusterInCheck.toString());
				}
				
			}
			
			clusterList = new ArrayList<Cluster>();
			for(Cluster c: targetClusterList) {
				if(c.getWordValues().size() > 0) clusterList.add(c);
			}
			
			return clusterList;
		}
		
		
		
		private static void doIterationOutput(List<Cluster> clusterList, double minSim, double maxMinSim, int iteration, boolean lastIteration, WordSequences wsmodel) {
			System.out.println("ITERATION:" + iteration);
			System.out.println("CLUSTERS SIZE: " + clusterList.size());
			System.out.println("MIN SIM: " + minSim);
			System.out.println("MAX SIM: " + maxMinSim);

			int ii = 0;
			for(Cluster c: clusterList) {
				if (ii >300 && iteration >1 && iteration < 5) break;
				if(iteration < 2)
					System.out.println(c.toStringAll());
				else 
					System.out.println(c.toString() + "\t" + MyUtils.rdouble(c.getSim()));
				ii++;
			}
			
			if(lastIteration) {
				System.out.println("\n\nITERATION SORTED");
				for(Cluster c: clusterList) {
					for(String s: c.toStringAlphabet()) {
						if(Words.isLeftWord(wsmodel.getWord(s)))
							System.out.print(s + "_ , ");
						else if(!Words.isLeftWord(wsmodel.getWord(s)))
							System.out.print("_"+ s + " , ");
						else
							System.out.print(s + " , ");
					}
					System.out.println();
				}
			}
			
			System.out.println("\n\n");
		}
		
		private static void outputClusterCentroides(Writer outClusters, List<Cluster> clusterList) throws IOException {
			outClusters.write("\n");
			for(Cluster c: clusterList) {
				outClusters.write(c.toStringShort(50) + "\n");
				outClusters.write(c.computeCentroide().toString() + "\n");
				outClusters.write(c.getMedianCentroide().toString() + "\n");
			}
			outClusters.flush();
		}
		
		public static void main(String[] args) throws IOException {
//			mainDeCluster();
			mainUkrCluster();
		}
		
		private static void mainDeCluster() throws IOException {
//			WordSequences wsmodel = BuildWordSequenceModelDE.getWSmodelDe(400000, false);
			WordSequences wsmodel = BuildWordSequenceModelDE.getWSModel("de", new String[] {"news", "wiki"}, 300000, false,1);

//			WordSequences wsmodel = BuildWordSequenceModelDE.getBigModel(null);
//			outClusters.write("Clusters for GERMAN\n");
			trainClusters(wsmodel, 10000, 0, true, "noSplitter"); //3000
		}
		
		private static void mainUkrCluster() throws IOException {
			WordSequences wsmodel = BuildWordSequenceModelUKR.getWSModel("ukr", new String[] {"wiki", "news"}, 400000, false,1);
//			WordSequences wsmodel = BuildWordSequenceModelUKR.getWSModel("ukr", new String[] {"news"}, 800000, false);

//			outClusters.write("Clusters for UKR\n");
			List<Cluster> output = trainClusters(wsmodel, 15000, 0, false, "noSplitter"); //5000
			wsmodel.idx().setSyntPars(output, SyntModel.PARADIGM_PREF);
			for(Cluster c: output) 	System.out.println(c.getLabel() + "\t" + MyUtils.rdouble(c.getSim()) + "\t" + c.toString());
			for(Cluster c: output) {
				for(String wString: c.getWords()) {
					Word w = wsmodel.getWord(wString);
					w.setCluster(c);
					Word clusterword = wsmodel.getWord(c.getLabel());
					clusterword.addFreq();
				}
			}
			BuildWordSequenceModel.getInput(wsmodel, null, null, null);
		}
	
}
