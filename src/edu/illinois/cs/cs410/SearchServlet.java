package edu.illinois.cs.cs410;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String queryStr = request.getParameter("query");
		if (queryStr == null || queryStr.trim().length() < 1) {
			request.setAttribute("query", "");
			request.getRequestDispatcher("/index.jsp").forward(request, response);
			return;
		}
		request.setAttribute("query", queryStr);

		Analyzer analyzer = new PorterAnalyzer();

		Query query = null;
	    try {
			query = new MultiFieldQueryParser(
					Version.LUCENE_35,
					new String[]{"title", "description"},
					analyzer)
			.parse(queryStr);
		} catch (ParseException e) {
			request.getRequestDispatcher("/index.jsp").forward(request, response);
			return;
		}

		SearchResultsBean results = new SearchResultsBean();
		List<Result> list = new ArrayList<Result>();

		Directory dir = FSDirectory.open(new File(IndexServlet.INDEX_PATH));
		IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));

		int hitsPerPage = 10;
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(query, collector);
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;

	    for(int i=0; i < hits.length; ++i) {
	      int docId = hits[i].doc;
	      Document d = searcher.doc(docId);

	      list.add(new Result(
	    		  d.get("title"),
	    		  d.get("date"),
	    		  d.get("link"),
	    		  d.get("description")));
	      }

	    searcher.close();

		results.setResults(list);
		request.setAttribute("results", results);
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

}