package edu.illinois.cs.cs410.helpers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.print.Doc;

import org.apache.lucene.document.Field;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.apache.commons.codec.language.*;

import edu.illinois.cs.cs410.analysis.PorterAnalyzer;

public class Tester {

	public static String getCorrection(String wordToRespell) throws Exception {
		String spellCheckDir = "data/spellcheckindex";
		Directory dir = FSDirectory.open(new File(spellCheckDir));
		SpellChecker spell = new SpellChecker(dir);

		wordToRespell = wordToRespell.toLowerCase();
		if (spell.exist(wordToRespell))
			return wordToRespell;

		LevensteinDistance dist = new LevensteinDistance();
		spell.setStringDistance(dist);

		String[] suggestions = spell.suggestSimilar(wordToRespell, 10);

		if (suggestions.length < 1)
			return "";

		RefinedSoundex soundex = new RefinedSoundex();
		String snd1 = soundex.soundex(wordToRespell);

		String best = suggestions[0];
		double best_score = dist.getDistance(snd1, soundex.soundex(best));

		System.out.println("Suggestions for : " + wordToRespell + "(" + snd1
				+ ")");

		String snd2 = soundex.soundex(suggestions[0]);
		System.out.println("  " + suggestions[0] + "(" + snd2 + ")");
		for (int i = 1; i < suggestions.length; i++) {
			snd2 = soundex.soundex(suggestions[i]);
			System.out.println("  " + suggestions[i] + "(" + snd2 + ")");

			double tmp = dist.getDistance(snd1, snd2);
			if (tmp > best_score) {
				best_score = tmp;
				best = suggestions[i];
			}
		}

		dir.close();
		spell.close();

		return best;
	}

	public static void indexDictionary() {
		try {
			FileInputStream fstream = new FileInputStream(
					"/usr/share/dict/words");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			Directory dir = FSDirectory.open(new File("data/dictionaryindex"));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			IndexWriter writer = new IndexWriter(dir, analyzer, MaxFieldLength.UNLIMITED);

			int i = 0;

			while ((strLine = br.readLine()) != null) {
				Document doc = new Document();
				doc.add(new Field("word", strLine, Store.YES, Index.ANALYZED));
				writer.addDocument(doc);
				++i;
				if (i % 1000 == 0)
					System.out.println(i);
			}
			writer.commit();

			System.out.println("Added " + i + "words!");

			in.close();
			writer.close();
			dir.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		String[] strs = new String[] { "lieks", "monee" };
		for (String str : strs) {
			System.out.println(getCorrection(str));
		}
	}
}