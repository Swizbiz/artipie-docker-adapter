/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/docker-adapter/LICENSE.txt
 */
package com.artipie.docker;

import com.artipie.asto.Content;

/**
 * Docker repositories catalog.
 *
 * @since 0.8
 */
public interface Catalog {

    /**
     * Read catalog in JSON format.
     *
     * @return Catalog in JSON format.
     */
    Content json();
}
