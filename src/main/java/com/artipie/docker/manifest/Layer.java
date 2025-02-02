/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/docker-adapter/LICENSE.txt
 */
package com.artipie.docker.manifest;

import com.artipie.docker.Digest;
import java.net.URL;
import java.util.Collection;

/**
 * Image layer.
 *
 * @since 0.2
 */
public interface Layer {

    /**
     * Read layer content digest.
     *
     * @return Layer content digest..
     */
    Digest digest();

    /**
     * Provides a list of URLs from which the content may be fetched.
     *
     * @return URLs, might be empty
     */
    Collection<URL> urls();
}
