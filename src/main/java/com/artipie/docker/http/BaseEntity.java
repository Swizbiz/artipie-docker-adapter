/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/docker-adapter/LICENSE.txt
 */
package com.artipie.docker.http;

import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.regex.Pattern;
import org.reactivestreams.Publisher;

/**
 * Base entity in Docker HTTP API.
 * See <a href="https://docs.docker.com/registry/spec/api/#base">Base</a>.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class BaseEntity implements ScopeSlice {

    /**
     * RegEx pattern for path.
     */
    public static final Pattern PATH = Pattern.compile("^/v2/$");

    @Override
    public Scope scope(final String line) {
        return new Scope.Registry("base", "*");
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        return new RsWithHeaders(
            new RsWithStatus(RsStatus.OK),
            "Docker-Distribution-API-Version", "registry/2.0"
        );
    }
}
