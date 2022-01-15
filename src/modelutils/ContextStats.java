package modelutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import modelparts.Word;
import util.MyUtils;

public class ContextStats {

	
	/**
	 * coef for a word which is a left context is computed: take meanL for this word, then compare it with the meanR of each context. 
	 * sum all cases where this meanL(word) > meanR(context). coef = sum/all context size. maybe take only signif contexts: >0.0001 or 0.001 
	 * fot the word with is a right context - everything vice versa 
	 * @param w
	 * @param wordIsLeftOf
	 */
	public static double computeContextsCoef(Word w, boolean wordIsLeftOf) {
		double coef;
		HashMap<Word, Double> contexts;
		double thisWordMean;
		if(wordIsLeftOf && w.meanL < 0) computeContextsStats(w, wordIsLeftOf);
		if(!wordIsLeftOf && w.meanR < 0) computeContextsStats(w, wordIsLeftOf);
		
		if (wordIsLeftOf) 	{		contexts = w.left_of; thisWordMean = w.meanL; }
		else				{		contexts = w.right_of; thisWordMean = w.meanR; }
		
		double sumOfTrueWinners = 0.0;
		for(Word cont: contexts.keySet()) {
			if(wordIsLeftOf) {
				if(cont.meanR < 0) computeContextsStats(cont, false);
				if(thisWordMean > cont.meanR) sumOfTrueWinners++;
			} else {
				if(cont.meanL < 0) computeContextsStats(cont, true);
				if(thisWordMean > cont.meanL) sumOfTrueWinners++;
			}
		}
		coef = MyUtils.rdouble(sumOfTrueWinners / contexts.size() );
		return coef;
	}
	
	
	/**
	 * coef for a word which is a left context is computed: take meanL for this word, then compare it with the meanL of each context. 
	 * sum all cases where this meanL(word) > meanL(context). coef = sum/all context size. maybe take only signif contexts: >0.0001 or 0.001 
	 * fot the word with is a right context - everything vice versa 
	 * @param w
	 * @param wordIsLeftOf
	 */
	public static double computeContextsCoefOneDirection(Word w, boolean wordIsLeftOf) {
		double coef;
		HashMap<Word, Double> contexts;
		double thisWordMean;
		if(wordIsLeftOf && w.meanL < 0) computeContextsStats(w, wordIsLeftOf);
		if(!wordIsLeftOf && w.meanR < 0) computeContextsStats(w, wordIsLeftOf);
		
		if (wordIsLeftOf) 	{		contexts = w.left_of; thisWordMean = w.meanL; }
		else				{		contexts = w.right_of; thisWordMean = w.meanR; }
		
		double sumOfTrueWinners = 0.0;
		for(Word cont: contexts.keySet()) {
			if(wordIsLeftOf) {
				if(cont.meanL < 0) computeContextsStats(cont, true);
				if(thisWordMean > cont.meanL) sumOfTrueWinners++;
			} else {
				if(cont.meanR < 0) computeContextsStats(cont, false);
				if(thisWordMean > cont.meanR) sumOfTrueWinners++;
			}
		}
		coef = MyUtils.rdouble(sumOfTrueWinners / contexts.size() );
		return coef;
	}
	
	public static void computeContextsStats(Word w, boolean wordIsLeftOf) {
		HashMap<Word, Double> contexts;
		if (wordIsLeftOf) 			contexts = w.left_of;
		else						contexts = w.right_of;
		List<Double> signifList = new ArrayList<>();
		double contextsSize = contexts.size();
		double sumSignif = 0.0;
		for (Word cont : contexts.keySet()) {
			double freq = contexts.get(cont);
			//double signifCOnt = MyUtils.rdouble(freq / wfreq);
			double signifTestWordDouble = MyUtils.rdouble(freq / cont.freq() ); //w.freq() );
			if(signifTestWordDouble < 0.0001  || signifTestWordDouble == 1.0) {
				contextsSize--;
				continue;
			}
			signifList.add(signifTestWordDouble);
			sumSignif = sumSignif + signifTestWordDouble;
		}
		
		double mean = MyUtils.rdouble(sumSignif / contextsSize);
		StandardDeviation std = new StandardDeviation();
		double stdd = MyUtils.rdouble(std.evaluate(signifList.stream().mapToDouble(i->i).toArray(), mean) );
		
		if(wordIsLeftOf) {
			w.meanL = mean;
			//w.sampleL = contexts.size();
			//w.stdL = stdd;
		}
		else {
			w.meanR = mean;
			//w.sampleR = contexts.size();
			//w.stdR = stdd;
		}
		
	}
	
}
