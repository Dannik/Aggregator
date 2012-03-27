package edu.illinois.cs.cs410.analysis;

import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;

public class AnalyzerUtils {
	public static void displayTokens(Analyzer analyzer, String text)
			throws IOException {
		displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
	}

	public static void displayTokens(TokenStream stream) throws IOException {

		TermAttribute term = stream.addAttribute(TermAttribute.class);
		while (stream.incrementToken()) {
			System.out.print("[" + term.term() + "] ");
		}
	}

	public static String getTokens(Analyzer analyzer, String text)
			throws IOException {
		if (text == null || text.length() < 1)
			return "";
		StringBuffer str = new StringBuffer();
		getTokens(analyzer.tokenStream("contents", new StringReader(text)), str);
		return str.toString();
	}

	public static void getTokens(TokenStream stream, StringBuffer str) throws IOException {

		TermAttribute term = stream.addAttribute(TermAttribute.class);
		while (stream.incrementToken()) {
			str.append(term.term() + " ");
		}
		if (str.length() > 0)
			str.deleteCharAt(str.length()-1);
	}

	public static int getPositionIncrement(AttributeSource source) {
		PositionIncrementAttribute attr = source
				.addAttribute(PositionIncrementAttribute.class);
		return attr.getPositionIncrement();
	}

	public static String getTerm(AttributeSource source) {
		TermAttribute attr = source.addAttribute(TermAttribute.class);
		return attr.term();
	}

	public static String getType(AttributeSource source) {
		TypeAttribute attr = source.addAttribute(TypeAttribute.class);
		return attr.type();
	}

	public static void setPositionIncrement(AttributeSource source, int posIncr) {
		PositionIncrementAttribute attr = source
				.addAttribute(PositionIncrementAttribute.class);
		attr.setPositionIncrement(posIncr);
	}

	public static void setTerm(AttributeSource source, String term) {
		TermAttribute attr = source.addAttribute(TermAttribute.class);
		attr.setTermBuffer(term);
	}

	public static void setType(AttributeSource source, String type) {
		TypeAttribute attr = source.addAttribute(TypeAttribute.class);
		attr.setType(type);
	}

	public static void displayTokensWithPositions(Analyzer analyzer, String text)
			throws IOException {

		TokenStream stream = analyzer.tokenStream("contents", new StringReader(
				text));
		TermAttribute term = stream.addAttribute(TermAttribute.class);
		PositionIncrementAttribute posIncr = stream
				.addAttribute(PositionIncrementAttribute.class);

		int position = 0;
		while (stream.incrementToken()) {
			int increment = posIncr.getPositionIncrement();
			if (increment > 0) {
				position = position + increment;
				System.out.println();
				System.out.print(position + ": ");
			}

			System.out.print("[" + term.term() + "] ");
		}
		System.out.println();
	}

	public static void displayTokensWithFullDetails(Analyzer analyzer,
			String text) throws IOException {

		TokenStream stream = analyzer.tokenStream("contents", new StringReader(
				text));

		TermAttribute term = stream.addAttribute(TermAttribute.class);
		PositionIncrementAttribute posIncr = stream
				.addAttribute(PositionIncrementAttribute.class);
		OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);
		TypeAttribute type = stream.addAttribute(TypeAttribute.class);

		int position = 0;
		while (stream.incrementToken()) {

			int increment = posIncr.getPositionIncrement();
			if (increment > 0) {
				position = position + increment;
				System.out.println();
				System.out.print(position + ": ");
			}

			System.out.print("[" + term.term() + ":" + offset.startOffset()
					+ "->" + offset.endOffset() + ":" + type.type() + "] ");
		}
		System.out.println();
	}

	public static void displayPositionIncrements(Analyzer analyzer, String text)
			throws IOException {
		TokenStream stream = analyzer.tokenStream("contents", new StringReader(
				text));
		PositionIncrementAttribute posIncr = stream
				.addAttribute(PositionIncrementAttribute.class);
		while (stream.incrementToken()) {
			System.out.println("posIncr=" + posIncr.getPositionIncrement());
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("SimpleAnalyzer");
		displayTokensWithFullDetails(new SimpleAnalyzer(),
				"The quick brown fox....");

		System.out.println("\n----");
		System.out.println("StandardAnalyzer");
		displayTokensWithFullDetails(new StandardAnalyzer(Version.LUCENE_30),
				"I'll email you at xyz@example.com");
	}
}