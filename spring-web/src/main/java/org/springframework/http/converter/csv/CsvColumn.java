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

import java.util.function.Function;

import org.supercsv.cellprocessor.ift.CellProcessor;


/**
 * {@link CsvColumn} define how to generate column value, his header, and formatter with {@link CellProcessor}
 *
 * @author Julien Bouyoud
 * @since 5.0
 */
public class CsvColumn<T> {

	/**
	 * Column columnValueExtractor able to extract column value for row type
	 */
	private final Function<T, ?> columnValueExtractor;

	/**
	 * Column header
	 */
	private final String header;

	/**
	 * Column {@link CellProcessor} used to format value in csv
	 */
	private final CellProcessor cellProcessor;

	/**
	 * Construct a new {@link CsvColumn}
	 */
	public CsvColumn() {
		this(null, null, null);
	}

	/**
	 * Construct a new {@link CsvColumn}
	 *
	 * @param columnValueExtractor column columnValueExtractor
	 */
	public CsvColumn(Function<T, ?> columnValueExtractor) {
		this(columnValueExtractor, null, null);
	}

	/**
	 * Construct a new {@link CsvColumn}
	 *
	 * @param columnValueExtractor column columnValueExtractor
	 * @param header column header
	 */
	public CsvColumn(Function<T, ?> columnValueExtractor, String header) {
		this(columnValueExtractor, header, null);
	}

	/**
	 * Construct a new {@link CsvColumn}
	 *
	 * @param columnValueExtractor        column columnValueExtractor
	 * @param header        column header
	 * @param cellProcessor column cell processor formatter
	 */
	public CsvColumn(Function<T, ?> columnValueExtractor, String header, CellProcessor cellProcessor) {
		this.columnValueExtractor = columnValueExtractor;
		this.header = header;
		this.cellProcessor = cellProcessor;
	}

	/**
	 * Returns column columnValueExtractor
	 *
	 * @return column columnValueExtractor or {@code null}
	 */
	public Function<T, ?> getColumnValueExtractor() {
		return columnValueExtractor;
	}

	/**
	 * Returns column header
	 *
	 * @return column header or {@code null}
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Returns column {@link CellProcessor} able to format value in csv
	 *
	 * @return cellProcessor or {@code null}
	 */
	public CellProcessor getCellProcessor() {
		return cellProcessor;
	}

	/**
	 * Construct a new {@link CsvColumn} with a new {@link #columnValueExtractor}
	 *
	 * @param columnValueExtractor function used to extract column value for row type
	 * @return new csv column instance
	 */
	public CsvColumn<T> withColumnValueExtractor(Function<T, ?> columnValueExtractor) {
		return new CsvColumn<>(columnValueExtractor, getHeader(), getCellProcessor());
	}

	/**
	 * Construct a new {@link CsvColumn} with a new {@link #header}
	 *
	 * @param header column header to use with this column
	 * @return new csv column instance
	 */
	public CsvColumn<T> withHeader(String header) {
		return new CsvColumn<>(getColumnValueExtractor(), header, getCellProcessor());
	}

	/**
	 * Construct a new {@link CsvColumn} with a new {@link CellProcessor} able to format column value to csv
	 *
	 * @param cellProcessor cellprocessor to use with this column
	 * @return new csv column instance
	 */
	public CsvColumn<T> withCellProcessor(CellProcessor cellProcessor) {
		return new CsvColumn<>(getColumnValueExtractor(), getHeader(), cellProcessor);
	}

}
