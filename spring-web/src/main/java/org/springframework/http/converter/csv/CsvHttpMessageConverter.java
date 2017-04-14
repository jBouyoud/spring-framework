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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.util.Assert;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.encoder.DefaultCsvEncoder;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * An {@code HttpMessageConverter} that writes {@link CsvMessage}s using
 * <a href="http://super-csv.github.io/super-csv/index.html">Super-Csv</a>.
 *
 * <p>This converter supports  {@code "text/csv"} and {@code "text/csv;charset=utf-8"}
 *
 * <p>This converter use Super-Csv to serialize messages.
 *
 * <p>Reads is not supported for instance.
 *
 * @author Julien Bouyoud
 * @since 5.0
 */
public class CsvHttpMessageConverter extends AbstractHttpMessageConverter<CsvMessage> {

	private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

	/**
	 * Super Csv preference
	 */
	private final CsvPreference csvPreference;

	/**
	 * Construct a new {@link CsvHttpMessageConverter} with default csv preference {@link CsvPreference#EXCEL_NORTH_EUROPE_PREFERENCE}
	 */
	public CsvHttpMessageConverter() {
		this(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
	}

	/**
	 * Construct a new {@link CsvHttpMessageConverter} with default csv preference {@link CsvPreference#EXCEL_NORTH_EUROPE_PREFERENCE}
	 *
	 * @param csvPreference to use while converting {@link CsvMessage}
	 */
	public CsvHttpMessageConverter(final CsvPreference csvPreference) {
		super(MediaType.TEXT_CSV_UTF8, MediaType.TEXT_CSV);
		Assert.notNull(csvPreference, "csvPreference must be defined");
		this.csvPreference = csvPreference;
	}

	@Override
	protected boolean supports(final Class<?> clazz) {
		Assert.notNull(clazz, "clazz cannot be null");
		return CsvMessage.class.equals(clazz);
	}

	@Override
	protected void writeInternal(final CsvMessage response,
			final HttpOutputMessage output) throws IOException {

		Assert.notNull(response, "response cannot be null");
		Assert.notNull(output, "output cannot be null");

		MediaType contentType = output.getHeaders().getContentType();
		if (contentType == null) {
			contentType = getDefaultContentType(response);
			output.getHeaders().setContentType(contentType);
		}
		Charset charset = contentType.getCharset();
		if (charset == null) {
			charset = DEFAULT_CHARSET;
		}

		if (response.getFilename() != null) {
			output.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION,
					ContentDisposition.builder("attatchment")
							.filename(response.getFilename(), charset).build().toString());
		}

		// Build a new Preference each Time :: due to SuperCSV issue
		CsvPreference pref = new CsvPreference.Builder(csvPreference)
				.useEncoder(new DefaultCsvEncoder()).build();

		try (Writer writer = new OutputStreamWriter(output.getBody(), charset);
		     ICsvListWriter csvWriter = new CsvListWriter(writer, pref)) {

			// Should insert utf-8 BOM
			if (response.isWithBOM()) {
				writer.write("\uFEFF");
			}

			for (CsvBlock<?> csvBlock : response.getBlocks()) {
				writeBlock(csvWriter, csvBlock);
			}
			csvWriter.flush();
		}
	}


	private <T> void writeBlock(ICsvListWriter csvWriter, CsvBlock<T> csvBlock) throws IOException {

		Assert.notNull(csvWriter, "csvWriter cannot be null");
		Assert.notNull(csvBlock, "csvBlock cannot be null");

		// write comments, if there
		final String[] comments = csvBlock.getComments();
		if (comments != null) {
			for (final String comment : comments) {
				csvWriter.writeComment(comment);
			}
		}

		// write headers
		List<String> headers = csvBlock.getHeaders();
		if (!headers.isEmpty()) {
			csvWriter.writeHeader(headers.toArray(new String[headers.size()]));
		}

		List<CellProcessor> cellProcessors = csvBlock.getCellProcessors();
		CellProcessor[] cellProcessorsAsArray = cellProcessors.toArray(new CellProcessor[cellProcessors.size()]);

		try {
			// write content
			csvBlock.getRows().forEachOrdered(row -> {
				List<Object> values = csvBlock.getMappers().map(m -> m.apply(row)).collect(Collectors.toList());
				try {
					if (cellProcessorsAsArray.length > 0) {
						csvWriter.write(values, cellProcessorsAsArray);
					}
					else {
						csvWriter.write(values);
					}
				}
				catch (IOException e) {
					throw new WrappedIOException(e);
				}
			});
		}
		catch (WrappedIOException ex) {
			logger.trace("Rethrow RuntimeIOException", ex);
			throw ex.getCause();
		}
	}

	@Override
	protected CsvMessage readInternal(Class<? extends CsvMessage> clazz, HttpInputMessage inputMessage)
			throws IOException {

		throw new IllegalStateException("Read CSV is not yet implemented");
	}

	/**
	 * {@link IOException} runtime wrapper
	 * <p>
	 * Wrap an {@link IOException} to a {@link RuntimeException}
	 */
	private static class WrappedIOException extends RuntimeException {

		private static final long serialVersionUID = -7178993036038664317L;

		/**
		 * Construct a new {@link WrappedIOException}
		 *
		 * @param cause IOException to wrap
		 */
		public WrappedIOException(IOException cause) {
			super(cause);
			Assert.notNull(cause, "cause must be specified");
		}

		/**
		 * Returns the wrapped {@link IOException}
		 *
		 * @return the wrapped {@link IOException}
		 */
		@Override
		public IOException getCause() {
			return (IOException) super.getCause();
		}
	}
}
