package se.tink.libraries.http.client;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.requesttracing.RequestTracer;

public class RequestTracingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestTracingFilter.class);
    static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        RequestTracer.startTracing(getRequestId(request));
        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        RequestTracer.getRequestId()
                .ifPresent(v -> response.getHttpHeaders().putSingle(REQUEST_ID_HEADER, v));
        RequestTracer.stopTracing();
        return response;
    }

    private Optional<String> getRequestId(ContainerRequest containerRequest) {
        Optional<String> requestId =
                Optional.ofNullable(containerRequest.getRequestHeader(REQUEST_ID_HEADER))
                        .map(Collection::stream)
                        .flatMap(Stream::findFirst);

        if (!requestId.isPresent()) {
            logger.warn("Did not receive a requestId");
        }

        return requestId;
    }
}
