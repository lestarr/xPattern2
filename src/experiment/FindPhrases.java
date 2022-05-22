package experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import model.WordSequences;
import modelparts.Collocation;
import modelparts.PhrasesOld;
import modelparts.Sentences;
import modelparts.Similarity;
import tokenizer.TestTokenizer;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;

public class FindPhrases {
	static public List<String> analyzeSentsAll(WordSequences model, int start, int step, int howmany) {
		List<String> sents = Sentences.getSents(model.getLang(), start, step, howmany, model, false);
		return analyzeSentsSkript(model, sents);
	}

	static public List<String> analyzeTestSentsPred(WordSequences model, int howmany) {
		List<String> sents = Sentences.getSentsTest(model.getLang(), howmany);
		return analyzeSentsSkript(model, sents);
	}
	
	static public List<String> analyzeTestSentsParadigmExpectation(WordSequences model, int howmany) {
		List<String> sents = Sentences.getSentsTest(model.getLang(), howmany);
		return analyzeSentsSkriptParadigmExpectation(model, sents);
	}
	
	private static List<String> analyzeSentsSkript(WordSequences model, List<String> sents) {
		addSentsToModel(model, sents);
		sents = analyzeSentsPred(model, sents);
		return sents;
	}
	private static List<String> analyzeSentsSkriptParadigmExpectation(WordSequences model, List<String> sents) {
		addSentsToModel(model, sents);
		PhrasesOld.setParadigmVectors(model, 40);
		model.idx().fillBuckets(2);
		
		model.computeParadigmExpectations();
		model.analyzeMorphCatsForTerminals();
		model.analyzeSyntCatsForTerminals();
		System.out.println("MPars Terminals: " + model.idx().morphTerminals.toString());
		sents = analyzeSentsParadigmExpectation(model, sents);
		return sents;
	}
	private static void addSentsToModel(WordSequences model, List<String> sents) {
		model.addWordsToModel( sents,  false, true);
	}

	private static List<String> analyzeSentsPred(WordSequences model, List<String> sents) {
		List<String> sentsA = new ArrayList<String>();
		PhrasesOld.setParadigmVectors(model, 40);
		model.idx().fillBuckets(2);
		for(String sent: sents) {
			List<MyPairWord> bigrs = PhrasesOld.getBigrams(sent, model.getLang(), model);
			bigrs = PhrasesOld.interpretExpectations(bigrs, model, 0.01, true, false);
			bigrs = PhrasesOld.analyseBigramsForPeak(bigrs, 1);
			bigrs = PhrasesOld.interpretExpectations(bigrs, model, 0.01, false, false);
			bigrs = PhrasesOld.analyseBigramsForPeak(bigrs, 2);
//			bigrs = Phrases.analyseBigramsForPeak(bigrs, 3);
			
			String	sentA= PhrasesOld.getSentFromBigrams(bigrs, true);
			sentsA.add(sentA);
			
		}
		return sentsA;
	}	
	private static List<String> analyzeSentsParadigmExpectation(WordSequences model, List<String> sents) {
		List<String> sentsA = new ArrayList<String>();
		for(String sent: sents) {
			List<MyPairWord> bigrs = PhrasesOld.getBigrams(sent, model.getLang(), model);
			
			// add here simple collocations aka New York
			
//			bigrs = Phrases.interpretParadigmExpectations(bigrs, model, false);
			PhrasesOld.interpretBigramsMainScript(bigrs, model, false);
			
			String	sentA= PhrasesOld.getSentFromBigrams(bigrs, false);
			sentsA.add(sentA);
			
		}
		return sentsA;
	}

	/**
	 * Takes sent, outputs tokens and sometimes phrases --> PToken
	 */
	public static List<String> sentnceToPTokens(WordSequences model, List<String> sents) {
		List<String> sentsA = new ArrayList<String>();
		for(String sent: sents) {
			List<MyPair> bigrs = TestIdeaPhrase.getBigrams(sent, model.getLang());
			bigrs = TestIdeaPhrase.interpretCollocations(bigrs, model, "_", new Similarity(0.1, 0.01));
			bigrs = TestIdeaPhrase.interpretPhrasesSplitterRight(bigrs, model, "_", new Similarity(0.001, 0.1));
			
			String sentA = TestIdeaPhrase.getSentFromBigrams(bigrs, " ")+".";
			sentsA.add(sentA);
		}
		return sentsA;
	}
	
	static public List<String> analyzeTestSents(WordSequences model, int howmany) {
		List<String> sents = Sentences.getSentsTest(model.getLang(), howmany);
		
		return analyzeSentsPred(model, sents);
	}

	private static List<String> analyzeSents(WordSequences model, List<String> sents) {
		List<String> sentsA = new ArrayList<String>();

		for(String sent: sents) {
			List<MyPair> bigrs = TestIdeaPhrase.getBigrams(sent, model.getLang());
			bigrs = TestIdeaPhrase.interpretCollocationsLeftRight(bigrs, model, "_", 0.01);
			
			//			bigrs = TestIdea.interpretCollocations(bigrs, model, "_", new Similarity(0.1, 0.01));
			//			bigrs = TestIdea.interpretMorphCollocations(bigrs, model, "_._", new Similarity(0.1, 0.01));
			//			bigrs = TestIdea.interpretMorphCollocations(bigrs, model, "_.._", new Similarity(0.01, 0.01));
			
			//			bigrs = TestIdea.interpretMorphCollocations(bigrs, model, "_..._", new Similarity(0.01, 0.01));
			
			//			bigrs = TestIdea.interpretSignificance(bigrs, model, "_");
			//			bigrs = TestIdea.interpretCoef(bigrs, model, "_._");
			//			bigrs = TestIdea.interpretSplitter(bigrs, model, "_._");
						
			//			bigrs = TestIdea.interpretExpectations(bigrs, model, "_", true);
			//			bigrs = TestIdea.interpretExpectations(bigrs, model, "_r", false);
						
			//			bigrs = TestIdea.interpretPredArgStrong(bigrs, model, "_._._");
			//			bigrs = TestIdea.interpretPredArgWeak(bigrs, model, "_._._._");
			
//			String sentA = TestIdea.printBigrams(bigrs, "\n");
			String sentA = TestIdeaPhrase.getSentFromBigrams(bigrs, "\n");
						sentsA.add(sentA);
			
		}
		sentsA = addNormalizedSents(sentsA);

		return sentsA;
	}
	

	
	private static List<String> addNormalizedSents(List<String> sents) {
		List<String> sentsA = new ArrayList<String>();
		sentsA.addAll(sents);
		sentsA.add("\n");
		for(String sent: sents) {
			sent = sent.replaceAll("[0-9\\.]+_[0-9\\.]+", " ").replaceAll("[0-9.]+\\n", " ");
			sentsA.add(sent);
		}
		return sentsA;
	}



	static public void analyzeSentsCollocationWins(WordSequences model, int start, int step, int howmany) {
		List<String> sents = Sentences.getSents(model.getLang(), start, step, howmany, model, false);
		for(String sent: sents) {
			List<MyPair> bigrsInput = TestIdeaPhrase.getBigrams(sent, model.getLang());
			List<List<Collocation>> bigrsCollocations= TestIdeaPhrase.getCollocationsFromBigram(bigrsInput, model);
			int i = 0;
			for(MyPair p: bigrsInput) {
				System.out.println(p.first + "\t" + p.second);
				List<Collocation> colls = bigrsCollocations.get(i);
				Collections.sort(colls);
				System.out.println(colls.toString());
				i++;
			}
		}
	}
	
	static public void analyzeSentsPA(WordSequences model, int start, int step, int howmany) {
		List<String> sents = Sentences.getSents(model.getLang(), start, step, howmany, model, false);
		for(String sent: sents) {
			System.out.println(sent);
			for(String s: TestTokenizer.getTokens(sent, model.getLang())) {
				String signifRight = TestIdeaPhrase.findPredArgStrongWord(model.getWord(s),false);
				String signifLeft = TestIdeaPhrase.findPredArgStrongWord(model.getWord(s),true);
				System.out.println(signifLeft + "\t\t" + signifRight);
			}
			System.out.println();
		}
	}
	
	static public List<String> analyzeSents(WordSequences model, int start, int step, int howmany) {
		List<String> sentsA = new ArrayList<String>();
		List<String> sents = Sentences.getSents(model.getLang(), start, step, howmany, model, false);
		for(String sent: sents) {
			List<MyPair> bigrs = TestIdeaPhrase.getBigrams(sent, model.getLang());
			bigrs = TestIdeaPhrase.interpretCollocations(bigrs, model, "_", new Similarity(0.01, 0.01));
			bigrs = TestIdeaPhrase.interpretPredArgWeak(bigrs, model, "_._");
			String sentA = TestIdeaPhrase.getSentFromBigrams(bigrs, "\n");
			sentsA.add(sentA);
		}
		return sentsA;
	}
	
	
	static public void analyzeSentsAllGetrennt(WordSequences model, int start, int step, int howmany) {
		List<String> sentsA = new ArrayList<String>();
		List<String> sents = Sentences.getSents(model.getLang(), start, step, howmany, model, false);
		int count = 0;
		for(String sent: sents) {
			count++;
			if(count%100 == 0) System.out.println("phrases " + count );
			List<MyPair> bigrsInput = TestIdeaPhrase.getBigrams(sent, model.getLang());
			List<MyPair> bigrs = TestIdeaPhrase.interpretMorphCollocations(bigrsInput, model, "_", new Similarity(0.01, 0.01));
			saveBigrIntoModel(bigrs, model, 0);
			bigrs = TestIdeaPhrase.interpretCollocations(bigrsInput, model, "_._", new Similarity(0.01, 0.01));
			saveBigrIntoModel(bigrs, model, 1);

			bigrs = TestIdeaPhrase.interpretPredArgStrong(bigrsInput, model, "_._._");
			saveBigrIntoModel(bigrs, model, 2);
			bigrs = TestIdeaPhrase.interpretPredArgWeak(bigrsInput, model, "_._._._");
			saveBigrIntoModel(bigrs, model, 3);

		}
	}
	
	private static void saveBigrIntoModel(List<MyPair> bigrams, WordSequences model, int idx) {
		Map<String,Double> pmap = model.idx().getPhraseMap(idx);
		getPhrasesStatsOnSent(TestIdeaPhrase.getSentFromBigrams(bigrams, " "), pmap);
	}

	static public void getPrasesStats(List<String> inputSents, Map<String,Double> saveMap) {
		for(String s: inputSents) {
			getPhrasesStatsOnSent(s, saveMap);
		}
	}

	private static void getPhrasesStatsOnSent(String s, Map<String, Double> saveMap) {
		for(String phrase: s.split("\\s+")) {
			if(phrase.contains("_"))
				MapsOps.addFreq(phrase, saveMap);
		}
	}
}
