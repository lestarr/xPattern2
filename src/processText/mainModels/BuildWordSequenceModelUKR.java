package processText.mainModels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import experiment.FindPhrases;
import lucene.Search;
import model.LetterTokModel;
import model.MorphVectorModel;
import model.SyntModel;
import model.WordSequences;
import model.Words;
import modelparts.Collocation;
import modelparts.CollocationCollection;
import modelparts.Collocations;
import modelparts.Word;
import modeltrain.MorphAnalyzer;
import util.CorpusUtils;
import util.MapsOps;
import util.MyPair;

public class BuildWordSequenceModelUKR extends BuildWordSequenceModel{

  private static WordSequences mainGetModelWithClusters(boolean getMorphClusters, boolean getSyntClusters
      , LetterTokModel ltmodel) throws IOException {
  long starttime = System.nanoTime();

  Search search = new Search("out/index/indexUKR");
  //load base model
  String lang = "ukr";
  int howmany = 40000*2;
  int start = 1;
  String[] corpora = new String[] {"news", "wiki"};
  
  WordSequences wsmodel = BuildWordSequenceModelUKR.getWSModel(lang, corpora, howmany, false, start);
  
  Words.addSplitterWordStats(wsmodel);
  String syntModelPath = "model/synt/ukr-splitter-left.model";

  SyntModel.PARADIGM_PREF = "x_";
  SyntModel spsplitter = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
//  spsplitter.train(wsmodel);
//  spsplitter.addParInfoIntoModel(wsmodel, false, true, "", false); //write Labels
//  spsplitter.trainVectorClusters(wsmodel, 300, 10, 6.0, 11.0, 5);
  
  String modelVectorMorphPath = "model/morph/ukr-morphVector-newVectorScores-highThh.model";
//  String modelVectorMorphPath = "model/morph/ukr-morphVector.model";
  MorphVectorModel mpv = new MorphVectorModel(1, "MorphPar1");
  mpv.setLetterTokModel(ltmodel);
//  mpv.train(wsmodel);
//   mpv.saveModel(modelVectorMorphPath, wsmodel);
  mpv.loadModel(modelVectorMorphPath, wsmodel, 20, 13);

  System.out.println("KNOWN: " + wsmodel.idx().knownParadigmLabels);
  wsmodel.idx().deletedParadigmLabels.addAll(wsmodel.idx().knownParadigmLabels);
  wsmodel.idx().knownParadigmLabels.clear();
  wsmodel.idx().syntPars().clear();
  SyntModel.PARADIGM_PREF = "z_";
  spsplitter = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
  spsplitter.train(wsmodel);
  spsplitter.addParInfoIntoModel(wsmodel, false, true, "", false); //write Labels
//  spsplitter.trainVectorClusters(wsmodel, 300, 10, 6.0, 11.0, 5);

    SyntModel.PARADIGM_PREF = "y_";
    SyntModel spsplitterL = new SyntModel(3, "SyntPar2", 750, 200, SyntModel.SPLITTER_PRED_LEFT);
  spsplitterL.train(wsmodel);
  spsplitterL.addParInfoIntoModel(wsmodel, false, false, "", true); //write Labels
  spsplitterL.trainVectorClusters(wsmodel, 5000, 40, 15.0, 15.0, 20);
//  spsplitterL.trainVectorClusters(wsmodel, 5000, 40, 20.0, 20.0, 20);
  
  
//        Words.addPhraseWordStats(wsmodel, 0.01); // s_left, s_right ...
//        addPhrases(wsmodel, corpora, start, howmany);
  
//    wsmodel.idx().deletedParadigmLabels.addAll(wsmodel.idx().knownParadigmLabels);
//    wsmodel.idx().knownParadigmLabels.clear();
  
  
  //semantics
  String modelsempath = "model/sem/ukr-sem.model";
//        TestIdeaPred.findSynParadigms(wsmodel, 5, 10000, 20,Double.MAX_VALUE,modelsempath) ;
//        TestIdeaPred.loadSynParadigms(wsmodel, modelsempath);


  
//  System.out.println("KNOWN: " + wsmodel.idx().knownParadigmLabels);
//  System.out.println("DELETED: " + wsmodel.idx().deletedParadigmLabels);
//
  BuildWordSequenceModel.printParadigmExpectations(wsmodel);
//  BuildWordSequenceModel.printParadigmAssociations(wsmodel);

   long endtime = System.nanoTime();
   double time = (double)(endtime-starttime)/1000000000.0;
  System.out.println("\nprocess time:\t"+(time)+"\tsec\t" + (time / 60.0) + "\tmin");
   
   
  return wsmodel;
}

	private static WordSequences mainGetModelWithClustersOld(boolean getMorphClusters, boolean getSyntClusters
  			, LetterTokModel ltmodel) throws IOException {
  		long starttime = System.nanoTime();
  
  		Search search = new Search("out/index/indexUKR");
  		//load base model
  		String lang = "ukr";
  		int howmany = 40000*2;
  		int start = 1;
  		String[] corpora = new String[] {"news", "wiki"};
  		
  		WordSequences wsmodel = BuildWordSequenceModelUKR.getWSModel(lang, corpora, howmany, false, start);
  		
  		Words.addSplitterWordStats(wsmodel);
        String syntModelPath = "model/synt/ukr-splitter-left.model";

  		SyntModel.PARADIGM_PREF = "x_";
  		SyntModel spsplitter = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
  		spsplitter.train(wsmodel);
  		spsplitter.addParInfoIntoModel(wsmodel, false, true, "", false); //write Labels
  		spsplitter.trainVectorClusters(wsmodel, 300, 10, 6.0, 11.0, 5);
        
  		String modelVectorMorphPath = "model/morph/ukr-morphVector-RecursionFromAbove-scaleCheckAllWords-parSimThhScaled-2.model";
        MorphVectorModel mpv = new MorphVectorModel(1, "MorphPar1");
        mpv.setLetterTokModel(ltmodel);
        mpv.train(wsmodel);
        mpv.saveModel(modelVectorMorphPath, wsmodel);
        
//        wsmodel.idx().knownParadigmLabels.clear();
//        wsmodel.idx().syntPars().clear();
//        SyntModel.PARADIGM_PREF = "z_";
//        spsplitter = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
//        spsplitter.train(wsmodel);
//        spsplitter.addParInfoIntoModel(wsmodel, false, true, ""); //write Labels
//        spsplitter.trainVectorClusters(wsmodel, 300, 10, 6.0, 11.0, 5);
//
//  		SyntModel.PARADIGM_PREF = "y_";
//  		SyntModel spsplitterL = new SyntModel(3, "SyntPar2", 750, 200, SyntModel.SPLITTER_PRED_LEFT);
//        spsplitterL.train(wsmodel);
//        spsplitterL.addParInfoIntoModel(wsmodel, false, false, ""); //write Labels
//        spsplitterL.trainVectorClusters(wsmodel, 10000, 40, 9.0, 11.0, 20);
  		

  		

  //        Words.addPhraseWordStats(wsmodel, 0.01); // s_left, s_right ...
  //        addPhrases(wsmodel, corpora, start, howmany);
        
//  		wsmodel.idx().deletedParadigmLabels.addAll(wsmodel.idx().knownParadigmLabels);
//  		wsmodel.idx().knownParadigmLabels.clear();
  		
  		/*
        SyntModel.PARADIGM_PREF = "x_";
        SyntModel  spsplitterL = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
        spsplitterL.train(wsmodel);
        spsplitterL.addParInfoIntoModel(wsmodel, false, true, ""); //write Labels
        spsplitterL = new SyntModel(3, "SyntPar2", 750, 200, SyntModel.SPLITTER_PRED_LEFT);
  		spsplitterL.train(wsmodel);
  		spsplitterL.addParInfoIntoModel(wsmodel, false, false, ""); //write Labels
  		spsplitterL.trainVectorClusters(wsmodel, 10000, 20, 7.0, 6.0, 20);
  		
*/
  		
  		
  //		spsplitterL = new SyntModel(3, "SyntPar2", 300, 200, SyntModel.SPLITTER_PRED_LEFT);
  //		spsplitterL.train(wsmodel);
  //		spsplitterL.addParInfoIntoModel(wsmodel, "m_", false); //write Labels
  		
  //		spsplitterL.tagWithVectors(wsmodel, 500, 5.0, true);
  //		spsplitterL.trainVectorClusters(wsmodel, 1500)
//  		System.exit(0);
  
       /* 
  		//cluster splitters
  		SyntModel sps = new SyntModel(2, "SyntPar1", 3000, 0, SyntModel.SPLITTER_LEFTRIGHT);
  		String syntModelPath = "model/synt/ukr-splitter.model";
  //		sps.train(wsmodel);
  //		sps.saveModel(wsmodel, syntModelPath);
  		sps.loadModel(wsmodel, syntModelPath);
  		sps.tagTrained(wsmodel);
  		sps.addParInfoIntoModel(wsmodel, false, false, "m_"); //write Labels
  		
        String modelVectorMorphPath = "model/morph/ukr-morphVector-RecursionFromAbove-scaleCheckAllWords-parSimThhScaled.model";
        MorphVectorModel mpv = new MorphVectorModel(1, "MorphPar1");
        mpv.setLetterTokModel(ltmodel);
        mpv.train(wsmodel);
        mpv.saveModel(modelVectorMorphPath, wsmodel);
  //        Words.addPhraseWordStats(wsmodel, 0.01); // s_left, s_right ...
  //        addPhrases(wsmodel, corpora, start, howmany);
  		*/
  		
  		//semantics
  		String modelsempath = "model/sem/ukr-sem.model";
  //		TestIdeaPred.findSynParadigms(wsmodel, 5, 10000, 20,Double.MAX_VALUE,modelsempath) ;
  //		TestIdeaPred.loadSynParadigms(wsmodel, modelsempath);
  
  /*
  		if(getMorphClusters) {
  			String modelMorphPath = "model/morph/ukr-morph.new.model";
  			
  			
  			//train MorphPar, 		//WSmodel.hasMorphanbiet
  			MorphModel mp = new MorphModel(1, "MorphPar1");
  			mp.setThhFreqForTrain(400); // 200 ist 16 min, Ergebnis wie bei 100, 300 = 12 min, Ergebnis gleich, 400 = 10 min, ERG-auch ok
  			mp.setLetterTokModel(ltmodel);
  //			mp.train(wsmodel);
  //			mp.saveModel(wsmodel, modelMorphPath);
  			
  			mp.loadModel(wsmodel, modelMorphPath, ltmodel);
  			System.out.println("morph sem paradigms size: " + wsmodel.idx().getMPlabels().size());
  
  			mp.tag(wsmodel); //second time after paradigm cleaning
  //			mp.tagAmbig(wsmodel, false, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50.txt");
  //			mp.tagAmbig(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vector1.txt");
  			mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion1a.txt", true, 1, 20);
  			mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion2a.txt", true, 2, 20);
  			mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion3a.txt", true, 3, 20 );
  
  			mp.saveModel(wsmodel, modelMorphPath+".-check"+".model");
  			mp.printMorphStats(wsmodel);
  			
  			for(String mpstring: wsmodel.idx().getMPlabels()) {
  				MorphParadigm mp1 = wsmodel.idx().getMorphParadigm(mpstring);
  				if(mp == null) continue;
  				System.out.println(mp1.getLabel() + "\t" + mp1.getSortedFlexFreqMap() + "\twating\t" + mp1.waitingFlexWordMap.toString());
  			}
  		}
  		*/
  		if(getSyntClusters) {
  			
  /*			
  //			if(wsmodel.idx().morphPars().size() > 0)
  //				Cluster.morphParNum = wsmodel.idx().morphPars().size();
  			String modelSyntPath = "model/synt/ukr-synt";
  			//train SyntPar
  			SyntModel sp = new SyntModel(2, "SyntPar1", 1000);
  			sp.train(wsmodel);
  			sp.saveModel(wsmodel, modelSyntPath+".new" + ".model");
  	//		sp.loadModel(wsmodel, modelSyntPath);
  			sp.tag(wsmodel);
  //			sp.addParInfoIntoModel(wsmodel, true); //write Labels
  			
  			for(Cluster c: wsmodel.idx().syntPars().values()) 	
  				System.out.println(c.getLabel() + "\t" + 
  						MyUtils.rdouble(c.getSim()) + "\t" + 
  						c.morphPar + "\t"+c.toString());		
  						
  						*/
  			}
  		
  		
        System.out.println("KNOWN: " + wsmodel.idx().knownParadigmLabels);
        System.out.println("DELETED: " + wsmodel.idx().deletedParadigmLabels);
  
  	
  
  		 long endtime = System.nanoTime();
  		 double time = (double)(endtime-starttime)/1000000000.0;
   		System.out.println("\nprocess time:\t"+(time)+"\tsec\t" + (time / 60.0) + "\tmin");
  		 
  		 
  		return wsmodel;
  	}

  public static void main(String[] args) throws IOException {
		
//		mainStandard();
		String lang = "ukr";
		String filePath = CorpusUtils.getLeipzigWordsPath(lang, "wiki");
		LetterTokModel ltmodel = MorphAnalyzer.trainLetterTokModel(lang, filePath, 30, 3);
		
		
		WordSequences model = getModel(ltmodel);
//		WordSequences model = getModelWithClustersMorph(ltmodel);
//		MorphSynt.findAgreement(model);
		
//		WordSequences model = getModelWithClustersSynt(ltmodel);
//		WordSequences model = getModelWithClustersMorphSynt(ltmodel);
//		mainExploreCollocations();
		
//		analyzePhrasesAllGetrennt(model, lang);
		
		getInput(model, ltmodel, null, null, null);	

	}

	private static void analyzePhrases(WordSequences model) {
		List<String> sents = FindPhrases.analyzeSentsAll(model, 9, 9, 9999);
		Map<String,Double> saveMap = new HashMap<>();
		FindPhrases.getPrasesStats(sents, saveMap);
		MapsOps.printSortedMap(saveMap, "out/phrasen-ukr.txt", 2);
	}
	
	public static void analyzePhrasesAllGetrennt(WordSequences model, String lang) {
		FindPhrases.analyzeSentsAllGetrennt(model, 9, 9, 9999);
		int i = 0;
		for(Map<String,Double> m: model.idx().phraseMaps) {
			i++;
			MapsOps.printSortedMap(m, "out/phrasen-"+lang+i+".txt", 1);
		}
	}

	private static void mainExploreCollocations() throws IOException {
		WordSequences wsmodel = BuildWordSequenceModelUKR.getWSModel("ukr", new String[] {"wiki", "news"}, 400000, false,1);
		List<MyPair> bigrams = new ArrayList<>();
		for(Word w: wsmodel.idx().getSortedWords()) {
			if(w.freq() < 25) break;
			for(Word rightContextWord: w.left_of.keySet())
				bigrams.add(new MyPair(w.toString(), rightContextWord.toString(), w.left_of.get(rightContextWord)));
		}
		CollocationCollection ccoll = Collocations.findCollocations(bigrams, wsmodel);
		for(Collocation coll: ccoll.getStrongestCollocations()) {
			System.out.println("STRONGEST:\t" + coll.toString());
		}
		System.out.println("\n\n");
		for(Collocation coll: ccoll.getStrongCollocations()) {
			System.out.println("STRONG:\t" + coll.toString());
		}
		System.out.println("\n\n");
		for(Collocation coll: ccoll.getMediumCollocations()) {
			System.out.println("MEDIUM:\t" + coll.toString());
		}
		System.out.println("\n\n");
		for(Collocation coll: ccoll.getWeakCollocations()) {
			System.out.println("WEAK:\t" + coll.toString());
		}
	}
	
	private static void mainStandard() throws IOException {
		 Search search = new Search("out/index/indexUKR");
		 search = null;
		
		String filePath = "E:\\ukr_web_2012_1M-words.txt";
		filePath = "C:\\2Projects\\CorporaIndices\\Leipzig/ukr_wikipedia_2016_3M-words.txt";
	
		String[] corpora = null;
		corpora = new String[] {"wiki"}; //, "news" wiki};
		
		WordSequences wsmodel = getWSModel("ukr", corpora, 800000, false, 1); //800000
	
		
		getInput(wsmodel, null, null, null, search);		
	}

	/**
	 * get plain model without any clusters
	 */
	public static WordSequences getModel(LetterTokModel ltmodel) throws IOException {
		return mainGetModelWithClusters(false, false, ltmodel);
	}
	
	public static WordSequences getModelWithClustersMorph(LetterTokModel ltmodel) throws IOException {
		return mainGetModelWithClusters(true, false, ltmodel);
	}

	public static WordSequences getModelWithClustersSynt(LetterTokModel ltmodel) throws IOException {
		return mainGetModelWithClusters(false, true, ltmodel);
	}

	public static WordSequences getModelWithClustersMorphSynt(LetterTokModel ltmodel) throws IOException {
		return mainGetModelWithClusters(true, true, ltmodel);
	}
	
	


}
