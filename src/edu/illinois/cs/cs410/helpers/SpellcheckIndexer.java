package edu.illinois.cs.cs410.helpers;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SpellcheckIndexer {
	public static void main(String[] args) throws Exception {
		String spellCheckDir = "data/spellcheckindex";
		String indexDir = "data/index";
		String indexDir2 = "data/dictionaryindex";
		String indexField = "contents_pure";
		String indexField2 = "word";

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
		dir2.close();


		dir2 = FSDirectory.open(new File(indexDir2));
		r = IndexReader.open(dir2);
		try {
			spell.indexDictionary(new LuceneDictionary(r, indexField2));
		} finally {
			r.close();
		}
		dir2.close();

		long endTime = System.currentTimeMillis();
		System.out.println("  took " + (endTime - startTime) + " milliseconds");
	}
}