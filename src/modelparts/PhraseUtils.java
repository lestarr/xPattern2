package modelparts;

import model.WordSequences;
import tokenizer.TestTokenizer;
import util.MyPairWord;
import util.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class PhraseUtils {
  static public List<MyPairWord> getBigrams(String sent, String lang, WordSequences model){
    List<MyPairWord> list = new ArrayList<>();
    String prev = null;
    for(String curr: TestTokenizer.getTokens(sent, lang, true, true)) {
      if(prev == null) {
        prev = curr;
        continue;
      }
      MyPairWord p = new MyPairWord(model.getWord(prev), model.getWord(curr));
      prev = curr;
      list.add(p);
    }
    return list;
  }

  public static String getSentFromBigrams(List<MyPairWord> bigrams, boolean addBigramSignif) {
    StringBuffer sb = new StringBuffer();
    for(MyPairWord p: bigrams) {

      sb.append(p.left.toString());
      if(addBigramSignif) {
        sb.append("/");
        sb.append(MyUtils.rdouble(p.signif));

      }
      if(p.hasLow) sb.append("|");
      if(p.peakInRound == 1) sb.append("_");
      if(p.peakInRound == 2) sb.append("_._");
      if(p.peakInRound > 2) sb.append("__._");
      sb.append(" ");
    }
    sb.append(bigrams.get(bigrams.size()-1).right.toString());

    return sb.toString().replaceAll("_ ", "_").replaceAll("\\|", "\n");
  }


}
