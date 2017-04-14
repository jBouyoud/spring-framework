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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.supercsv.cellprocessor.Trim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test suite for {@link CsvHttpMessageConverter}.
 *
 * @author Julien Bouyoud
 */
public class CsvHttpMessageConverterTests {

	private static final String SERIALIZED_TEST_MESSAGE = "Header;\n" +
			"  a  ;a\n" +
			"  b  ;b\n" +
			"  c  ;c\n" +
			"\n" +
			"Header;bar\n" +
			"a;10\n" +
			"B;20\n";

	private CsvHttpMessageConverter converter;
	private CsvMessage testMsg;

	@Before
	public void setUp() {
		this.converter = new CsvHttpMessageConverter();
		testMsg = new CsvMessage(Arrays.asList(getFirstBlock(), getSecondBlock()));
	}


	private CsvBlock<String> getFirstBlock() {
		return new CsvBlock<>(Arrays.asList(
				new CsvColumn<>(Function.identity(), "Header"),
				new CsvColumn<>(null, null, new Trim())
		), Stream.of("  a  ", "  b  ", "  c  "));
	}

	private CsvBlock<CustomBean> getSecondBlock() {
		return new CsvBlock<>(Arrays.asList(
				new CsvColumn<>(c -> c.foo, "Header", new Trim()),
				new CsvColumn<>(c -> c.bar * 10, "bar")
		), Stream.of(new CustomBean(" a ", 1), new CustomBean(" B ", 2)))
				.setComments(new String[]{""});
	}

	@Test(expected = IllegalArgumentException.class)
	public void needsPreference() {
		new CsvHttpMessageConverter(null);
	}

	@Test
	public void canRead() {
		assertTrue(this.converter.canRead(CsvMessage.class, null));
		assertTrue(this.converter.canRead(CsvMessage.class, MediaType.TEXT_CSV));
		assertTrue(this.converter.canRead(CsvMessage.class, MediaType.TEXT_CSV_UTF8));
	}

	@Test
	public void canWrite() {
		assertTrue(this.converter.canWrite(CsvMessage.class, null));
		assertTrue(this.converter.canWrite(CsvMessage.class, MediaType.TEXT_CSV));
		assertTrue(this.converter.canWrite(CsvMessage.class, MediaType.TEXT_CSV_UTF8));
	}

	@Test(expected = IllegalStateException.class)
	public void read() throws IOException {
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(new byte[]{});
		this.converter.read(CsvMessage.class, inputMessage);
	}

	@Test
	public void write() throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MediaType contentType = MediaType.TEXT_CSV_UTF8;
		this.converter.write(this.testMsg, contentType, outputMessage);
		assertEquals(contentType, outputMessage.getHeaders().getContentType());
		assertEquals(-1, outputMessage.getHeaders().getContentLength());
		assertTrue(outputMessage.getBodyAsBytes().length > 0);
		assertEquals(SERIALIZED_TEST_MESSAGE, outputMessage.getBodyAsString(Charset.forName("utf-8")));
	}

	@Test
	public void writeWithFilename() throws IOException {

		CsvMessage csvMessage = new CsvMessage(testMsg.getBlocks());
		csvMessage.setFilename("filename.csv");

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MediaType contentType = MediaType.TEXT_CSV_UTF8;
		this.converter.write(csvMessage, contentType, outputMessage);
		assertEquals(contentType, outputMessage.getHeaders().getContentType());
		assertEquals(-1, outputMessage.getHeaders().getContentLength());
		assertEquals(csvMessage.getFilename(), outputMessage.getHeaders().getContentDisposition().getFilename());
		assertTrue(outputMessage.getBodyAsBytes().length > 0);
		assertEquals(SERIALIZED_TEST_MESSAGE, outputMessage.getBodyAsString(Charset.forName("utf-8")));
	}

	@Test
	public void writeWithBOM() throws IOException {
		CsvMessage csvMessage = new CsvMessage(testMsg.getBlocks());
		csvMessage.setWithBOM(true);

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MediaType contentType = MediaType.TEXT_CSV_UTF8;
		this.converter.write(csvMessage, contentType, outputMessage);
		assertEquals(contentType, outputMessage.getHeaders().getContentType());
		assertEquals(-1, outputMessage.getHeaders().getContentLength());
		assertTrue(outputMessage.getBodyAsBytes().length > 0);

		byte[] expectedBom = "\uFEFF".getBytes("utf-8");
		byte[] actual = new byte[expectedBom.length];
		byte[] content = new byte[outputMessage.getBodyAsBytes().length - expectedBom.length];
		System.arraycopy(outputMessage.getBodyAsBytes(), 0, actual, 0, expectedBom.length);
		System.arraycopy(outputMessage.getBodyAsBytes(), expectedBom.length, content, 0, outputMessage.getBodyAsBytes().length - expectedBom.length);
		Assert.assertArrayEquals(expectedBom, actual);
		assertEquals(SERIALIZED_TEST_MESSAGE, new String(content));
	}

	private static final class CustomBean {

		private String foo;
		private int bar;


		public CustomBean(String foo, int bar) {
			this.foo = foo;
			this.bar = bar;
		}
	}
}
