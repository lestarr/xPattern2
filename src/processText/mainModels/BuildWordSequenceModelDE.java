package processText.mainModels;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import experiment.FindPhrases;
import lucene.Search;
import model.LetterTokModel;
import model.MorphVectorAnalyzer;
import model.MorphVectorModel;
import model.SyntModel;
import model.WordSequences;
import model.Words;
import modeltrain.MorphAnalyzer;
import util.CorpusUtils;
import util.MapsOps;

public class BuildWordSequenceModelDE extends BuildWordSequenceModel{
	
	private static WordSequences mainGetModelWithClusters(boolean getMorphClusters, boolean getSyntClusters,LetterTokModel ltmodel) throws IOException {
  		long starttime = System.nanoTime();
  		String lang = "de";
  
  		//load base model
  		int howmany = 30000*2;
  		int start = 0;
  		String[] corpora = new String[] {"news", "wiki"};
  		
  			WordSequences wsmodel = BuildWordSequenceModelDE.getWSModel(lang, corpora, howmany, false,start);
  			
  			String corpusPath = CorpusUtils.getCorpusDe("company");
  			List<String> sents = CorpusUtils.getLeipzigSentences(lang, corpusPath, 0, howmany);
  			wsmodel.addWordsToModel( sents,  false, false);
  			System.out.println("WORDS in Model: " + wsmodel.idx().words.size());
  //			wsmodel.removeSeldomWords(1.0);
  			System.out.println("WORDS in Model after clean seldom: " + wsmodel.idx().words.size());
  
  			Words.addSplitterWordStats(wsmodel);
  			
  			//cluster Predicatives right
  	       SyntModel.PARADIGM_PREF = "x_";
  	       SyntModel spsplitterL = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
//  			String syntModelPath = "model/synt/de-splitter-left.model";
//  			spsplitterL.train(wsmodel);
//  			spsplitterL.addParInfoIntoModel(wsmodel, false, true, "", false); //write Labels 
//            spsplitterL.trainVectorClusters(wsmodel, 300, 10, 6.0, 11.0, 5);
//
            String modelVectorMorphPath = "model/morph/de-morphVector-small-newVectorScores.model";
						String modelVectorSyntPath = "model/morph/de-syntVector.model";

						MorphVectorModel mpv = new MorphVectorModel(1, "MorphPar1");
            mpv.setLetterTokModel(ltmodel);
//            mpv.train(wsmodel);
//            mpv.saveModel(modelVectorMorphPath, wsmodel);
            mpv.loadModel(modelVectorMorphPath, wsmodel, 20, 3);
            MorphVectorAnalyzer.printMorphParStats(wsmodel, 5, "last stats");

 /*           wsmodel.idx().deletedParadigmLabels.addAll(wsmodel.idx().knownParadigmLabels);
            wsmodel.idx().knownParadigmLabels.clear();
            wsmodel.idx().syntPars().clear();
            SyntModel.PARADIGM_PREF = "z_";
            spsplitterL = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
            spsplitterL.train(wsmodel);
            spsplitterL.addParInfoIntoModel(wsmodel, false, true, "", false); //write Labels
//            spsplitterL.trainVectorClusters(wsmodel, 300, 10, 6.0, 11.0, 5);
            SyntModel.PARADIGM_PREF = "y_";
            spsplitterL = new SyntModel(3, "SyntPar2", 750, 200, SyntModel.SPLITTER_PRED_LEFT);
  			spsplitterL.train(wsmodel);
  			spsplitterL.addParInfoIntoModel(wsmodel, false, false, "", true); //write Labels
        spsplitterL.trainVectorClusters(wsmodel, 5000, 30, 15.0, 15.0, 20);
//      spsplitterL.trainVectorClusters(wsmodel, 5000, 40, 20.0, 20.0, 20);
				spsplitterL.saveModel(modelVectorSyntPath, wsmodel);
*/
		SyntModel.loadModel(modelVectorSyntPath, wsmodel, 40, 15, 10000);

		BuildWordSequenceModel.printParadigmExpectations(wsmodel);

//		wsmodel.tagMorphSynt();

  			/*			
  //			System.exit(0);
  			//cluster splitters
  			SyntModel spsplitter = new SyntModel(2, "SyntPar1", 3000, 0, SyntModel.SPLITTER_LEFTRIGHT);
  			syntModelPath = "model/synt/de-splitter.model";
  //			spsplitter.train(wsmodel);
  //			spsplitter.saveModel(wsmodel, syntModelPath);
  			spsplitter.loadModel(wsmodel, syntModelPath);
  			spsplitter.tagTrained(wsmodel);
  			spsplitter.addParInfoIntoModel(wsmodel,  false); //write Labels
  			
  			
  //			Words.addPhraseWordStats(wsmodel, 0.01); // s_left, s_right ...
  //			wsmodel.closed = true;
  
  			String modelVectorMorphPath = "model/morph/de-morphVector-RecursionFromAbove-scaleCheckAllWords-parSimThhScaled.model";
  			MorphVectorModel mpv = new MorphVectorModel(1, "MorphPar1");
  			mpv.setLetterTokModel(ltmodel);
  			mpv.train(wsmodel);
  			//temporary here: correct paradigms for adj, flex = _
  			//get new paradigm or init it
  			wsmodel.idx().removeStopCreatingParadigms();
  			Set<Flexion> flexes = SetOps.of(wsmodel.idx().getFlex("_"));
  			MorphParadigm mpNew = new MorphParadigm(flexes,
  					MorphParadigm.getEmptyFlexFreqMap(flexes), "m_99");
  			System.out.println(mpNew.getLabel());
  			MorphVectorAnalyzer.removeFLexFromPar("_", "m_1", mpNew, wsmodel, 3);
  			wsmodel.idx().knownParadigmLabels.add("m_99");
  
  			mpv.saveModel(modelVectorMorphPath, wsmodel);
  
  //			//phrases + collocations
  //			addPhrases(wsmodel, corpora, start, howmany);
  //			addPhrases(wsmodel, new String[] {corpusPath}, 0, 100000);
  			
  			//semantics
  			String modelsempath = "model/sem/de-sem.model";
  //			TestIdeaPred.findSynParadigms(wsmodel, 5, 10000, 20,Double.MAX_VALUE,modelsempath) ;
  //			TestIdeaPred.loadSynParadigms(wsmodel, modelsempath);
  			
  //			TestIdeaPred.testSeldomFreq5(wsmodel);
  			*/
  			/*
  			if(getMorphClusters) {
  				String modelMorphPath = "model/morph/de-morph.new1.model";
  				
  				//train MorphPar, 		//WSmodel.hasMorph
  				MorphModel mp = new MorphModel(1, "MorphPar1");
  				mp.setThhFreqForTrain(100); //100 = 5.8min
  				mp.setLetterTokModel(ltmodel);
  //				mp.train(wsmodel);
  //				mp.saveModel(wsmodel, modelMorphPath+".new1"+".model");
  				
  				mp.loadModel(wsmodel, modelMorphPath, ltmodel);
  				System.out.println("morph sem paradigms size: " + wsmodel.idx().getMPlabels().size());
  				mp.tag(wsmodel);
  //				mp.tagAmbig(wsmodel,false, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50.txt");
  //				mp.tagAmbig(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vector1.txt");
  				mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion1a.txt", true, 1, 20);
  				mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion2a.txt", true, 2, 20);
  				mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion3a.txt", true, 3, 20);
  
  				for(String mpstring: wsmodel.idx().getMPlabels()) {
  					MorphParadigm mp1 = wsmodel.idx().getMorphParadigm(mpstring);
  					if(mp == null) continue;				
  					System.out.println(mp1.getLabel() + "\t" + mp1.getSortedFlexFreqMap() + "\twating\t" + mp1.waitingFlexWordMap.toString());
  				}
  				
  				mp.saveModel(wsmodel, modelMorphPath+".-check"+".model");
  				mp.printMorphStats(wsmodel);
  				
  			}	
  			*/
  			if(getSyntClusters) {
  				
  				
  				
  //				if(wsmodel.idx().morphPars().size() > 0)
  //					Cluster.morphParNum = wsmodel.idx().morphPars().size();
  				//train SyntPar standard
  //				String modelSyntPath = "model/synt/de-synt";
  //				SyntModel sp = new SyntModel(2, "SyntPar2", 300);
  //				sp.train(wsmodel);
  //				sp.saveModel(wsmodel, modelSyntPath + "" + ".model"); //+".new");
  //	//			sp.loadModel(wsmodel, modelSyntPath);
  //				sp.tag(wsmodel);
  ////				sp.addParInfoIntoModel(wsmodel, true); //write Labels
  //				
  //				for(Cluster c: wsmodel.idx().syntPars().values()) 	
  //					System.out.println(c.getLabel() + "\t" + 
  //							MyUtils.rdouble(c.getSim()) + "\t" + 
  //							c.morphPar + "\t"+c.toString());
  			}
  			
  //			wsmodel.fillAssociations(5.0);
  //			wsmodel.printAssociations(0.1);
  			
  			 long endtime = System.nanoTime();
  			 double time = (double)(endtime-starttime)/1000000000.0;
  	 		System.out.println("\nprocess time:\t"+(time)+"\tsec\t" + (time / 60.0) + "\tmin");
  			
  			//test
  			return wsmodel;
  		}
  public static String fileMarker = "-wiki-cmp-800k-lowerPredThhForNonFreqWords";

	public static final String splitString = "[,]";

	public static void main(String[] args) throws IOException {
		String lang = "de";
		String filePath = CorpusUtils.getLeipzigWordsPath(lang, "news");
		LetterTokModel ltmodel = MorphAnalyzer.trainLetterTokModel(lang, filePath, 30, 3); 
		 Search search = new Search("out/index/indexDE");
		 search = null;
//		mainStandard();
		WordSequences model = getModel(ltmodel);
//		WordSequences model = getModelWithClustersSynt(ltmodel);
//		WordSequences model = getModelWithClustersMorph(ltmodel);
//		MorphSynt.findAgreement(model);
//		WordSequences model = getModelWithClustersMorphSynt(ltmodel);
//		analyzePhrases(model);
		
//		BuildWordSequenceModelUKR.analyzePhrasesAllGetrennt(model, lang);

		getInput(model, ltmodel, null, null, search);	

	}
	
	
	private static void analyzePhrases(WordSequences model) {
		List<String> sents = FindPhrases.analyzeSentsAll(model, 9, 9, 9999);
		Map<String,Double> saveMap = new HashMap<>();
		FindPhrases.getPrasesStats(sents, saveMap);
		MapsOps.printSortedMap(saveMap, "out/phrasen-de.txt", 1);
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
	private static void mainStandard() throws IOException {
		WordSequences wsmodelStd = null;
		wsmodelStd = getWSmodelDe(800000, false);

		getInput(wsmodelStd, null, null, null, null);		
	}



	public static WordSequences getWSmodelDe(int howmany, boolean toLower) {
		List<String> sents;
		String lang = "de";
		String testFilePath = "C:\\2Projects\\CorporaIndices\\goldenstandard/corpusDPA_apr024_50.txt";
		List<String> testCorpus = CorpusUtils.getSentences(testFilePath);

		String corpusPath = CorpusUtils.getCorpusDe("company");
		sents = CorpusUtils.getLeipzigSentences(lang, corpusPath, 0, howmany);
		WordSequences wsmodel = new WordSequences(lang);
		wsmodel.addWordsToModel( sents,  toLower, false);
		
		wsmodel.addWordsToModel( testCorpus, toLower, false);
		return wsmodel;
	}
	
	
	public static  WordSequences getBigModel(boolean toLower) {
		List<String> sents;
		String lang = "de";
		String testFilePath = "C:\\2Projects\\CorporaIndices\\goldenstandard/corpusDPA_apr024_50.txt";
		String corpusPath = CorpusUtils.getCorpusDe("company");
		sents = CorpusUtils.getLeipzigSentences(lang, corpusPath, 0, 800000);
		System.out.println("!!! " + sents.size());
		List<String> testCorpus = CorpusUtils.getSentences(testFilePath);
		WordSequences wsmodel = new WordSequences(lang);
		wsmodel.addWordsToModel( sents,  toLower, false);
		corpusPath = CorpusUtils.getCorpusDe("wiki");
		
		sents = CorpusUtils.getLeipzigSentences(lang, corpusPath, 0, 700000);
		System.out.println("!!! " + sents.size());
		wsmodel.addWordsToModel( sents,  toLower, false);
		
		wsmodel.addWordsToModel( testCorpus,  toLower, false);
		return wsmodel;
	}




	

}
