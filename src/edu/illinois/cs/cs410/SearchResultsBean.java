package edu.illinois.cs.cs410;

import java.util.List;

//List of all pages returned to the browser
public class SearchResultsBean {
	private List<Result> results;

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

}
