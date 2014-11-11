import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Scanner;

/**
 * 
 * @author Aloke Desai
 * 11/7/14
 *
 *EMAlgorithm that computes word alignments using IBM model 1
 */
public class EMAlgorithm {
	// the hashtable that we stored P(f|e) in 
	private Hashtable<String, Hashtable<String, Double>> probAlignments = new Hashtable<String, Hashtable<String, Double>>();
	
	// the english sentences post processing
	private ArrayList<ArrayList<String>> englishSentences = new ArrayList<ArrayList<String>>();
	
	//the foreign corpus post processing
	private ArrayList<ArrayList<String>> foreignSentences = new ArrayList<ArrayList<String>>();
	
	/**
	 * Runs the EMAlgorithm a certain number of time 
	 * 
	 * @param english the filename of the english sentences
	 * @param foreign the filename of the foreign sentence
	 * @param iterations the number of iterations the EM algorithm should run
	 */
	public EMAlgorithm(String english, String foreign, int iterations) {
		// file input, put all english sentences and foreign sentences into associated arraylist

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(english),
			        "UTF8"));
			String currentLine = in.readLine();
			
			while (currentLine != null) {
				String sentence = currentLine;
				ArrayList<String> E = new ArrayList<String>(Arrays.asList(sentence.split(" ")));
				E.add(0, "NULL");
				englishSentences.add(E);
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
				ArrayList<String> F = new ArrayList<String>(Arrays.asList(sentence.split(" ")));
				foreignSentences.add(F);
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

		for (int j = 0; j < iterations; j++) {
			// Count(e)
			Hashtable<String, Double> wordCount = new Hashtable<String, Double>();
			// Count(e,f)
			HashMap<Pair, Double> pairCount = new HashMap<Pair, Double>();
			// all possible pairs of words
			HashSet<Pair> pairs = new HashSet<Pair>();
			
			// The E part of the algorithm
			for (int i = 0; i < englishSentences.size(); i ++) {
				ArrayList<String> E = englishSentences.get(i);
				ArrayList<String> F = foreignSentences.get(i);

				for (String f : F)  {
					
					// precompute denominator to save time
					double denominator = 0.0;
					if (j != 0) {
						for (String e: E) {
							denominator += probAlignments.get(e).get(f);
						}
					}
					
					for (String e : E) {
						if (j == 0) {
							double oldCount = wordCount.containsKey(e) ? wordCount.get(e) : 0.0;
							wordCount.put(e, .1 + oldCount);
							
							Pair p = new Pair(e,f);
							pairs.add(p);
							
							double pCount = pairCount.containsKey(p) ? pairCount.get(p) : 0.0;
							pairCount.put(p, .1 + pCount);
						} else {
							Pair p = new Pair(e,f);
							pairs.add(p);
							
							double oldCount = wordCount.containsKey(e) ? wordCount.get(e) : 0.0;
							double pCount = pairCount.containsKey(p) ? pairCount.get(p) : 0.0;
	
							double prob = probAlignments.get(e).get(f) / denominator;
							
							pairCount.put(p, pCount + prob);
							wordCount.put(e, oldCount + prob);
						}
					}
				}
			}
			
			// M part of the algorithm
			for (Pair pair : pairs) {
				Double prob = pairCount.get(pair) / wordCount.get(pair.e());
				addToAlignments(pair.e(), pair.f(), prob);
			}
		}
	}
	
	/**
	 * Updated P(f|e) given a new probability
	 * 
	 * @param english An english word
	 * @param foreign A foreign word
	 * @param prob An updated probability for P(foreign|english)
	 */
	private void addToAlignments(String english, String foreign, Double prob) {
		if (!probAlignments.containsKey(english)) {
			probAlignments.put(english, new Hashtable<String, Double>());
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
		List<String> tmp = Collections.list(probAlignments.keys());
		Collections.sort(tmp);
		Iterator<String> it = tmp.iterator();
		
		while (it.hasNext()) {
			String elem = it.next();
			Hashtable<String, Double> val = probAlignments.get(elem);
			
			List<String> inner = Collections.list(val.keys());
			Collections.sort(inner);
			Iterator<String> words = inner.iterator();
			
			while (words.hasNext()) {
				String word= words.next();
				double prob = val.get(word);
				if (prob >= threshold) {
					System.out.println(elem + "\t" + word + "\t" + prob);
				}				
			}
		}
	}
	
	public void printAlignments(String foreign, String english) {
		String[] foreignWords = foreign.split(" ");
		String[] englishWords = english.split(" ");

		for (String f : foreignWords) {
			double maxProbability = 0.0;
			String currentWord = englishWords[0];
			for (String e : englishWords) {
				double probValue = probAlignments.get(e).containsKey(f) ? probAlignments.get(e).get(f) : 0.0;
				if (probValue >= maxProbability) {
					maxProbability = probValue;
					currentWord = e;
				}
			}
			System.out.println(f + " => " + currentWord);
		}
	}
	
	public static void main(String[] args) {
		EMAlgorithm x = new EMAlgorithm("enhead","eshead", 10);
		x.printAlignments("madam president , on a point of order", "se–ora presidenta , una cuesti—n relativa al reglamento");
//		x.printAlignments(0.0);
	}
}
