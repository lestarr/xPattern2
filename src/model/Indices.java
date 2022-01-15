package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import modelparts.Flexion;
import modelparts.MorphParadigm;
import modelparts.Root;
import modelparts.SemParadigm;
import modelparts.Word;
import modelutils.Cluster;
import util.MapsOps;

public class Indices {

  public static Map<String, String> semparLabels = new HashMap<>();
  public Map<Word, Pair<String, Double>> flexPar777 = new HashMap<>();;
  public Map<Word, List<SemParadigm>> synonymPars = new HashMap<>();
  public Map<Word, List<SemParadigm>> semPars = new HashMap<>();
  public Map<String, Word> words = new HashMap<>();
  private Map<String, Cluster> syntPars = new HashMap<>();
  private Map<String, MorphParadigm> morphPars = new HashMap<>();
  private Map<String, Double> morphParsFreqs = new HashMap<>();


  private Set<Word> cats = new HashSet<>();
  public Set<String> tmpCats = new HashSet<>();
  public Map<Word, Map<Word, Double>> assocs = new HashMap<>();

  // holds set of Root with their features, just as Words for this model
  public HashMap<String, Root> roots = new HashMap<String, Root>();

  // holds set of Flexion with their features
  public HashMap<String, Flexion> flexes = new HashMap<String, Flexion>();

  public Set<String> seenRoots = new HashSet<>(); // root to flexes it was seen with
  public Map<String, Double> seenFlexes = new HashMap<>();

  public Map<String, Map<String, Double>> seenSuffixes = new HashMap<>();
  public Map<String, Set<String>> seenRootsNotChecked = new HashMap<>(); // root to flexes it was seen with, roots are
                                                                         // taken form suf slices and pref slices
                                                                         // without check
  public Map<String, Double> seenFlexesNotChecked = new HashMap<>(); // stats for unchecked flexes to eliminate very
                                                                     // seldom then
  // holds flex stats for each found paradigm. the stats comes from words which
  // were clustered for this paradigm. used to eliminate seldom paradigms
  public Map<MorphParadigm, Map<Flexion, Double>> seenMorphParToFlexFreqMap = new HashMap<>();

  public List<Map<String, Double>> phraseMaps = new ArrayList<>();

  // saves format: wesentlich#Ziel e#e
  public Map<String, Set<String>> rrff = new HashMap<>();

  public void addCat(Word catword) {
    if (!catword.isCat())
      return;
    this.cats.add(catword);
  }

  public Set<Word> cats() {
    return this.cats;
  }

  public void addAsso(Word keyCat, Word assoCat) {
    MapsOps.addFreq(assoCat, assocs.get(keyCat));
  }

  public List<Word> getSortedWords() {
    List<Word> words = new ArrayList<>();
    words.addAll(this.words.values());
    Collections.sort(words);
    return words;
  }

  public Root getRoot(String root) {
    Root r = roots.containsKey(root) ? roots.get(root) : new Root(root);
    roots.put(root, r);
    return r;
  }

  public Flexion getFlex(String flex, Root root) {
    Flexion f = flexes.containsKey(flex) ? flexes.get(flex) : new Flexion(flex, root);
    flexes.put(flex, f);
    f.addRoot(root);
    root.addFlex(f);
    return f;
  }

  public Flexion getFlex(String flex) {
    Flexion f = flexes.containsKey(flex) ? flexes.get(flex) : new Flexion(flex);
    flexes.put(flex, f);
    return f;
  }

  // holds stats for seen prefixes
  public HashMap<String, Double> prefixFreq = new HashMap<>();

  public void setSyntPars(List<Cluster> clusters, String parPrefix) {
    for (Cluster c : clusters) {
      String id = parPrefix + Integer.toString(syntPars.size());
      c.setLabel(id);
      syntPars.put(id, c);
    }
  }

//	public void setMorphParsIdx(List<MorphParadigm> mpars) {
//		this.morphPars = new HashMap<>();
//		for(MorphParadigm mpar: mpars) {
//			String id = getMparID();
//			mpar.setLabel(id);
//			morphPars.put(id, mpar);
//		}
//	}

  private int mpID = 0;

  public String getMparID() {
    String id = MorphModel.MPREF + Integer.toString(mpID);
    mpID++;
    return id;
  }

  public Map<String, Cluster> syntPars() {
    return this.syntPars;
  }

  public Set<String> getMPlabels() {
    return this.morphPars.keySet();
  }

  public Map<String, Double> getSortedFlexes() {
    Map<String, Double> flexFreqMap = new HashMap<>();
    for (Flexion f : this.flexes.values()) {
      if (f.freq() > 1)
        MapsOps.addFreq(f.toString(), flexFreqMap, f.freq());
    }
    return flexFreqMap;
  }

  public Map<String, Double> getPhraseMap(int idx) {
    if (phraseMaps.size() <= idx)
      phraseMaps.add(idx, new HashMap<>());
    return phraseMaps.get(idx);
  }

  public List<SemParadigm> getAllSemPars() {
    List<SemParadigm> splist = new ArrayList<>();
    for (Word w : this.semPars.keySet())
      splist.addAll(this.semPars.get(w));

    return splist;
  }

  public Map<String, Set<Word>> prefixBucket = new HashMap<>();

  public void fillBuckets(double minfreq) {
    prefixBucket = new HashMap<>();
    for (Word w : this.words.values()) {
      if (w.freq() < minfreq)
        continue;
      addToPrefixBucket(w);
    }
  }

  public void emptyBuckets() {
    prefixBucket = new HashMap<>();
  }

  public void addToPrefixBucket(Word w) {
    String prefix = w.toString().length() > 4 ? w.toString().substring(0, 5) : w.toString().substring(0, 1);
    if (!prefixBucket.containsKey(prefix))
      prefixBucket.put(prefix, new HashSet<>());
    prefixBucket.get(prefix).add(w);
  }

  public Set<Word> getPrefixBucket(Word w) {
    String prefix = w.toString().length() > 4 ? w.toString().substring(0, 5) : w.toString().substring(0, 1);
    if (!prefixBucket.containsKey(prefix))
      return new HashSet<>();
    return prefixBucket.get(prefix);
  }

  public Set<Root> getPrefixBucketRoot(Word w) {
    Set<Root> roots = new HashSet<>();
    Set<Word> words = getPrefixBucket(w);
    for (Word w1 : words)
      if (w1.getRoot() != null)
        roots.add(this.getRoot(w1.getRoot()));

    return roots;
  }

  public void cleanMorphPars() {
    saveMorphParsFreqs();
    this.morphPars.clear();
    this.paradigmFlexesToLabel.clear();
  }

  public void deleteMorphPar(String label) {
    this.morphPars.remove(label);
    this.knownParadigmLabels.remove(label);
    this.deletedParadigmLabels.add(label);
  }
//	public void resetMorphPars() {
//		if(this.morphPars == null) return;
//		saveMorphParsFreqs();
//		for(String mpLabel: this.morphPars.keySet()) {
//			this.morphPars.get(mpLabel).freqNull();
//		}
//	}

  public void saveMorphParsFreqs() {
    if (this.morphPars == null)
      return;
    for (String mpLabel : this.morphPars.keySet()) {
      morphParsFreqs.put(mpLabel, this.morphPars.get(mpLabel).getFreq());
    }
  }

  public Map<String, Double> getSavedMorphParsFreqs() {
    return morphParsFreqs;
  }

  private Map<String, String> paradigmFlexesToLabel = new HashMap<>();
  private boolean stopCreatingParadigms = false;
  public Set<String> knownParadigmLabels = new HashSet<>();
  public Set<String> deletedParadigmLabels = new HashSet<>();

  /**
   * saves good phrase patterns, e.g. ADJ+NOUN or PREP+DET or DET+NOUN
   */
  public Map<String, Set<String>> phraseExpectations = new HashMap<>();
  public Set<String> morphTerminals = new HashSet<>();

  public void setStopCreatingParadigms() {
    this.stopCreatingParadigms = true;
  }

  public void removeStopCreatingParadigms() {
    this.stopCreatingParadigms = false;
  }

  public MorphParadigm getMorphParadigm(String label) {
    if (label == null)
      return null;
    return this.morphPars.get(label);
  }

  public Collection<MorphParadigm> getMorphParadigms() {
    if (this.morphPars == null)
      return null;
    return this.morphPars.values();
  }

  public MorphParadigm getNewMorphParadigm(Set<Flexion> flexes, String label) {
    return getNewMorphParadigm(flexes, MorphParadigm.getEmptyFlexFreqMap(flexes), label);
  }

  public MorphParadigm getNewMorphParadigm(Set<Flexion> flexes, Map<String, Double> flexFreqMap, String label) {
    if (stopCreatingParadigms) {
      System.out.println("PARADIGM CREATING NOT POSSIBLE, STOP IS SET in indices");
      return null;
    }
    String sortedFlexes = MorphParadigm.getSortedFlex(flexes);
    if (paradigmFlexesToLabel.containsKey(sortedFlexes)) {
      MorphParadigm mpar = this.morphPars.get(paradigmFlexesToLabel.get(sortedFlexes));
      return mpar;
    }
    if (label == null)
      label = this.getMparID();
    MorphParadigm mpar = new MorphParadigm(flexes, flexFreqMap, label);
    this.morphPars.put(label, mpar);
    paradigmFlexesToLabel.put(sortedFlexes, label);
    return mpar;

  }

  public boolean containsWord(String wstring) {
    if (this.words.containsKey(wstring))
      return true;
    return false;
  }

  public Collection<Cluster> getSyntParadigms() {
    if (this.syntPars == null)
      return null;
    return this.syntPars.values();
  }

  public Cluster getSyntParadigm(String label) {
    if (label == null)
      return null;
    return this.syntPars.get(label);
  }

  public Cluster deleteSyntParadigm(String clabel) {
    this.knownParadigmLabels.remove(clabel);
    return this.syntPars.remove(clabel);
  }

  public MorphParadigm deleteMorphParadigm(String clabel) {
    this.knownParadigmLabels.remove(clabel);
    return this.morphPars.remove(clabel);
  }

  public void addSyntParadigm(Cluster c) {
    this.knownParadigmLabels.add(c.getLabel());
    this.syntPars.put(c.getLabel(), c);
  }
  
  public void addMorphParadigm(MorphParadigm c) {
    this.morphPars.put(c.getLabel(), c);
  }
  
}
