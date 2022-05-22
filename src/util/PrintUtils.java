package util;

import model.WordSequences;
import modelparts.Word;
import modelparts.WordMaps;
import modeltrain.SyntParVectorTrain;
import modelutils.Cluster;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class PrintUtils {
  public static void printWMAPSinfo(Word w, Writer out, boolean explain, WordSequences model) {
    StringBuffer sb = new StringBuffer();

    List<MyPair> votes = w.getMPVotes();
    String votedMPfirst = w.getFirstVotedMP(votes);
    boolean secondMPsameScoreAsFirst = false;
    if(votes.size() > 1 && votes.get(0).freq == votes.get(1).freq){
      secondMPsameScoreAsFirst  = true;
    }
    String taggedMP = w.getMorphParadigm() == null ? SyntParVectorTrain.MZERO : w.getMorphParadigm().getLabel();
    if(secondMPsameScoreAsFirst){
      sb.append("DOUBLE\t"+w.toString()+"\t"+taggedMP+"\t"+votedMPfirst+"\t"+votes.get(1).first+"\t");
    }
    else if(votedMPfirst.equals(taggedMP))
      sb.append("SAME\t"+w.toString()+"\t"+votedMPfirst+"\t");
    else
      sb.append("DIFF\t"+w.toString()+"\t"+taggedMP+"\t"+votedMPfirst+"\t");
    sb.append(w.toString());
    sb.append("\t");
    List<MyPair> mpairs = w.wmaps().getVotes_mp();
    sb.append(mpairs.toString());
    sb.append("\t");
    mpairs = w.wmaps().getVotes_sy();
    sb.append(mpairs.toString());
    sb.append("\t");
    if(explain)    explainSyCats(w.wmaps(), sb, model);

    printInfo(w.wmaps(), sb);
    sb.append("\t");
    System.out.println(sb.toString());
    if (out != null){
      try {
        out.write(sb.toString()+"\n");
        out.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void printInfo(WordMaps wmaps, StringBuffer sb) {
    sb.append(wmaps.getMp_map().toString());
    sb.append("\t");
    sb.append(wmaps.getSy_map().toString());
    sb.append("\t");
  }



  public static void explainSyCats(WordMaps wmaps, StringBuffer sb, WordSequences model) {
    for(MyPair mp: wmaps.getVotes_sy()){
      sb.append(mp.first).append("\t").append(mp.freq).append("\t");
      String cat = mp.first;
      Cluster c = model.idx().getSyntParadigm(cat);
      if(c != null) sb.append(c.toStringInfoShort()+";");
      else sb.append(cat+";");
      break;
    }
  }
}
