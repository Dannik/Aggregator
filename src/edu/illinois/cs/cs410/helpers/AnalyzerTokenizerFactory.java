package edu.illinois.cs.cs410.helpers;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.Serializable;

/*x AnalyzerTokenizerFactory.1 */
public class AnalyzerTokenizerFactory implements TokenizerFactory, Serializable {

	private final Analyzer mAnalyzer;
	private final String mFieldName;

	public AnalyzerTokenizerFactory(Analyzer analyzer, String fieldName) {
		mAnalyzer = analyzer;
		mFieldName = fieldName;
	}

	/* x */

	public Analyzer analyzer() {
		return mAnalyzer;
	}

	/* x AnalyzerTokenizerFactory.2 */
	public Tokenizer tokenizer(char[] cs, int start, int len) {
		Reader reader = new CharArrayReader(cs, start, len);
		TokenStream tokenStream = mAnalyzer.tokenStream(mFieldName, reader);
		return new TokenStreamTokenizer(tokenStream);
	}

	/* x */

	Object writeReplace() {
		return new Serializer(this);
	}

	/* x AnalyzerTokenizerFactory.3 */
	static class TokenStreamTokenizer extends Tokenizer {

		private final TokenStream mTokenStream;
		private final TermAttribute mTermAttribute;
		private final OffsetAttribute mOffsetAttribute;

		private int mLastTokenStartPosition = -1;
		private int mLastTokenEndPosition = -1;

		public TokenStreamTokenizer(TokenStream tokenStream) {
			mTokenStream = tokenStream;
			mTermAttribute = mTokenStream.addAttribute(TermAttribute.class);
			mOffsetAttribute = mTokenStream.addAttribute(OffsetAttribute.class);
		}

		/* x */

		/* x AnalyzerTokenizerFactory.4 */
		@Override
		public String nextToken() {
			try {
				if (mTokenStream.incrementToken()) {
					mLastTokenStartPosition = mOffsetAttribute.startOffset();
					mLastTokenEndPosition = mOffsetAttribute.endOffset();
					return mTermAttribute.term();
				} else {
					closeQuietly();
					return null;
				}
			} catch (IOException e) {
				closeQuietly();
				return null;
			}
		}

		/* x */

		/* x AnalyzerTokenizerFactory.5 */
		@Override
		public int lastTokenStartPosition() {
			return mLastTokenStartPosition;
		}

		/* x */

		@Override
		public int lastTokenEndPosition() {
			return mLastTokenEndPosition;
		}

		/* x AnalyzerTokenizerFactory.6 */
		public void closeQuietly() {
			try {
				mTokenStream.end();
			} catch (IOException e) {
				/* ignore */
			} finally {
				Streams.closeQuietly(mTokenStream);
			}
		}
		/* x */

	}

	static class Serializer extends AbstractExternalizable {
		final AnalyzerTokenizerFactory mFactory;

		public Serializer() {
			this(null);
		}

		Serializer(AnalyzerTokenizerFactory factory) {
			mFactory = factory;
		}

		@Override
		public Object read(ObjectInput in) throws IOException,
				ClassNotFoundException {
			Analyzer analyzer = (Analyzer) in.readObject();
			String fieldName = in.readUTF();
			return new AnalyzerTokenizerFactory(analyzer, fieldName);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(mFactory.mAnalyzer);
			out.writeUTF(mFactory.mFieldName);
		}

		static final long serialVersionUID = -7760363964471913868L;
	}

	static final long serialVersionUID = -1953835346323331784L;

}