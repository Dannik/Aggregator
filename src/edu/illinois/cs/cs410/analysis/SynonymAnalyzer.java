package edu.illinois.cs.cs410.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

import java.io.Reader;

public class SynonymAnalyzer extends Analyzer {
	private SynonymEngine engine;

	public SynonymAnalyzer(SynonymEngine engine) {
		this.engine = engine;
	}

	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = new SynonymFilter(new StopFilter(true,
				new LowerCaseFilter(new StandardFilter(new StandardTokenizer(
						Version.LUCENE_30, reader))),
				StopWords.stopWords), engine);
		return result;
	}
}