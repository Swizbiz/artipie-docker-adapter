/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/docker-adapter/LICENSE.txt
 */
package com.artipie.docker.asto;

import com.artipie.asto.Key;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.google.common.base.Splitter;
import java.io.StringReader;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.json.Json;
import javax.json.JsonReader;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wtf.g4s8.hamcrest.json.JsonContains;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.JsonValueIs;

/**
 * Tests for {@link AstoTags}.
 *
 * @since 0.9
 */
final class AstoTagsTest {

    /**
     * Repository name used in tests.
     */
    private RepoName name;

    /**
     * Tag keys.
     */
    private Collection<Key> keys;

    @BeforeEach
    void setUp() {
        this.name = new RepoName.Simple("test");
        this.keys = Stream.of("foo/1.0", "foo/0.1-rc", "foo/latest", "foo/0.1")
            .map(Key.From::new)
            .collect(Collectors.toList());
    }

    @ParameterizedTest
    @CsvSource({
        ",,0.1;0.1-rc;1.0;latest",
        "0.1-rc,,1.0;latest",
        "xyz,,''",
        ",2,0.1;0.1-rc",
        "0.1,2,0.1-rc;1.0"
    })
    void shouldSupportPaging(final String from, final Integer limit, final String result) {
        MatcherAssert.assertThat(
            new PublisherAs(
                new AstoTags(
                    this.name,
                    new Key.From("foo"),
                    this.keys,
                    Optional.ofNullable(from).map(Tag.Valid::new),
                    Optional.ofNullable(limit).orElse(Integer.MAX_VALUE)
                ).json()
            ).asciiString().thenApply(
                str -> {
                    try (JsonReader reader = Json.createReader(new StringReader(str))) {
                        return reader.readObject();
                    }
                }
            ).toCompletableFuture().join(),
            new JsonHas(
                "tags",
                new JsonContains(
                    StreamSupport.stream(
                        Splitter.on(";").omitEmptyStrings().split(result).spliterator(),
                        false
                    ).map(JsonValueIs::new).collect(Collectors.toList())
                )
            )
        );
    }
}
