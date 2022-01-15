//package experiment;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import model.WordSequences;
//import processText.mainModels.BuildWordSequenceModelDE;
//import processText.mainModels.BuildWordSequenceModelUKR;
//import util.MapsOps;
//import util.MyUtils;
//
//public class TryMorphParadigmOnSemRows {
//	
//	public static void main(String[] args) throws IOException {
//		String lang = "ukr";
////		lang = "de";
//		
//		
//		
//		WordSequences model = getModelWithMorph(lang);
//		process(model);
//	}
//	
//	private static void process(WordSequences model) throws IOException {
//		List<String> lines = getLines(model.getLang() );
//		
//		for(String line: lines) {
//			//for each sem argument line: check which word is in differen Cluster than first words
//			// get words from argument row
//			List<String> semArguments = getWordsFromLine(line);
//			//get cluster per word, save into HashMap
//			Map<String,String> wordToMcluster = getWordToClusterMap(model, semArguments);
//			//output cluster stats for row members
//			outputStats(semArguments, wordToMcluster, line);
//		}
//
//		
//		
//	}
//
//	private static void outputStats(List<String> semArguments, Map<String, String> wordToMcluster, String line) {
//		Map<String,Double> clusterFreqMap = new HashMap<>();
//		Map<String,Set<String>> clusterToWordMap = new HashMap<>();
//		for(String w: semArguments) {
//			String cluster = wordToMcluster.get(w);
//			MapsOps.addFreq(cluster, clusterFreqMap);
//			MapsOps.addStringToValueSet(cluster, clusterToWordMap, w);
//		}
//		System.out.println("for line:\t" + line.substring(0, Math.min(line.length(), 100)));
//		System.out.println("found clusters:\t" + clusterFreqMap.size() + "\t" + MapsOps.getSortedMap(clusterFreqMap, null, false).toString());
//		System.out.println("cluster to word:\t" + clusterToWordMap.toString());
//	}
//
//	private static Map<String, String> getWordToClusterMap(WordSequences model, List<String> semArguments) {
//		Map<String,String> wordToClusterMap = new HashMap<String, String>();
//		for(String wString: semArguments) {
//			String morphLabel = model.getWord(wString).morphLabel;
//			if(morphLabel == null) morphLabel = "mNull";
//			wordToClusterMap.put(wString, morphLabel);
//		}
//		return wordToClusterMap;
//	}
//
//	private static List<String> getWordsFromLine(String line) {
//		List<String> words = new ArrayList<String>();
//		String[] sarr = line.split(", ");
//		for(String s: sarr) {
//			words.add(s.split("=")[0]);
//		}
//		return words;
//	}
//
//	private static WordSequences getModelWithMorph(String lang) throws IOException {
//		if(lang.equals("ukr")) return BuildWordSequenceModelUKR.getModelWithClustersMorph();
//		else if (lang.equals("de")) return BuildWordSequenceModelDE.getModelWithClustersMorph();
//		else return null;
//	}
//	
//
//	public static List<String> getLines(String lang) throws IOException{
//		String inFileString = null;
//		if(lang.equals("ukr")) inFileString = "in\\ukr-someArgumentRows-toTestMorphOnSem.txt";
//		else if(lang.equals("de")) inFileString = "in\\de-someArgumentRows-toTestMorphOnSem.txt";
//		
//		return MyUtils.readLines(inFileString);
//	}
//
//}
