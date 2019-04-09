package se.tink.libraries.jersey.logging;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

public class PublicAPIHTTPHeadersFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        MultivaluedMap<String, Object> headers = response.getHttpHeaders();

        setHttpHeader(headers, "X-Content-Type-Options", "nosniff");
        setHttpHeader(headers, "X-XSS-Protection", "1; mode=block");
        setHttpHeader(headers, "Strict-Transport-Security", "max-age=31536000");
        setHttpHeader(headers, "X-Contact", "jobs@tink.se,whitehat@tink.se");
        setHttpHeader(headers, "Cache-Control", "no-cache");
        setHttpHeader(headers, "Pragma", "no-cache");

        return response;
    }

    private static void setHttpHeader(
            MultivaluedMap<String, Object> headers, String key, String value) {
        // Note that we override previously added headers.
        headers.put(key, ImmutableList.<Object>of(value));
    }
}
