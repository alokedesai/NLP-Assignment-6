import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Aloke Desai
 * 11/7/14
 *
 *EMAlgorithm that computes word alignments using IBM model 1
 */
public class EMAlgorithm {
	Hashtable<String, Integer> englishMapping = new Hashtable<String, Integer>();
	Hashtable<String, Integer> foreignMapping = new Hashtable<String, Integer>();
	String[] backwardsForeignMapping;

	int numEnglish = 0;
	int numForeign = 0;
	int numLines = 0;

	private int[][] englishSentences;
	int numEnglishSentences = 0;
	
	private int[][] foreignSentences;
	int numForeignSentences = 0;
	
	// the hashtable that we stored P(f|e) in 
	private Hashtable<Integer, Hashtable<Integer, Double>> probAlignments = new Hashtable<Integer, Hashtable<Integer, Double>>();
	
	// the english sentences post processing
//	private ArrayList<ArrayList<String>> englishSentences = new ArrayList<ArrayList<String>>();
	
	//the foreign corpus post processing
//	private ArrayList<ArrayList<String>> foreignSentences = new ArrayList<ArrayList<String>>();
	
	/**
	 * Runs the EMAlgorithm a certain number of time 
	 * 
	 * @param english the filename of the english sentences
	 * @param foreign the filename of the foreign sentence
	 * @param iterations the number of iterations the EM algorithm should run
	 */
	public EMAlgorithm(String english, String foreign, int iterations) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(english),
			        "UTF8"));
			String currentLine = in.readLine();
			
			while (currentLine != null) {
				numLines++;
				String sentence = currentLine;
				
				for (String s: sentence.split(" ")) {

					if (!englishMapping.containsKey(s)) {
						englishMapping.put(s, numEnglish);
						numEnglish++;
					}
				}
				currentLine = in.readLine();
			}
			in.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(foreign),
			        "UTF8"));
			String currentLine = in.readLine();
			
			while (currentLine != null) {
				String sentence = currentLine;
				for (String s : sentence.split(" ")) {
					if (!foreignMapping.containsKey(s)) {
						foreignMapping.put(s, numForeign);
						numForeign++;
					}
				}
				currentLine = in.readLine();
			}
			in.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		englishSentences = new int[numLines][];
		foreignSentences = new int[numLines][];
		
		englishMapping.put("NULL", numEnglish);
		numEnglish++;

		// file input, put all english sentences and foreign sentences into associated arraylist
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(english),
			        "UTF8"));
			String currentLine = in.readLine();
			
			while (currentLine != null) {
				String sentence = currentLine;
				String[] temp = sentence.split(" ");
				int[] E = new int[temp.length + 1];
				
				for (int i = 0; i < temp.length; i++) {
					E[i+1] = englishMapping.get(temp[i]);
				}
				E[0] = englishMapping.get("NULL");

				englishSentences[numEnglishSentences] = E;
				numEnglishSentences++;

				currentLine = in.readLine();
			}
			in.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(foreign),
			        "UTF8"));
			String currentLine = in.readLine();
			

			while (currentLine != null) {
				String sentence = currentLine;
				String[] temp = sentence.split(" ");
				int[] F = new int[temp.length];
				
				for (int i = 0; i < temp.length; i++) {
					F[i] = foreignMapping.get(temp[i]);
				}				
				
				foreignSentences[numForeignSentences] = F;
				numForeignSentences++;

				currentLine = in.readLine();
			}
			in.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// construct backwards foreign mapping
		backwardsForeignMapping = new String[foreignMapping.size()];
		for(String s : foreignMapping.keySet()) {
			backwardsForeignMapping[foreignMapping.get(s)] = s;
		}
		
		for (int j = 0; j < iterations; j++) {
			// Count(e)
			double[] wordCount = new double[numEnglish];

			// Count(e,f)
			HashMap<Pair, Double> pairCount = new HashMap<Pair, Double>();
			HashSet<Pair> pairs = new HashSet<Pair>();
			
			// The E part of the algorithm
			for (int i = 0; i < englishSentences.length; i ++) {
				int[] E = englishSentences[i];
				int[] F = foreignSentences[i];
				
				for (int f : F)  {
					
					// precompute denominator to save time
					double denominator = 0.0;
					if (j != 0) {
						for (int e: E) {
							denominator += probAlignments.get(e).get(f);
						}
					}
					
					for (int e : E) {
						if (j == 0) {
							double val =wordCount[e];

							double oldCount = val >= 0.0 ? wordCount[e] : 0.0;
							wordCount[e] = oldCount + .1;
							
							Pair p = new Pair(e,f);
							pairs.add(p);
							
							double pCount = pairCount.containsKey(p) ? pairCount.get(p) : 0.0;
							pairCount.put(p, .1 + pCount);
						} else {
							Pair p = new Pair(e,f);
							pairs.add(p);
							
							double val = wordCount[e];
							double oldCount = val >= 0.0 ? val : 0.0;
							double pCount = pairCount.containsKey(p) ? pairCount.get(p) : 0.0;
	
							double prob = probAlignments.get(e).get(f) / denominator;
							
							pairCount.put(p, pCount + prob);
							wordCount[e] = oldCount + prob;
						}
					}
				}
			}
			
			// M part of the algorithm
			for (Pair pair : pairs) {
				Double prob = pairCount.get(pair) / wordCount[pair.e()];
				addToAlignments(pair.e(), pair.f(), prob);
			}
		}
		
		
		// post processing step
		
	}
	
	/**
	 * Updated P(f|e) given a new probability
	 * 
	 * @param english An english word
	 * @param foreign A foreign word
	 * @param prob An updated probability for P(foreign|english)
	 */
	private void addToAlignments(int english, int foreign, double prob) {
		if (!probAlignments.containsKey(english)) {
			probAlignments.put(english, new Hashtable<Integer, Double>());
		}
		if (!probAlignments.get(english).containsKey(foreign)) {
			probAlignments.get(english).put(foreign, 0.0);
		}
		
		probAlignments.get(english).put(foreign, prob);
	}
	
	/**
	 * prints out the probability threshold for all alignments greater than threshold
	 * 
	 * @param threshold the threshold to determine which alignments to print
	 */
	public void printAlignments(double threshold) {
		List<String> tmp = Collections.list(englishMapping.keys());
		Collections.sort(tmp);
		Iterator<String> it = tmp.iterator();
		
		while (it.hasNext()) {
			String elem = it.next();

			Hashtable<Integer, Double> val = probAlignments.get(englishMapping.get(elem));

			String[] foreignWords = new String[val.size()];
			int currentLength = 0;
			
			for (int i : val.keySet()) {
				foreignWords[currentLength] = backwardsForeignMapping[i];
				currentLength++;
			}
			
			// sort the foreignWords
			Arrays.sort(foreignWords);
			
			for (String f: foreignWords) {
				double prob = val.get(foreignMapping.get(f));
				if (prob >= threshold) {
					System.out.println(elem + "\t" + f + "\t" + val.get(foreignMapping.get(f)));
				}
			}
//			while (words.hasNext()) {
//				String word= words.next();
//				double prob = val.get(word);
//				if (prob >= threshold) {
//					System.out.println(elem + "\t" + word + "\t" + prob);
//				}				
//			}
		}
	}
	
	public void printActualAlignments(String english, String foreign) {
		String[] foreignWords = foreign.split(" ");
		String[] englishWords = english.split(" ");
		System.out.println("************************");
		for (String f : foreignWords) {
			double maxProbability = 0.0;
			String currentWord = englishWords[0];
			for (String e : englishWords) {
				double probValue = probAlignments.get(e).containsKey(f) ? probAlignments.get(e).get(f) : 0.0;
				if (probValue > maxProbability) {
					maxProbability = probValue;
					currentWord = e;
				}
			}
			System.out.println(f + " => " + currentWord);
		}
		System.out.println("************************");
	}
	
	public static void main(String[] args) {
		EMAlgorithm x = new EMAlgorithm(args[0],args[1], Integer.parseInt(args[2]));
		x.printAlignments(Double.parseDouble(args[3]));
//		x.printActualAlignments("madam president , on a point of order .", "se–ora presidenta , una cuesti—n relativa al reglamento .");
//		x.printActualAlignments("it will , i hope , be examined in a positive light .", "espero que se examine con un esp’ritu positivo .");
//		x.printActualAlignments("the house", "la casa");
//		x.printActualAlignments("thank you mr poettering", "gracias senor poettering");
//		x.printActualAlignments("i remain worried about the whole operation of the judicial process . ", "sigo preocupado por el funcionamiento en conjunto del proceso judicial . ");
//		x.printActualAlignments("i would ask you to support this common position .", "les pido que apoyen esta posici—n comœn .");
//		x.printActualAlignments("however , more work is needed on this .", "sin embargo , todav’a es necesario elaborar m‡s este enfoque .");
//		x.printActualAlignments("mr president , i welcome the statement from the commissioner . ", "se–or presidente, acojo con benepl‡cito la declaraci—n de la comisaria");
//		x.printActualAlignments("the debate is closed", "el debate queda cerrado");
//		x.printActualAlignments("the consequences do not inspire hope .", "las consecuencias no se hacen esperar .");
//		x.printActualAlignments("the vote will take place tomorrow at 11.30 a.m.", "la votaci—n tendr‡ lugar ma–ana a las 11.30 horas .");
	}
}
