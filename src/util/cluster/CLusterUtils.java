package util.cluster;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javafx.util.Pair;
import model.WordSequences;
import modelparts.Word;
import modeltrain.SyntParTrain;
import modelutils.Cluster;
import modelutils.Clusters;
import modelutils.Vector;
import util.ListOps;
import util.MapsOps;
import util.MyUtils;

public class CLusterUtils {
	public static void analyseClusterConsistence(List<Cluster> clusters, WordSequences wsmodel) {
		Map<Cluster,SortedMap<String, Double>> foundL = new HashMap<>();
		Map<Cluster,SortedMap<String, Double>> foundR = new HashMap<>();
		for(Cluster c: clusters) {
			//collect all right neighbours stats
			Map<Word,Double> freqMapR = new HashMap<>();
			Map<Word,Double> freqMapL = new HashMap<>();
			for(Pair<String,Vector> p: c.getWordValues()) {
				Word w = wsmodel.getWord(p.getKey()); 
				for(Word wright: w.left_of.keySet()) {
					if(wright.toString().equals("AAANF") || wright.toString().equals("EEEND")|| wright.toString().equals("und")
							|| wright.toString().equals("і") || wright.toString().equals("та") ) continue;
					MapsOps.addFreqObject(wright, freqMapR, 1.0);
				}
				//collect all left neighbours stats
				for(Word wright: w.right_of.keySet()) {
					if(wright.toString().equals("AAANF") || wright.toString().equals("EEEND")|| wright.toString().equals("und")
							|| wright.toString().equals("і") || wright.toString().equals("та") ) continue;
					MapsOps.addFreqObject(wright, freqMapL, 1.0);
				}
			}
			//compute stats per cluster: % cluster members as left / right NBours; % of cluster members with the same context left or right
//			double sumFoundRight = 0;
//			double sumFoundLeft = 0;
//			for(Pair<String,Vector> p: c.getWordValues()) {
//				Word w = wsmodel.getWord(p.getKey()); 
//				if(freqMapR.containsKey(w)) sumFoundRight++;
//				if(freqMapL.containsKey(w)) sumFoundLeft++;
//				
//			}
			//ouput stats
//			System.out.println("FoundR\t" + MyUtils.rdouble(sumFoundRight/c.size()) 
//				+ "\tFoundL\t" + MyUtils.rdouble(sumFoundLeft/c.size()) +"\t" + c.toString());
//			MyPair sameContextR = MapsOps.getFirst(freqMapR);
//			MyPair sameContextL = MapsOps.getFirst(freqMapL);
			
			SortedMap<String, Double> sameContextR = MapsOps.getSortedMapWord(freqMapR); //MapsOps.getFirstEntries(freqMapR, 50);
			SortedMap<String,Double> sameContextL = MapsOps.getSortedMapWord(freqMapL);
			foundL.put(c, sameContextL);
			foundR.put(c, sameContextR);
			
		}
		
		Map<Cluster,List<String>> distinctContextsMapR = foundDistinctContexts(foundR);
		Map<Cluster,List<String>> distinctContextsMapL = foundDistinctContexts(foundL);
		
		for(Cluster c: distinctContextsMapR.keySet())
		System.out.println("FoundR\t" + distinctContextsMapR.get(c).toString() +"\t" + c.toString());
		System.out.println("\n\n\n");
		for(Cluster c: distinctContextsMapL.keySet())
			System.out.println("FoundL\t" + distinctContextsMapL.get(c).toString()  +"\t" + c.toString());
	}
	
	private static Map<Cluster,List<String>> foundDistinctContexts(Map<Cluster, SortedMap<String, Double>> foundR) {
		Map<Cluster,List<String>> foundRdistinct = new HashMap<>();
		
		for(Cluster c: foundR.keySet()) {
			SortedMap<String, Double> contexts = foundR.get(c);
			for(String con: contexts.keySet()) {
				double sum = 0.0;
				for(SortedMap<String,Double> conMap: foundR.values()) {
					Set<String> keySet = new HashSet();
					for(String c1: conMap.keySet()) keySet.add(c1);
					if(keySet.contains(con)) sum++;
				}
				if((double)(sum / foundR.size()) < 0.7) {
					if(foundRdistinct.containsKey(c)) foundRdistinct.get(c).add(con); else foundRdistinct.put(c, ListOps.of(con)) ;
				}
				if(foundRdistinct.get(c) != null && foundRdistinct.get(c).size() > 9) break;
			}
		}
		return foundRdistinct;
	}
	
	public static void writeBest5PartnesPerWord(Map<Double, Pair<Integer, Integer>> word_sim_matrix, Cluster cluster, Map<Integer, Cluster> cluster_index, Set<Cluster> seen) {
		List<Double> simList = ListOps.of(word_sim_matrix.keySet());
		Collections.sort(simList);
		StringBuffer sb = new StringBuffer();
		sb.append(cluster.toStringShort() + "\t");
		int foundBest = 0;
		for (int i = 0; i < simList.size(); i++) {
			if(foundBest > 4) break;
			double sim = simList.get(i);
			if(sim > 1.0) break; //check only super similar
			Cluster bestCluster = cluster_index.get(word_sim_matrix.get(sim).getValue()); 
			if(seen.contains(bestCluster)) continue;
			
			sb.append(bestCluster.toStringShort()); //append the cluster which its similarity
			sb.append(MyUtils.rdouble(sim) + "\t");
			foundBest++;
			//seen.add(bestCluster);
		}
		if(foundBest != 0)
			System.out.println(sb.toString());
	}
	




	public static boolean combineClosestClusters(double minSim, Map<Double, Pair<Integer, Integer>> sim_matrix, Map<Integer, Cluster> cluster_index, WordSequences wsmodel) {
		Pair<Integer,Integer> closestClustersIDs = sim_matrix.get(minSim);
		int id_base = closestClustersIDs.getKey();
		int id_added = closestClustersIDs.getValue();
		Cluster c1 = cluster_index.get(id_base);
		Cluster c2 = cluster_index.get(id_added);
		if(Clusters.accepts(c1,c2,wsmodel))
			c1.addCluster(c2);
		else return false;
//		System.out.println("were added: " + c1.toString() + " AND " + c2.toString() + " and sim was: " + MyUtils.rdouble(minSim));
		//now recompute similarities for id_base and delete similarities for id_added
		Set<Double> simKeysToDelete = new HashSet<>();
		Map<Double,Pair<Integer,Integer>> sim_KeyValuesToAdd= new HashMap<>();
		for(double sim: sim_matrix.keySet()) {
			Pair<Integer, Integer> currentClusterPair = sim_matrix.get(sim);
			int id1 = currentClusterPair.getKey();
			int id2 = currentClusterPair.getValue();
			if(id_added == id1 || id_added == id2) {
				simKeysToDelete.add(sim);
				continue;
			}
			if(id_base == id1 || id_base == id2) {
				simKeysToDelete.add(sim);
				double new_sim = Cluster.computeSimilarity(cluster_index.get(id1), cluster_index.get(id2));
				if(sim_KeyValuesToAdd.containsKey(new_sim))
					new_sim = SyntParTrain.getSlightlyDifferentKexForMatrix(new_sim);
				sim_KeyValuesToAdd.put(new_sim, currentClusterPair);
			}
			
		}
		for(double simDelete: simKeysToDelete)
			sim_matrix.remove(simDelete);
		for(double simAdd: sim_KeyValuesToAdd.keySet()) {
			Pair<Integer, Integer> clusterPair = sim_KeyValuesToAdd.get(simAdd);
			if(sim_matrix.containsKey(simAdd))
				simAdd = SyntParTrain.getSlightlyDifferentKexForMatrix(simAdd);
			sim_matrix.put(simAdd, clusterPair);
		}
		return true;
	}
	

}
