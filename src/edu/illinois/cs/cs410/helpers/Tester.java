package edu.illinois.cs.cs410.helpers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similar.MoreLikeThis;

import org.apache.lucene.document.Field;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.dirichlet.UncommonDistributions;
import org.apache.mahout.clustering.kmeans.KMeansClusterer;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.clustering.lda.LDADriver;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.DenseVector;

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

	public static String[] moreLikeThis(int docID) throws IOException {
		String indexDir = "data/index";
		FSDirectory directory = FSDirectory.open(new File(indexDir));
		IndexReader reader = IndexReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		int numDocs = reader.maxDoc();
		MoreLikeThis mlt = new MoreLikeThis(reader);
		mlt.setFieldNames(new String[] { "title" });
		mlt.setMinTermFreq(1);
		mlt.setMinDocFreq(1);

		Document doc = reader.document(docID);

		Query query = mlt.like(docID);
		TopDocs similarDocs = searcher.search(query, 10);

		String[] docs = new String[similarDocs.scoreDocs.length];
		for (int i = 0; i < similarDocs.scoreDocs.length; i++) {
			doc = reader.document(similarDocs.scoreDocs[i].doc);
			docs[i] = doc.getField("id").stringValue();
		}

		searcher.close();
		reader.close();
		directory.close();

		return docs;
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
			IndexWriter writer = new IndexWriter(dir, analyzer,
					MaxFieldLength.UNLIMITED);

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

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);

		LDADriver driver = new LDADriver();
		Path input = new Path("/home/danil/mahout/lucene-index/lucene-index.vec");
		Path output = new Path("/home/danil/mahout/lucene-lda-sparse2");

		driver.run(conf, input, output, 5, 7000, 10, 20, false);
	}
}