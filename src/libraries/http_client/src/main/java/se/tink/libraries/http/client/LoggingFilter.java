package se.tink.libraries.http.client;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        try {
            byte[] rawBody = request.getEntity(byte[].class);
            if (rawBody != null) {
                // Put the body back into the input stream
                request.setEntityInputStream(new ByteArrayInputStream(rawBody));
            }
            String body = rawBody == null ? null : new String(rawBody);
            String headers = formatHeaders(request.getRequestHeaders());

            logger.info(
                    "incoming request:\n{} {},\nheaders: {},\nmedia type: {},\nbody: {}\n",
                    request.getMethod(),
                    request.getRequestUri(),
                    headers,
                    request.getMediaType(),
                    body);
        } catch (RuntimeException e) {
            logger.error("sth bad happened when logging incoming request", e);
        }
        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        try {
            String body = response.getEntity() == null ? null : response.getEntity().toString();
            String headers = formatHeaders(response.getHttpHeaders());

            logger.info(
                    "outgoing response:\nstatus: {},\n headers: {},\nmedia type: {},\nbody: {}",
                    response.getStatus(),
                    headers,
                    request.getMediaType(),
                    body);
        } catch (RuntimeException e) {
            logger.error("sth bad happened when logging incoming request", e);
        }
        return response;
    }

    private String formatHeaders(MultivaluedMap headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }

        return "\n\t"
                + headers.entrySet().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("\n\t"));
    }
}
