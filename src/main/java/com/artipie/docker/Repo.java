/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/docker-adapter/LICENSE.txt
 */

package com.artipie.docker;

/**
 * Docker repository files and metadata.
 * @since 0.1
 */
public interface Repo {

    /**
     * Repository layers.
     *
     * @return Layers.
     */
    Layers layers();

    /**
     * Repository manifests.
     *
     * @return Manifests.
     */
    Manifests manifests();

    /**
     * Repository uploads.
     *
     * @return Uploads.
     */
    Uploads uploads();
}
