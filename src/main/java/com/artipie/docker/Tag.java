/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.artipie.docker;

import com.artipie.docker.error.InvalidTagNameException;
import java.util.regex.Pattern;

/**
 * Docker image tag.
 * See <a href="https://docs.docker.com/engine/reference/commandline/tag/">docker tag</a>.
 *
 * @since 0.2
 */
public interface Tag {

    /**
     * Tag string.
     *
     * @return Tag as string.
     */
    String value();

    /**
     * Valid tag name.
     * Validation rules are the following:
     * <p>
     * A tag name must be valid ASCII and may contain
     * lowercase and uppercase letters, digits, underscores, periods and dashes.
     * A tag name may not start with a period or a dash and may contain a maximum of 128 characters.
     * </p>
     *
     * @since 0.1
     */
    final class Valid implements Tag {

        /**
         * RegEx tag validation pattern.
         */
        private static final Pattern PATTERN =
            Pattern.compile("^[a-zA-Z0-9_][a-zA-Z0-9_.-]{0,127}$");

        /**
         * Original unvalidated value.
         */
        private final String original;

        /**
         * Ctor.
         *
         * @param original Original unvalidated value.
         */
        public Valid(final String original) {
            this.original = original;
        }

        @Override
        public String value() {
            if (!this.valid()) {
                throw new InvalidTagNameException(
                    String.format("Invalid tag: '%s'", this.original)
                );
            }
            return this.original;
        }

        /**
         * Validates digest string.
         *
         * @return True if string is valid digest, false otherwise.
         */
        public boolean valid() {
            return Tag.Valid.PATTERN.matcher(this.original).matches();
        }
    }
}
