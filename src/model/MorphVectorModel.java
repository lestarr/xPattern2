package model;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelparts.Flexion;
import modelparts.MorphParadigm;
import modelparts.Word;
import util.MapsOps;
import util.MyPair;
import util.MyUtils;

public class MorphVectorModel {
  
  public static final String DELIMITER = "###";
  public static final String WORD_DELIMITER = ", ";

	private final int wordFreqTHHforModelTrain = 5;
	
	private LetterTokModel ltmodel = null;
	
	public void setLetterTokModel(LetterTokModel ltmodel) {
		this.ltmodel = ltmodel;
	}

	public MorphVectorModel(int id, String label) {
	}

	public void train(WordSequences model) {
		//freq thh 5, check word cuts with LetterTokModel, if suf cut == pref cut --> save root, flex; collect flex fmap
		int wfreqthh = 20;
		boolean initial = true;
		boolean checkAllWords = true;
		boolean dontStopRecursion = false;
		boolean stopRecursion = true;

		int contextcount_words = 20;
		int contextcount_paradigms = 40;
		MorphVectorAnalyzer.getRootFlex(model, ltmodel, wordFreqTHHforModelTrain, false);
		//tag most frequent flexes == proto paradigms
		
		MorphVectorAnalyzer.tagWordAddInitialMPasCateory(model, wfreqthh, false);
		printMpars(model, "after 1. tag initial");
		//find waiting flexes
		MorphVectorAnalyzer.findWaitingFlexesViaRoots(model,Words.ALLPARS_FILTER, false, contextcount_words);
//		MorphVectorAnalyzer.tagWordsAndFindWaitingFlexes(model,thh,Words.ALLPARS_FILTER,false);
		double thhw = printWaitingFlexesGetBiggest(model);
		//tag flexes + waiting flexes
		MorphVectorAnalyzer.writeTMPWaitingFlexCats(model,  false);
		//remove all to tag only matching all flexes
		MorphVectorAnalyzer.removeAllMparsinWords(model);
//		printMpars(model, "after remove all in words");
//        MorphVectorAnalyzer.cleanMorphTags(model, 0);
		MorphVectorAnalyzer.addWaitingFlexes(model,thhw, checkAllWords, Words.ALLPARS_FILTER,initial,false, contextcount_words);
//		MorphVectorAnalyzer.tagWordsWithWaitingFlexes(model, true, Words.ALLPARS_FILTER,initial,true);
//		printMpars(model, "after tag waiting init");
//		MorphVectorAnalyzer.retagWordsInParadigms(model, Words.ALLPARS_FILTER, checkAllWords, false);
		MorphVectorAnalyzer.retagWords(model, wfreqthh, Words.SYNSEM_FILTER,checkAllWords,stopRecursion,false, contextcount_words);

//		MorphVectorAnalyzer.tagWords(model, thh, Words.ALLPARS_FILTER, checkAllWords,dontStopRecursion, false);
		printMpars(model, "after re-tag waiting init");

		Set<MorphParadigm> mpWithOneFlex = MorphVectorAnalyzer.deleteMparsWithONEflexFromWords(model);
//		printMpars(model, "after delete one word");

		//clean: mpars, model.idx().flexPar777, vectors, all tagged cats
		MorphVectorAnalyzer.cleanMorphTags(model, 1);
//		printMpars(model, "after clean mp tags");
		MorphVectorAnalyzer.deleteVanishingAndEqualMPars(model, mpWithOneFlex);
		printMpars(model, "after delete vanishing ONE flex");

		MorphVectorAnalyzer.cleanMPwords(model);
//		printMpars(model, "after clean mp words");

		MorphVectorAnalyzer.addMPasCategory(model, wfreqthh, initial,false);
		
//		thh = thh > 8 ? thh -5 : thh;
//	      initial = false;

		MorphVectorAnalyzer.tagWords(model,wfreqthh,Words.SYNSEM_FILTER, checkAllWords,dontStopRecursion, false, contextcount_words);
		MorphVectorAnalyzer.findWaitingFlexes(model,wfreqthh,Words.SYNSEM_FILTER, checkAllWords, false, contextcount_words);
		MorphVectorAnalyzer.findWaitingFlexesViaRoots(model, Words.SYNSEM_FILTER, false, contextcount_words);
//		printMpars(model, "after 1. tag find waiting");
		printWaitingFlexesGetBiggest(model);
		MorphVectorAnalyzer.writeTMPWaitingFlexCats(model,  false);
//		printMpars(model, "after 1. tag write tmp waiting");
		MorphVectorAnalyzer.addWaitingFlexes(model,thhw, checkAllWords, Words.SYNSEM_FILTER,initial,false, contextcount_words);
//		printMpars(model, "after 1. tag add waiting");
//		MorphVectorAnalyzer.tagWordsWithWaitingFlexes(model, true, Words.SYNSEM_FILTER,initial,true);
//		printMpars(model, "after 1. tag tag waiting");
//		MorphVectorAnalyzer.retagWordsInParadigms(model, Words.SYNSEM_FILTER,checkAllWords,false);
		MorphVectorAnalyzer.retagWords(model, wfreqthh, Words.SYNSEM_FILTER,checkAllWords,stopRecursion,false, contextcount_words);
		printMpars(model, "after 1. tag re-tag waiting init");
		
		
		//2. tag
		int tag = 2;
		initial = false;
		checkAllWords = false;
		int lastTag = 10; //6
		while (tag <= lastTag) { // 11
			// thhw = Math.max(MorphVectorAnalyzer.HARD_THH_FOR_WAITING_FLEXES, (thhw/2));
			if(tag == 11 && !model.getLang().equals("ukr"))
				break;
			if(tag == 4) {
				System.out.println();
			}
			
//			MorphVectorAnalyzer.cleanMorphTags(model);
//			MorphVectorAnalyzer.addMPasCategory(model, thh, initial, false);
			
			Set<MorphParadigm> vanishingMPs = MorphVectorAnalyzer.collectVanishingMPars(model, Words.SYNSEM_FILTER, false,contextcount_paradigms);
			MorphVectorAnalyzer.deleteVanishingMParsFromWords(model, vanishingMPs);
			
			// printMpars(model, "after delete vanishing");
			MorphVectorAnalyzer.cleanMorphTags(model, tag);
			MorphVectorAnalyzer.addMPasCategory(model, wfreqthh, initial, false);
//			printMpars(model, "after " + tag + ". tag");
			
			MorphVectorAnalyzer.deleteVanishingAndEqualMPars(model, vanishingMPs);

			MorphVectorAnalyzer.tagWords(model, wfreqthh, Words.SYNSEM_FILTER,checkAllWords,dontStopRecursion, false, contextcount_words);
			MorphVectorAnalyzer.findWaitingFlexes(model, wfreqthh, Words.SYNSEM_FILTER,checkAllWords, false, contextcount_words);
			if(checkAllWords) 		MorphVectorAnalyzer.findWaitingFlexesViaRoots(model, Words.SYNSEM_FILTER, false, contextcount_words);

			printWaitingFlexesGetBiggest(model);
			MorphVectorAnalyzer.writeTMPWaitingFlexCats(model,  false);
			
			int waitingFlexWasAdded = MorphVectorAnalyzer.addWaitingFlexes(model, thhw, checkAllWords,
					Words.SYNSEM_FILTER, initial, false, contextcount_words);
			if (waitingFlexWasAdded < 5) {
				checkAllWords = false;
				System.out.println("\nCHECK ALL WORDS SET TO FALSE IN TAG: " + tag + "\n");
			}
			if(waitingFlexWasAdded < 2 && model.idx().getMorphParadigms().size() < 4 && tag < lastTag) {
			  tag = lastTag;
	           MorphVectorAnalyzer.retagWords(model, wfreqthh, Words.SYNSEM_FILTER,checkAllWords,stopRecursion, false, contextcount_words);
			  continue;
			}
			System.out.println("ADDED in tag: " + tag + ": " + waitingFlexWasAdded + " flexes");
//			MorphVectorAnalyzer.tagWordsWithWaitingFlexes(model, true, Words.SYNSEM_FILTER,initial,true);
//			printMpars(model, "after " + tag + ". tag waiting");
//			MorphVectorAnalyzer.retagWordsInParadigms(model, Words.SYNSEM_FILTER,checkAllWords,false);
			if(tag == lastTag) {
//			  saveModel(model, this.modelFile);
			  wfreqthh = 3;
			}
			
			System.out.println("UNIQ words count before tag: " + model.idx().words.size());

			MorphVectorAnalyzer.retagWords(model, wfreqthh, Words.SYNSEM_FILTER,checkAllWords,stopRecursion, false, contextcount_words);
			System.out.println("UNIQ words count after tag: " + model.idx().words.size());

			printMpars(model, "after " + tag + ". tag re-tag waiting init");
			System.out.println("UNIQ words count: " + model.idx().words.size());
			tag++;
		}
//		MorphVectorAnalyzer.cleanMorphTags(model);
//		MorphVectorAnalyzer.addMPasCategory(model, thh, initial, false);
		//experiment with light tag
//		MorphVectorAnalyzer.tagWordsMparVectorLight(model, 10, 9){
//			printMpars(model, "after " + "last and light" + ". tag re-tag waiting init");
//			System.out.println("UNIQ words count: " + model.idx().words.size());
//		}
		
		//4. tag
        MorphVectorAnalyzer.cleanMorphTags(model, -1);
        MorphVectorAnalyzer.addMPasCategory(model, 3, initial, false);

		MorphVectorAnalyzer.printMorphParStats(model, wfreqthh, "last stats");
		String outfile = "out/"+model.getLang()+"-morphVector.txt";
		printOutput(outfile,model);
		//forbidden!
//		model.idx().knownParadigmLabels.addAll(model.idx().getMPlabels());
	}

    public void saveModel(String outfile, WordSequences model) {
      Writer out = MyUtils.getWriter(outfile);
      try {
          Collection<MorphParadigm> mpars = model.idx().getMorphParadigms();
          StringBuffer sb = new StringBuffer();
          for(MorphParadigm mp: mpars) {
            sb = new StringBuffer();
            sb.append(mp.getLabel());
            sb.append(DELIMITER);
            for(String flex: mp.getSortedFlexList()) {
              sb.append(flex); sb.append(WORD_DELIMITER);
            }
            sb.append(DELIMITER);
            for(Word w: mp.getWords()) {
              sb.append(w.toString()); sb.append(WORD_DELIMITER);
            }
            out.write(sb.toString()+"\n");
          }
      out.close();
      } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      }
  } 
    
    public void loadModel(String infile, WordSequences model, int ccount, int wordfreqThh) {
      model.idx().cleanMorphPars();
      List<String> lines;
      try {
        lines = MyUtils.readLines(infile);
        for(String line: lines) {
          String[] parts = line.split(DELIMITER);
          if(parts.length != 3) {System.out.println("WRONG FORMAT: " + infile); return;}
          String label = parts[0];
          String flexes = parts[1];
          String words = parts[2];
          Set<Flexion> fset = new HashSet<>();
          for(String f: flexes.split(WORD_DELIMITER)) {
            if(f.equals("")) continue;
            fset.add(model.idx().getFlex(f));
          }
          MorphParadigm mp = model.idx().getNewMorphParadigm(fset, label);
          for(String w: words.split(WORD_DELIMITER)) {
            if(w.equals("")) continue;
            mp.addWord(model.getWord(w));
          }
        }
        MorphVectorAnalyzer.addMPasCategoryFromParWords(model, false, ccount);
        tag(model, ccount, wordfreqThh);
        printMpars(model, "after load and tag" );

        MorphVectorAnalyzer.printMorphParStats(model, wordfreqThh, "last stats");

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    public void saveModelVectors(String outfile, WordSequences model) {
    Writer out = MyUtils.getWriter(outfile);
    try {
        Collection<MorphParadigm> mpars = model.idx().getMorphParadigms();
        for(MorphParadigm mp: mpars) {
            out.write(mp.toString()+"\n");
        }
        for(String label: MorphVectorAnalyzer.mparContextsMapLeft.keySet()) {
            out.write("LEFT\t"+label+"\t"+MorphVectorAnalyzer.mparContextsMapLeft.get(label).toString()+"\n");
        }
        for(String label: MorphVectorAnalyzer.mparContextsMapRight.keySet()) {
            out.write("RIGHT\t"+label+"\t"+MorphVectorAnalyzer.mparContextsMapRight.get(label).toString()+"\n");
        }
    
    out.close();
    } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
    }
}
	
	private void printOutput(String outfile, WordSequences model) {
		Writer out = MyUtils.getWriter(outfile);
		try {
		for(Word w: model.idx().getSortedWords()) {
			if(w.getMorphParadigm() != null)
				out.write(w.getMorphParadigm().getLabel()+"\t"+w.getRoot()+"\t"+model.idx().getRoot(w.getRoot()).seenflexes+"\n");
			else if(w.freq() > 25) 
				out.write("zzzNOPAR"+"\t"+w.toString()+"\n");
			
		}
		out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private double printWaitingFlexesGetBiggest(WordSequences model) {
		double biggest = 0.0;
		for(String mpstring: model.idx().getMPlabels()) {
			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
			if(mp == null) continue;
			double firstWaitingFreq = MapsOps.getFirst(mp.getWaitingFlexMap()).freq;
			if(firstWaitingFreq > biggest) biggest = firstWaitingFreq;
			System.out.println(mp.getLabel() + "\t" + mp.getFreq()+" "+mp.getSortedFlex() + "\twaiting\t" + mp.getSortedWaitingFlexMap());
		}	
		System.out.println();
//		for(String mpstring: model.idx().getMPlabels()) {
//			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
//			if(mp == null) continue;
//			System.out.println(mp.getLabel() + "\t" + mp.getSortedFlexFreqMap() + "\twaiting WORD\t" + mp.waitingFlexWordMap.toString());
//		}
		System.out.println();
		return Math.max(MorphVectorAnalyzer.HARD_THH_FOR_WAITING_FLEXES, (biggest/10));
	}
	
	private void printMpars(WordSequences model, String info) {
		System.out.println(info);
		for(String mpstring: model.idx().getMPlabels()) {
			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
			if(mp == null) continue;
			Set<Word> wordsAbstract = new HashSet<>();
			int i = 0;
			for(Word w: mp.words) {
				i++;
				if(i>1000) break;
				wordsAbstract.add(w);
			}
			System.out.println(mp.getLabel() + "\t" + mp.getSortedFlex() + "\t" + mp.getFreq() + "\t"
			+ wordsAbstract.toString());
		}	
		System.out.println();
	}
	public void tag(WordSequences model, int ccount, int wordfreqThh) throws IOException {
      MorphVectorAnalyzer.retagWords(model, wordfreqThh, Words.SYNSEM_FILTER,false,true, false, ccount);

	}

  public static void getTagsOneWord(Word w, WordSequences model, boolean b, int i) {
        //get word vectors, get score with cat vectors, get best cat

        List<MyPair> parscores = MorphVectorAnalyzer.getMParFromParVector(model, w, false, Words.SYNSEM_FILTER, false, 20);
        if(parscores == null||parscores.isEmpty()) return ;
        Collections.sort(parscores);
        parscores = parscores.subList(Math.max(0, parscores.size()-5), parscores.size());
        for(MyPair p: parscores)
            System.out.println(p.freq + "\t" + p.first);
  }

	
}
