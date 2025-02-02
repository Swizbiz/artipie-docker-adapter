/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/docker-adapter/LICENSE.txt
 */
package com.artipie.docker.http;

import com.artipie.http.headers.ContentType;
import com.artipie.http.headers.Header;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Content-Type header with "application/json; charset=..." value.
 *
 * @since 0.9
 */
final class JsonContentType extends Header.Wrap {

    /**
     * Ctor.
     */
    protected JsonContentType() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * Ctor.
     *
     * @param charset Charset.
     */
    protected JsonContentType(final Charset charset) {
        super(
            new ContentType(
                String.format(
                    "application/json; charset=%s",
                    charset.displayName().toLowerCase(Locale.getDefault())
                )
            )
        );
    }
}
