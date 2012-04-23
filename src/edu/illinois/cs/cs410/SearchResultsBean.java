package edu.illinois.cs.cs410;

import java.util.List;

//List of all pages returned to the browser
public class SearchResultsBean {
	private List<Result> results;
	private String[] clusters;
	private Integer[] docToCluster;

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public String[] getClusters() {
		return clusters;
	}

	public void setClusters(String[] clusters) {
		this.clusters = clusters;
	}

	public Integer[] getDocToCluster() {
		return docToCluster;
	}

	public void setDocToCluster(Integer[] docToCluster) {
		this.docToCluster = docToCluster;
	}

}
