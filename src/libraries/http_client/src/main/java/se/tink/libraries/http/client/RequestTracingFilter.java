package se.tink.libraries.http.client;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.libraries.request_tracing.RequestTracer;

public class RequestTracingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    public static final String REQUEST_ID_HEADER = "Request-ID";

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        RequestTracer.startTracing(getRequestId(request));
        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        RequestTracer.stopTracing();
        return response;
    }

    private Optional<String> getRequestId(ContainerRequest containerRequest) {
        return Optional.ofNullable(containerRequest.getRequestHeader(REQUEST_ID_HEADER))
                .map(Collection::stream)
                .flatMap(Stream::findFirst);
    }
}
