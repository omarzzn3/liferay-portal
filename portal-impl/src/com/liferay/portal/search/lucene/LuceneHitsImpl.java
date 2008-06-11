/**
 * Copyright (c) 2000-2008 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.search.lucene;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.util.Time;
import com.liferay.util.search.DocumentImpl;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.Searcher;

/**
 * <a href="LuceneHitsImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 * @author Bruno Farache
 * @author Allen Chiang
 *
 */
public class LuceneHitsImpl implements Hits {

	public LuceneHitsImpl() {
		_start = System.currentTimeMillis();
	}

	public long getStart() {
		return _start;
	}

	public void setStart(long start) {
		_start = start;
	}

	public float getSearchTime() {
		return _searchTime;
	}

	public void setSearchTime(float time) {
		_searchTime = time;
	}

	public Document[] getDocs() {
		return _docs;
	}

	public void setDocs(Document[] docs) {
		_docs = docs;
	}

	public int getLength() {
		return _length;
	}

	public void setLength(int length) {
		_length = length;
	}

	public float[] getScores() {
		return _scores;
	}

	public void setScores(float[] scores) {
		_scores = scores;
	}

	public void setScores(Float[] scores) {
		float[] primScores = new float[scores.length];

		for (int i = 0; i < scores.length; i++) {
			primScores[i] = scores[i].floatValue();
		}

		setScores(primScores);
	}

	public Searcher getSearcher() {
		return _searcher;
	}

	public void setSearcher(Searcher searcher) {
		_searcher = searcher;
	}

	/**
	 * @deprecated
	 */
	public void closeSearcher() {
	}

	/**
	 * @deprecated
	 */
	public Hits closeSearcher(String keywords, Exception e) {
		return null;
	}

	public Document doc(int n) {
		try {
			if ((_docs[n] == null) && (_hits != null)) {
				_docs[n] =_getDocument(_hits.doc(n));
			}
		}
		catch (IOException ioe) {
		}

		return _docs[n];
	}

	public float score(int n) {
		try {
			if ((_scores[n] == 0) && (_hits != null)) {
				_scores[n] = _hits.score(n);
			}
		}
		catch (IOException ioe) {
		}

		return _scores[n];
	}

	public LuceneHitsImpl subset(int start, int end) {
		LuceneHitsImpl subset = new LuceneHitsImpl();

		if ((start > - 1) && (start <= end)) {
			subset.setStart(getStart());

			if (end > _length) {
				end = _length;
			}

			int subsetTotal = end - start;

			Document[] subsetDocs = new DocumentImpl[subsetTotal];
			float[] subsetScores = new float[subsetTotal];

			int j = 0;

			for (int i = start; (i < end) && (i < getLength()); i++, j++) {
				subsetDocs[j] = doc(i);
				subsetScores[j] = score(i);
			}

			subset.setLength(_length);

			subset.setDocs(subsetDocs);
			subset.setScores(subsetScores);

			_searchTime =
				(float)(System.currentTimeMillis() - _start) / Time.SECOND;

			subset.setSearchTime(getSearchTime());
		}

		return subset;
	}

	public List<Document> toList() {
		List<Document> subset = new ArrayList<Document>(_docs.length);

		for (int i = 0; i < _docs.length; i++) {
			subset.add(_docs[i]);
		}

		return subset;
	}

	public void recordHits(
		org.apache.lucene.search.Hits hits, Searcher searcher) {

		_hits = hits;
		_length = hits.length();
		_docs = new DocumentImpl[_length];
		_scores = new float[_length];
		_searcher = searcher;
	}

	private DocumentImpl _getDocument(
		org.apache.lucene.document.Document oldDoc) {

		DocumentImpl newDoc = new DocumentImpl();

		List<org.apache.lucene.document.Field> oldFields = oldDoc.getFields();

		for (org.apache.lucene.document.Field oldField : oldFields) {
			String[] values = oldDoc.getValues(oldField.name());

			if ((values != null) && (values.length > 1)) {
				Field newField = new Field(
					oldField.name(), values, oldField.isTokenized());

				newDoc.add(newField);
			}
			else {
				Field newField = new Field(
					oldField.name(), oldField.stringValue(),
					oldField.isTokenized());

				newDoc.add(newField);
			}
		}

		return newDoc;
	}

	private org.apache.lucene.search.Hits _hits;
	private long _start;
	private float _searchTime;
	private Document[] _docs = new DocumentImpl[0];
	private int _length;
	private float[] _scores = new float[0];
	private Searcher _searcher;

}