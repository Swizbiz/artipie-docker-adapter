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
package com.artipie.docker.asto;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.SubStorage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.docker.error.InvalidDigestException;
import com.google.common.base.Throwables;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link AstoBlobs}.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class AstoBlobsITCase {
    @Test
    void saveBlobDataAtCorrectPath() throws Exception {
        final InMemoryStorage storage = new InMemoryStorage();
        final AstoBlobs blobs = new AstoBlobs(
            new SubStorage(RegistryRoot.V2, storage),
            new DefaultLayout(),
            new RepoName.Simple("does not matter")
        );
        final ByteBuffer buf = ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02, 0x03});
        final Digest digest = blobs.put(
            new Content.From(Flowable.fromArray(buf)), new Digest.Sha256(buf.array())
        ).toCompletableFuture().get().digest();
        MatcherAssert.assertThat(
            "Digest alg is not correct",
            digest.alg(), Matchers.equalTo("sha256")
        );
        final String hash = "054edec1d0211f624fed0cbca9d4f9400b0e491c43742af2c5b0abebf0c990d8";
        MatcherAssert.assertThat(
            "Digest sum is not correct",
            digest.hex(),
            Matchers.equalTo(hash)
        );
        MatcherAssert.assertThat(
            "File content is not correct",
            new BlockingStorage(storage).value(
                new Key.From(String.format("docker/registry/v2/blobs/sha256/05/%s/data", hash))
            ),
            Matchers.equalTo(buf.array())
        );
    }

    @Test
    void failsOnDigestMismatch() {
        final InMemoryStorage storage = new InMemoryStorage();
        final AstoBlobs blobs = new AstoBlobs(
            storage, new DefaultLayout(), new RepoName.Simple("any")
        );
        final ByteBuffer buf = ByteBuffer.wrap("data".getBytes());
        final String digest = "123";
        blobs.put(
            new Content.From(Flowable.fromArray(buf)), new Digest.Sha256(digest)
        ).toCompletableFuture().handle(
            (blob, throwable) -> {
                MatcherAssert.assertThat(
                    "Exception thrown",
                    throwable,
                    new IsNot<>(new IsNull<>())
                );
                MatcherAssert.assertThat(
                    "Exception is InvalidDigestException",
                    Throwables.getRootCause(throwable),
                    new IsInstanceOf(InvalidDigestException.class)
                );
                MatcherAssert.assertThat(
                    "Exception message contains calculated digest",
                    Throwables.getRootCause(throwable).getMessage(),
                    new StringContains(
                        true,
                        "3a6eb0790f39ac87c94f3856b2dd2c5d110e6811602261a9a923d3bb23adc8b7"
                    )
                );
                MatcherAssert.assertThat(
                    "Exception message contains expected digest",
                    Throwables.getRootCause(throwable).getMessage(),
                    new StringContains(true, digest)
                );
                return CompletableFuture.allOf();
            }
        ).join();
    }

    @Test
    void writeAndReadBlob() throws Exception {
        final AstoBlobs blobs = new AstoBlobs(
            new InMemoryStorage(), new DefaultLayout(), new RepoName.Simple("test")
        );
        final ByteBuffer buf = ByteBuffer.wrap(new byte[] {0x05, 0x06, 0x07, 0x08});
        final Digest digest = blobs.put(
            new Content.From(Flowable.fromArray(buf)), new Digest.Sha256(buf.array())
        ).toCompletableFuture().get().digest();
        final byte[] read = Flowable.fromPublisher(
            blobs.blob(digest)
                .toCompletableFuture().get()
                .get().content()
                .toCompletableFuture().get()
        ).toList().blockingGet().get(0).array();
        MatcherAssert.assertThat(read, Matchers.equalTo(buf.array()));
    }

    @Test
    void readAbsentBlob() throws Exception {
        final AstoBlobs blobs = new AstoBlobs(
            new InMemoryStorage(), new DefaultLayout(), new RepoName.Simple("whatever")
        );
        final Digest digest = new Digest.Sha256(
            "0123456789012345678901234567890123456789012345678901234567890123"
        );
        MatcherAssert.assertThat(
            blobs.blob(digest).toCompletableFuture().get().isPresent(),
            new IsEqual<>(false)
        );
    }
}
