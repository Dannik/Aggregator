package edu.illinois.cs.cs410.helpers;

import org.apache.lucene.wordnet.Syns2Index;

public class WordnetIndexer {

	public static void main(String[] args) throws Throwable {
		Syns2Index.main(new String[] {"data/wn_s.pl", "data/wordnetindex"});
	}
}