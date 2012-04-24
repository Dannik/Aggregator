package edu.illinois.cs.cs410.helpers;

import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreQuery;

public class RecencyBoostingQuery extends CustomScoreQuery {
	private static final long serialVersionUID = 1L;

	Map<Integer, Integer> daysAgo;
	double multiplier;
	int maxDaysAgo;

	public RecencyBoostingQuery(Query q, Map<Integer, Integer> daysAgo, double multiplier,
			int maxDaysAgo) {
		super(q);
		this.daysAgo = daysAgo;
		this.multiplier = multiplier;
		this.maxDaysAgo = maxDaysAgo;
	}

	public float customScore(int doc, float subQueryScore, float valSrcScore) {
		if (daysAgo.get(doc) < maxDaysAgo) {
			float boost = (float) (multiplier * (maxDaysAgo - daysAgo.get(doc)) / maxDaysAgo);
			return (float) (subQueryScore * (1.0 + boost));
		} else
			return subQueryScore;
	}
};