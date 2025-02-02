/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/docker-adapter/LICENSE.txt
 */
package com.artipie.docker.fake;

import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.Layers;
import com.artipie.docker.asto.BlobSource;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Auxiliary class for tests for {@link com.artipie.docker.cache.CacheLayers}.
 *
 * @since 0.5
 */
public final class FakeLayers implements Layers {
    /**
     * Layers.
     */
    private final Layers layers;

    /**
     * Ctor.
     *
     * @param type Type of layers.
     */
    public FakeLayers(final String type) {
        this.layers = layersFromType(type);
    }

    @Override
    public CompletionStage<Blob> put(final BlobSource source) {
        return this.layers.put(source);
    }

    @Override
    public CompletionStage<Blob> mount(final Blob blob) {
        return this.layers.mount(blob);
    }

    @Override
    public CompletionStage<Optional<Blob>> get(final Digest digest) {
        return this.layers.get(digest);
    }

    /**
     * Creates layers.
     *
     * @param type Type of layers.
     * @return Layers.
     */
    private static Layers layersFromType(final String type) {
        final Layers tmplayers;
        switch (type) {
            case "empty":
                tmplayers = new EmptyGetLayers();
                break;
            case "full":
                tmplayers = new FullGetLayers();
                break;
            case "faulty":
                tmplayers = new FaultyGetLayers();
                break;
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported type: %s", type)
                );
        }
        return tmplayers;
    }
}
