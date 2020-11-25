/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * URI utilities.
 * 
 * The following code have some copy/paste from Spring Framework.
 * 
 * @see https://github.com/spring-projects/spring-framework/blob/396fb0cd513588863491929ddbfe22a49de05beb/spring-web/src/main/java/org/springframework/web/util/HierarchicalUriComponents.java#L331
 * @see https://github.com/spring-projects/spring-framework/blob/396fb0cd513588863491929ddbfe22a49de05beb/spring-core/src/main/java/org/springframework/util/StreamUtils.java#L105
 */
public class URIUtils {

	/**
	 * Encode the given file URI.
	 * 
	 * <ul>
	 * <li>file:///C:/Users/</li>
	 * 
	 * </ul>
	 * 
	 * @param source the file URI to encode.
	 * 
	 * @return the given file URI.
	 */
	public static String encodeFileURI(String source) {
		return encodeFileURI(source, StandardCharsets.UTF_8);
	}

	private static String encodeFileURI(String source, Charset charset) {
		String fileScheme = "";
		int index = -1;
		if (source.startsWith("file://")) {
			index = 6;
			if (source.charAt(7) == '/') {
				index = 7;
			}
		}
		if (index != -1) {
			fileScheme = source.substring(0, index + 1);
			source = source.substring(index + 1, source.length());
		}

		byte[] bytes = source.getBytes(charset);
		boolean original = true;
		for (byte b : bytes) {
			if (!isAllowed(b)) {
				original = false;
				break;
			}
		}
		if (original) {
			return source;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		for (byte b : bytes) {
			if (isAllowed(b)) {
				baos.write(b);
			} else {
				baos.write('%');
				char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
				char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
				baos.write(hex1);
				baos.write(hex2);
			}
		}
		return fileScheme + copyToString(baos, charset);
	}

	/**
	 * Copy the contents of the given {@link ByteArrayOutputStream} into a
	 * {@link String}.
	 * <p>
	 * This is a more effective equivalent of
	 * {@code new String(baos.toByteArray(), charset)}.
	 * 
	 * @param baos    the {@code ByteArrayOutputStream} to be copied into a String
	 * @param charset the {@link Charset} to use to decode the bytes
	 * @return the String that has been copied to (possibly empty)
	 */
	private static String copyToString(ByteArrayOutputStream baos, Charset charset) {
		try {
			// Can be replaced with toString(Charset) call in Java 10+
			return baos.toString(charset.name());
		} catch (UnsupportedEncodingException ex) {
			// Should never happen
			throw new IllegalArgumentException("Invalid charset name: " + charset, ex);
		}
	}

	private static boolean isAllowed(int c) {
		return isUnreserved(c) || '/' == c;
	}

	/**
	 * Indicates whether the given character is in the {@code unreserved} set.
	 * 
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	private static boolean isUnreserved(int c) {
		return (isAlpha(c) || isDigit(c) || '-' == c || '.' == c || '_' == c || '~' == c);
	}

	/**
	 * Indicates whether the given character is in the {@code ALPHA} set.
	 * 
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	private static boolean isAlpha(int c) {
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
	}

	/**
	 * Indicates whether the given character is in the {@code DIGIT} set.
	 * 
	 * @see <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986, appendix A</a>
	 */
	private static boolean isDigit(int c) {
		return (c >= '0' && c <= '9');
	}

}
