package edu.illinois.cs.cs410.analysis;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class PorterAnalyzer extends Analyzer {
	private Set<?> stopSet;

	public PorterAnalyzer() {
		stopSet = StopWords.stopWords;
	}

	public PorterAnalyzer(String[] stopWords) {
		//stopSet = StopFilter.makeStopSet(stopWords);
		stopSet = StopWords.stopWords;
	}

	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = new StandardTokenizer(Version.LUCENE_30, reader);
		result = new StandardFilter(result);
		result = new LowerCaseFilter(result);
		result = new StopFilter(false, result, stopSet, true);
		result = new PorterStemFilter(result);
		result = new StopFilter(false, result, stopSet, true);
		return result;
	}
}