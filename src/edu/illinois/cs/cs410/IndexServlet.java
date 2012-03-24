package edu.illinois.cs.cs410;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

@WebServlet("/IndexServlet")
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final String FEEDS_PATH = "workspace/NewsAggregator/data/feeds.txt";
	static final String INDEX_PATH = "workspace/NewsAggregator/data/index";

	IndexWriter writer;
	Analyzer analyzer;
	Directory dir;
	PrintWriter w = null;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			dir = FSDirectory.open(new File(IndexServlet.INDEX_PATH));
			writer = new IndexWriter(dir, new IndexWriterConfig(
					Version.LUCENE_35, new PorterAnalyzer()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		try {
			dir.close();
			writer.close();
		} catch (Exception e) {
			return;
		}
	}

	private void indexFeed(String url) {

		URL feedSource;
		try {
			feedSource = new URL(url);
		} catch (MalformedURLException e1) {
			if (w != null)
				w.println("Couldn't parse " + url);
			return;
		}

		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = null;

		try {
			feed = input.build(new XmlReader(feedSource));
		} catch (Exception e) {
			if (w != null)
				w.println("Couldn't parse the feed at " + url);
		}

		List l = feed.getEntries();
		Iterator it = l.iterator();

		while (it.hasNext()) {
			SyndEntry entry = (SyndEntry) it.next();
			String title = entry.getTitle();
			String date = DateTools.dateToString(entry.getPublishedDate(),
					DateTools.Resolution.DAY);
			String link = entry.getLink();
			String description = entry.getDescription().getValue();

			Document doc = new Document();
			doc.add(new Field("title", title, Store.YES, Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
			doc.add(new Field("description", description, Store.YES,
					Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
			doc.add(new Field("date", date, Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("link", link, Store.YES, Index.NOT_ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));

			try {
				writer.updateDocument(new Term("link", link), doc);
			} catch (Exception e) {
				if (w != null)
					w.println("Couldn't update index during parsing");
				return;
			}

			w.println("Indexed " + title);
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		w = response.getWriter();

		try {
			FileInputStream fstream = new FileInputStream(FEEDS_PATH);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				indexFeed(strLine);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		writer.commit();
		w.close();
	}
}