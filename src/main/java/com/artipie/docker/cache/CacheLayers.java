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
package com.artipie.docker.cache;

import com.artipie.asto.Content;
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.Layers;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Cache implementation of {@link Layers}.
 *
 * @since 0.3
 */
public final class CacheLayers implements Layers {

    /**
     * Origin layers.
     */
    private final Layers origin;

    /**
     * Cache layers.
     */
    private final Layers cache;

    /**
     * Ctor.
     *
     * @param origin Origin layers.
     * @param cache Cache layers.
     */
    public CacheLayers(final Layers origin, final Layers cache) {
        this.origin = origin;
        this.cache = cache;
    }

    @Override
    public CompletionStage<Blob> put(final Content content, final Digest digest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionStage<Blob> mount(final Blob blob) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionStage<Optional<Blob>> get(final Digest digest) {
        return this.cache.get(digest).handle(
            (cached, throwable) -> {
                final CompletionStage<Optional<Blob>> result;
                if (throwable == null) {
                    if (cached.isPresent()) {
                        result = CompletableFuture.completedFuture(cached);
                    } else {
                        result = this.origin.get(digest).exceptionally(ignored -> cached);
                    }
                } else {
                    result = this.origin.get(digest);
                }
                return result;
            }
        ).thenCompose(Function.identity());
    }
}
