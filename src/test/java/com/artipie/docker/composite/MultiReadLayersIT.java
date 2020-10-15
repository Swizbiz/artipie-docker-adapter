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
package com.artipie.docker.composite;

import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.docker.misc.DigestFromContent;
import com.artipie.docker.proxy.ProxyLayers;
import com.artipie.http.client.Settings;
import com.artipie.http.client.auth.AuthClientSlice;
import com.artipie.http.client.auth.GenericAuthenticator;
import com.artipie.http.client.jetty.JettyClientSlices;
import com.artipie.http.slice.LoggingSlice;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link MultiReadLayers}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
class MultiReadLayersIT {

    /**
     * HTTP client used for proxy.
     */
    private JettyClientSlices slices;

    @BeforeEach
    void setUp() throws Exception {
        this.slices = new JettyClientSlices(new Settings.WithFollowRedirects(true));
        this.slices.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        this.slices.stop();
    }

    @Test
    void shouldGetBlob() {
        final RepoName name = new RepoName.Valid("library/busybox");
        final MultiReadLayers layers = new MultiReadLayers(
            Stream.of(
                this.slices.https("mcr.microsoft.com"),
                new AuthClientSlice(
                    this.slices.https("registry-1.docker.io"),
                    new GenericAuthenticator(this.slices)
                )
            ).map(LoggingSlice::new).map(
                slice -> new ProxyLayers(slice, name)
            ).collect(Collectors.toList())
        );
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "78096d0a54788961ca68393e5f8038704b97d8af374249dc5c8faec1b8045e42"
        );
        MatcherAssert.assertThat(
            layers.get(new Digest.FromString(digest))
                .thenApply(Optional::get)
                .thenCompose(Blob::content)
                .thenApply(DigestFromContent::new)
                .thenCompose(DigestFromContent::digest)
                .thenApply(Digest::string)
                .toCompletableFuture().join(),
            new IsEqual<>(digest)
        );
    }
}
