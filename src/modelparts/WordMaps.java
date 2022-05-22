package modelparts;

import model.MorphModel;
import model.MorphVectorAnalyzer;
import model.WordSequences;
import model.Words;
import modeltrain.SyntParVectorTrain;
import modelutils.Cluster;
import util.ListOps;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;

import java.util.*;

public class WordMaps {

  private Word word;
  private WordSequences model;

  public WordMaps(Word w, WordSequences model){
    this.word = w;
    this.model = model;
  }

  public static void setCcont(int ccont) {
    WordMaps.ccont = ccont;
  }

  private static int ccont = 40;

   public static final String MPCATS = "mpcats";
  public static final String SyCATS = "sycats";
  public static final String CAT_EXP_LEFTWORD = "expleftword";
  public static final String CAT_EXP_RIGHTWORD = "exprightword";
  public static final String FLEX_EXP_LEFTWORD = "flexexpleftword"; //additional boost for mp cats
  public static final String FLEX_EXP_RIGHTWORD = "flexexprightword";
  public static final String CAT_EXP_LEFTCAT = "expleftcat";
  public static final String CAT_EXP_RIGHTCAT = "exprightcat";


  public List<MyPair> getVotes_mp() {
    return votes_mp;
  }

  public List<MyPair> getVotes_sy() {
    return votes_sy;
  }

  public Map<String, List<String>> getMp_map() {
    return mp_map;
  }

  public Map<String, List<String>> getSy_map() {
    return sy_map;
  }

  private Map<String,List<String>> mp_map = new HashMap<>(1);
  private Map<String,List<String>> sy_map = new HashMap<>(1);
  private String bestMorphCatOfFirstSyntCat = null;

  private List<MyPair> votes_mp = new ArrayList<>(1);
  private List<MyPair> votes_sy = new ArrayList<>(1);

  public void fillMPcats(){

    List<String> sarrMP = getSavedCats(mp_map, MPCATS);
    sarrMP.clear();

    List<MyPair> bestmpars = MorphVectorAnalyzer.getMParFromParVector(model, word,
            false, Words.SYNSEM_FILTER, false, ccont);
    Collections.sort(bestmpars, Collections.reverseOrder());
    if(bestmpars.isEmpty()) bestmpars.add(new MyPair(SyntParVectorTrain.MZERO, "", 1.0));

    int i = 0;
    for (MyPair mpair: bestmpars){
      sarrMP.add(mpair.first);
      i++;
      if(i > 2) break;
    }
    //replace MPCATS
    this.mp_map.put(MPCATS, sarrMP);
    //replace vote for MorphSy
    if(sarrMP.size() > 0 && bestMorphCatOfFirstSyntCat != null
            && sarrMP.get(0).equals(bestMorphCatOfFirstSyntCat)){
      sarrMP.add(bestMorphCatOfFirstSyntCat); //boost fist morph cat
      if(sy_map.get(SyCATS) != null && sy_map.get(SyCATS).size() > 0
          && sy_map.get(SyCATS).get(0) != null)
        sy_map.get(SyCATS).add(sy_map.get(SyCATS).get(0)); // boost also first synt cat, as its main morph == first morph cat
    }
    //boost first cat
    sarrMP.add(sarrMP.get(0));
    //boost second, if its score is very close to the first
    if(bestmpars.size() > 1 && (bestmpars.get(1).freq - bestmpars.get(0).freq) < 0.5)
      sarrMP.add(sarrMP.get(1));
  }

  private List<String> getSavedCats(Map<String, List<String>> map, String key) {
    if(!map.containsKey(key)) map.put(key, new ArrayList<>());
    return map.get(key);
  }

  private void vote(List<MyPair> votes, Map<String,List<String>> catmap) {
    votes.clear();
    Map<String,Double> tmp_map = new HashMap<>();
    for(List<String> sarr: catmap.values()){
      for(String cat: sarr){
        MapsOps.addFreq(cat, tmp_map);
      }
    }
    for(String cat: tmp_map.keySet()){
      double vote = tmp_map.get(cat);
      MyPair catVote = new MyPair(cat, "", vote);
      votes.add(catVote);
    }
    Collections.sort(votes);
  }

  public void vote(){
    vote(votes_mp, mp_map);
    vote(votes_sy, sy_map);
  }




  public void fillSyntcats(){
    List<String> sarr = getSavedCats(sy_map, SyCATS);
    sarr.clear();

    List<MyPair> bestSyntpars = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(word, Words.SYN_FILTER,
            model, false, Double.MAX_VALUE,  ccont);
    Collections.sort(bestSyntpars, Collections.reverseOrder());
    int i = 0;
    for (MyPair mpair: bestSyntpars){
      sarr.add(mpair.first);
      i++;
      if(i > 2) break;
    }
    this.sy_map.put(SyCATS, sarr);

        //set bestMorphCatOfFirstSyntCat
    bestMorphCatOfFirstSyntCat = null;

    if(model.idx().getSyntParadigm(sarr.get(0)) != null){
      Cluster best = model.idx().getSyntParadigm(sarr.get(0));
      MyPair pair = best.getFirstMpar(model);
      if(pair != null){
        bestMorphCatOfFirstSyntCat = pair.first;
        //change vote for MORPH_SY
        if(ListOps.notNullEmpty(mp_map.get(MPCATS))
                && mp_map.get(MPCATS).get(0).equals(bestMorphCatOfFirstSyntCat)){
          mp_map.get(MPCATS).add(mp_map.get(MPCATS).get(0)); //as in MP
          sarr.add(sarr.get(0));
        }
      }
    }
    //boost first cat
    sarr.add(sarr.get(0));
    //boost second, if its score is very close to the first
    if(bestSyntpars.size() > 1 && (bestSyntpars.get(1).freq - bestSyntpars.get(0).freq) < 0.5)
      sarr.add(sarr.get(1));
  }

  //this word is right_of(previous)
  public void fillPrevContextCats(Word prev){
    List<MyPairWord> exp = getExp(prev, true);
    setContextCats(exp, MorphModel.MPREF, mp_map, CAT_EXP_LEFTWORD);
    setContextCats(exp, "[zyx]_", sy_map, CAT_EXP_LEFTWORD);
    setContextCats(exp, MorphModel.FPREF, mp_map, FLEX_EXP_LEFTWORD);

  }

  public void fillNextContextCats(Word next){
    List<MyPairWord> exp = getExp(next, false);
    setContextCats(exp, MorphModel.MPREF, mp_map, CAT_EXP_RIGHTWORD);
    setContextCats(exp, "[zyx]_", sy_map, CAT_EXP_RIGHTWORD);
    setContextCats(exp, MorphModel.FPREF, mp_map, FLEX_EXP_RIGHTWORD);

  }

  private List<MyPairWord> getExp(Word cont, boolean left) {
    List<MyPairWord> exp = Words.getExpectationsLeftRightSorted(cont, left, 0.1, model);
    if(exp.isEmpty()) exp = Words.getExpectationsLeftRightSorted(cont, left, 0.05, model);
    return exp;
  }

  public void setContextCats(List<MyPairWord> exp, String catPrefixRegex,
                             Map<String,List<String>> cat_map, String mapFeatureName){
    List<String> sarr = getSavedCats(cat_map, mapFeatureName);
    sarr.clear();
    for(MyPairWord mpw: exp){
      String cat = mpw.left.toString();
      if(cat.matches(catPrefixRegex+".+")){
        if(cat.startsWith(MorphModel.FPREF))
          cat = MorphModel.getMPlabelFromFlexPar(cat);
        sarr.add(cat);
      }
    }
    cat_map.put(mapFeatureName, sarr);
  }


   public void fillWmaps(){
    fillMPcats();
    fillSyntcats();
    if(!mp_map.get(MPCATS).isEmpty() && mp_map.get(MPCATS).get(0).equals(SyntParVectorTrain.MZERO))
      return; //don't look for neighbour cats, because MZERO is not trained yet, maybe use synt cats instead
    if(this.word.prev != null)
      fillPrevContextCats(this.word.prev);
    else {
      List<MyPairWord> bestPreviousContexts = word.getBestContexts(false, 1, Words.ALLPARS_FILTER, model, true);
      if (bestPreviousContexts.size() > 0) {
        Word bestPreviousWord = bestPreviousContexts.get(0).left;
        fillPrevContextCats(bestPreviousWord);
      }
    }
    if(this.word.next != null)
      fillNextContextCats(this.word.next);
    else {
      List<MyPairWord> bestNextContexts = word.getBestContexts(true, 1, Words.ALLPARS_FILTER, model, true);
      if (bestNextContexts.size() > 0) {
        Word bestNext = bestNextContexts.get(0).left;
        fillNextContextCats(bestNext);
      }
    }
  }

}
