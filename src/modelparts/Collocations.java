package modelparts;

import java.util.List;

import model.WordSequences;
import util.MyPair;

public class Collocations {
	
	public static CollocationCollection findCollocations(List<MyPair> pairs, WordSequences model) {
		CollocationCollection ccoll = new CollocationCollection();
		for(MyPair bigram: pairs) {
			ccoll.add(bigram, computeScore(bigram, model));
		}
		return ccoll;
	}

	public static Similarity computeScore(MyPair bigram, WordSequences model) {
		if(model.getWord(bigram.first).freq() == 0.0 || model.getWord(bigram.second).freq() == 0.0 || bigram.freq == 0.0)
			return new Similarity(0.0, 0.0);
		double freq1 = model.getWord(bigram.first).freq();
		double freq2 = model.getWord(bigram.second).freq();
		return new Similarity((double)(bigram.freq/freq1), (double)(bigram.freq/freq2) );
	}
}
