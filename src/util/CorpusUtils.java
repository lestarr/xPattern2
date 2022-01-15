package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CorpusUtils {
	
	private static final Pattern SHORTCUT_PATTERN = Pattern.compile("((\\b\\p{Lu}(\\p{L})?)|[\\d])\\.");
	private static final Pattern DOT_PATTERN = Pattern.compile("#");

	
	private static String replaceDotsAfterShortcuts(String text) {
		return SHORTCUT_PATTERN.matcher(text).replaceAll("$1"+DOT_PATTERN);
	}
	
  private static String restoreDeletedDots(String word) {
  	return DOT_PATTERN.matcher(word).replaceAll(".");
	}
	
	public static List<String> getLines(String fileIn){
		List<String> lines = new ArrayList<String>();
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileIn)), "UTF-8"));
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lines;
	}
	
	public static List<String> getSentences(String corpusIn){
		return getSentences(corpusIn, 0, Integer.MAX_VALUE);
	}
	
	public static List<String> getSentences(String corpusIn, int start, int howmany){
		BufferedReader br;
		List<String> sentences = new ArrayList<String>();
		int step = 1;
		if(start == 0)
			step = calculateStepForCorpus(corpusIn, howmany);
		if(howmany == -1) {
			howmany = Integer.MAX_VALUE;
			step = 1;
		}
		if(step < 1) step = 1;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(corpusIn)), "UTF-8"));
			int c = 0;
			String line;
			while ((line = br.readLine()) != null && c < start+(howmany*step)) {
				c++;
				if(c < start)
					continue;
				if(c%step != 0) 
					continue;
				// find end of sentence:
				line = replaceDotsAfterShortcuts(line);
				String [] sents = line.split("[\\.:]");
				for(String sent: sents) {
					sent = restoreDeletedDots(sent);
					sentences.add(sent);
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sentences;
	}
	
	private static int calculateStepForCorpus(String corpusIn, int howmany) {
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(corpusIn)), "UTF-8"));
			int sumOfLines = 0;
			String line;
			while ((line = br.readLine()) != null ) {
				sumOfLines++;
			}
			br.close();
			//calculate step to get sents from whole corpus, not just from beginning, useful if sorted alphabetically
			return sumOfLines/howmany;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	public static List<String> getLeipzigSentences(String lang, String resource, int start, int numSentencesToRead) {
		String path = "C:\\2Projects\\CorporaIndices\\Leipzig/";
		String corpusIn = null;
		if(lang.equalsIgnoreCase("de")) {
			if(resource.equalsIgnoreCase("wiki"))
				corpusIn = path + "deu_wikipedia_2016_3M-sentences.txt";
			else if(resource.equalsIgnoreCase("news"))
				corpusIn = path + "deu_news_2015_3M-sentences.txt";
			else
				corpusIn = resource;
		}
		else if(lang.equalsIgnoreCase("ukr")) {
			if(resource.equalsIgnoreCase("wiki"))
				corpusIn = path + "ukr_wikipedia_2016_1M-sentences.txt";
			else if(resource.equalsIgnoreCase("news"))
				corpusIn = path + "ukr_newscrawl_2011_1M-sentences.txt";
			else if(resource.equalsIgnoreCase("wiki2"))
				corpusIn = path + "ukr-wiki_dump.tokenized.txt";
			else if(resource.equalsIgnoreCase("lit"))
				corpusIn = path + "ukr-fiction.tokenized.shuffled.txt";
			else if(resource.equalsIgnoreCase("law"))
				corpusIn = path + "laws.txt.tokenized.bz2";
			else
				corpusIn = resource;
		}
		else if(lang.equalsIgnoreCase("ita")) {
			if(resource.equalsIgnoreCase("wiki"))
				corpusIn = path + "ita_wikipedia_2016_1M-sentences.txt";
			else if(resource.equalsIgnoreCase("news"))
				corpusIn = path + "ita_newscrawl_2019_1M-sentences.txt";
			else
				corpusIn = resource;
		}
		else if(lang.equalsIgnoreCase("en")) {
			if(resource.equalsIgnoreCase("news"))
				corpusIn = path + "eng_news_2015_3M-sentences.txt";
			else if(resource.equalsIgnoreCase("wiki"))
				corpusIn = path + "eng_wikipedia_2016_1M-sentences.txt";
			else
				corpusIn = resource;
		}
		if(corpusIn == null)
			return new ArrayList<String>();
		return getSentences(corpusIn, start, numSentencesToRead);

	}

	public static String getLeipzigWordsPath(String lang, String resource) {
		String path = "C:\\2Projects\\CorporaIndices\\Leipzig/";
		String wordsPath = null;
		if(lang.equalsIgnoreCase("de")) {
			//if(resource.equalsIgnoreCase("news"))
				wordsPath = path + "deu_news_2015_3M-words.txt";
			
		}
		else if(lang.equalsIgnoreCase("ukr")) {
			if(resource.equalsIgnoreCase("wiki"))
				wordsPath =  path + "ukr_wikipedia_2016_3M-words.txt";
			else if(resource.equalsIgnoreCase("news"))
				wordsPath = path + "ukr_web_2012_1M-words.txt";
			else if(resource.equalsIgnoreCase("wiki2"))
				wordsPath = path + "ukr-wiki_dump.tokenized.txt";
			else if(resource.equalsIgnoreCase("lit"))
				wordsPath = path + "ukr-fiction.tokenized.shuffled.txt";
			else if(resource.equalsIgnoreCase("law"))
				wordsPath = path + "laws.txt.tokenized.bz2";
		}
		else if(lang.equalsIgnoreCase("ita")) {
			//if(resource.equalsIgnoreCase("wiki"))
				wordsPath = path + "ita_wikipedia_2016_1M-words.txt";
		}
		else if(lang.equalsIgnoreCase("en")) {
			//if(resource.equalsIgnoreCase("news"))
				wordsPath = path + "eng_news_2015_3M-words.txt";
			//else if(resource.equalsIgnoreCase("wiki"))
		}
		return wordsPath;

	}
	
	
	public static String getCorpusDe() {
		return getCorpusDe("wiki");
	}
	
	public static List<String> getStandardCorpora(String lang) {
		List<String> corpora = new ArrayList<>();
		if(lang.equals("de") || lang.equals("en") || lang.equals("it")) {
			corpora.add("wiki");
			corpora.add("news");
		}else if (lang.equals("ukr")) {
			corpora.add("wiki");
			corpora.add("news");
//			corpora.add("wiki2");
//			corpora.add("lit");
//			corpora.add("");
		}else {
			corpora.add("wiki");
			corpora.add("news");
		}
		return corpora;
	}
	
	public static String getCorpusDe(String corpusName) {
		if(corpusName.equals("company"))
			return "C:\\2Projects\\CorporaIndices\\Leipzig\\de-companyCorpus.txt";
		else if(corpusName.equals("disease"))
			return "C:\\2Projects\\CorporaIndices\\Leipzig\\de-diseaseCorpus.txt";
		return corpusName;
	}

	public static List<String> getSentsDE() {
		return Arrays.asList(
				"Peter Müller arbeitet bei ALPHA Solutions AG in New York und Santa Clara.",
				"Ebenfalls ist die HADAG für den Werksverkehr von Airbus zuständig.",
				"Am 14. November 1924 wurde die Gründung der I.G. Farben AG beschlossen.",
				"Im Unterschied zu vielen anderen (ehemaligen) Flagcarriern in Europa befindet sich die Deutsche Lufthansa AG heute mehrheitlich in Privatbesitz	.",
				"Der Konzern Deutsche Lufthansa AG ist im DAX an der Frankfurter Wertpapierbörse notiert.",
				"Almea und Lufthansa waren in diesem Verhältnis (51:49) Eigentümer der Firma AirTrust AG, die wiederum Alleineigentümerin der Swiss war.",
				"An der deutschen Regionalfluggesellschaft Eurowings Luftverkehrs AG ist der Lufthansa-Konzern mit 49 Prozent Kapitalanteil beteiligt.",
				"Aufgrund seiner Stimmenmehrheit bei der Eurowings Luftverkehrs AG leitete der Lufthansa-Konzern einige Zeit indirekt die Billigfluggesellschaft Germanwings.",
				"Die Lufthansa Consulting GmbH mit Sitz in Köln ist weltweit aktiv im luftfahrtaffinen Beratungsgeschäft.",
				"Seit ihrer Ausgliederung aus der Deutschen Lufthansa AG und Gründung als eigenständige GmbH im Jahr 1988 hat sie über 1.500 luftfahrtspezifische Projekte durchgeführt (Stand: 2007), vor allem in den Bereichen Airline Strategy, Airline Restructuring, Air Cargo Logistics, Airports und Airline Operations.",
				"1969 übernahm die BASF die Wintershall AG aus Kassel (Umsatz 1965: 1,24 Milliarden DM), und sicherte somit ihre inländische Rohstoffversorgung ab.",
				"1970 wurden die Produktionsstätten der Wintershall AG mit der Salzdetfurth AG und der Burbach-Kaliwerke AG in die neu gegründete Kali und Salz GmbH in Kassel eingebracht, an der die BASF fortan die Aktienmehrheit hielt.",
				"1970 wurde zusammen mit Degussa in dem Gemeinschaftsunternehmen Ultraform GmbH die Produktion von Acetal-Copolymerisat aufgenommen.",
				"Zur Stärkung des Pharma-Sektors übernahm man 1975 die Mehrheit an der Knoll AG in Ludwigshafen.",
				"1982 wurde die Knoll AG eine 100 %-Tochter.",
				"Die Vorstöße von Bundeskanzler Schröder und Außenminister Fischer stellen im Grunde der Uno ein schlechtes Zeugnis aus.Die Vorschläge zur Trennung der Konfliktparteien in Nahost, die allerdings nur durch einen internationalen Militäreinsatz möglich wäre, hätten von der Uno kommen müssen.",
				"Die Wahl Baden-Württembergs für einen möglichen Streik in der Metallindustrie kam alles andere als überraschend.",
				"Mercedes-Benz, Porsche, Bosch - diese Namen von Unternehmen aus dem Südwesten sind in aller Welt bekannt.",
				"Die meisten Werte im Deutschen Aktienindex DAX haben am Mittwoch bei geringen Umsätzen in der Verlustzone gestanden.",
				"Der größte deutsche Papier- und Schreibwarenhersteller Herlitz ist vorerst gerettet.",
				"Paolo Bettini gewann am Sonntag das älteste Weltcup-Rennen. ",
				"Der Fahrgastverband Pro Bahn hat bei der Ausschreibung von Nahverkehrsleistungen auf der Schiene transparente Verfahren gefordert.",
				"Der amerikanische Rock'n'Roller Jerry Lee Lewis (66) ist offiziell wieder Single.",
				"Der designierte Chefdirigent der Berliner Philharmoniker, Sir Simon Rattle.",
				"Real Madrid bangt vor dem Viertelfinal-Hinspiel in der Fußball-Champions-League am Dienstagabend beim FC Bayern München weiterhin um den Einsatz seines Mittelfeldstars Zinedine Zidane.",
				"Der Psychoanalytiker und Friedensaktivist Horst-Eberhard Richter ist am Dienstag mit der Goetheplakette der Stadt Frankfurt ausgezeichnet worden.",
				"Die Adam Opel AG setzt nach massiven Absatzeinbußen auf das zweite Halbjahr.",
				"Im Metall-Tarifkonflikt steht eine Einigung weiter aus. Arbeitgeber und IG Metall beendeten ihre siebte Verhandlungsrunde im Bezirk Baden-Württemberg am Mittwoch in Ludwigsburg ohne Ergebnis.",
				"US-Präsident George W. Bush setzt Palästinenserführer Jassir Arafat nicht mit den Anstiftern des internationalen Terrorismus gleich.",
				"An der polnischen Regionalfluggesellschaft. ",
				"An der italienischen Regionalfluggesellschaft. ",
				"Kerrie Lewis werde das Musikunternehmen JKL Enterprises auch in Zukunft als Präsidentin leiten, hieß es.",
				"Metro 1,02 35,45 (- 1,16) MLP 0,38 73,76 (+ 0,27) Münchener Rück ",
				"Umstrittenen Rabatt-Kauf bei Ikea.	",
				"ABC Holiday Plus GmbH,     ADAC Reisebüro im Service-Center München (ost), ABA-Tours e.K.,",
				"Sachsens vorzeitig zurückgetretener Regierungschef Kurt Biedenkopf (CDU) und seine Frau haben beim schwedischen Möbelhersteller Ikea nicht nur einmal Rabatt erhalten.",
				"Davor war bekannt geworden, dass Biedenkopf einen bei Ikea eigentlich unüblichen Rabatt von 15 Prozent erhalten hatte. ",
				"Der Präsident der Deutschen Schutzvereinigung für Wertpapierbesitz (DSW), Roland Oetker, wird auch in einem weiteren Fall des Insiderhandels verdächtigt.",
				"Oberstaatsanwalt Johannes Mocken von der Düsseldorfer Staatsanwaltschaft bestätigte am Samstag einen Bericht des Nachrichtenmagazins «Focus»,",
				" wonach gegen Oetker auch wegen des Verdachts ermittelt werde,",
				" bei der Übernahme des Textilunternehmens Verseidag AG durch die Gamma Holding 1998 von seinem Insiderwissen Gebrauch gemacht zu haben.",
				" SAP, Schering, Siemens, ThyssenKrupp, VW, Stand Veränderung DAX, MDAX, CDAX, NEMAX-50 882,26"
				);
		}
}
