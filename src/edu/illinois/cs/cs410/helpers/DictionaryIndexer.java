package edu.illinois.cs.cs410.helpers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class DictionaryIndexer {
	public static void main(String[] args) {
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
}