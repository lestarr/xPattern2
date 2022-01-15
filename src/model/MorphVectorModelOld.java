package model;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import modelparts.MorphParadigm;
import modelparts.Paradigm;
import modelparts.Word;
import util.MapsOps;
import util.MyUtils;

public class MorphVectorModelOld extends Paradigm{
	
	private final int wordFreqTHHforModelTrain = 5;
	
	private LetterTokModel ltmodel = null;
	
	public void setLetterTokModel(LetterTokModel ltmodel) {
		this.ltmodel = ltmodel;
	}

	public MorphVectorModelOld(int id, String label) {
		super(id, label);
	}

	@Override
	public void train(WordSequences model) {
		//freq thh 5, check word cuts with LetterTokModel, if suf cut == pref cut --> save root, flex; collect flex fmap
		int thh = 25;
		boolean initial = true;
		MorphVectorAnalyzerOld.getRootFlex(model, ltmodel, wordFreqTHHforModelTrain, false);
		//tag most frequent flexes == proto paradigms
		
		MorphVectorAnalyzerOld.addInitialMPasCateory(model, thh, false);
		printMpars(model, "after 1. tag initial");
		//find waiting flexes
		MorphVectorAnalyzerOld.findWaitingFlexesViaRoots(model,Words.ALLPARS_FILTER, false);
//		MorphVectorAnalyzerOld.tagWordsAndFindWaitingFlexes(model,thh,Words.ALLPARS_FILTER,false);
		double thhw = printWaitingFlexesGetBiggest(model);
		//tag flexes + waiting flexes
		MorphVectorAnalyzerOld.writeTMPWaitingFlexCats(model, thhw, false);
		//remove all to tag only matching all flexes
		MorphVectorAnalyzerOld.removeAllMparsinWords(model);
//		printMpars(model, "after remove all in words");

		MorphVectorAnalyzerOld.addWaitingFlexes(model,thhw, true, Words.ALLPARS_FILTER,initial,true);
		MorphVectorAnalyzerOld.tagWaitingFlexes(model,thhw, true, Words.ALLPARS_FILTER,initial,true);
		printMpars(model, "after tag waiting init");

		Set<MorphParadigm> mpWithOneFlex = MorphVectorAnalyzerOld.deleteMparsWithONEflexFromWords(model);
//		printMpars(model, "after delete one word");

		//clean: mpars, model.idx().flexPar777, vectors, all tagged cats
		MorphVectorAnalyzerOld.cleanMorphTags(model,thh);
//		printMpars(model, "after clean mp tags");
		MorphVectorAnalyzerOld.deleteVanishingAndEqualMPars(model, mpWithOneFlex);
		printMpars(model, "after delete vanishing ONE flex");

		MorphVectorAnalyzerOld.cleanMPwords(model);
//		printMpars(model, "after clean mp words");

		MorphVectorAnalyzerOld.addMPasCategory(model, thh, initial,false);
		
//		thh = thh > 8 ? thh -5 : thh;
		initial = false;
		MorphVectorAnalyzerOld.tagWordsAndFindWaitingFlexes(model,thh,Words.SYNSEM_FILTER,false);
		MorphVectorAnalyzerOld.findWaitingFlexesViaRoots(model, Words.SYNSEM_FILTER, false);
//		printMpars(model, "after 1. tag find waiting");
		printWaitingFlexesGetBiggest(model);
		MorphVectorAnalyzerOld.writeTMPWaitingFlexCats(model, thhw, false);
//		printMpars(model, "after 1. tag write tmp waiting");
		MorphVectorAnalyzerOld.addWaitingFlexes(model,thhw, true, Words.ALLPARS_FILTER,initial,true);
//		printMpars(model, "after 1. tag add waiting");
		MorphVectorAnalyzerOld.tagWaitingFlexes(model,thhw, true, Words.ALLPARS_FILTER,initial,true);
		printMpars(model, "after 1. tag tag waiting");
	
		
		//2. tag
		int tag = 2;
		boolean checkAllWords = true;
		while (tag < 15) { // 11
			// thhw = Math.max(MorphVectorAnalyzerOld.HARD_THH_FOR_WAITING_FLEXES, (thhw/2));
			if(tag == 11 && !model.getLang().equals("ukr"))
				break;
			Set<MorphParadigm> vanishingMPs = MorphVectorAnalyzerOld.collectVanishingMPars(model);
			MorphVectorAnalyzerOld.deleteVanishingMParsFromWords(model, vanishingMPs);
			// printMpars(model, "after delete vanishing");
			MorphVectorAnalyzerOld.cleanMorphTags(model, thh);
			MorphVectorAnalyzerOld.deleteVanishingAndEqualMPars(model, vanishingMPs);

			MorphVectorAnalyzerOld.addMPasCategory(model, thh, initial, false);
			printMpars(model, "after " + tag + ". tag");
			
			MorphVectorAnalyzerOld.tagWordsAndFindWaitingFlexes(model, thh, Words.SYNSEM_FILTER, false);
			if(checkAllWords) 		MorphVectorAnalyzerOld.findWaitingFlexesViaRoots(model, Words.SYNSEM_FILTER, false);

			printWaitingFlexesGetBiggest(model);
			MorphVectorAnalyzerOld.writeTMPWaitingFlexCats(model, thhw, false);
			int waitingFlexWasAdded = MorphVectorAnalyzerOld.addWaitingFlexes(model, thhw, checkAllWords,
					Words.SYNSEM_FILTER, initial, true);
			if (waitingFlexWasAdded < 5) {
				checkAllWords = false;
				System.out.println("\nCHECK ALL WORDS SET TO FALSE IN TAG: " + tag + "\n");
			}
			MorphVectorAnalyzerOld.tagWaitingFlexes(model,thhw, true, Words.ALLPARS_FILTER,initial,true);
			printMpars(model, "after " + tag + ". tag waiting");

			tag++;
		}
		
		//4. tag
		MorphVectorAnalyzerOld.printMorphParStats(model, thh, "last stats");
		String outfile = "out/"+model.getLang()+"-morphVector.txt";
		printOutput(outfile,model);
		
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
			double firstWaitingFreq = MapsOps.getFirst(mp.waitingFlexFreqMap).freq;
			if(firstWaitingFreq > biggest) biggest = firstWaitingFreq;
			System.out.println(mp.getLabel() + "\t" + mp.getSortedFlex() + "\twaiting\t" + mp.getSortedWaitingFlexMap());
		}	
		System.out.println();
		for(String mpstring: model.idx().getMPlabels()) {
			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
			if(mp == null) continue;
			System.out.println(mp.getLabel() + "\t" + mp.getSortedFlexFreqMap() + "\twaiting WORD\t" + mp.waitingFlexWordMap.toString());
		}
		System.out.println();
		return Math.max(MorphVectorAnalyzerOld.HARD_THH_FOR_WAITING_FLEXES, (biggest/10));
	}
	
	private void printMpars(WordSequences model, String info) {
		System.out.println(info);
		for(String mpstring: model.idx().getMPlabels()) {
			MorphParadigm mp = model.idx().getMorphParadigm(mpstring);
			if(mp == null) continue;
			System.out.println(mp.getLabel() + "\t" + mp.getSortedFlex() + "\t" + mp.getFreq() + "\t" + mp.words.toString());
		}	
		System.out.println();
	}
	@Override
	public void tag(WordSequences wsmodel) throws IOException {
		
	}

}
