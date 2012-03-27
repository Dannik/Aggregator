package edu.illinois.cs.cs410.helpers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jdom.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class Tester {
	public static void main(String[] args) throws Exception {
		String spellCheckDir = "data/spellcheckindex";
		String wordToRespell = "mony";
		Directory dir = FSDirectory.open(new File(spellCheckDir));
		System.out.println(dir);
		SpellChecker spell = new SpellChecker(dir);
		spell.setStringDistance(new LevensteinDistance());
		// spell.setStringDistance(new JaroWinklerDistance());
		String[] suggestions = spell.suggestSimilar(wordToRespell, 5);
		System.out.println(suggestions.length + " suggestions for '"
				+ wordToRespell + "':");
		for (int i = 0; i < suggestions.length; i++)
			System.out.println(" " + suggestions[i]);
	}
}