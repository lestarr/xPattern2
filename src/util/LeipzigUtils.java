package util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeipzigUtils {

	/**
	 * Takes cooccurences from Leipzig and writes the real words down instead of word indices
	 * @param wordMapIn
	 * @param wordCoocIn
	 * @param outFile
	 */
	public static List<MyPair> translateWordCoocs( String wordCoocIn, String outFile, int minFreq, int freqIdx) {
		 Map<String, String> wmap = new HashMap<>();
		Writer out = null;
		if(outFile != null)
			out = MyUtils.getWriter(outFile);
		List<MyPair> toReturn = new ArrayList<MyPair>();
		try {
			for (String line : CorpusUtils.getLines(wordCoocIn)) {
				String[] sarr = line.split("\t");
				if (sarr.length < 4)
					continue;
				if(Integer.parseInt(sarr[2]) < minFreq)
					continue;
				String wleft = sarr[0];
				String wright = sarr[1];

				if (wmap.containsKey(wleft) && wmap.containsKey(wright)) {
					if(out != null)
						out.write(wmap.get(wleft) + "\t" + wmap.get(wright) + "\t" + sarr[2] + "\t" + sarr[3] + "\n");
					toReturn.add(new MyPair(wmap.get(wleft), wmap.get(wright), Double.parseDouble(sarr[2]) ));
				}
			}
			if(out != null)
				out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toReturn;
	}
	



}
