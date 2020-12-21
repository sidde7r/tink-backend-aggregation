package se.tink.libraries.http.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.http.client.masker.SensitiveDataMasker;

public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        try {
            byte[] rawBody = request.getEntity(byte[].class);
            if (rawBody != null) {
                // Put the body back into the input stream
                request.setEntityInputStream(new ByteArrayInputStream(rawBody));
            }
            String body = rawBody == null ? null : new String(rawBody);
            body = SensitiveDataMasker.mask(body);
            String headers = formatHeaders(request.getRequestHeaders());

            logger.info(
                    "incoming request:\n{} {}\npath: {}\nheaders: {}\nmedia type: {}\nbody: {}\n",
                    request.getMethod(),
                    request.getRequestUri(),
                    request.getPath(),
                    headers,
                    request.getMediaType(),
                    prettify(body));
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
                    "outgoing response:\nstatus: {}\n headers: {}\nmedia type: {}\nbody: {}",
                    response.getStatus(),
                    headers,
                    request.getMediaType(),
                    prettify(body));
        } catch (RuntimeException e) {
            logger.error("sth bad happened when logging incoming request", e);
        }
        return response;
    }

    private String prettify(String uglyString) {
        if (uglyString == null) {
            return null;
        }
        try {
            return GSON.toJson(new JsonParser().parse(uglyString));
        } catch (JsonParseException e) {
            return uglyString;
        }
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
