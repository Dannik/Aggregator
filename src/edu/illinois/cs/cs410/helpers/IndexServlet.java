package edu.illinois.cs.cs410.helpers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import edu.illinois.cs.cs410.analysis.PorterAnalyzer;

@WebServlet("/IndexServlet")
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String ROOT_PATH = "workspace/Aggregator/";
	public static final String DATA_PATH = ROOT_PATH + "data/";
	public static final String INDEX_PATH = DATA_PATH + "index/";
	public static final String FEEDS_PATH = DATA_PATH + "feeds.txt";
	public static final String SYNONYM_PATH = DATA_PATH + "wordnetindex/";
	public static final String SPELLCHECK_PATH = DATA_PATH + "spellcheckindex/";

	IndexWriter writer;
	Analyzer analyzer;
	Directory dir;
	PrintWriter w = null;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			dir = FSDirectory.open(new File(IndexServlet.INDEX_PATH));
			writer = new IndexWriter(dir, new PorterAnalyzer(),
					MaxFieldLength.UNLIMITED);
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

		List<?> l = feed.getEntries();
		Iterator<?> it = l.iterator();

		String aRegex = "[‘’]";
		String qRegex = "[“”]";

		while (it.hasNext()) {
			SyndEntry entry = (SyndEntry) it.next();
			String title = entry.getTitle().replaceAll(aRegex, "'")
					.replaceAll(qRegex, "\"").replaceAll("—", "-");
			String date = DateTools.dateToString(entry.getPublishedDate(),
					DateTools.Resolution.DAY);
			String link = entry.getLink();
			String description = entry.getDescription().getValue()
					.replaceAll(aRegex, "'").replaceAll(qRegex, "\"")
					.replaceAll("—", "-");
			String image = "";

			org.jsoup.nodes.Document jDoc = null;
			jDoc = org.jsoup.Jsoup.parse(description);
			org.jsoup.nodes.Element ele = jDoc.select("img").first();

			if (ele != null) {
				image = ele.attr("src");
			}

			description = jDoc.text();

			try {
				jDoc = org.jsoup.Jsoup.connect(link).get();
			} catch (IOException e) {
				continue;
			}

			ele = jDoc.select("div.yom-art-content").first();
			if (ele == null)
				continue;
			String contents = ele.text().replaceAll(aRegex, "'")
					.replaceAll(qRegex, "\"").replaceAll("—", "-");

			Document doc = new Document();
			doc.add(new Field("title", title, Store.YES, Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
			doc.add(new Field("description", description, Store.YES,
					Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
			doc.add(new Field("contents", contents, Store.YES, Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
			doc.add(new Field("contents_pure", contents, Store.YES, Index.NOT_ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
			doc.add(new Field("date", date, Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("link", link, Store.YES, Index.NOT_ANALYZED));
			doc.add(new Field("image", image, Store.YES, Index.NOT_ANALYZED));

			try {
				writer.updateDocument(new Term("link", link), doc);
			} catch (Exception e) {
				if (w != null)
					w.println("Couldn't update index during parsing");
				return;
			}

			w.println("Indexed " + title);
			System.out.println("Title: " + title);
			System.out.println("Description: " + description);
			System.out.println("Image: " + image);
			System.out.println();
		}
		System.out.println("DONE!");
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