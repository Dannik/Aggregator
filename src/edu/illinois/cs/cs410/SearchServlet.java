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

import org.apache.lucene.analysis.Analyzer;
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

		String result = "No highlighs available";
		try {
			result = highlighter.getBestFragments(tokens, text, 5, "...");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private String getCorrection(String str) {
		return str;
	}

	private boolean spelledCorrectly(String str) {
		return true;
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
				list.add(new Result(
						d.get("title"),
						new SimpleDateFormat("MMMM d, yyyy").format(DateTools.stringToDate(d.get("date"))),
						d.get("description"),
						d.get("image"),
						d.get("link"),
						getHighlightedText(query, "contents", d.get("contents"))));
			} catch (java.text.ParseException e) {
				continue;
			}
		}

		searcher.close();

		results.setResults(list);
		request.setAttribute("results", results);
		request.setAttribute("resultsNum", hits.length);
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

}