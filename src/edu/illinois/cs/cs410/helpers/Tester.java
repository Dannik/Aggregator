package edu.illinois.cs.cs410.helpers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.cluster.LatentDirichletAllocation.GibbsSample;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.spell.EditDistance;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Arrays;
import com.aliasi.util.Distance;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import edu.illinois.cs.cs410.LDAClusterer;
import edu.illinois.cs.cs410.Result;
import edu.illinois.cs.cs410.analysis.StopWords;

public class Tester {

	public static void main(String[] args) throws IOException {
		Directory dir = FSDirectory.open(new File("data/index"));
		IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));

		int hitsPerPage = 50;
		TopScoreDocCollector collector = TopScoreDocCollector.create(
				hitsPerPage, true);
		searcher.search(new TermQuery(new Term("title", "obama")), collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		CharSequence[] texts = new String[hits.length];

		Document[] docs = new Document[hits.length];

		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			docs[i] = d;
			texts[i] = d.get("title");
		}

		System.out.println("\nTHE NEW IMPLEMENTATION");
		LDAClusterer.LDAClusterData data = LDAClusterer.LDA(docs, 2, "title", 5);
		for (int i = 0; i < data.clusterWords.length; i++) {
			System.out.printf("TOPIC %d: %s\n", i, data.clusterWords[i]);
		}
		for (int i = 0; i < data.clusterIDs.length; i++) {
			System.out.printf("DOC %d: topic %d (%f)\n", i, data.clusterIDs[i], data.clusterPercents[i]);
		}
		System.out.println("END OF NEW IMPLEMENTATION\n");

		TokenizerFactory factory = new AnalyzerTokenizerFactory(
				new StandardAnalyzer(Version.LUCENE_30, StopWords.stopWords),
				"title");

		SymbolTable symTab = new MapSymbolTable();
		int minCount = 1;
		int[][] docWords = LatentDirichletAllocation.tokenizeDocuments(texts,
				factory, symTab, minCount);

		LdaReportingHandler handler = new LdaReportingHandler(symTab);

		int numTopics = 2;
		double docTopicPrior = 0.1;
		double topicWordPrior = 0.01;
		int burninEphochs = 0;
		int sampleLag = 1;
		int numSamples = docWords.length;

		LatentDirichletAllocation.GibbsSample sample = LatentDirichletAllocation
				.gibbsSampler(docWords, (short) numTopics, docTopicPrior,
						topicWordPrior, burninEphochs, sampleLag, numSamples,
						new Random(42), handler);

		handler.fullReport(sample, 5, 2, true);

		searcher.close();
		dir.close();
	}
}

class LdaReportingHandler implements
		ObjectHandler<LatentDirichletAllocation.GibbsSample> {

	private final SymbolTable mSymbolTable;
	private final long mStartTime;

	LdaReportingHandler(SymbolTable symbolTable) {
		mSymbolTable = symbolTable;
		mStartTime = System.currentTimeMillis();
	}

	public void handle(LatentDirichletAllocation.GibbsSample sample) {

		System.out.printf("Epoch=%3d   elapsed time=%s\n", sample.epoch(),
				Strings.msToString(System.currentTimeMillis() - mStartTime));

		if ((sample.epoch() % 10) == 0) {
			double corpusLog2Prob = sample.corpusLog2Probability();
			System.out.println("      log2 p(corpus|phi,theta)="
					+ corpusLog2Prob + "     token cross-entropy rate="
					+ (-corpusLog2Prob / sample.numTokens()));
		}
	}

	void fullReport(LatentDirichletAllocation.GibbsSample sample,
			int maxWordsPerTopic, int maxTopicsPerDoc, boolean reportTokens) {

		System.out.println("\nFull Report");

		int numTopics = sample.numTopics();
		int numWords = sample.numWords();
		int numDocs = sample.numDocuments();
		int numTokens = sample.numTokens();

		System.out.println("epoch=" + sample.epoch());
		System.out.println("numDocs=" + numDocs);
		System.out.println("numTokens=" + numTokens);
		System.out.println("numWords=" + numWords);
		System.out.println("numTopics=" + numTopics);

		for (int topic = 0; topic < numTopics; ++topic) {
			int topicCount = sample.topicCount(topic);
			ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
			for (int word = 0; word < numWords; ++word)
				counter.set(Integer.valueOf(word),
						sample.topicWordCount(topic, word));
			List<Integer> topWords = counter.keysOrderedByCountList();
			System.out.println("\nTOPIC " + topic + "  (total count="
					+ topicCount + ")");
			System.out
					.println("SYMBOL             WORD    COUNT   PROB          Z");
			System.out
					.println("--------------------------------------------------");
			for (int rank = 0; rank < maxWordsPerTopic
					&& rank < topWords.size(); ++rank) {
				int wordId = topWords.get(rank);
				String word = mSymbolTable.idToSymbol(wordId);
				int wordCount = sample.wordCount(wordId);
				int topicWordCount = sample.topicWordCount(topic, wordId);
				double topicWordProb = sample.topicWordProb(topic, wordId);
				double z = binomialZ(topicWordCount, topicCount, wordCount,
						numTokens);

				System.out.printf("%6d  %15s  %7d   %4.3f  %8.1f\n", wordId,
						word, topicWordCount, topicWordProb, z);
			}
		}

		for (int doc = 0; doc < numDocs; ++doc) {
			int docCount = 0;
			for (int topic = 0; topic < numTopics; ++topic)
				docCount += sample.documentTopicCount(doc, topic);
			ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
			for (int topic = 0; topic < numTopics; ++topic)
				counter.set(Integer.valueOf(topic),
						sample.documentTopicCount(doc, topic));
			List<Integer> topTopics = counter.keysOrderedByCountList();
			System.out.println("\nDOC " + doc);
			System.out.println("TOPIC    COUNT    PROB");
			System.out.println("----------------------");
			for (int rank = 0; rank < topTopics.size()
					&& rank < maxTopicsPerDoc; ++rank) {
				int topic = topTopics.get(rank);
				int docTopicCount = sample.documentTopicCount(doc, topic);
				double docTopicPrior = sample.documentTopicPrior();
				double docTopicProb = (sample.documentTopicCount(doc, topic) + docTopicPrior)
						/ (docCount + numTopics * docTopicPrior);
				System.out.printf("%5d  %7d   %4.3f\n", topic, docTopicCount,
						docTopicProb);
			}
			System.out.println();
			if (!reportTokens)
				continue;
			int numDocTokens = sample.documentLength(doc);
			for (int tok = 0; tok < numDocTokens; ++tok) {
				int symbol = sample.word(doc, tok);
				short topic = sample.topicSample(doc, tok);
				String word = mSymbolTable.idToSymbol(symbol);
				System.out.print(word + "(" + topic + ") ");
			}
			System.out.println();
		}
	}

	static double binomialZ(double wordCountInDoc, double wordsInDoc,
			double wordCountinCorpus, double wordsInCorpus) {
		double pCorpus = wordCountinCorpus / wordsInCorpus;
		double var = wordsInCorpus * pCorpus * (1 - pCorpus);
		double dev = Math.sqrt(var);
		double expected = wordsInDoc * pCorpus;
		double z = (wordCountInDoc - expected) / dev;
		return z;
	}

}