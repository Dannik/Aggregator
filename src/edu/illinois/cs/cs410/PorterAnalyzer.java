package edu.illinois.cs.cs410;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class PorterAnalyzer extends Analyzer {
    private Set<?> stopSet;

    public PorterAnalyzer() {
    	stopSet = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }

    public PorterAnalyzer(String[] stopWords) {
        stopSet = StopFilter.makeStopSet(Version.LUCENE_35, stopWords);
    }


    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new StandardTokenizer(Version.LUCENE_35, reader);
        result = new StandardFilter(Version.LUCENE_35, result);
        result = new LowerCaseFilter(Version.LUCENE_35, result);
        result = new StopFilter(Version.LUCENE_35, result, stopSet, true);
        result = new PorterStemFilter(result);
        result = new StopFilter(Version.LUCENE_35, result, stopSet, true);
        return result;
    }

}