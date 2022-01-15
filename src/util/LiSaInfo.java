//package util;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.Set;
//
//import com.dtg.LiSa.wordanalyzer.Reading;
//import com.dtg.LiSa.wordanalyzer.Result;
//import com.dtg.LiSa.wordanalyzer.WordAnalyzer;
//import com.dtg.LiSa.wordanalyzer.WordAnalyzer.Workbench;
//
//public class LiSaInfo {
//	
//	static Result result = new Result();
//	static 		    WordAnalyzer.TokenCase tokenCase = WordAnalyzer.FLOW_CASE;
//
//	public static Reading[] getLiSaReadings(String w, String lang) {
//		 boolean simplifiedCats = false;
//		    WordAnalyzer analyzer;
////		    WordAnalyzer.setLicenseFile(new File ("C:/workspace/LiSa/Resource/License/intern_lisa.lic"));
//		    WordAnalyzer.Workbench workbench;
//		    Result result = new Result();
//		    Reading[] readings = null;
//
//		    String word = "ABS";
//		    
//		    System.out.println("\n Bitte [Wort,Sprache] oder [Wort] eingeben.");
//		    System.out.println("(Abbruch durch Eingabe von e): ");
//		    while(true){
//		      BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
//		    try {
//		      String words = in.readLine();
//		      String[] sarr = words.split("[,;:]");
//		      if(sarr.length == 1)
//		        word = sarr[0];
//		      else if(sarr.length == 2){
//		        word = sarr[0]; lang = sarr[1];
//		      } else{
//		        System.err.println("wrong arguments");
//		        continue;
//		      }
//		      /* Eingabe einer Zeile über die Tastatur. */
//		      if ( word.equals("e") == true ) break;
////		    word = "città";
////		    lang = "it";
//		      analyzer = WordAnalyzer.getInstance(lang);
//		      workbench = analyzer.getWorkbench();
//		      workbench.hyphenAnalyze(word, result, simplifiedCats, tokenCase, true);
//		      readings = result.getReadings();
//		      if(readings.length == 0){
//		        System.err.println(word + ">UNKNOWN");
//		      } else{
//		        System.out.println(word);
//		        for(int i = 0; i < readings.length; ++i){
//		          System.out.println("\t" + readings[i]);
//		        }      
//		      
//		      }
//		    }catch( Exception e ){   
//		      System.out.println("Eingabefehler: "+e.toString());
//		    }
//		    }
//			return readings;
//		    
//	}
//	
//	public static String getCat(Workbench wb, String w, boolean simplCats) {
//		result.clear();
//		wb.hyphenAnalyze(w, result, simplCats, tokenCase, true);
//		if(result.getReadings() == null || result.getReadings().length == 0)
//			return "UNKNOWN";
//		 
//		for(Reading r: result.getReadings()) {
//			if(r.cat().toString().equals("FUNCTIONAL"))
//				return r.cat().toString();
//		}
//		return result.getReadings()[0].cat().toString();
//	}
//
//	public static String mapCat(String cat) {
//		//[ADJECTIVE_AT, ADJECTIVE_PR, UNDEFINED, VERB_PA, VERB_PP, NOUN_NE, VERB_IN, NOUN, VERB_FL]
//		if(cat.startsWith("ADJ")) return "ADJECTIVE";
//		if(cat.startsWith("VERB_P")) return "ADJECTIVE";
//		if(cat.startsWith("VERB")) return "VERB";
////		if(cat.startsWith("NOUN")) return "NOUN";
//		return cat;
//	}
//
//}
// 