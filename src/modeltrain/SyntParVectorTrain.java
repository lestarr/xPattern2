package modeltrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.MorphModel;
import model.MorphVectorAnalyzer;
import model.WordSequences;
import model.Words;
import modelparts.MorphParadigm;
import modelparts.Word;
import modelutils.Cluster;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;

public class SyntParVectorTrain {
  
  public static final double SMALL_THH = 3.0;
  private static final String MZERO = "mZero";
  private static final String SPLITTER = "mSplitter";

  public static boolean combineClusters(WordSequences model, int howmany, double simthh, int round,
      int minParadigmNumber, int clusterMinMembers, int contextcount) {
    // combine most similar clusters, based on clusters' vectors
    // for each cluster x find its second best cluster y
    // if sim(x,y) < 5.0 && y is second best for x --> combine: put paradigm words
    // from y into x, delete paradigm words from y
//    writeBestClustersForFirstThreeMembers(model);
    boolean combineIsPossible = false;
    if (model.idx().getSyntParadigms().size() < minParadigmNumber) {
      System.out.println("BREAK: min paradigm reached : " + minParadigmNumber);
      return false;
    }
    prepareVectorsForTagging(model, false, round, contextcount); // needed for tagging of clusters and finding best cluster to combine
    if (combineANDisPossible(model, simthh, contextcount)) {
      System.out.println("BREAK: combine AND was possible");
      return false;
    }
    for (Cluster c : model.idx().getSyntParadigms()) {
      if (c.getParadigmWords().size() == 0) continue;
      MyPair p2 = combine(c, model, simthh, contextcount, 2);
      if (p2 != null) {
        Cluster cbest = model.idx().getSyntParadigm(p2.first);
//        System.out.println("DEBUG: " + "for " +c + " is best " + p2.toString() + " " + cbest);
        if (c.isAndCluster || cbest.isAndCluster)
          continue;
        boolean allowCombine = false;
//        if(model.idx().getMorphParadigms() == null || model.idx().getMorphParadigms().isEmpty())
        allowCombine = true; //allowClusterCombine(c, cbest, model, contextcount);
        
//        if(!allowCombine && model.idx().getMorphParadigms() != null && !model.idx().getMorphParadigms().isEmpty())
//          allowCombine = allowCombineMorphCheck(c, cbest, model, contextcount, false);
//        boolean allowCombineExpec = allowCombineCheckExpectations(c, cbest, model);
//        if(allowCombine && allowCombineExpec) {
//          System.out.println("R"+round+" AGGREE: " + c.toStringInfo());
//          System.out.println("R"+round+" AND: " + cbest.toStringInfo());
//        }
//        if(allowCombine && !allowCombineExpec) {
//          System.out.println("R"+round+" FORBID: " + c.toStringInfo());
//          System.out.println("R"+round+" AND: " + cbest.toStringInfo());
//        }
//        if(!allowCombine && allowCombineExpec) {
//          System.out.println("R"+round+" ALLOW: " + c.toStringInfo());
//          System.out.println("R"+round+" AND: " + cbest.toStringInfo());
//        }
        if (allowCombine) {
          if(combineClustersIntern(c, cbest, p2.freq, model, "1"))        
            combineIsPossible = true;
          else {
            p2 = combine(c, model, simthh, contextcount, 3);
            if (p2 != null) 
              cbest = model.idx().getSyntParadigm(p2.first);
            if(combineClustersIntern(c, cbest, p2.freq, model, "2"))        
              combineIsPossible = true;
          }
        } else {
          System.out.println("NOT COMB: " + c.toStringInfo());
          System.out.println("AND: " + p2.freq + " " + cbest.toStringInfo());
        }
      } 
//      else {
//        p2 = combine2(c, model, simthh, contextcount);
//        if (p2 != null && p2.freq < SMALL_THH) {
//          Cluster cbest = model.idx().getSyntParadigm(p2.first);
//          boolean allowCombine = false;
//          if(model.idx().getMorphParadigms() == null || model.idx().getMorphParadigms().isEmpty())
//            allowCombine = allowClusterCombine(c, cbest, model, contextcount);
//          else allowCombine = allowCombineMorphCheck(c, cbest, model, contextcount, false);
//          if (allowCombine) {
//            if(combineClustersIntern(c, cbest, p2.freq, model, "2"))
//            combineIsPossible = true;
//          } else {
//            System.out.println("NOT COMB2: " + c.getLabel() + " " + c.getParadigmWords());
//            System.out.println("AND: " + cbest.getLabel() + " " + p2.freq + " " + cbest.getParadigmWords());
//          }
//        }
//      }
    }

    // delete clusters with no paradigm words
    deleteCLustersWithNoParWords(model, round, true, clusterMinMembers);
    return combineIsPossible;
  }

  private static MyPair combine(Cluster c, WordSequences model, double simthh, int contextcount, int moveIndexToLeft) {
    
    // get Best paradigms for this cluster
    List<MyPair> bestparlist = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(model.getWord(c.getLabel()),
        Words.SYN_FILTER, model, false, simthh, contextcount);
    MyPair secondBest = MorphVectorAnalyzer.getSecondBestPair(bestparlist, contextcount, moveIndexToLeft);
    c.bestCluster = secondBest; // bestparlist.get(bestparlist.size() - 2);
  
    return secondBest;
  
  }

  private static MyPair combine(Cluster c, WordSequences model, double simthh, int contextcount) {
      
      // get Best paradigms for this cluster
      List<MyPair> bestparlist1 = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(model.getWord(c.getLabel()),
          Words.SYN_FILTER, model, false, simthh, contextcount);
      MyPair p = getBestParOtherThanThisOrNull(bestparlist1, c.getLabel(), simthh);
      c.bestCluster = p; // bestparlist.get(bestparlist.size() - 2);
      
      boolean print = true;
      print = true;
    if(print) {
      if(bestparlist1.size() > 5)
        bestparlist1 = bestparlist1.subList(bestparlist1.size()-5, bestparlist1.size());
      System.out.println("DEBUG " + c.getLabel() + " " + c.getParadigmWords().size() 
      + " " +c.getParadigmWordsSorted().subList(0, Math.min(c.getParadigmWords().size(), 4))+ " ... " + bestparlist1);
    }
    if (c.getParadigmWords().size() == 0)
      return null;
    if (p == null)
      return null;
  //    String bestparLabel = p.first;
  //    // get best paradigm for best cluster
  //    List<MyPair> bestparlist2 = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(model.getWord(bestparLabel), Words.SYN_FILTER,
  //        model, false, simthh, contextcount);
  //    MyPair p2 = getBestParOtherThanThisOrNull(bestparlist2, bestparLabel, simthh);
  //    if (p2 == null)
  //      return null;
  //    String bestparOfBest = p2.first;
  //    // if best paradigms match and < 5.0 --> combine
  //    if (c.getLabel().equals(bestparOfBest)) {
  //      return p;
  //    }
      /*
      //check second best of tested cluster
      else if(bestparlist1.size() > 2 && bestparlist1.get(bestparlist1.size() - 3)!= null){
        MyPair toReturn = checkSecondBest(c, bestparlist1.get(bestparlist1.size() - 3), model, simthh, contextcount);
        if(toReturn != null) return toReturn;
      }
      //check second best of best par if it == tested cluster
      if(bestparlist2.size() > 2 && bestparlist2.get(bestparlist2.size() - 3)!= null) {
        MyPair p3 = bestparlist2.get(bestparlist2.size() - 3);
        if (c.getLabel().equals(p3.first)) {
          System.out.println("debug3: THIRDBEST of best "  + p.first + " for " + c.getLabel());
          return p;
        }
      }
      return null; */
      return p;
    }

  private static boolean allowCombineBasedOnSize(Cluster c, Cluster cbest, WordSequences model) {
    int csize = c.getParadigmWords().size();
    int cbestsize = cbest.getParadigmWords().size();
    if( (csize*10 < cbestsize) || (cbestsize * 10 < csize) )
      return false;
    double meanC = c.getFreqOfAllWOrds(model) / csize;
    double meanCbest = cbest.getFreqOfAllWOrds(model) / cbestsize;
    if(meanC*10.0 <= meanCbest || meanCbest*10.0 <= meanC)
      return false;
    return true;
  }

  private static boolean allowCombineCheckExpectations(Cluster c, Cluster cbest, WordSequences model) {
    Set<String> expTestCluster = new HashSet<>();
    Set<String> expBest = new HashSet<>();
    Word cWord = model.getWord(c.getLabel());
    Word cbestWord = model.getWord(cbest.getLabel());
    List<MyPairWord> expLtest = Words.getExpectationsLeftRightSorted(cWord, true, 0.01, model, "([sbtf]_.+)");
    List<MyPairWord> expLbest = Words.getExpectationsLeftRightSorted(cbestWord, true, 0.01, model, "([sbtf]_.+)");
    List<MyPairWord> expRtest = Words.getExpectationsLeftRightSorted(cWord, false, 0.01, model, "([sbtf]_.+)");
    List<MyPairWord> expRbest = Words.getExpectationsLeftRightSorted(cbestWord, false, 0.01, model, "([sbtf]_.+)");
    expTestCluster = getNBestExp(expTestCluster, expLtest, 2);
    expBest = getNBestExp(expBest, expLbest, 2);
    boolean setsAreMatching = checkSetMatching(expTestCluster, expBest);
    if(setsAreMatching) {
      expTestCluster = getNBestExp(expTestCluster, expRtest, 2);
      expBest = getNBestExp(expBest, expRbest, 2);
      setsAreMatching = checkSetMatching(expTestCluster, expBest);
      if(setsAreMatching) return true;
    }else {
      //if first two expectations match - is good, the third doesn't have to match, otherwise check 3
      expTestCluster = getNBestExp(expTestCluster, expLtest, 3);
      expBest = getNBestExp(expBest, expLbest, 3);
      setsAreMatching = checkSetMatching(expTestCluster, expBest);
      if(!setsAreMatching) return false;
      expTestCluster = getNBestExp(expTestCluster, expRtest, 3);
      expBest = getNBestExp(expBest, expRbest, 3);
      setsAreMatching = checkSetMatching(expTestCluster, expBest);
      if(setsAreMatching) return true;
    }
    return false;
  }

  private static boolean checkSetMatching(Set<String> expTestCluster, Set<String> expBest) {
    for(String s: expTestCluster) 
      if(!expBest.contains(s)) 
        return false;
    return true;
  }

  private static Set<String> getNBestExp(Set<String> expSet, List<MyPairWord> exp, int howmany) {
    expSet = new HashSet<>();
    for (int i = 0; i < Math.min(exp.size(), howmany); i++) {
      expSet.add(exp.get(i).left.toString());
    }
    return expSet;
  }

  private static void writeBestClustersForFirstThreeMembers(WordSequences model, int contextcount) {
    for (Cluster c : model.idx().getSyntParadigms()) {
      int i = 0;
      String debug = "";
      for (MyPair mp : c.getParadigmWordsSorted()) {
        if (i > 2)
          break;
        Word w = model.getWord(mp.first);
        List<MyPair> plist = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(w, Words.SYN_FILTER, model, false,
            Double.MAX_VALUE, contextcount);
        MyPair bestpar = getBestParOtherThanThisOrNull(plist, c.getLabel(), Double.MAX_VALUE);
        debug = debug + "_" + bestpar.first;
        i++;
      }
      System.out.println("FOR PAR " + c.getLabel() + " BESTies: " + debug);
    }
  }

  private static boolean combineANDisPossible(WordSequences model, double simthh, int contextcount) {
    Cluster AND_cluster = null;
    String AND_string = model.getAndString();
    if (AND_string == null)
      return false;
    for (Cluster c : model.idx().getSyntParadigms()) {
      for (MyPair p : c.getParadigmWords()) {
        if (p.first.equals(AND_string)) {
          AND_cluster = c;
          AND_cluster.isAndCluster = true;
          if (combine(AND_cluster, model, SMALL_THH, contextcount) != null)
            return true;
          MyPair p2 = combine2(c, model, simthh, contextcount);
          if (p2 != null && p2.freq < SMALL_THH)
            return true;
        }
      }
    }
    return false;
  }
  
  private static MyPair combine2(Cluster c, WordSequences model, double simthh, int contextcount) {
    if (c.getParadigmWords().size() == 0)
      return null;
    // get Best paradigms for this cluster
    List<MyPair> bestparlist = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(model.getWord(c.getLabel()),
        Words.SYN_FILTER, model, false, simthh, contextcount);
    MyPair p = getBestParOtherThanThisOrNull(bestparlist, c.getLabel(), simthh);
    if (p == null)
      return null;
    c.bestCluster = bestparlist.get(bestparlist.size() - 2);
    return p;
  }
  
  private static MyPair checkSecondBest(Cluster c, MyPair secondBest, WordSequences model, double simthh, int contextcount) {
    List<MyPair> bestparlist = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(model.getWord(secondBest.first), Words.SYN_FILTER,
        model, false, simthh, contextcount);
    MyPair p2 = getBestParOtherThanThisOrNull(bestparlist, secondBest.first, simthh);
    if (p2 == null)
      return null;
    String bestparOfBest = p2.first;
    // if best paradigms match and < 5.0 --> combine
    if (c.getLabel().equals(bestparOfBest)) {
      System.out.println("debug2: THIRDBEST "  + secondBest.first + " for " + c.getLabel());
      return secondBest;
    }
    return null;
  }

  public static void prepareVectorsForTagging(WordSequences model, boolean useClean, int roundNr, int contextcount) {

    if (useClean)
      cleanClustersParWords(model); // leaves only words with best score
//	  SyntParVectorTrain.deleteClusterWords(model);
    System.out.println("delete cwords done");

    SyntParVectorTrain.addClusterWordsFromParWords(model, roundNr);
    System.out.println("add cwords done");

    model.collectKnownParVectors(Words.SYN_FILTER, contextcount);
    System.out.println("vectors computed");
  }

  public static boolean isAndCLuster(Cluster c, String AND_string) {
    for (MyPair p : c.getParadigmWords()) {
      if (p.first.equals(AND_string)) return true;
    }
    return false;
  }

  private static void cleanClustersParWords(WordSequences model) {
    // leaves only words with best score
    // best score: sorted words / 2, the last part (worser scores) is deleted
    // if we have < 4 par words --> they stay all to enable small grammatical
    // clusters like: und, oder
    List<MyPair> plist = new ArrayList<>();
    for (Cluster c : model.idx().getSyntParadigms()) {
      
//      double freqThh = getWordFrequencyThh(c.getParadigmWords(), model);
//      for (Iterator<MyPair> iterator = c.getParadigmWords().iterator(); iterator.hasNext();) {
//        MyPair myPair = (MyPair) iterator.next();
//        Word w = model.getWord(myPair.first);
//        if(w.freq() < freqThh) iterator.remove();
//      }
//      System.out.println("LEFT: "  + c.getLabel() + " " + c.getParadigmWords());
      
      if (c.getParadigmWords().size() < 6)
        continue;
      plist.clear();
      plist.addAll(c.getParadigmWords());
      Collections.sort(plist); // bigger score is bad, bad will be at the beginning

      int half = plist.size() / 2;
      if (half < 10)
        half = plist.size() - 10; //10 words should always stay
//      if(c.getParadigmWords().size() > 20)
//        half = plist.size()/3;
      
      for (int i = 0; i < half; i++) {
        c.getParadigmWords().remove(plist.get(i));
      }
//      for (Iterator iterator = c.getParadigmWords().iterator(); iterator.hasNext();) {
//        MyPair myPair = (MyPair) iterator.next();
//        if(myPair.freq > 6.0)
//          iterator.remove(); -- not nice effect
//      }
    }

  }

  private static double getWordFrequencyThh(Set<MyPair> paradigmWords, WordSequences model) {
    double biggestFreq = 0;
    for(MyPair p: paradigmWords) {
      if(model.getWord(p.first).freq() > biggestFreq) 
        biggestFreq = model.getWord(p.first).freq();
    }
    return (biggestFreq / 50);
  }

  public static MyPair getBestParOtherThanThisOrNull(List<MyPair> bestparlist, String clabel, double simthh) {
    if (bestparlist == null || bestparlist.size() == 0)
      return null;
    int last = bestparlist.size() - 1;
    MyPair bestpar = null;
    if (!bestparlist.get(last).first.equals(clabel) && bestparlist.get(last).freq < simthh)
      bestpar = bestparlist.get(last);
    else if (bestparlist.size() > 1 && bestparlist.get(last - 1).freq < simthh)
      bestpar = bestparlist.get(last - 1); // get second best - this is the main case
    return bestpar;
  }

  private static boolean combineClustersIntern(Cluster c, Cluster cbest, double sim, WordSequences model, String info) {
    
//    if(clustersAreSyntacticalNeighbours(c,cbest,model)) {
//      System.out.println("NO COMB: " + c.getLabel() + " " + c.getParadigmWordsSorted());
//      System.out.println("AND: " + cbest.getLabel() + " " + cbest.getParadigmWordsSorted());
//      return false;
//    }
    
    if (cbest.getParadigmWords().size() == 0 && cbest.combinedToCLuster != null) {
      Cluster upCluster = model.idx().getSyntParadigm(cbest.combinedToCLuster);
      cbest = c;
      c = upCluster;
    }

    // do not combine AND_cluster
    if (c.isAndCluster || cbest.isAndCluster)
      return false;
    double meanC = c.getFreqOfAllWOrds(model) / c.getParadigmWords().size();
    double meanCbest = cbest.getFreqOfAllWOrds(model) / cbest.getParadigmWords().size();
    if(!allowCombineBasedOnSize(c, cbest, model)) {
      System.out.println("FORBIDDEN COMB for "+ MyUtils.rdouble(meanC) + " " + c.toStringInfo());
      System.out.println("AND: "+ MyUtils.rdouble(meanCbest) + " " + cbest.toStringInfo());
      return false;
    }
    if(!allowCombineBasedOnProminentFeature(c, cbest, model)) {
      System.out.println("NO COMB for " + c.toStringInfo());
      System.out.println("AND: " + cbest.toStringInfo());
      return false;
    } 
    
    System.out.println("COMB" + info + ": " + c.getLabel() + " " + c.getParadigmWords().size() +" "
    + c.getFreqOfAllWOrds(model) + " " + MyUtils.rdouble(meanC) + " "+ c.getParadigmWordsSorted());
    System.out.println("AND" + info + ": " + cbest.getLabel() + " " + cbest.getParadigmWords().size() +" "
        + cbest.getFreqOfAllWOrds(model) + " " + MyUtils.rdouble(meanCbest)+ " " + MyUtils.rdouble(sim) + " " + cbest.getParadigmWordsSorted());
    Words.printExpectations(model.getWord(c.getLabel()), 0.01, model);
    Words.printExpectations(model.getWord(cbest.getLabel()), 0.01, model);
    // put paradigm words from y into x, delete paradigm words from y
		for(MyPair w: cbest.getParadigmWordsSorted()) {
			c.addParadigmWord(w, model.getWord(w.first).freq(), model.getFreqOfAnd()/50.0);
		}
    cbest.clearParadigmWords();
    cbest.combinedToCLuster = c.getLabel();
    return true;
  }
  private static boolean allowCombineBasedOnProminentFeature(Cluster c, Cluster cbest, WordSequences model) {

    boolean print = true;
    List<MyPair> featureList = getMorphFreqs(c.getParadigmWords(),model);
    if(print) System.out.println("morph: " + c.getLabel() + " " + featureList.toString());
    if(featureList.size() == 0) return true;
    c.firstMpar = featureList.get(0);
    String mainFeature = c.firstMpar.first;
    double mainFreq = c.firstMpar.freq;
    featureList = getMorphFreqs(cbest.getParadigmWords(),model);
    if(print) System.out.println("morph: " + cbest.getLabel() + " " + featureList.toString());

    if(featureList.size() == 0) return true;
    cbest.firstMpar = featureList.get(0);
    String mainFeatureBest = cbest.firstMpar.first;
    double mainFreqBest = cbest.firstMpar.freq;
//    if(c.getParadigmWords().size() < 6 || cbest.getParadigmWords().size() < 6)
//      return true;
    if((mainFeature.equals(MZERO) && mainFreq == 1.0) || (mainFeatureBest.equals(MZERO) && mainFreqBest == 1.0))
      return true; // case for article and other functional word parts
    if(!mainFeature.equals(mainFeatureBest)) return false;
    double difference = Math.abs(mainFreqBest-mainFreq);
    if(difference > 0.33) return false;
    return true;
  }

  private static List<MyPair> getMorphFreqs(Set<MyPair> paradigmWords, WordSequences model) {
    Map<String,Double> morphFreqs = new HashMap<>();
    for(MyPair parWord: paradigmWords) {
      String w = parWord.first;
      Word word = model.getWord(w);
      MorphParadigm mp = word.getMorphParadigm();
      String morphLabel = MZERO;
      if(mp != null) morphLabel = mp.getLabel();
      MapsOps.addFreq(morphLabel, morphFreqs);
    }
    List<MyPair> mpList = new ArrayList<>();
    for(String s: morphFreqs.keySet()) {
      mpList.add(new MyPair(s,  "", ((double)morphFreqs.get(s)/paradigmWords.size())));
    }
    Collections.sort(mpList);
    return mpList;
  }

  private static boolean allowClusterCombine(Cluster mainCluster, Cluster cbest, WordSequences model, int contextcount) {
    if(checkBestParForSeedWords(mainCluster, cbest, model, contextcount)) {
//      if(checkBestParForSeedWords(cbest, mainCluster, model, contextcount))
        return true;
    }
    return false;
  }
  public static boolean allowCombineMorphCheck(Cluster c, Cluster best, WordSequences model, int contextcount, boolean print) {
    if(model.idx().getMorphParadigms().size() > 0) {
      if(MorphVectorAnalyzer.mparContextsMapLeft.isEmpty()) MorphVectorAnalyzer.collectMParVectorsParadigm(model, Words.SYN_FILTER, false, contextcount);
      if(MorphVectorAnalyzer.mparContextsMapLeftFlexion.isEmpty()) MorphVectorAnalyzer.collectMParVectorsFlexion(model, Words.SYN_FILTER, false, contextcount);
    }
    Word cWord = model.getWord(c.getLabel());
    MyPair bestpar = MorphVectorAnalyzer.getBestPar(MorphVectorAnalyzer.getMParFromParVector(model, cWord, false, Words.SYN_FILTER, false, contextcount));
    if(bestpar == null) return false;
    String bplabel = MorphModel.getMPlabelFromFlexPar(bestpar.first).replaceFirst("e|(#.+)", "");
    String debug = bestpar.toString() + " ";
    MyPair bestparFlex = MorphVectorAnalyzer.getBestPar(MorphVectorAnalyzer.getMParFromFlexVector(model, cWord, false, Words.SYN_FILTER, false, contextcount));
    if(bestparFlex == null) return false;

    String bpFlexlabel = MorphModel.getMPlabelFromFlexPar(bestparFlex.first).replaceFirst("e|(#.+)", "");
    debug = debug + bestparFlex.toString() + " ";
   
    Word bestCword = model.getWord(best.getLabel());
    MyPair bestpar2 = MorphVectorAnalyzer.getBestPar(MorphVectorAnalyzer.getMParFromParVector(model, bestCword, false, Words.SYN_FILTER, false, contextcount));
    if(bestpar2 == null) return false;

    String bplabel2 = MorphModel.getMPlabelFromFlexPar(bestpar2.first).replaceFirst("e|(#.+)", "");
    debug = debug + bestpar2.toString() + " ";
    MyPair bestparFlex2 = MorphVectorAnalyzer.getBestPar(MorphVectorAnalyzer.getMParFromFlexVector(model, bestCword, false, Words.SYN_FILTER, false, contextcount));
    if(bestparFlex2 == null) return false;

    String bpFlexlabel2 = MorphModel.getMPlabelFromFlexPar(bestparFlex2.first).replaceFirst("e|(#.+)", "");
    debug = debug + bestparFlex2.toString() + " ";

    if(print) System.out.println(debug);
    System.out.println(bplabel+bpFlexlabel+bplabel2+bpFlexlabel2);
    if(bplabel.equals(bplabel2) && bplabel.equals(bpFlexlabel) && bplabel.equals(bpFlexlabel2)) return true;
    return false;
  }

  private static boolean checkBestParForSeedWords(Cluster mainCluster, Cluster cbest, WordSequences model, int contextcount) {
//    if(model.idx().getMorphParadigms().size() > 0) {
//      if(MorphVectorAnalyzer.mparContextsMapLeft.isEmpty()) MorphVectorAnalyzer.collectMParVectorsParadigm(model, Words.SEM_FILTER, false, contextcount);
//      if(MorphVectorAnalyzer.mparContextsMapLeftFlexion.isEmpty()) MorphVectorAnalyzer.collectMParVectorsFlexion(model, Words.SEM_FILTER, false, contextcount);
//      BuildWordSequenceModel.printMorphAssociations(mainCluster, model, contextcount);
//      BuildWordSequenceModel.printMorphAssociations(cbest, model, contextcount);
//    }
     int sumMainClustersParadigms = 0;
    int i = 0;
    String debug = "";
    for (MyPair mp : cbest.getParadigmWordsSorted()) {
      if (i > 2)
        break;
      Word w = model.getWord(mp.first);
      List<MyPair> plist = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(w, Words.SYN_FILTER, model, false,
          Double.MAX_VALUE, contextcount);
      MyPair bestpar = getBestParOtherThanThisOrNull(plist, cbest.getLabel(), Double.MAX_VALUE);
      debug = debug + " " + bestpar.toString();
      if (bestpar.first.equals(mainCluster.getLabel()))
        sumMainClustersParadigms++;
      i++;
    }
    if (sumMainClustersParadigms > 1 ) { //|| (sumMainClustersParadigms == 1 && cbest.getParadigmWords().size() == 2)) {
      System.out.println("DEBUG " + cbest.getLabel() + " is GOOD for: " + debug);
      return true;
    }
    System.out.println("NOTGOOD " + cbest.getLabel() + " FOR: " + debug);
    return false;
  }

  private static boolean allowClusterCombineOld(Cluster c, Cluster cbest, WordSequences model) {
    double meanC = c.getFreqOfAllWOrds(model) / c.getNrOfAllWOrds();
    double meanCbest = cbest.getFreqOfAllWOrds(model) / cbest.getNrOfAllWOrds();
    if ((meanC / meanCbest) > 5.0 || (meanCbest / meanC) > 5.0)
      return false;
    return true;
  }

  public static void deleteCLustersWithNoParWords(WordSequences model, int round, boolean clusterWasCleaned,
      int clusterMinMembers) {
    List<Cluster> clustersToRemove = new ArrayList<>();

    double clusterFreqMedian = getCLusterFreqMedian(model);
    double clusterMemberNrMedian = getClusterMemberMedian(model);

    for (Cluster c : model.idx().getSyntParadigms()) {
      double freq = c.getFreqOfAllWOrds(model);
      double memberNr = c.getParadigmWords().size();
      if (memberNr < 2)
        clustersToRemove.add(c);
      else if (round > 2 && memberNr < clusterMinMembers
//            else if(clusterWasCleaned && round > 2 && memberNr < 25 
//                && freq < clusterFreqMedian && memberNr <= clusterMemberNrMedian) 
//              clustersToRemove.add(c);
//            else if(!clusterWasCleaned && round > 2 && memberNr < 50 
          && freq < clusterFreqMedian && memberNr <= clusterMemberNrMedian)
        clustersToRemove.add(c);
    }
    for (Cluster c : clustersToRemove) {
      if (isAndCLuster(c, model.getAndString()))
        continue;
      System.out.println("DELETED: " + c.getLabel() + " " + c.getFreqOfAllWOrds(model) + " " + c.getNrOfAllWOrds() + " "
          + c.getParadigmWords() + " " + c.getWords());
      deleteCluster(model, c);
    }
  }

  private static double getClusterMemberMedian(WordSequences model) {
    List<Double> list = new ArrayList<>();
    for (Cluster c : model.idx().getSyntParadigms()) {
      double memberNr = c.getParadigmWords().size();
      list.add(memberNr);
    }
    Collections.sort(list);
    return list.get(list.size() / 3);
  }

  private static double getCLusterFreqMedian(WordSequences model) {
    List<Double> list = new ArrayList<>();
    for (Cluster c : model.idx().getSyntParadigms()) {
      double freq = c.getFreqOfAllWOrds(model);
      list.add(freq);
    }
    Collections.sort(list);
    return list.get(list.size() / 3);
  }

  public static void deleteCLustersWithNoParWordsOld(WordSequences model) {
    List<Cluster> clustersToRemove = new ArrayList<>();

    double clusterFreqMax = getCLusterFreqMax(model);
    double clusterMeanMax = getCLusterMeanMax(model);

    double clusterFreqTHH = clusterFreqMax / 100;
    double clusterMeanTHH = clusterMeanMax / 100;

    for (Cluster c : model.idx().getSyntParadigms()) {
      double freq = c.getFreqOfAllWOrds(model);
      if (c.getParadigmWords().size() < 2)
        clustersToRemove.add(c);
      else if (freq < clusterFreqTHH)
        clustersToRemove.add(c);
      else if ((freq / c.getParadigmWords().size()) < clusterMeanTHH)
        clustersToRemove.add(c);
    }
    for (Cluster c : clustersToRemove) {
      System.out.println("DELETED: " + c.getLabel() + " " + c.getFreqOfAllWOrds(model) + " " + c.getNrOfAllWOrds() + " "
          + c.getWords());
      deleteCluster(model, c);
    }
  }

  private static double getCLusterFreqMax(WordSequences model) {
    double max = 0;
    for (Cluster c : model.idx().getSyntParadigms()) {
      double freq = c.getFreqOfAllWOrds(model);
      if (freq > max)
        max = freq;
    }

    return max;
  }

  private static double getCLusterMeanMax(WordSequences model) {
    double max = 0;
    for (Cluster c : model.idx().getSyntParadigms()) {
      double mean = c.getFreqOfAllWOrds(model) / c.getParadigmWords().size();
      if ((mean) > max)
        max = mean;
    }

    return max;
  }

  public static void deleteCluster(WordSequences model, Cluster c) {
    model.idx().deletedParadigmLabels.add(c.getLabel());
    model.idx().deleteSyntParadigm(c.getLabel());
  }

  public static void deleteClusterWords(WordSequences model) {
    for (Cluster c : model.idx().getSyntParadigms()) {
      Word cword = model.getWord(c.getLabel());
      model.deleteCategory(cword);
    }
  }

  public static void addClusterWordsFromParWords(WordSequences model, int round) {
    for (Cluster c : model.idx().getSyntParadigms()) {
      String oldLabel = c.getLabel();
      String newLabel;
      if(round == -1) //case for rename after training
        newLabel = oldLabel.replaceFirst("#[0-9]+$", "e");
      else
       newLabel = getNewLabel(oldLabel, round);
      model.idx().knownParadigmLabels.remove(oldLabel);
      model.idx().deletedParadigmLabels.add(oldLabel);
      c.setLabel(newLabel);

      // add new cluster words
      for (MyPair w : c.getParadigmWords()) {
        model.addCategory(c.getLabel(), model.getWord(w.first));
      }
    }
    for (String deleted : model.idx().deletedParadigmLabels) {
      Cluster cDel = model.idx().getSyntParadigm(deleted);
      if (cDel == null)
        continue;
      model.idx().deleteSyntParadigm(deleted);
      model.idx().addSyntParadigm(cDel);
    }
  }

  public static String getNewLabel(String oldLabel, int round) {
    String newLabel = oldLabel.replaceFirst("#[0-9]+$", "");
    newLabel = newLabel + "#" + Integer.toString(round);
    return newLabel;
  }

  public static void addClusterWordsFromParWordsOld(WordSequences model, int round) {
    for (Cluster c : model.idx().getSyntParadigms()) {
      for (MyPair w : c.getParadigmWords()) {
        model.addCategory(c.getLabel(), model.getWord(w.first));
      }
    }
  }

  public static void ClearParWordsInClusters(WordSequences model) {
    for (Cluster c : model.idx().getSyntParadigms()) {
      c.clearParadigmWords();
    }

  }

  public static void tagWords(WordSequences model, int howmany, double thh, boolean useSecondCuster, 
      int contextcount, boolean lastRound) {
    int i = 0;
//    model.collectKnownParVectors(Words.SEM_FILTER, contextcount);
    for (Word w : model.idx().getSortedWords()) {
      if (w.toString().contains("_") || w.toString().matches("EEEND|AAANF"))
        continue;
//      if(w.freq() < model.getWord(w.toString().toLowerCase()).freq())
//        continue;
      i++;
      if (i > howmany)
        break;
      List<MyPair> plist = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(w, Words.SYN_FILTER, model, false,
          Double.MAX_VALUE, contextcount);
      MyPair bestpar = MorphVectorAnalyzer.getBestPair(plist, contextcount);
//			System.out.println(w.toString() + "\t" + (bestpar == null ? "null" : bestpar));
      if (bestpar == null)
        continue;
      
      doSomeOutputs(model, contextcount, w, plist, bestpar);
      
      if (bestpar.freq > thh)
        continue;
      
      Cluster c = model.idx().getSyntParadigm(bestpar.first);
      if (useSecondCuster) {
        int leftshift = 1; //this is the index of first best cluster
        while(leftshift < 6) {
          leftshift++; // get next best cluster
          MyPair secondBest = MorphVectorAnalyzer.getSecondBestPair(plist, contextcount, leftshift);
          if (secondBest != null && (secondBest.freq - bestpar.freq) < 1.0 ) {
            Cluster c2 = model.idx().getSyntParadigm(secondBest.first);
            if(!allowWordInCluster(w, c2, secondBest.freq, model)) {
              continue;
            }
            c = checkFirstMorphFeature(w,c,c2,model);
          } 
        }
      }
      if(!allowWordInCluster(w, c, bestpar.freq, model)) {
        MyPair secondBest = MorphVectorAnalyzer.getSecondBestPair(plist, contextcount, 2);
        if(secondBest == null) continue;
        c = model.idx().getSyntParadigm(secondBest.first);

        if(!allowWordInCluster(w, c, secondBest.freq, model))
          continue;
//        else      System.out.println("NEW CLUSTER: " + secondBest.freq + " info: " + c.toStringInfoShort());
      }
      checkContextAndTag(w, c, bestpar.freq, model);
     }

  }

  private static Cluster checkFirstMorphFeature(Word w, Cluster c, Cluster c2, WordSequences model) {
    if(w.getMorphParadigm() == null || c.firstMpar == null || c2.firstMpar == null) return c;
    String wordMpar = w.getMorphParadigm().getLabel();
    double cFirstMparValue = 0.0;
    double c2firstMparValue = 0.0;
    if(c.firstMpar.first.equals(wordMpar)) cFirstMparValue = c.firstMpar.freq;
    if(c2.firstMpar.first.equals(wordMpar)) c2firstMparValue = c2.firstMpar.freq;
    if(c2firstMparValue > cFirstMparValue) {
//      System.out.println("CHANGED: " + w + " from "+ c.toStringInfoShort() + " to " + c2.toStringInfoShort());
      return c2;
    }
    return c;
  }

  private static boolean allowWordInCluster(Word w, Cluster c, double sim, WordSequences model) {
    
    double meanC = c.getFreqOfAllWOrds(model) / c.getParadigmWords().size();
    double wfreq = w.freq();
    if(meanC*10.0 <= wfreq || wfreq*10.0 <= meanC) {
//      System.out.println("WORD FORBIDDEN: " + w.toString() + " sim  " + sim + " into CLUSTER " + c.toStringInfoShort());
      return false;
    }
    return true;  
   }

  private static void doSomeOutputs(WordSequences model, int contextcount, Word w, List<MyPair> plist, MyPair bestpar) {
    if (w.toString().equals("und") || w.toString().equals("і") || w.toString().equals("oder")
        || w.toString().equals("та"))
      System.out.println("UND\t" + bestpar.toString());
    if (w.toString().equals("вона") || w.toString().equals("він") || w.toString().equals("вони")
        || w.toString().equals("ми")|| w.toString().equals("я"))
      System.out.println(w + "\t" + bestpar.toString());
    if(w.toString().equals("комуністи") || w.toString().equals("лікарі")) {
      System.out.println(w+"\t" + plist.subList(Math.max(0, plist.size()-5), plist.size()));
      String[] sarr = bestpar.first.split("#");
      
      System.out.println("cluster: " + model.knownMparContextsMapLeft.get(bestpar.first));
      System.out.println("cluster: " + model.knownMparContextsMapRight.get(bestpar.first));
      System.out.println("word: " + w.getBestContextsComputeNew(true, contextcount, Words.SYN_FILTER, model, false));
      System.out.println("word: " + w.getBestContextsComputeNew(false, contextcount, Words.SYN_FILTER, model, false));
    }
  }

  private static void checkContextAndTag(Word w, Cluster c, double freq, WordSequences model) {
    if(!clusterIsNeighbourOfWord(w, c, model)) {
      w.syntLabel = c.getLabel();
      //    c.addParadigmWord(w);
      c.addParadigmWord(new MyPair(w.toString(), "", freq), w.freq(), model.getFreqOfAnd() / 50.0);

    }
  }

  private static boolean clusterIsNeighbourOfWord(Word w, Cluster c, WordSequences model) {
    MyPairWord expWordLeftRight = getExpWord(w, true, 0.01, model);
//    System.out.println("CHECK: " + expWordLeftRight + " c: " + c.toString() + " " + c.getLabel());
    if(expWordLeftRight != null && expWordLeftRight.left.toString().equals(c.getLabel())) return true;
    expWordLeftRight = getExpWord(w, false, 0.01, model);
    if(expWordLeftRight != null && expWordLeftRight.left.toString().equals(c.getLabel())) return true;
    return false;
  }

  private static MyPair getBestOfTwo(MyPair bestpar, MyPair secondBest, WordSequences model, double thh, Word w) {
    // TODO Auto-generated method stub
    if (secondBest == null)
      return bestpar;
    if (secondBest.freq > thh)
      return bestpar;
    if ((secondBest.freq - bestpar.freq) > 0.5)
      return bestpar;
    // now we know secondBest is close, take it if the cluster secondbest is bigger

    Cluster cbest = model.idx().getSyntParadigm(bestpar.first);
    Cluster csecond = model.idx().getSyntParadigm(secondBest.first);

    if (cbest == null || csecond == null)
      return bestpar;
    if (csecond.getParadigmWords().size() > cbest.getParadigmWords().size()) {
//      System.out.println("SECONDBEST: " + w + " " + bestpar + " STATT " + secondBest);
      return secondBest;
    }

    return bestpar;
  }

  
  
  public static boolean combineClustersExperiment(WordSequences model, int round, 
      int minParadigmNumber, int seedMemberNr, int contextcount) {
    boolean combineIsPossible = false;
    if (model.idx().getSyntParadigms().size() < minParadigmNumber)
      return false;
    prepareVectorsForTagging(model, true, round, contextcount); // needed for tagging of clusters and finding best cluster to combine
    findClustersSeedMembers(model, seedMemberNr);
    writeBestClustersForSeedMembers(model, contextcount);
    findBestClustersBasedOnSeedMembers(model);
    if(combineBasedOnSeed(model)) 
      combineIsPossible = true;
    return combineIsPossible;
  }
  
  private static boolean combineBasedOnSeed(WordSequences model) {
    boolean combineWasPossible = false;
    for (Cluster c : model.idx().getSyntParadigms()) {
      if(c.getParadigmWords().isEmpty()) continue;
      if(c.bestCluster == null) continue;
      String cBestLabel = c.bestCluster.first;
      Cluster cBest = model.idx().getSyntParadigm(cBestLabel);

      if(cBest.getParadigmWords().isEmpty()) continue;
      if(cBest.bestCluster == null) continue;
      String bestOfcBest = cBest.bestCluster.first;
      if(bestOfcBest.equals(c.getLabel())) {
        if(combineClustersInternSeed(c, cBest, model))
          combineWasPossible = true;
      }
    }
    return combineWasPossible;
  }
  private static boolean combineClustersInternSeed(Cluster c, Cluster cBest, WordSequences model) {
    if(isAndCLuster(c, model.getAndString()) || isAndCLuster(cBest, model.getAndString())) return false;
    if(clustersAreSyntacticalNeighbours(c,cBest,model)) {
      System.out.println("NO COMB: " + c.getLabel() + " " + c.getParadigmWordsSorted());
      System.out.println("AND: " + cBest.getLabel() + " " + cBest.getParadigmWordsSorted());
      return false;
    }
    c.clearParadigmWords();
    cBest.clearParadigmWords();
    for(MyPair mp: c.seedMembers) c.addParadigmWord(mp);
    for(MyPair mp: cBest.seedMembers) c.addParadigmWord(mp);
    System.out.println("COMB: " + c.getLabel() + " " + c.bestSeedClustesScore + " " + c.seedMembers);
    System.out.println("AND: " + cBest.getLabel() + " " + cBest.bestSeedClustesScore + " " + cBest.seedMembers);
    return true;
  }
  
  private static boolean clustersAreSyntacticalNeighbours(Cluster c, Cluster cBest, WordSequences model) {
    Word cWord = model.getWord(c.getLabel());
    Word cBestWord = model.getWord(cBest.getLabel());
    return clustersAreSyntacticalNeighbours(cWord, cBestWord, model);
  }
  

  public static boolean clustersAreSyntacticalNeighbours(Word cWord, Word cBestWord, WordSequences model) {
    MyPairWord expWordLeftRight = getExpWord(cWord, true, 0.01, model);
    if(expWordLeftRight != null && expWordLeftRight.left.toString().equals(cBestWord.toString()) && expWordLeftRight.signif > 0.2) return true;
    expWordLeftRight = getExpWord(cWord, false, 0.01, model);
    if(expWordLeftRight != null && expWordLeftRight.left.toString().equals(cBestWord.toString())&& expWordLeftRight.signif > 0.2) return true;
    expWordLeftRight = getExpWord(cBestWord, true, 0.01, model);
    if(expWordLeftRight != null && expWordLeftRight.left.toString().equals(cWord.toString())&& expWordLeftRight.signif > 0.2) return true;
    expWordLeftRight = getExpWord(cBestWord, false, 0.01, model);
    if(expWordLeftRight != null && expWordLeftRight.left.toString().equals(cWord.toString())&& expWordLeftRight.signif > 0.2) return true;
    return false;
  }

  public static List<MyPairWord> getExpWords(Word cWord, boolean left, double signif, WordSequences model) {
    List<MyPairWord> exp = Words.getExpectationsLeftRightSorted(cWord, left, signif, model);
    return exp;
  }
  
  private static MyPairWord getExpWord(Word cWord, boolean left, double signif, WordSequences model) {
    List<MyPairWord> exp = getExpWords(cWord, left, signif, model);
    
    if(!exp.isEmpty() ) {
      if(exp.get(0).left.toString().matches("EEEND|ZZZAHL|AAANF")) {
        if(exp.size() > 1) return exp.get(1);
      }
       return exp.get(0);
    }
    return null;
  }
  
  public static MyPairWord getExpFirstWord( List<MyPairWord> exp) {
    
    if(!exp.isEmpty() ) {
      if(exp.get(0).left.toString().matches("EEEND|ZZZAHL|AAANF")) {
        if(exp.size() > 1) return exp.get(1);
      }
       return exp.get(0);
    }
    return null;
  }
  
  public static MyPairWord getExpSecondWord( List<MyPairWord> exp) {
    
    if(!exp.isEmpty() ) {
      if(exp.get(0).left.toString().matches("EEEND|ZZZAHL|AAANF")) {
        if(exp.size() > 2) return exp.get(2);
      }
      if(exp.size() > 1) return exp.get(1);
    }
    return null;
  }

  private static void findBestClustersBasedOnSeedMembers(WordSequences model) {
    for (Cluster c : model.idx().getSyntParadigms()) {
      double bestClusterScore = 0.0;
      for(MyPair mp: MapsOps.getFirstEntriesAsList(c.bestSeedClustesScore, c.bestSeedClustesScore.size())) {
        String bestClusterCandidate = mp.first;
        double candScore = mp.freq;
        if(candScore < 0.3) continue;
        Cluster bestCandCluster = model.idx().syntPars().get(bestClusterCandidate);
        if(bestCandCluster == null) {
          System.out.println("ERROR1: " + c.getLabel() + " cluster not found: " + bestClusterCandidate);
          continue;
        }
        double bestCandClusterScore = 0.0;
        if(bestCandCluster.bestSeedClustesScore.containsKey(c.getLabel()))
          bestCandClusterScore = bestCandCluster.bestSeedClustesScore.get(c.getLabel());
        if(bestCandClusterScore < 0.3) continue;
        double newScore = (candScore+bestCandClusterScore) / 2.0;
        if(newScore > 0.5 && newScore > bestClusterScore)
          c.bestCluster = new MyPair(bestClusterCandidate,"",newScore);
      }
    }
  }
  private static void writeBestClustersForSeedMembers(WordSequences model, int contextcount) {
    for (Cluster c : model.idx().getSyntParadigms()) {
      for (MyPair mp : c.seedMembers) {
        Word w = model.getWord(mp.first);
        List<MyPair> plist = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(w, Words.SYN_FILTER, model, false,
            Double.MAX_VALUE, contextcount);
        MyPair bestpar = getBestParOtherThanThisOrNull(plist, c.getLabel(), Double.MAX_VALUE);
        if(bestpar != null) {
          MapsOps.addFreq(bestpar.first, c.bestSeedClustesScore, 1.0);
        }
      }
      for(String cLabel: c.bestSeedClustesScore.keySet()) {
        double normalizedScore = c.bestSeedClustesScore.get(cLabel)/c.seedMembers.size();
        c.bestSeedClustesScore.put(cLabel, MyUtils.rdouble(normalizedScore));
      }
    }
  }
  private static void findClustersSeedMembers(WordSequences model, int seedMemberNr) {
    for (Cluster c : model.idx().getSyntParadigms()) {
      c.seedMembers.clear();
      c.bestSeedClustesScore.clear();
      c.bestCluster = null;
      int i = 1;
      for (MyPair mp : c.getParadigmWordsSorted()) {
        if (i > seedMemberNr)          break;
        c.seedMembers.add(mp);
        i++;
      }
    }
  }
}
