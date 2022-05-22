package model;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javafx.util.Pair;
import modelparts.Flexion;
import modelparts.MorphParadigm;
import modelparts.Word;
import modeltrain.SyntParTrain;
import modeltrain.SyntParVectorTrain;
import modelutils.Cluster;
import modelutils.Vector;
import processText.mainModels.BuildWordSequenceModel;
import util.ListOps;
import util.MyPair;
import util.MyUtils;

public class SyntModel  {

  public final static String SPLITTER_PREFIX = "SPLITTER_";

  public final static String SPLITTER_LEFTRIGHT = SPLITTER_PREFIX + "LEFTRIGHT";
  public final static String SPLITTER_PRED_LEFT = SPLITTER_PREFIX + "PRED_LEFT";
  public final static String SPLITTER_PRED_RIGHT = SPLITTER_PREFIX + "PRED_RIGHT";

  public static String PARADIGM_PREF = "s_";

  private int clusterNum = 15000;
  private String splitterOnly = "noSplitter";

  private int start = 0;

  public SyntModel(int id, String label) {
  }

  public SyntModel(int id, String label, int clusternum) {
    this.clusterNum = clusternum;
  }

  public SyntModel(int id, String label, int clusternum, int start, String splitterOnly) {
    this.clusterNum = clusternum;
    this.splitterOnly = splitterOnly;
    this.start = start;
  }

  public void saveModel(String outfile, WordSequences model) {
    Writer out = MyUtils.getWriter(outfile);
    try {
      Collection<Cluster> spars = model.idx().getSyntParadigms();
      StringBuffer sb = new StringBuffer();
      for(Cluster c: spars) {
        sb = new StringBuffer();
        sb.append(c.getLabel());
        sb.append(MorphVectorModel.DELIMITER);
        for(MyPair mp: c.getParadigmWordsSorted()) {

          sb.append(mp.first);
          sb.append(MyPair.MYPAIR_DELIMITER);
          sb.append(mp.freq);
          sb.append(MorphVectorModel.WORD_DELIMITER);
        }
        out.write(sb.toString()+"\n");
      }
      out.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void loadModel(String infile, WordSequences model, int ccount, int bestfreqThh, int howmany) {
    List<String> lines;
    try {
      lines = MyUtils.readLines(infile);
      List<Cluster> clist = new ArrayList<>();
      for(String line: lines) {
        String[] parts = line.split(MorphVectorModel.DELIMITER);
        if(parts.length != 2) {System.out.println("WRONG FORMAT: " + infile); return;}
        String label = parts[0];
        String words = parts[1];
        Cluster c = new Cluster(new ArrayList<>());
        c.setLabel(label);
        for(String mp: words.split(MorphVectorModel.WORD_DELIMITER)) {
          String[] mparr = mp.split(MyPair.MYPAIR_DELIMITER);
          if(mparr.length != 2) continue;
          c.addParadigmWord(new MyPair(mparr[0], "", Double.parseDouble(mparr[1])));
        }
        clist.add(c);
      }
      model.idx().setSyntPars(clist, PARADIGM_PREF);

      SyntParVectorTrain.addClusterWordsFromParWords(model, -1);
      System.out.println("add cwords done");

      model.collectKnownParVectors(Words.SYN_FILTER, ccount);
      System.out.println("vectors computed");

      SyntParVectorTrain.tagWords(model, howmany, bestfreqThh, false, ccount, true);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void train(WordSequences wsmodel) {
    try {
      List<Cluster> output = SyntParTrain.trainClusters(wsmodel, clusterNum, start, true, this.splitterOnly);
      wsmodel.idx().setSyntPars(output, PARADIGM_PREF);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } // 5000

//		List<Cluster> output = SyntSemParadigms.trainParadigms(wsmodel, 2000);
//		wsmodel.idx().setSyntPars(output);
  }

  /**
   * "some more rounds" for synt clusters with vectors
   * 
   * @param model
   */
  public void trainVectorClusters(WordSequences model, int howmany, int minParadigmNumber, 
      double tagSimThh, double clusterSimThh, int clusterMinMembers) {
    Cluster.maxClusterSize = 10000;
    int roundNr = 0;
    int contextcount_words = 40;
    int contextcount_clusters = 40;
    boolean lastround = false;
    boolean useSecondCluster = true;
    tag(model, howmany, tagSimThh, true, roundNr, false, clusterMinMembers, contextcount_words, lastround);
    printClusters(model, roundNr, false, contextcount_clusters);
    while(true) {
      roundNr++;
      if(    
//          !SyntParVectorTrain.combineClustersExperiment(model, roundNr, minParadigmNumber, 9)
          !SyntParVectorTrain.combineClusters(model, howmany, clusterSimThh, roundNr, minParadigmNumber, clusterMinMembers, contextcount_clusters) 
          ) 
        break;
//      printClusters(model, roundNr, false, contextcount_clusters);
      roundNr++;

      tag(model, howmany, tagSimThh, true, roundNr, useSecondCluster, clusterMinMembers, contextcount_words, lastround);
      printClusters(model, roundNr, false, contextcount_clusters);
    }
    System.out.println("last tag");
    roundNr++;
    Cluster.maxClusterSize = 10000;
    tag(model, howmany, tagSimThh, true, roundNr, useSecondCluster, clusterMinMembers, contextcount_words, lastround);
    printClusters(model, roundNr, true, contextcount_clusters);
    
//    tagSimThh = 5.0;
    System.out.println("last last tag");
    roundNr++;
    tagSimThh = tagSimThh + 4.0;
    SyntParVectorTrain.ClearParWordsInClusters(model);
    System.out.println("clear parwords done");
    SyntParVectorTrain.tagWords(model, howmany, tagSimThh, useSecondCluster, contextcount_words, true);    
    SyntParVectorTrain.addClusterWordsFromParWords(model, -1);
    model.collectKnownParVectors(Words.SYN_FILTER, contextcount_clusters);
    printClustersLast(model, roundNr, false, contextcount_words);
    
    }

  public void tag(WordSequences model, int howmany, double thh, boolean useClean, int round, 
      boolean useSecondCuster, int clusterMinMembers, int contextcount, boolean lastRound) {
//		tagTrained(wsmodel);

    // delete cluster words,
    // add cluster words from paradigm words,
    // clear clusters' paradigm words
    // tag words (fills clusters' paradigm words), delete syn info from words,
    // boolean

    SyntParVectorTrain.prepareVectorsForTagging(model, useClean, round, contextcount);
    SyntParVectorTrain.ClearParWordsInClusters(model);
    System.out.println("clear parwords done");
    SyntParVectorTrain.tagWords(model, howmany, thh, useSecondCuster, contextcount, lastRound);
    System.out.println("tag words done");
    SyntParVectorTrain.deleteCLustersWithNoParWords(model, round, false, clusterMinMembers);
    System.out.println("delete clusters done");
  }

  public void tagTrained(WordSequences wsmodel) {
    boolean tagOnlyWordsWithoutCluster = false;
    if (this.splitterOnly.equals(SPLITTER_LEFTRIGHT))
      tagOnlyWordsWithoutCluster = true;

    if (tagOnlyWordsWithoutCluster) {
      // first write cluster into word
      for (Cluster c : wsmodel.idx().syntPars().values()) {
        for (Pair<String, Vector> p : c.getWordValues()) {
          Word w = wsmodel.getWord(p.getKey());
          w.setCluster(c);
        }
      }
    }

    List<Pair<String, Vector>> wordsValuesList = SyntParTrain.getWordsValuesList(wsmodel, 1500, 0, this.splitterOnly,
        tagOnlyWordsWithoutCluster);
    List<Cluster> filledClusters = SyntParTrain.tagWordsToClusters(wsmodel, wordsValuesList, 1.0, false);
    for (Cluster c : filledClusters) {
      for (Pair<String, Vector> p : c.getWordValues()) {
        Word w = wsmodel.getWord(p.getKey());
        w.setCluster(c);
      }
    }
  }

  public void saveModelOld(WordSequences wsmodel, String path) {
    try {
      Writer out = MyUtils.getWriter(path);
      List<Cluster> clusters = ListOps.of(wsmodel.idx().syntPars().values());
      for (Cluster c : clusters) {
        StringBuffer sb = new StringBuffer();
        for (Pair<String, Vector> pair : c.getWordValues()) {
          sb.append(pair.toString()).append("#");
        }
        out.write(sb.toString() + "\n");
      }
      out.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void loadModelOld(WordSequences wsmodel, String path) {
    try {
      List<Cluster> clusters = new ArrayList<Cluster>();
      List<String> lines = MyUtils.readLines(path);
      for (String line : lines) {
        List<Pair<String, Vector>> wordValuesList = new ArrayList<>();
        String[] wordVectorValues = line.split("#");
        for (String pairString : wordVectorValues) {
          String[] wordVectorArr = pairString.split("=");
          String word = wordVectorArr[0];
          String vectorString = wordVectorArr[1].replaceFirst("^\\[", "").replaceFirst("\\]$", "");
          String[] vectorArr = vectorString.split(", ");
          List<Double> vectorList = new ArrayList<>();
          for (String v : vectorArr) {
            if (v.length() == 0)
              continue;
            vectorList.add(Double.parseDouble(v));
          }
          Vector vector = new Vector(vectorList.toArray(new Double[vectorList.size()]));
          Pair<String, Vector> pair = new Pair<String, Vector>(word, vector);
          wordValuesList.add(pair);
        }
        Cluster c = new Cluster(wordValuesList);
        clusters.add(c);
      }
      wsmodel.idx().setSyntPars(clusters, PARADIGM_PREF);
      System.out.println("synt model loaded");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static Pair<Cluster, Double> tagWord(Word w, WordSequences model) {
    Vector values = new Vector(SyntParTrain.collectFeatures(w, model));
    List<Cluster> inputClusters = ListOps.of(model.idx().syntPars().values());

    Cluster wordInCheckCluster = new Cluster(new Pair<String, Vector>(w.toString(), values));
    List<Pair<Cluster, Double>> bestClusters = SyntParTrain.computeBestClusters(inputClusters, Double.MAX_VALUE, -1,
        wordInCheckCluster);
    if (bestClusters == null || bestClusters.size() < 1)
      return new Pair<Cluster, Double>(new Cluster(new Vector(new Double[] {})), -1.0);
    return bestClusters.get(0);
  }

//	public void addParInfoIntoModel(WordSequences wsmodel) {
//		addParInfoIntoModel(wsmodel,"", false);
//	}

  public void addParInfoIntoModel(WordSequences wsmodel, boolean addMorphSyntWord, boolean highFreqCluster, String paradigmPref, boolean breakAfterOne) {
    List<Cluster> clusterlist = new ArrayList<>();
    for (Cluster c : wsmodel.idx().syntPars().values()) {
      clusterlist.add(c);
    }
    for (Cluster c : clusterlist) {
      String clusterName = paradigmPref+c.getLabel();
      wsmodel.idx().syntPars().remove(c.getLabel());
      c.setLabel(clusterName);
      wsmodel.idx().syntPars().put(clusterName, c);
      wsmodel.idx().knownParadigmLabels.add(clusterName);
      for (String wString : c.getWords()) {
        Word w = wsmodel.getWord(wString);
        w.syntLabel = c.getLabel();
        // add cluster label freq for each word of this cluster for statistics
        Word clusterword = wsmodel.addCategory(clusterName, w);
        clusterword.paradigmWords.add(w);
        c.addParadigmWord(new MyPair(w.toString(), "", 0.0), 1,0);
        if(highFreqCluster) c.highFreqCluster = true;
        if (addMorphSyntWord) {
          addParadigmCombiLabel(wsmodel, w.getMorphLabelNotNull(), c.getLabel(), w);
        }
        if(breakAfterOne)
          break; // means add only 1 word for cluster
      }
    }
  }

  /**
   * Makes an artifitial word out of morph label + synt label of a word to enable
   * statistics measurement on how often one label comes with another in one word
   * 
   * @param wsmodel
   * @param firstLabel
   * @param mainLabel
   */
  private void addParadigmCombiLabel(WordSequences wsmodel, String firstLabel, String mainLabel, Word w) {
    if (firstLabel != null) {
      String combiLabel = firstLabel + mainLabel;
      Word wLabel = wsmodel.getWord(combiLabel);
      wLabel.addFreq();
      wLabel.fillLeftOf(w);
    }
  }

  public void tagWithVectors(WordSequences model, int thh, double minsim, boolean print, int contextcount) {
    model.collectKnownParVectors(SPLITTER_LEFTRIGHT, contextcount);
    System.out.println();
    for (Word w : model.idx().getSortedWords()) {

      if (w.freq() < thh)
        break;
      if (w.toString().contains("_"))
        continue;
      Pair<String, Double> bestpar = MorphVectorAnalyzer.computeBestKnownVectorParadigm(w, Words.SYN_FILTER, model,
          false, Double.MAX_VALUE, contextcount);
      if (bestpar != null && bestpar.getValue() > minsim)
        bestpar = null;
      if (bestpar != null) {
        String clusterName = bestpar.getKey().replaceFirst("^m_", "");
        Cluster c = model.idx().syntPars().get(clusterName);
        model.getWord(bestpar.getKey()).paradigmWords.add(w);
        w.syntLabel = c.getLabel();
//				c.addWord(w.toString(), null);
      }
      if (print) {
        if (bestpar == null)
          System.out.println(w + " " + "null");
        else {
          String clusterName = bestpar.getKey().replaceFirst("^m_", "");

          System.out.println(w + " " + bestpar.toString() + "\t" + model.idx().syntPars().get(clusterName) + "\t#\t"
              + model.getWord(bestpar.getKey()).paradigmWords);
        }
      }
    }
    for (Cluster c : model.idx().syntPars().values()) {
      System.out.println(c.getLabel() + "\t" + c.toString());
    }
    System.out.println();
    for (String par : model.idx().knownParadigmLabels) {
      System.out.println(par + "\t" + model.getWord(par).paradigmWords);
    }

  }
  private void printClustersLast(WordSequences model, int round, boolean getBestPar, int contextcount) {
    System.out.println("Round: " + round + "\t" + model.idx().getSyntParadigms().size());
    for(Cluster c: model.idx().getSyntParadigms()) {
      System.out.println(c.getLabel() + "\t" + c.getParadigmWords().size()
          + "\t" + c.getParadigmWordsSorted());
    }  
  }
    private void printClusters(WordSequences model, int round, boolean getBestPar, int contextcount) {
    System.out.println("Round: " + round + "\t" + model.idx().getSyntParadigms().size());
	for(Cluster c: model.idx().getSyntParadigms()) {
	  System.out.println(c.getLabel() + "\t" + c.bestCluster+ "\t" + c.getParadigmWords().size()
	      + "\t" + c.getParadigmWordsSorted());
	}
//	System.out.println();
//    System.out.println("Round insight: " + round + "\t" + model.idx().getSyntParadigms().size());
//
//   for(Cluster c: model.idx().getSyntParadigms()) {
//
//	  if(getBestPar || PARADIGM_PREF.startsWith("y") || PARADIGM_PREF.startsWith("z_")) {
//		List<MyPair> bestpairS = 
//			MorphVectorAnalyzer.computeBestKnownVectorParadigmS(model.getWord(c.getLabel()), Words.SYN_FILTER, model, false, Double.MAX_VALUE, contextcount);
//		MyPair bestpair = SyntParVectorTrain.getBestParOtherThanThisOrNull(bestpairS, c.getLabel(), 20.0);
//		if(getBestPar) { 
//		  if(bestpair == null) System.out.println("BEST\tnull"); 
//		  else System.out.println("BEST\t"+bestpair.toString()+"\t"+model.idx().getSyntParadigm(bestpair.first).getParadigmWordsSorted());
//	      System.out.println();
//		}
//		MyPair bestpairOfBest = null;
//		if(bestpair != null) {
//		  bestpairS = 
//	            MorphVectorAnalyzer.computeBestKnownVectorParadigmS(model.getWord(bestpair.first), Words.SYN_FILTER, model, false, Double.MAX_VALUE, contextcount);
//	        bestpairOfBest = SyntParVectorTrain.getBestParOtherThanThisOrNull(bestpairS, bestpair.first, 20.0);
//		}
//		if(PARADIGM_PREF.startsWith("y") || PARADIGM_PREF.startsWith("z_") ) {
//		  if(bestpair == null) {
//		    System.out.println("test: " + c.toStringInfo());
//		    System.out.println("best: " + "null");
//		  }else {
//		    Cluster best = model.idx().getSyntParadigm(bestpair.first);
//		    System.out.println("test: " + c.toStringInfo());
//            System.out.println("best: " + bestpair.freq + " " + best.toStringInfo());
//            if(bestpairOfBest != null && bestpairOfBest.first.equals(c.getLabel()))
//              System.out.println("sameBoB: TRUE");
//            else System.out.println("sameBoB: FALSE");
//		    SyntParVectorTrain.allowCombineMorphCheck(c, best, model, contextcount, true);
//		     Words.printExpectations(model.getWord(c.getLabel()), 0.01, model);
//		     Words.printExpectations(model.getWord(best.getLabel()), 0.01, model);
//
//		  }
//          System.out.println();
//		}
//	  }
//	}
	if(getBestPar) {
	  System.out.println("last clusters");
	  printClusters(model, round, false, contextcount);
	  //FORSCHUNG
	    MorphVectorAnalyzer.collectMParVectorsParadigm(model, Words.SYN_FILTER, false, 40);
	    for(Cluster c: model.idx().syntPars().values()) {
	      BuildWordSequenceModel.printSyntParadigmInsight(model, c);
	    }
	}
	

  }	
	



}
