package processText.mainModels;


import java.io.IOException;

import lucene.Search;
import model.LetterTokModel;
import model.MorphModel;
import model.MorphVectorModel;
import model.SyntModel;
import model.WordSequences;
import model.Words;
import modeltrain.MorphAnalyzer;
import modelutils.Cluster;
import util.CorpusUtils;
import util.MyUtils;

public class BuildWordSequenceModelEN extends BuildWordSequenceModel{
	
	private static WordSequences mainGetModelWithClusters(boolean getMorphClusters, boolean getSyntClusters,LetterTokModel ltmodel) throws IOException {
  		long starttime = System.nanoTime();
  	
  		//load base model
  			String lang = "en";
  			int howmany = 30000*2;
  			WordSequences wsmodel = BuildWordSequenceModelDE.getWSModel("en", new String[] {"news", "wiki"}, howmany, false,0);
  
  			Words.addSplitterWordStats(wsmodel);
  			//cluster Predicatives right
  			 SyntModel.PARADIGM_PREF = "x_";
             SyntModel spsplitterL = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
              String syntModelPath = "model/synt/en-splitter-left.model";
//              spsplitterL.train(wsmodel);
//              spsplitterL.addParInfoIntoModel(wsmodel, false, true, "", false); //write Labels 
//              spsplitterL.trainVectorClusters(wsmodel, 300, 10, 6.0, 11.0, 5);
  //
              String modelVectorMorphPath = "model/morph/en-morphVector-RecursionFromAbove-scaleCheckAllWords-parSimThhScaled.model";
              MorphVectorModel mpv = new MorphVectorModel(1, "MorphPar1");
              mpv.setLetterTokModel(ltmodel);
              mpv.train(wsmodel);
              mpv.saveModel(modelVectorMorphPath, wsmodel);
              
              wsmodel.idx().deletedParadigmLabels.addAll(wsmodel.idx().knownParadigmLabels);

              wsmodel.idx().knownParadigmLabels.clear();
              wsmodel.idx().syntPars().clear();
              SyntModel.PARADIGM_PREF = "z_";
              spsplitterL = new SyntModel(3, "SyntPar2", 200, 0, SyntModel.SPLITTER_PRED_LEFT);
              spsplitterL.train(wsmodel);
              spsplitterL.addParInfoIntoModel(wsmodel, false, true, "", false); //write Labels
//              spsplitterL.trainVectorClusters(wsmodel, 300, 10, 6.0, 11.0, 5);

              
              SyntModel.PARADIGM_PREF = "y_";
              spsplitterL = new SyntModel(3, "SyntPar2", 750, 200, SyntModel.SPLITTER_PRED_LEFT);
              spsplitterL.train(wsmodel);
              spsplitterL.addParInfoIntoModel(wsmodel, false, false, "", true); //write Labels
              spsplitterL.trainVectorClusters(wsmodel, 5000, 30, 15.0, 15.0, 20);
              
              MorphModel.FPREF = "g_";
              MorphModel.MPREF = "n_";
              wsmodel.idx().cleanMorphPars();
              wsmodel.idx().removeStopCreatingParadigms();
              mpv.train(wsmodel);

//              BuildWordSequenceModel.printParadigmExpectations(wsmodel);
              

             

  //			Words.addPhraseWordStats(wsmodel, 0.01);
  			
  			//phrases + collocations
  //			addPhrases(wsmodel, new String[] {"news", "wiki"}, 0, 300000);
  			
  			String modelsempath = "model/sem/en-sem.model";
  
  //			TestIdeaPred.findSynParadigms(wsmodel, 5, 10000, 20,Double.MAX_VALUE,modelsempath) ;
  /*
  			if(getMorphClusters) {
  				String modelMorphPath = "model/morph/en-morph.new1.model";
  				
  				//train MorphPar, 		//WSmodel.hasMorph
  				MorphModel mp = new MorphModel(1, "MorphPar1");
  				mp.setThhFreqForTrain(100);
  				mp.setLetterTokModel(ltmodel);
  				
  //				mp.train(wsmodel);
  //				mp.saveModel(wsmodel, modelMorphPath);
  				mp.loadModel(wsmodel, modelMorphPath, ltmodel);
  				
  				System.out.println("morph sem paradigms size: " + wsmodel.idx().getMPlabels().size());
  
  				mp.tag(wsmodel);
  //				mp.tagAmbig(wsmodel, false, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50.txt");
  //				mp.tagAmbig(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-Vector.txt");
  				mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion1a.txt", true, 1, 20);
  				mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion2a.txt", true, 2, 20);
  				mp.tagAmbigFlexion(wsmodel, true, "out/"+ wsmodel.getLang()+"-taggedMorph-tagForSeenFlexes-AMBIG_50-vectorFlexion3a.txt", true, 3, 20);
  
  				mp.printMorphStats(wsmodel);
  				
  				
  			}	
  				*/	
  			if(getSyntClusters) {
  				
  				/*
  				//train SyntPar
  				String modelSyntPath = "model/synt/en-synt";
  				SyntModel sp = new SyntModel(2, "SyntPar1");
  				sp.train(wsmodel);
  				sp.saveModel(wsmodel, modelSyntPath + "" + ".model"); //+".new");
  	//			sp.loadModel(wsmodel, modelSyntPath);
  				sp.tag(wsmodel);
  				sp.addParInfoIntoModel(wsmodel,"", true); //write Labels
  				*/
  				for(Cluster c: wsmodel.idx().syntPars().values()) 	System.out.println(c.getLabel() + "\t" + MyUtils.rdouble(c.getSim()) + "\t" + c.toString());
  			}
  			
  			
  			 long endtime = System.nanoTime();
  			 double time = (double)(endtime-starttime)/1000000000.0;
  	 		System.out.println("\nprocess time:\t"+(time)+"\tsec\t" + (time / 60.0) + "\tmin");
  			
  			return wsmodel;
  		}

  public static final String splitString = "[,]";
	
	public static void main(String[] args) throws IOException {
		
		 Search search = null;

		 String lang = "en";
			String filePath = CorpusUtils.getLeipzigWordsPath(lang, "news");
			LetterTokModel ltmodel = MorphAnalyzer.trainLetterTokModel(lang, filePath, 3, 3); 
			String modelLetterTokPath = "model/lettertok/en-lettertok.new.model";
			ltmodel.serializeModel(modelLetterTokPath);
//			LetterTokModel ltmodel = LetterTokModel.readModel(modelLetterTokPath);
			
//			mainStandard();
			WordSequences model = getModel(ltmodel);

//			getModelWithClustersSynt();
//			WordSequences model = getModelWithClustersMorph(ltmodel);
//			MorphSynt.findAgreement(model);
//			WordSequences model = getModelWithClustersMorphSynt(ltmodel);
			
//			BuildWordSequenceModelUKR.analyzePhrasesAllGetrennt(model, lang);

			getInput(model, ltmodel, null, null, null);	
	}

	public static  WordSequences getBigModel(boolean toLower) {
		String lang = "en";
		WordSequences wsmodel = new WordSequences(lang);
	
		String[] corpora = null;
		corpora = new String[] {"wiki", "news"}; //};
		
		for(String corpus: corpora)
			wsmodel.addWordsToModel( CorpusUtils.getLeipzigSentences(lang, corpus, 0, 400000), toLower, false);
		
		return wsmodel;
	}
	
	/**
	 * get plain model without any clusters
	 */
	public static WordSequences getModel(LetterTokModel ltmodel) throws IOException {
		return mainGetModelWithClusters(false, false, ltmodel);
	}

	public static WordSequences getModelWithClustersMorph(LetterTokModel ltmodel) throws IOException {
		return mainGetModelWithClusters(true, false,ltmodel);
	}
	
	public static WordSequences getModelWithClustersMorphSynt(LetterTokModel ltmodel) throws IOException {
		return mainGetModelWithClusters(true, true,ltmodel);
	}
	
	

	

}
