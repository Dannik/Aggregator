package edu.illinois.cs.cs410.analysis;

import java.io.IOException;

public interface SynonymEngine {
	String[] getSynonyms(String s) throws IOException;
}
