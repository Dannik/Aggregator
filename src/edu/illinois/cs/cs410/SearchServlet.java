package edu.illinois.cs.cs410;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.illinois.cs.cs410.analysis.AnalyzerUtils;
import edu.illinois.cs.cs410.analysis.PorterAnalyzer;
import edu.illinois.cs.cs410.analysis.PorterSynonymAnalyzer;
import edu.illinois.cs.cs410.analysis.SynonymAnalyzer;
import edu.illinois.cs.cs410.analysis.WordNetSynonymEngine;
import edu.illinois.cs.cs410.helpers.IndexServlet;

@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String getHighlightedText(Query query, String field, String text) {
		SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(
				"<span class=\"highlight\">", "</span>");

		TokenStream tokens = new PorterAnalyzer().tokenStream(field,
				new StringReader(text));

		QueryScorer scorer = new QueryScorer(query, field);

		Highlighter highlighter = new Highlighter(formatter, scorer);
		highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, 100));

		String result = "No highlights available";

		try {
			result = highlighter.getBestFragments(tokens, text, 5, "...");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private String getCorrection(String wordToRespell) throws Exception {
		String spellCheckDir = IndexServlet.SPELLCHECK_PATH;
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

//		System.out.println("Suggestions for : " + wordToRespell + "(" + snd1 + ")");

		String snd2 = soundex.soundex(suggestions[0]);
//		System.out.println("  " + suggestions[0] + "(" + snd2 + ")");
		for (int i = 1; i < suggestions.length; i++) {
			snd2 = soundex.soundex(suggestions[i]);
//			System.out.println("  " + suggestions[i] + "(" + snd2 + ")");

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

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String queryStr = request.getParameter("query");
		boolean isSynonym = request.getParameter("isSynonym") != null;

		if (queryStr == null || queryStr.trim().length() < 1) {
			request.setAttribute("query", "");
			request.getRequestDispatcher("/index.jsp").forward(request,
					response);
			return;
		}

		String[] words = queryStr.split(" ");
		StringBuilder bld = new StringBuilder();
		boolean misspelled = false;
		String newQuery = "";

		try {
			for (String word : words) {
				String tmp = getCorrection(word);

				if (!StopAnalyzer.ENGLISH_STOP_WORDS_SET.contains(word)
						&& !word.toLowerCase().equals(tmp)) {
					misspelled = true;
					bld.append(tmp + " ");
				} else {
					bld.append(word + " ");
				}
			}
			bld.deleteCharAt(bld.length()-1);
			newQuery = bld.toString();
		} catch (Exception e) {
			misspelled = false;
			e.printStackTrace();
		}

		Analyzer analyzer;

		if (isSynonym) {
			queryStr = AnalyzerUtils.getTokens(new SynonymAnalyzer(
					new WordNetSynonymEngine(
							new File(IndexServlet.SYNONYM_PATH))), queryStr);
			analyzer = new PorterSynonymAnalyzer();
		} else {
			analyzer = new PorterAnalyzer();
		}

		request.setAttribute("query", queryStr);
		request.setAttribute("misspelled", misspelled);
		request.setAttribute("newQuery", newQuery);

		Query query = null;
		try {
			query = new MultiFieldQueryParser(Version.LUCENE_30, new String[] {
					"title", "contents" }, analyzer).parse(queryStr);
		} catch (ParseException e) {
			request.getRequestDispatcher("/index.jsp").forward(request,
					response);
			return;
		}

		SearchResultsBean results = new SearchResultsBean();
		List<Result> list = new ArrayList<Result>();

		Directory dir = FSDirectory.open(new File(IndexServlet.INDEX_PATH));
		IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));

		int hitsPerPage = 50;
		TopScoreDocCollector collector = TopScoreDocCollector.create(
				hitsPerPage, true);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);

			try {
				list.add(new Result(d.get("title"), new SimpleDateFormat(
						"MMMM d, yyyy").format(DateTools.stringToDate(d
						.get("date"))), d.get("description"), d.get("image"), d
						.get("link"), getHighlightedText(query, "contents",
						d.get("contents"))));
			} catch (java.text.ParseException e) {
				continue;
			}
		}

		searcher.close();
		dir.close();

		results.setResults(list);
		request.setAttribute("results", results);
		request.setAttribute("resultsNum", hits.length);
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

}