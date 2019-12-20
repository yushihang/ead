/**
 * eAdventure is a research project of the
 *    e-UCM research group.
 *
 *    Copyright 2005-2014 e-UCM research group.
 *
 *    You can access a list of all the contributors to eAdventure at:
 *          http://e-adventure.e-ucm.es/contributors
 *
 *    e-UCM is a research group of the Department of Software Engineering
 *          and Artificial Intelligence at the Complutense University of Madrid
 *          (School of Computer Science).
 *
 *          CL Profesor Jose Garcia Santesmases 9,
 *          28040 Madrid (Madrid), Spain.
 *
 *          For more info please visit:  <http://e-adventure.e-ucm.es> or
 *          <http://www.e-ucm.es>
 *
 * ****************************************************************************
 *
 *  This file is part of eAdventure
 *
 *      eAdventure is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      eAdventure is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with eAdventure.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.eucm.utils.apache.commons.lang3.translate;

import java.io.IOException;
import java.io.Writer;

/**
 * File adapted from Apache's StringEscapeUtils class, which can be found in
 * package commons-lang3. Original file licensed under terms of Apache 2.0
 * license: http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Adapted for the Mokap project by jtorrente
 * 
 * Translate escaped octal Strings back to their octal values.
 * 
 * For example, "\45" should go back to being the specific value (a %).
 * 
 * Note that this currently only supports the viable range of octal for Java;
 * namely 1 to 377. This is because parsing Java is the main use case.
 * 
 * @since 3.0
 * @version $Id: OctalUnescaper.java 967237 2010-07-23 20:08:57Z mbenson $
 */
public class OctalUnescaper extends CharSequenceTranslator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int translate(final CharSequence input, final int index,
			final Writer out) throws IOException {
		final int remaining = input.length() - index - 1; // how many characters
															// left, ignoring
															// the first \
		final StringBuilder builder = new StringBuilder();
		if (input.charAt(index) == '\\' && remaining > 0
				&& isOctalDigit(input.charAt(index + 1))) {
			final int next = index + 1;
			final int next2 = index + 2;
			final int next3 = index + 3;

			// we know this is good as we checked it in the if block above
			builder.append(input.charAt(next));

			if (remaining > 1 && isOctalDigit(input.charAt(next2))) {
				builder.append(input.charAt(next2));
				if (remaining > 2 && isZeroToThree(input.charAt(next))
						&& isOctalDigit(input.charAt(next3))) {
					builder.append(input.charAt(next3));
				}
			}

			out.write(Integer.parseInt(builder.toString(), 8));
			return 1 + builder.length();
		}
		return 0;
	}

	/**
	 * Checks if the given char is an octal digit. Octal digits are the
	 * character representations of the digits 0 to 7.
	 * 
	 * @param ch
	 *            the char to check
	 * @return true if the given char is the character representation of one of
	 *         the digits from 0 to 7
	 */
	private boolean isOctalDigit(final char ch) {
		return ch >= '0' && ch <= '7';
	}

	/**
	 * Checks if the given char is the character representation of one of the
	 * digit from 0 to 3.
	 * 
	 * @param ch
	 *            the char to check
	 * @return true if the given char is the character representation of one of
	 *         the digits from 0 to 3
	 */
	private boolean isZeroToThree(final char ch) {
		return ch >= '0' && ch <= '3';
	}
}