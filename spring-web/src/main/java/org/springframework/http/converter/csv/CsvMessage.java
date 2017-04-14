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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

/**
 * A {@link CsvMessage} represents a standard web response mapped to a csv file.
 *
 * <p>A {@link CsvMessage} is segmented in parts called {@link CsvBlock}.
 * Each block may have differents types
 *
 * @author Julien Bouyoud
 * @since 5.0
 */
public class CsvMessage {

	/**
	 * Each csvBlock of this file
	 */
	private final List<CsvBlock<?>> blocks;

	/**
	 * response filename
	 *
	 * <p>If specified, will automatocaly add {@code "Content-Disposition"} http header
	 * with {@code "attatchment; filename="} as value
	 */
	private String filename = null;

	/**
	 * Should insert UTF-8 BOM
	 */
	private boolean withBOM = false;

	/**
	 * Default constructor
	 *
	 * @param blocks response block
	 */
	public CsvMessage(List<CsvBlock<?>> blocks) {
		Assert.notNull(blocks, "blocks cannot be null");
		this.blocks = Collections.unmodifiableList(blocks);
	}

	/**
	 * Returns blocks of this csv response
	 *
	 * @return list of csv block
	 */
	public List<CsvBlock<?>> getBlocks() {
		return blocks;
	}

	/**
	 * Returns csv filename
	 *
	 * @return csv filename of null
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Set a filename.
	 *
	 * <p>If specified a non null value, it will automatocaly add {@code "Content-Disposition"} http header
	 * with {@code "attatchment; filename="} as value
	 *
	 * @param filename filename to set
	 * @return self
	 */
	public CsvMessage setFilename(String filename) {
		this.filename = filename;
		return this;
	}

	/**
	 * Indicates if the current file contains an UTF-8 BOM character
	 *
	 * @return true if the file contains a BOM, false else
	 */
	public boolean isWithBOM() {
		return withBOM;
	}

	/**
	 * Set whenever the file contains an UTF-8 BOM
	 *
	 * @param withBOM true if the file contains a BOM, false else
	 * @return self
	 */
	public CsvMessage setWithBOM(boolean withBOM) {
		this.withBOM = withBOM;
		return this;
	}

}
