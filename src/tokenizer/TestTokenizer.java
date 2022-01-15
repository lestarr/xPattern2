package tokenizer;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.LetterTokModel;

public class TestTokenizer {
	
	public static final String DELIM = "#";
	public static final String AAANF = "AAANF";
	public static final String EEEND = "EEEND";
	
	public static void main(String[] args) throws IOException {
		String filePath = "C:\\2Projects\\DataAnalysis\\TextMining\\SwiftKey\\final\\de_DE/de_DE.news.txt";
		List<String> texte = Files.readAllLines(Paths.get(filePath), Charset.forName("UTF-8"));
		

		
	}
	
	static Pattern p = Pattern.compile("\\b");
	static Pattern hyphenPat = Pattern.compile(" - ");
	static Pattern digit = Pattern.compile("(\\d) [,\\.] (\\d)");
	static Pattern apo = Pattern.compile(" ' s ");
	static Pattern apoUkr = Pattern.compile(" ?(( ' )|’|'|´|`) ?");
	static Pattern iUkr = Pattern.compile("i");
	
	public static StringTokenizer getWordTokenizer(String sent, String lang) {
//		return new StringTokenizer(sent, " .,\"+#()[]{}!?«»<>„“\n\r\t:;\\*”\\/");//'’-
		
		Matcher m = p.matcher(sent);
		sent = m.replaceAll(" ");
		m = hyphenPat.matcher(sent);
		sent = m.replaceAll("-");
		m = apo.matcher(sent);
		sent = m.replaceAll("'s ");
		if(lang.equals("ukr")) {
			m = apoUkr.matcher(sent);
			sent = m.replaceAll("'");
			m = iUkr.matcher(sent);
			sent = m.replaceAll("і");
		}
		m = digit.matcher(sent);
		sent = m.replaceAll("$1.$2");
		return new StringTokenizer(sent, " ");//'’-
	}
	
	public static List<String> getTokens(String s, String lang) {
		return getTokens(s, lang, false);
	}
	
	public static List<String> getTokens(String s, String lang, LetterTokModel ltmodel) {
		return getTokens(s, lang, false, ltmodel, false);
	}

	public static List<String> getTokens(String s, String lang, boolean addStartEnd) {
		return getTokens(s, lang, addStartEnd, null, false);
	}
	
	
	public static List<String> getTokens(String s, String lang, boolean addStartEnd, boolean replaceDigits) {
		
		return getTokens(s, lang, addStartEnd, null, replaceDigits);
	}
	
	public static List<String> getTokens(String s, String lang, boolean addStartEnd, LetterTokModel ltmodel, 
			boolean replaceDigits) {
		s = trimSentencePrefix(s);
		if(addStartEnd)
			s  = AAANF + " "+s+ " " + EEEND;
		List<String> tokens = new ArrayList<String>();
		StringTokenizer stok = getWordTokenizer(s, lang);
		while(stok.hasMoreTokens()) {
			String token = stok.nextToken();
			if(replaceDigits && token.matches("\\d+([,\\.]\\d+)?")) 
				token = "ZZZAHL";
			if(ltmodel == null)
				tokens.add(token);
//			else {
//				String[] rootFlex = MorphAnalyzer.getRootFlex(ltmodel, token);
//				if(rootFlex.length > 1) {
//					tokens.add(rootFlex[0]);
//					if(rootFlex[1].length() > 0)
//						tokens.add("-"+rootFlex[1]);
//				}
//				else	tokens.add(token);
//			}
		}
		return tokens;
	}
	
	private static String trimSentencePrefix(String s) {
		//30	00.43 Uhr: 
//		return s.replaceFirst("^([0-9]+\t)?\\s*(([0-9]*[\\.:][0-9]*\\s*(Uhr|UHR):?)?)", "");
		return s.replaceFirst("^([0-9]+\t)\\s*", "");

	}

	public static StringTokenizer getLetterTokenizer(String text, String[] strings){
		String t = preprocess(text);
		t = putLetterDelimiters(DELIM, t);
		for (String p : strings) { // delete delimiter from letterpattern
			String pDelim = putLetterDelimiters(DELIM, p);
			t = saveLetter(pDelim, p, t);
		}
		StringTokenizer stokenizer = new StringTokenizer(t, "#");
		return stokenizer;
	}

	private static String saveLetter(String pattern, String replacement, String t) {
		t = t.replaceAll(pattern, replacement);
		return t;
	}

	private static String putLetterDelimiters(String delim, String t) {
		char[]carr = t.toCharArray();
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < carr.length; i++) {
			sbuf.append(carr[i]).append(delim);
		}
		t = sbuf.substring(0, sbuf.length()-1);
		return t;
	}

	private static String preprocess(String t) {
		t = t.toLowerCase();
		t = t.replaceAll("[\\.,\\!\\?\\(\\)\\[\\]:;\"]+", " ");
		t = t.replaceAll("[ \\s]+", " ");
		return t;
	}

}
