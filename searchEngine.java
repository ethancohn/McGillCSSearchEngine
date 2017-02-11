package assignment5;

import java.util.*;
import java.io.*;

// This class implements a google-like search engine
public class searchEngine {

	public HashMap<String, LinkedList<String>> wordIndex; // this will contain a
															// set of pairs
															// (String,
															// LinkedList of
															// Strings)
	public directedGraph internet; // this is our internet graph

	// Constructor initializes everything to empty data structures
	// It also sets the location of the internet files
	searchEngine() {
		// Below is the directory that contains all the internet files
		htmlParsing.internetFilesLocation = "internetFiles";
		wordIndex = new HashMap<String, LinkedList<String>>();
		internet = new directedGraph();
	} // end of constructor2015

	// Returns a String description of a searchEngine
	public String toString() {
		return "wordIndex:\n" + wordIndex + "\ninternet:\n" + internet;
	}

	// This does a graph traversal of the internet, starting at the given url.
	// For each new vertex seen, it updates the wordIndex, the internet graph,
	// and the set of visited vertices.

	void traverseInternet(String url) throws Exception {

		internet.setVisited(url, true); // sets visited to true
		internet.addVertex(url); // adds url as vertex

		// parse content of url and add resulting linkedList to wordIndex

		LinkedList<String> content = htmlParsing.getContent(url); 
		Iterator<String> i = content.iterator();
		while (i.hasNext()) {
			String s = i.next();
			// add to wordIndex
			if (wordIndex.containsKey(s)) { // if wordIndex contains word
				if (!wordIndex.get(s).contains(url)) { // if url is not already
														// inserted
					wordIndex.get(s).addLast(url);
				}
			} else { //if wordIndex doesn't contain word
				LinkedList<String> myUrl = new LinkedList<String>();
				myUrl.add(url);
				wordIndex.put(s, myUrl);

			}
			
		}

		// parse links, add resulting linkedList to vertices

		LinkedList<String> links = htmlParsing.getLinks(url);
		// use iterator to go through each link, adding it as a vertex and
		// adding an edge
		i = links.iterator();
		while (i.hasNext()) {
			String s = i.next();
			internet.addVertex(s); // adds links as vertices
			internet.addEdge(url, s); // adds links as edges from url
			if (internet.getVisited(s) == false) {
				traverseInternet(s); // use recursive DFS to traverse all links
			}
		}
		
		
		
		
		/*
		 * Hints 0) This should take about 50-70 lines of code (or less) 1) To
		 * parse the content of the url, call htmlParsing.getContent(url), which
		 * returns a LinkedList of Strings containing all the words at the given
		 * url. Also call htmlParsing.getLinks(url). and assign their results to
		 * a LinkedList of Strings. 2) To iterate over all elements of a
		 * LinkedList, use an Iterator, as described in the text of the
		 * assignment 3) Refer to the description of the LinkedList methods at
		 * http://docs.oracle.com/javase/6/docs/api/ . You will most likely need
		 * to use the methods contains(String s), addLast(String s), iterator()
		 * 4) Refer to the description of the HashMap methods at
		 * http://docs.oracle.com/javase/6/docs/api/ . You will most likely need
		 * to use the methods containsKey(String s), get(String s), put(String
		 * s, LinkedList l).
		 */

	} // end of traverseInternet

	/*
	 * This computes the pageRanks for every vertex in the internet graph. It
	 * will only be called after the internet graph has been constructed using
	 * traverseInternet. Use the iterative procedure described in the text of
	 * the assignment to compute the pageRanks for every vertices in the graph.
	 * 
	 * This method will probably fit in about 30 lines.
	 */
	void computePageRanks() {
		
		//initialize PR(v) to 1 for all vertices
		Iterator<String> i = internet.getVertices().iterator();
		while (i.hasNext()) {
			String s = i.next();
			internet.setPageRank(s, 1);
		}
		//calculate page rank of each page
		for (int j = 0; j < 100; j++) { //repeat until convergence (100 times)
			//loop through all vertices
			i = internet.getVertices().iterator();
			while (i.hasNext()) { 
				String s = i.next();
				double pr = 0.5;
				//loop through all vertices that have links to v, update page rank according to formula
				Iterator<String> l = internet.getEdgesInto(s).iterator();
				while (l.hasNext()) { 
					String m = l.next();
					pr += 0.5*(internet.getPageRank(m)/internet.getOutDegree(m));		
				}
				internet.setPageRank(s, pr); 
			}		
		}
		/*
		Iterator<String> x = internet.getVertices().iterator();
		while (x.hasNext()) {
			String s = x.next();
			System.out.println(s + ": " + internet.getPageRank(s));
		}*/

	} // end of computePageRanks

	/*
	 * Returns the URL of the page with the high page-rank containing the query
	 * word Returns the String "" if no web site contains the query. This method
	 * can only be called after the computePageRanks method has been executed.
	 * Start by obtaining the list of URLs containing the query word. Then
	 * return the URL with the highest pageRank. This method should take about
	 * 25 lines of code.
	 */
	String getBestURL(String query) {
		//obtain the list of urls containing the query word
		String bestURL;
		query = query.toLowerCase(); //set query word to lower case
		LinkedList<String> containsWord = new LinkedList<String>();
		if (wordIndex.containsKey(query)) { 
			//if query word is in wordIndex...
			containsWord = wordIndex.get(query); //add to containsWord all urls that contain query
			Iterator<String> i = containsWord.iterator();
			bestURL = i.next();
			while (i.hasNext()) { //loop through urls. at the end, bestUrl is the url with highest PR
				String s = i.next();
				if (internet.getPageRank(s) > internet.getPageRank(bestURL)) {
					bestURL = s;
				}
			}
		}
		else { //if no url has word, returns String ""
			bestURL = "";
		}
		return bestURL;
	} // end of getBestURL

	public static void main(String args[]) throws Exception {
		searchEngine mySearchEngine = new searchEngine();
		// to debug your program, start with.
		//mySearchEngine.traverseInternet("http://www.cs.mcgill.ca/~blanchem/250/a.html");

		// When your program is working on the small example, move on to
		mySearchEngine.traverseInternet("http://www.cs.mcgill.ca");

		// this is just for debugging purposes. REMOVE THIS BEFORE SUBMITTING
		//System.out.println(mySearchEngine);
		
		mySearchEngine.computePageRanks();

		BufferedReader stndin = new BufferedReader(new InputStreamReader(
				System.in));
		String query;
		do {
			System.out.print("Enter query: ");
			query = stndin.readLine();
			if (query != null && query.length() > 0) {
				System.out.println("Best site = "
						+ mySearchEngine.getBestURL(query));
			}
		} while (query != null && query.length() > 0);
	} // end of main
}