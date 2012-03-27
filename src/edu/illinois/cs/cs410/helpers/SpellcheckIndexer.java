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

public class SpellcheckIndexer {
	public static void main(String[] args) throws Exception {
		String spellCheckDir = "data/spellcheckindex";
		String indexDir = "data/index";
		String indexField = "contents_pure";

		System.out.println("Now build SpellChecker index...");
		Directory dir = FSDirectory.open(new File(spellCheckDir));
		SpellChecker spell = new SpellChecker(dir);
		long startTime = System.currentTimeMillis();

		Directory dir2 = FSDirectory.open(new File(indexDir));
		IndexReader r = IndexReader.open(dir2);
		try {
			spell.indexDictionary(new LuceneDictionary(r, indexField));
		} finally {
			r.close();
		}
		dir.close();
		dir2.close();
		long endTime = System.currentTimeMillis();
		System.out.println("  took " + (endTime - startTime) + " milliseconds");
	}
}