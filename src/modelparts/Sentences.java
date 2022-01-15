package modelparts;

import java.util.ArrayList;
import java.util.List;

import model.WordSequences;
import tokenizer.TestTokenizer;
import util.CorpusUtils;

public class Sentences {
	
	static public List<String> getSentsOnly(String lang, int start, int step, int howmany){
		List<String> sents = new ArrayList<String>();
		for(String corpus: CorpusUtils.getStandardCorpora(lang)) {
			sents.add("CORPUS!!!!\t:" + corpus);
			int count = 0;
			for(String sent: CorpusUtils.getLeipzigSentences(lang, corpus, start, howmany*step)) {
				count++;
				if(count%step == 0) 
					sents.add(sent);
			}
		}
		return sents;
	}
	
	static public List<String> getSentsTest(String lang, int howmany){
		String corpusIn = "";
		if(lang.equals("de"))
			corpusIn = "C:\\2Projects\\CorporaIndices\\Leipzig\\de_sents.txt";
		else if(lang.equals("ukr"))
			corpusIn = "C:\\2Projects\\CorporaIndices\\Leipzig\\ukr_sents.txt";
		else if(lang.equals("en"))
			corpusIn = "C:\\2Projects\\CorporaIndices\\Leipzig\\en_sents.txt";
		else if(lang.equals("ita"))
			corpusIn = "C:\\2Projects\\CorporaIndices\\Leipzig\\ita_sents.txt";
		else 
			corpusIn = "C:\\2Projects\\CorporaIndices\\Leipzig\\ukr_sents.txt";
		List<String> sents = new ArrayList<String>();
			for(String sent: CorpusUtils.getLeipzigSentences(lang, corpusIn, -1, howmany)) {
					sents.add(sent);
			}
		return sents;
	}

	static public List<String> getSents(String lang, int start, int step, int howmany, WordSequences model, boolean checkMorph){
		List<String> sents = new ArrayList<String>();
		for(String corpus: CorpusUtils.getStandardCorpora(lang)) {
			int count = 0;
			boolean addSent = false;
			for(String sent: CorpusUtils.getLeipzigSentences(lang, corpus, start, howmany*step)) {
				count++;
				if(count%step == 0) addSent = true;
				if(addSent && goodSent(sent)) {
					if(checkMorph && hasZeroMorphWord(sent,model)) {
						continue;
					}
					sents.add(sent);
					addSent = false;
				}
			}
		}
		return sents;
	}
	
	private static boolean hasZeroMorphWord(String sent, WordSequences model) {
		for(String curr: TestTokenizer.getTokens(sent, model.getLang(), true, true)) {
			Word w = model.getWord(curr);
			if(w.isSplitterLeftRight(model.getFreqOfAnd()) || w.getMorphParadigm() != null)
				continue;
			else return true;
		}
		return false;
	}

	private static boolean goodSent(String sent) {
		if(sent.matches(".+[^\\p{L}, \\.\\?\\!].*")) return false;
		if(sent.split(" ").length < 5) return false;
		return true;
	}

}
