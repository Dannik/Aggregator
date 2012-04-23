package edu.illinois.cs.cs410;

import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.Version;

import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.cluster.LatentDirichletAllocation.GibbsSample;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToCounterMap;

import edu.illinois.cs.cs410.analysis.StopWords;
import edu.illinois.cs.cs410.helpers.AnalyzerTokenizerFactory;

public class LDAClusterer {
	public static class LDAClusterData {
		public String[] clusterWords;
		public Integer[] clusterIDs;
		public Double[] clusterPercents;
	}

	public static LDAClusterData LDA(Document[] docs, int numTopics,
			String field, int numTopWords) {
		int n = docs.length;

		CharSequence[] texts = new String[n];

		for (int i = 0; i < n; i++)
			texts[i] = docs[i].get(field);

		TokenizerFactory factory = new AnalyzerTokenizerFactory(
				new StandardAnalyzer(Version.LUCENE_30, StopWords.stopWords),
				field);

		SymbolTable symTab = new MapSymbolTable();
		int minCount = 1;
		int[][] docWords = LatentDirichletAllocation.tokenizeDocuments(texts,
				factory, symTab, minCount);

		ObjectHandler<LatentDirichletAllocation.GibbsSample> handler = new ObjectHandler<LatentDirichletAllocation.GibbsSample>() {
			public void handle(GibbsSample arg0) {
			}
		};

		double docTopicPrior = 0.1;
		double topicWordPrior = 0.01;
		int burninEphochs = 0;
		int sampleLag = 1;
		int numSamples = n;

		LatentDirichletAllocation.GibbsSample sample = LatentDirichletAllocation
				.gibbsSampler(docWords, (short) numTopics, docTopicPrior,
						topicWordPrior, burninEphochs, sampleLag, numSamples,
						new Random(42), handler);

		/* ANALYZE HERE */

		numTopics = sample.numTopics();
		int numWords = sample.numWords();
		int numDocs = sample.numDocuments();

		LDAClusterData result = new LDAClusterData();
		result.clusterWords = new String[numTopics];
		result.clusterIDs = new Integer[numDocs];
		result.clusterPercents = new Double[numDocs];

		Integer[] docMaxClusterCount = new Integer[numDocs];
		Integer[] docTotalClusterCount = new Integer[numDocs];

		for (int topic = 0; topic < numTopics; topic++) {
			ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
			for (int word = 0; word < numWords; word++) {
				counter.set(Integer.valueOf(word),
						sample.topicWordCount(topic, word));
			}
			List<Integer> topWords = counter.keysOrderedByCountList();
			StringBuilder bld = new StringBuilder();
			for (int i = 0; i < numTopWords; i++)
				bld.append(symTab.idToSymbol(topWords.get(i)) + " ");
			bld.delete(bld.length() - 1, bld.length());

			result.clusterWords[topic] = bld.toString();

			for (int i = 0; i < numDocs; i++) {
				int count = sample.documentTopicCount(i, topic);

				if (topic == 0)
					docTotalClusterCount[i] = count;
				else
					docTotalClusterCount[i] += count;

				if (topic == 0 || docMaxClusterCount[i] < count) {
					result.clusterIDs[i] = topic;
					docMaxClusterCount[i] = count;
				}
			}
		}

		for (int i = 0; i < numDocs; i++) {
			result.clusterPercents[i] = (docMaxClusterCount[i] + docTopicPrior)
					/ (docTotalClusterCount[i] + numTopics * docTopicPrior);
		}

		return result;
	}
}