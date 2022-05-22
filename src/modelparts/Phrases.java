package modelparts;

import model.MorphVectorModel;
import model.WordSequences;
import util.MyPairWord;

import java.util.List;

public class Phrases {

  public static void testSentencesMorphCats(WordSequences model){
    List<String> sents = Sentences.getSentsTest(model.getLang(), -1);
    for(String sent: sents){
      testMorphCats(sent, model);
    }
  }

  public static void testMorphCats(String sent, WordSequences model){
    List<MyPairWord> bigrams = PhraseUtils.getBigrams(sent, model.getLang(), model);
    Word prev = null;
    for(MyPairWord mpw: bigrams){
      if(prev == null){
        prev = mpw.left;
        continue;
      }
      Word actualWord = mpw.left;
      Word next = mpw.right;
      actualWord.prev = prev;
      actualWord.next = next;
      MorphVectorModel.tagWordVoting(actualWord, model, null, true);
      prev = actualWord;
    }
  }
}
