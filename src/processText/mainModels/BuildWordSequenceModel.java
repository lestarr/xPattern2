package processText.mainModels;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import experiment.FindPhrases;
import experiment.TestIdeaPhrase;
import experiment.TestIdeaPred;
import javafx.util.Pair;
import lucene.LuceneHelper;
import lucene.Search;
import model.LetterTokModel;
import model.MorphVectorAnalyzer;
import model.MorphVectorModel;
import model.SyntModel;
import model.WordSequences;
import model.Words;
import modelparts.Collocation;
import modelparts.MorphParadigm;
import modelparts.MorphSemParadigms;
import modelparts.Phrases;
import modelparts.Root;
import modelparts.SemParadigms;
import modelparts.Sentences;
import modelparts.Word;
import modeltrain.MorphAnalyzer;
import modeltrain.SyntParTrain;
import modelutils.Cluster;
import modelutils.Vector;
import tokenizer.TestTokenizer;
import util.CorpusUtils;
import util.MapsOps;
import util.MyPair;
import util.MyPairWord;
import util.MyUtils;

public class BuildWordSequenceModel {
	
	public static final String splitString = "[,]";
	public static boolean firsttime77 = true;
    public static boolean firsttime78 = true;
    public static boolean firsttime789 = true;
	
	public static void getInput(WordSequences wsmodel, LetterTokModel ltmodel, WordSequences wsmod2, 
			List<Set<String>> paradigmBundles) throws IOException {
		getInput(wsmodel, ltmodel, wsmod2, paradigmBundles, null);
	}
	
	public static void getInput(WordSequences wsmodel, LetterTokModel ltmodel, WordSequences wsmod2, 
			List<Set<String>> paradigmBundles, Search search) throws IOException {
			while(true) {
	
			String inputString = MyUtils.getSystemInput();
			if(inputString.equals("e")) break;
			
			if(inputString.startsWith("tt")) {
				if(inputString.startsWith("tt0")) 		printSplitterInfo(wsmodel);
				if(inputString.startsWith("tt1")) {		TestIdeaPred.analyzeSplitterPred(wsmodel);}
			}
			
			if(inputString.startsWith("ss1,")) {
				String[] sarr = inputString.split(",");
				if(sarr.length < 4) continue;
				List<String> sents = Sentences.getSentsOnly(wsmodel.getLang(),
						Integer.parseInt(sarr[1]), Integer.parseInt(sarr[2]), Integer.parseInt(sarr[3]) 
						);
				for(String s: sents) {
					System.out.println(s); System.out.println();
				}
			}
			
			if(inputString.startsWith("sss")) {
				String[] sarr = inputString.split(",");
				int start = 1000;
				int step = 500;
				int howmany = 20;
				if(sarr.length > 3) {
					start = Integer.parseInt(sarr[1]);
					step = Integer.parseInt(sarr[2]);
					howmany = Integer.parseInt(sarr[3]);
				}
				List<String> sents = FindPhrases.analyzeSentsAll(wsmodel, start, step, howmany);
				for(String s: sents) {
					System.out.println(s); System.out.println();
				}
			}
			if(inputString.startsWith("sst")) {
				int howmany = 100;
				String[] sarr = inputString.split(",");
				if(sarr.length >1) howmany = Integer.parseInt(sarr[1]);
				List<String> sents = FindPhrases.analyzeTestSents(wsmodel, howmany);
				for(String s: sents) {
					System.out.println(s); System.out.println();
				}
			}
			if(inputString.startsWith("ssp")) {
				int howmany = 50;
				String[] sarr = inputString.split(",");
				if(sarr.length >1) howmany = Integer.parseInt(sarr[1]);
				List<String> sents;
				if(inputString.startsWith("ssp1"))
					sents = FindPhrases.analyzeTestSentsParadigmExpectation(wsmodel, howmany);
				else
					sents = FindPhrases.analyzeTestSentsPred(wsmodel, howmany);
				for(String s: sents) {
					System.out.println(s); System.out.println();
				}
			}

			if(inputString.startsWith("q1")) {
				String[] sarr = inputString.split(",");
				int howmany = 100;
				int thh = 20;
				if(sarr.length >1) thh = Integer.parseInt(sarr[1]);
				if(sarr.length >2) howmany = Integer.parseInt(sarr[2]);
				MorphVectorAnalyzer.morphTagTest(wsmodel, (double)thh, howmany, 20);
			}
			
			
			if(inputString.startsWith("pa")) {
				String[] sarr = inputString.split("#");
				if(sarr.length < 3) continue;
				if(inputString.startsWith("pas")) {
					String sent = sarr[1];
					for(String s: TestTokenizer.getTokens(sent, wsmodel.getLang())) {
						String signifRight = TestIdeaPhrase.findPredArgStrongWord(wsmodel.getWord(s),false);
						String signifLeft = TestIdeaPhrase.findPredArgStrongWord(wsmodel.getWord(s),true);
						System.out.println(signifLeft + "\t\t" + signifRight);
					}
					System.out.println();
					for(String s: TestTokenizer.getTokens(sent, wsmodel.getLang())) {
					}
				}else {
					if(sarr.length < 3) continue;
					TestIdeaPhrase.findPredArgStrong(wsmodel, Integer.parseInt(sarr[1]), Integer.parseInt(sarr[2]));
				}
			}
			if(inputString.startsWith("ssyn")) {
				String[] sarr = inputString.split(",");
				int start = 0;
				int end = 2;
				int predlength = 20;
				double thh = Double.MAX_VALUE;
				if(sarr.length > 1)
					start =	Integer.parseInt(sarr[1]);
				if(sarr.length > 2)
					end =	Integer.parseInt(sarr[2]);
				if(sarr.length > 3)
					predlength =	Integer.parseInt(sarr[3]);
				if(sarr.length > 4)
					thh = Double.parseDouble(sarr[4]);
				int count = 0;
				
				if(inputString.startsWith("ssynd"))
					TestIdeaPred.findSynParadigmsDeep(wsmodel, start, end, predlength,(int)thh) ;
				else
					TestIdeaPred.findSynParadigms(wsmodel, start, end, predlength,thh, "model/sem/"+wsmodel.getLang()+"-sem.exper.model") ;


			
			}
			if(inputString.startsWith("iinf")) {
				String[] sarr = inputString.split(",");
				
				int start = 0;
				int end = 50;
				if(sarr.length > 1)
					start =	Integer.parseInt(sarr[1]);
				if(sarr.length > 2)
					end =	Integer.parseInt(sarr[2]);
				int count = 0;
				for(Word w: wsmodel.idx().getSortedWords()) {
                  if(w.toString().contains("_"))continue;
                    count++;
					if(count < start) continue;
					if(count > end) break;
					if(inputString.startsWith("iinfs"))
						System.out.println(wsmodel.getInfoShort(w.toString()));
					else
						System.out.println(wsmodel.getInfo(w.toString()));
				}
			}
			if(inputString.startsWith("allsuf")) {
				for(String s: wsmodel.idx().seenSuffixes.keySet()) {
					System.out.println(s + "\t-->");
					for(java.util.Map.Entry<String, Double> entry: MapsOps.getSortedMap(wsmodel.idx().seenSuffixes.get(s)).entrySet()) {
						if(entry.getValue() > 1.0)
							System.out.println("\t" + entry.getKey() + "=" + entry.getValue());
					}
				}
			}
			if(inputString.startsWith("cc")) {
				//c1,word,thh,howmany, default: 1=all words, thh = 0.01(means low signif), howmany = 50
				//c1,1,0.9,10
				String[] sarr = inputString.split(",");
				if(sarr.length < 2) continue;
				double thh = 0.01;
				double thh_max = 1.01;
				int howmany = 50;
				if(sarr.length > 2) thh = Double.parseDouble(sarr[2]);
				double thh_high = thh;
				if(sarr.length > 3)  thh_high = Double.parseDouble(sarr[3]);
				if(sarr.length > 4)  howmany = Integer.parseInt(sarr[4]);

				if(sarr.length > 5) thh_max = Double.parseDouble(sarr[5]);
				List<Collocation> colls;
				if( sarr[1].equals("1")) {
					colls = Words.getAllCollocaions(wsmodel, thh, thh_high, thh_max);
				}else {
					Word word = wsmodel.getWord(sarr[1]);
					if(sarr[0].equals("ccl"))
						colls = Words.getWordCollocations(true, word);
					else if(sarr[0].equals("ccr"))
						colls = Words.getWordCollocations(false, word);
					else{
						colls = Words.getWordCollocations(word, thh, 0, thh_high, howmany);
						System.out.println("isLeftCollocWord:\t" + word.isLeftCollocWord());
					}
				}
				System.out.println("found collocations:\t" + colls.size());
				int i = 0;
				for(Collocation c: colls) {
					if(c.sim.high() > thh_max || c.sim.low() > thh_max) continue;
					System.out.println(c.toString() + "\t" + c.getFreq());
//					if(i > howmany) break;
					i++;
				}
			}
			
			if(inputString.startsWith("ssim")) {
				//c1,word,thh,howmany, default: 1=all words, thh = 0.01(means low signif), howmany = 50
				//c1,1,0.9,10
				String[] sarr = inputString.split(",");
				if(sarr.length < 2) continue;
				int howmany = 20;
				int depth = 2;
				if(sarr.length > 2)					howmany = Integer.parseInt(sarr[2]);
				Word word = wsmodel.getWord(sarr[1]);
				
				double thh = Double.MAX_VALUE;
				if(sarr.length > 3) {
					thh = Double.parseDouble(sarr[3]);
					depth = (int)thh;
				}
				
				if(sarr[0].equals("ssimd")) {
					Map<Word,Double> output = TestIdeaPred.getSimilarWordsDeep(word, howmany, wsmodel, Double.MAX_VALUE, depth);
					TestIdeaPred.checkAndPrintSimilarWordsDeep(word, output);
				}
				 if(sarr[0].equals("ssiml"))
						TestIdeaPred.getSimilarWords2(word, wsmodel,howmany,true, thh);
				 else	if(sarr[0].equals("ssiml2"))
					TestIdeaPred.getSimilarWordsLeft(true, word, wsmodel,howmany,thh);
				else if(sarr[0].equals("ssimr"))
					TestIdeaPred.getSimilarWordsLeft(false, word, wsmodel,howmany,thh);
				else if(sarr[0].equals("ssim"))
					TestIdeaPred.getSimilarWords2(word, wsmodel,howmany,false, thh);
				else if(sarr[0].equals("ssim3"))
					TestIdeaPred.findExtendedSynParadigm(word,wsmodel);
				else {
					if(sarr.length < 4) TestIdeaPred.getSimilarWords2(word, wsmodel,howmany,false, thh);
					else TestIdeaPred.getSimilarWords(word, wsmodel,howmany,thh);
				}
			}

			
			if(inputString.startsWith("q#")) {
				// print collocaion info or sents containing collocation
				if(search == null) continue;
				String[] sarr = inputString.split("#");
				List<Integer> ids = LuceneHelper.lookupQuery(sarr[1], search.qp, search.is);
				System.out.println(ids.size());
				if(sarr[1].split(" ").length == 2) {
					//print out collocational info
					String phraseOnly = sarr[1].replace("\"", "");
					System.out.println(MyUtils.rdouble((double)ids.size() / (double)LuceneHelper.lookupQuery(phraseOnly.split(" ")[1], search.qp, search.is).size()) 
							+ ", " +  MyUtils.rdouble((double)ids.size() / (double)LuceneHelper.lookupQuery(phraseOnly.split(" ")[0], search.qp, search.is).size()) );
				}
				if(sarr.length == 3) {
					System.out.println("SENTS: ");
					int count = 0;
					int max = 5;
					if(sarr[2].length() > 0) max = Integer.parseInt(sarr[2]);
					for(String s: LuceneHelper.makeHits(ids, search.ir)) {
						count++;
						if(count > max) break;
						System.out.println(s);
					}
				}
				continue;
			}


			else if(inputString.contains(" ")) { // geht in beide Richtungen: preds vom w0 und args von w1 oder preds von w1 und args von w0
				String[] sarr = inputString.split(" ");
				if(sarr.length == 3 && sarr[2].equals("c")) {
					Words.isCollocation(wsmodel, wsmodel.getWord(sarr[0]), wsmodel.getWord(sarr[1]), 0.0, 0.001, true);
				}
				if(sarr.length == 3 && sarr[2].equals("b")) {
					Word l = wsmodel.getWord(sarr[0]);
					Word r = wsmodel.getWord(sarr[1]);
					Set<Word> wordcatsLeft = l.getWordCats(wsmodel);
					Set<Word> wordcatsRight = r.getWordCats(wsmodel);
					Phrases.getBiggestHighCollocation(wordcatsLeft, wordcatsRight, true);
				}
//				if(sarr.length == 3 && sarr[2].equals("p")) {
//					List<MyPairWord> bigrs = Phrases.getBigrams(
//							sarr[0] +" " + sarr[1], wsmodel.getLang(), wsmodel);
//					bigrs = Phrases.interpretExpectations(bigrs, wsmodel, 0.01, true);
//				}
//				if(sarr.length == 3 && sarr[2].equals("cc")) {
//					double res = Phrases.checkBigramExpectations(new MyPairWord(wsmodel.getWord(sarr[0]), wsmodel.getWord(sarr[1])),
//							wsmodel, 0.01,true);
//					System.out.println(MyUtils.rdouble(res));
//				}
				if(sarr.length == 3 && sarr[2].equals("sim")) {
					double sim = Cluster.computeVectorSimilarity(new Vector(SyntParTrain.collectFeatures( wsmodel.getWord(sarr[0]), wsmodel)), new Vector(SyntParTrain.collectFeatures( wsmodel.getWord(sarr[1]), wsmodel)));
					System.out.println(MyUtils.rdouble(sim));
				}
			}
//				if(sarr.length == 2) {
//					WordSequences.getPredArgSimilarity(wsmodel, sarr[0], sarr[1], true);
//				}
//			}
			
			String[] input = inputString.split(splitString);
			String input0 = input[0];
			

			
			Word inputword = wsmodel.getWord(input0);
			
			if(input.length == 2 && input[1].equals("9")) {
				System.out.println(inputword.getWordCats(wsmodel));
			}
            if(input.length == 2 && input[1].equals("8")) {
                System.out.println("expectations");
                Words.printExpectations(inputword, 0.01, wsmodel);
            }
            if(input.length == 2 && input[1].equals("88")) {
              List<MyPairWord> exp = inputword.getBestContextsComputeNew(true, 20, Words.SYN_FILTER, wsmodel, false);
                System.out.println("expectations");
                System.out.println(exp);
                exp = inputword.getBestContextsComputeNew(false, 20, Words.SYN_FILTER, wsmodel, false);
                System.out.println(exp);
            }
			
			if(input.length == 2 && input[1].equals("7")) {
				System.out.println("synt/morph contexts, thh 0.01, map String, Double");
				System.out.println(inputword.getBestContextsComputeNew(true, 20, Words.SYN_FILTER, wsmodel, true).toString());
				System.out.println(inputword.getBestContextsComputeNew(false, 20, Words.SYN_FILTER, wsmodel, true).toString());
			}
			if(input.length == 2 && (input[1].equals("77")||input[1].equals("tag"))) {
			  if(firsttime77) {
                MorphVectorAnalyzer.collectMParVectorsParadigm(wsmodel, Words.SYNSEM_FILTER, false, 20);
                MorphVectorAnalyzer.collectMParVectorsFlexion(wsmodel, Words.SYNSEM_FILTER, 20);
			  }
			  firsttime77 = false;
				MorphVectorModel.getTagsOneWord(inputword, wsmodel, true, 20);
			}
			if(input.length == 2 && (input[1].equals("78")||input[1].equals("789"))) {
			  int contextcount_words = 20;
			  if(input[1].equals("789")) contextcount_words = 40;
			  wsmodel.collectKnownParVectors(Words.SYN_FILTER, contextcount_words);
			  List<MyPair> parscores = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(inputword, Words.SYN_FILTER, wsmodel, true, Double.MAX_VALUE, 
			      contextcount_words);
		        Collections.sort(parscores);
		        parscores = parscores.subList(Math.max(0, parscores.size()-5), parscores.size()-1);
		        for(MyPair p: parscores)
		            System.out.println(p.freq + "\t" + p.first);
			}
			if(input.length == 2 && (input[1].equals("777")||input[1].equals("tagf"))) {
			  int contextcount_words = 20;
				MyPair bestpar = MorphVectorAnalyzer.getBestPar(MorphVectorAnalyzer.getMParFromFlexVector(wsmodel, inputword, 
				    false, Words.SYNSEM_FILTER, false, contextcount_words));
				if(bestpar != null) System.out.println(bestpar.toString());
			}
			if(input.length == 3 && input[0].equals("ttt")) {
			  int contextcount_words = 20;
				MorphVectorAnalyzer.tagWordsMparVectorLight(wsmodel, Double.parseDouble(input[1]), Double.parseDouble(input[2]), contextcount_words);
			}
			if(input.length == 2 && input[1].equals("6")) {
				System.out.println("sem contexts, thh 0.001, list MyPairWord");
				System.out.println(Words.getWordContextVector(true, inputword, 20, wsmodel));
				System.out.println(Words.getWordContextVector(false, inputword, 20, wsmodel));
			}
			
				if(ltmodel != null && input.length == 2 && input[1].equals("let")) {
					System.out.println(wsmodel.getInfoShort(inputString));
				}
				if(ltmodel != null && input.length == 2 && (input[1].equals("cut") || input[1].equals("0"))) {
					System.out.println(wsmodel.getInfoShort(inputString));
					System.out.println(MorphAnalyzer.getCutsAndSlices(wsmodel.getLang(), ltmodel, input0));
				}
				if(ltmodel != null && input.length == 2 && input[1].equals("1") && wsmodel.idx().roots.get(input0) != null) {
					System.out.println(wsmodel.idx().roots.get(input0).info());
				}
				if(ltmodel != null && input.length == 2 && input[1].equals("2") && wsmodel.idx().flexes.get(input0) != null) {
					System.out.println(wsmodel.idx().flexes.get(input0).info());
					System.out.println("size: "+ wsmodel.idx().flexes.get(input0).freq());
				}				
				if(ltmodel != null && input.length == 2 && (input[1].equals("suf") || input[1].equals("3")) ) {
					String value = "no such suffix";
					if(wsmodel.idx().seenSuffixes.containsKey(input0))
						value = wsmodel.idx().seenSuffixes.get(input0).toString();
					System.out.println(inputword + "-->\t" + value);
					if(!value.equals("no such suffix"))
						System.out.println(MapsOps.getFirst(wsmodel.idx().seenSuffixes.get(input0))	);
				}
				if(ltmodel != null && input.length == 2 && (input[1].equals("12") || input[1].equals("rf"))) {
					inputword.setFlex(null);
					inputword.setRoot(null);
					MorphAnalyzer.computeRootFlexOneWord(inputword, wsmodel, ltmodel, true);
				}
//				if(ltmodel != null && input.length == 2 && (input[1].equals("11") || input[1].equals("tag"))
//						&& inputword.getRoot() != null) {
//					Root root = wsmodel.idx().getRoot(inputword.getRoot());
//					MorphParadigm bestParadigm = MorphParTrain.getBestParadigm(root, wsmodel.idx().morphPars());
//					if(bestParadigm != null)
//						System.out.println("mpar:\t" + bestParadigm.getLabel() + "\t" + root.toString()  + "\t" + bestParadigm.getFlexes());
//					else if(root.getAmbigParadigmTrue() && root.ambigParadigms != null) {
//						for(MorphParadigm mpar: root.ambigParadigms) {
//							System.out.println("ambig:\t" + mpar.getLabel() + "\t" + root.toString()  + "\t" + mpar.getFlexes());
//						}
//					}
//					else
//						System.out.println("no paradigm found");
//				}
				if(input.length == 2 && (input[1].startsWith("msem") || input[1].equals("123"))) {
					System.out.println(wsmodel.getInfoShort(inputString));
					if(inputword.getRoot() != null) {
						Root r = wsmodel.idx().getRoot(inputword.getRoot());
						MorphSemParadigms.test(r, wsmodel, true, true);
						MorphSemParadigms.test(r, wsmodel, false, true);
					}

				}
				
				
				
				
			if(input.length == 1 && !input0.contains(" ")) {
					System.out.println(wsmodel.getInfoShort(inputString));
			}else if(input.length == 2 && input[0].equals("info")) {
				int howmany = Integer.parseInt(input[1]);
				int i = 0;
				for(Word w: wsmodel.idx().getSortedWords()) {
					i++;
					System.out.println(wsmodel.getInfoShort(w.toString()));
					if(i > howmany) break;
				}
			}
			else if(input.length > 1) {	
				if(input[1].startsWith("seml")) {
					List<List<MyPair>> groups = SemParadigms.computeContextsWithSignifRightLeftTest(inputword, wsmodel, true, true, true);
					if(input[1].startsWith("semlall")) {
						System.out.println("INPUT PRED: "+inputword);
						System.out.println("SEEN preds: "+inputword.getSeenPredicats(true).size()+" "+inputword.getSeenPredicats(true).toString());
						Words.printGroups(groups,Integer.MAX_VALUE);
					}
				}
				if(input[1].startsWith("semr")) {
					List<List<MyPair>> groups = SemParadigms.computeContextsWithSignifRightLeftTest(inputword, wsmodel, false, true, true);
					if(input[1].startsWith("semrall")) {
						System.out.println("INPUT PRED: "+inputword);
						System.out.println("SEEN preds: "+inputword.getSeenPredicats(false).size()+" "+inputword.getSeenPredicats(false).toString());
						Words.printGroups(groups,Integer.MAX_VALUE);
					}
				}
				
				 if(input[1].equals("v")) {
					System.out.println(inputword.toString() + "\t" + SyntParTrain.collectFeatures(inputword, wsmodel).toString());
				}
				 if(input[1].equals("synt")) {
					 Pair<Cluster, Double> pair = SyntModel.tagWord(inputword, wsmodel);
						System.out.println(inputword.toString() + "\t" + MyUtils.rdouble(pair.getValue()) + "\t" + pair.getKey().toStringShort(10));
					}
				 if(input[1].equals("coef")) {
						System.out.println("mean:\t"+MyUtils.rdouble(inputword.meanL) + "\tright:\t" + MyUtils.rdouble(inputword.meanR));

						System.out.println("coef:\t"+MyUtils.rdouble(inputword.getCoef(true)) + "\tright:\t" + MyUtils.rdouble(inputword.getCoef(false)));
					}
				 if(input[1].equals("coef1d")) {
						System.out.println(MyUtils.rdouble(inputword.getCoefOneDirection(true)) + "\t" + MyUtils.rdouble(inputword.getCoefOneDirection(false)));
					}
				 else if(input[1].equals("ro")||input[1].equals("root")||input[1].equals("r")) {
					if(inputword.getRoot() != null)
						System.out.println(wsmodel.idx().getRoot(inputword.getRoot()).info() + "\t--from model");
					else System.out.println("no root");
				}
				else if(input[1].equals("fl")||input[1].equals("flex"))
				{
					if(inputword.getFlex() != null)
						System.out.println(wsmodel.idx().getRoot(inputword.getFlex()).info() + "\t--from model");
					else System.out.println("no flex");
				}
				else if(input[1].equals("l")) {
					System.out.println(input+", left of = "+inputword.left_of.size()+": ");
					MapsOps.printSortedMap(inputword.left_of, null, input.length > 2 ? Integer.parseInt(input[2]):-1, 0, true, ", ");
				}else if(input[1].equals("ls")) {
					if(input.length > 2) {
						for(String s1: input[2].split("#")) {
							Word s1word = wsmodel.getWord(s1);
							System.out.println("\n"+s1+", left of = " + s1word.left_of.size() + ": ");
							Words.computeContextsWithSignif(s1word, true, -1, true);
						}
					}
					System.out.println(input[0]+", left of = "+inputword.left_of.size()+": ");
					Words.computeContextsWithSignif(inputword, true, -1, true);
				}
				else if(input[1].equals("rs")) {
					if(input.length > 2) {
						for(String s1: input[2].split("#")) {
							Word s1word = wsmodel.getWord(s1);
							System.out.println("\n"+s1+", right of = " + s1word.right_of.size() + ": ");
							Words.computeContextsWithSignif(s1word, false, -1, true);
						}
					}
						System.out.println(input[0]+", right of = "+inputword.right_of.size()+": ");
						Words.computeContextsWithSignif(inputword, false, -1, true);
				}
			else if(input[1].equals("r")) {
					System.out.println(input+", right of = "+inputword.right_of.size()+": ");
					MapsOps.printSortedMap(inputword.right_of, null, input.length > 2 ? Integer.parseInt(input[2]):-1, 0, true, ", ");
				}else if(input.length > 2 && input[2].equals("lr")) {
					double signifL = Words.getSignifOfLeft(inputword, wsmodel.getWord(input[1]));
					double signifR = Words.getSignifOfRight(inputword, wsmodel.getWord(input[1]));
					System.out.println(MyUtils.rdouble(signifL) + ", "+ MyUtils.rdouble(signifR));
				
				}
			}

			System.out.println("-------------------");
					
		}

	}
	
	private static void printSplitterInfo(WordSequences model) {
		Set<Word> splitters = model.getWord(Words.SPLITTER_WORD_LABEL).paradigmWords;
		for(Word splitter: splitters)
			System.out.println("splitter:\t" + model.getInfo(splitter.toString()));
	}

	/**
	 * @param lang
	 * @param corpora
	 * @param howManySents
	 * @param toLower
	 * @param start if start = 0, automatic step will be calculated to deal with alphabetically stored corus
	 * @return
	 * @throws IOException
	 */
	public static WordSequences getWSModel(String lang, String[] corpora, int howManySents, boolean toLower, int start) throws IOException {
		System.out.println("I am in test");
		if(corpora == null)	corpora = new String[] { "wiki" };
		WordSequences wsmodel = new WordSequences(lang);
		for(String corpus: corpora)
			wsmodel.addWordsToModel(CorpusUtils.getLeipzigSentences(lang, corpus, start, howManySents),	toLower, false);
		
		return wsmodel;
	}
	
	public static void addPhrases(WordSequences wsmodel, String[] corpora, int start, int howmany) {
		for(String c: corpora) {
			List<String>sents = CorpusUtils.getLeipzigSentences(wsmodel.getLang(), c, start, howmany);
			sents = FindPhrases.sentnceToPTokens(wsmodel, sents);
			wsmodel.addWordPhrasesToModel(sents, false);                       
		}		
	}
	
	public static void printSyntParadigmInsight(WordSequences model, Cluster c){
	  Word cWord = model.getWord(c.getLabel());
	     System.out.println(cWord + " " +c.getParadigmWords().size() + " " + c.getParadigmWordsSorted());
	     Words.printExpectations(cWord, 0.01, model);
//	   List<MyPair> parscores = MorphVectorAnalyzer.getMParFromFlexVector(model, cWord, false, Words.SYNSEM_FILTER, false, 40);
//	   if(parscores != null && parscores.size() > 0)   
//	     System.out.println("77: " + parscores.get(parscores.size()-1) + " ... " + parscores);
	}

  public static void printParadigmExpectations(WordSequences wsmodel) {
    for(Cluster c: wsmodel.idx().syntPars().values()) {
     Word cWord = wsmodel.getWord(c.getLabel());
     System.out.println(cWord + " " +c.getParadigmWords().size() + " " + c.getParadigmWordsSorted());
     Words.printExpectations(cWord, 0.01, wsmodel);
    }
    System.out.println();
    for(MorphParadigm mp: wsmodel.idx().getMorphParadigms()) {
      Word cWord = wsmodel.getWord(mp.getLabel());
      System.out.println(cWord + " " +mp.getFlexes().size() + " " + mp.getSortedFlex());
      Words.printExpectations(cWord, 0.01, wsmodel);
    }
  }

  public static void printParadigmAssociations(WordSequences wsmodel) {
    MorphVectorAnalyzer.collectMParVectorsFlexion( wsmodel, Words.SYNSEM_FILTER, 40);
    MorphVectorAnalyzer.collectMParVectorsParadigm(wsmodel, Words.SYNSEM_FILTER, false, 40);
    wsmodel.collectKnownParVectors(Words.SYNSEM_FILTER, 40);

    for(Cluster c: wsmodel.idx().syntPars().values()) {
      printMorphAssociations(c, wsmodel, 40);
    }
     System.out.println();
     for(MorphParadigm mp: wsmodel.idx().getMorphParadigms()) {
       Word cWord = wsmodel.getWord(mp.getLabel());
       System.out.println(cWord + " " +mp.getFlexes().size() + " " + mp.getSortedFlex());
       List<MyPair> parscores = MorphVectorAnalyzer.computeBestKnownVectorParadigmS(cWord, Words.SYNSEM_FILTER, wsmodel, true, Double.MAX_VALUE, 40);
         Collections.sort(parscores);
         parscores = parscores.subList(Math.max(0, parscores.size()-5), parscores.size()-1);
         System.out.println("78: " + parscores);
     }
  }

  public static void printMorphAssociations(Cluster c, WordSequences wsmodel, int contextcount) {
    Word cWord = wsmodel.getWord(c.getLabel());
    System.out.println(cWord + " " +c.getParadigmWords().size() + " " + c.getParadigmWordsSorted());
    MyPair bestpar = MorphVectorAnalyzer.getBestPar(
        MorphVectorAnalyzer.getMParFromVector(wsmodel, cWord, false, Words.SYNSEM_FILTER, false,
            MorphVectorAnalyzer.mparContextsMapLeftFlexion,MorphVectorAnalyzer.mparContextsMapRightFlexion, contextcount));
    System.out.println("777: " + bestpar);
    List<MyPair> parscores = MorphVectorAnalyzer.getMParFromVector(wsmodel, cWord, false, Words.SYNSEM_FILTER, false, 
        MorphVectorAnalyzer.mparContextsMapLeft, MorphVectorAnalyzer.mparContextsMapRight, contextcount);
    System.out.println("77: " + parscores.get(parscores.size()-1) + " ... " + parscores);
  }
	
}
