/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.converter.csv;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.Assert;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * A {@link CsvBlock} is a {@link Stream} of elements with the same type.
 * A {@link CsvBlock} is a part of csv file witch is defnied by {@link CsvMessage}.
 *
 * <p>A {@link CsvBlock} is defined at least by :
 * <ul>
 *     <li><a list of {@link CsvColumn} witch defines how columns values are extracted
 *     from data and their csv header and formmatter.
 *     <li>a {@link Stream} of data
 *
 * <p>A {@link CsvBlock} may contains some {@link #comments}
 *
 * @param <T> block element type
 * @author Julien Bouyoud
 * @since 5.0
 */
public class CsvBlock<T> {

	/**
	 * List of {@link CsvColumn} for this block
	 */
	private final List<CsvColumn<T>> columns;

	/**
	 * Data to serialize
	 */
	private final Stream<T> rows;

	/**
	 * Set of single-line comment to the CSV file (the comment must already include any special comment characters
	 * e.g. '#' at start).
	 * <p>Please note that comments are not part of RFC4180, so this may make your CSV file less
	 * portable.
	 */
	private String[] comments = null;

	/**
	 * Default constructor
	 *
	 * @param columns list of csv columns to extract
	 * @param rows
	 */
	public CsvBlock(final List<CsvColumn<T>> columns, final Stream<T> rows) {
		Assert.notNull(columns, "CsvBlock columns cannot be null");
		Assert.notNull(columns, "CsvBlock rows cannot be null");
		this.columns = Collections.unmodifiableList(columns);
		this.rows = rows;
	}

	/**
	 * Get {@link CsvColumn} for the current block
	 *
	 * @return csv columns, couldn't be null
	 */
	public List<CsvColumn<T>> getColumns() {
		return columns;
	}

	/**
	 * Get data
	 *
	 * @return data, couldn't be null
	 */
	public Stream<T> getRows() {
		return rows;
	}

	/**
	 * Get single-line comments
	 *
	 * @return array of comments or null
	 */
	public String[] getComments() {
		return comments;
	}

	/**
	 * Set single-line comments
	 *
	 * @param comments comments
	 * @return self
	 */
	public CsvBlock<T> setComments(String[] comments) {
		this.comments = comments;
		return this;
	}

	/**
	 * Returns block columns headers. If all headers are null, list of headers will be empty
	 *
	 * @return block columns headers
	 */
	public List<String> getHeaders() {
		if (columns.stream().map(CsvColumn::getHeader).filter(Objects::isNull).count() == columns.size()) {
			// All headers is empty
			return Collections.emptyList();
		}
		return columns.stream().map(CsvColumn::getHeader).collect(Collectors.toList());
	}

	/**
	 * Returns the stream of columns  mappers
	 *
	 * @return stream of columns  mappers
	 */
	public Stream<Function<T, ?>> getMappers() {
		return columns.stream()
				.map(CsvColumn::getColumnValueExtractor)
				.map(m -> m == null ? (t -> t) : m);
	}

	/**
	 * Returns the list of columns cell processors, each element of this list could be null
	 *
	 * @return the list of columns cell processors
	 */
	public List<CellProcessor> getCellProcessors() {
		if (columns.stream().map(CsvColumn::getCellProcessor).filter(Objects::isNull).count() == columns.size()) {
			// All headers is empty
			return Collections.emptyList();
		}
		return columns.stream().map(CsvColumn::getCellProcessor).collect(Collectors.toList());
	}
}