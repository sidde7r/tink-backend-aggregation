package se.tink.libraries.jersey.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientLoggingFilter extends ClientFilter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public ClientResponse handle(ClientRequest request) {
        logRequest(request);
        ClientResponse response = getNext() == null ? null : getNext().handle(request);
        logResponse(response);
        return response;
    }

    private void logResponse(ClientResponse response) {
        if (response == null) {
            return;
        }
        try {
            byte[] rawBody = response.getEntity(byte[].class);
            if (rawBody != null) {
                // Put the body back into the input stream
                response.setEntityInputStream(new ByteArrayInputStream(rawBody));
            }
            String body = rawBody == null ? null : new String(rawBody);
            log.info(
                    "incoming response:\nstatus: {}\nheaders:{}\nbody:\n{}",
                    response.getStatus(),
                    formatHeaders(response.getHeaders()),
                    prettify(body));
        } catch (RuntimeException e) {
            log.error("sth bad happened when logging incoming response", e);
        }
    }

    private void logRequest(ClientRequest request) {
        try {
            Object entity = request.getEntity();
            String body = null;
            if (entity instanceof SafelyLoggable) {
                body = ((SafelyLoggable) entity).toSafeString();
            } else if (entity != null) {
                body =
                        "the class: "
                                + entity.getClass().getName()
                                + " doesnt implement "
                                + SafelyLoggable.class.getName()
                                + " interface and cannot be safely printed. If you want to see the body of that request please implement that interface";
            }
            log.info(
                    "outgoing request:\n{} {}\nheaders:{}\nbody:\n{}",
                    request.getMethod(),
                    request.getURI(),
                    formatHeaders(request.getHeaders()),
                    body);
        } catch (RuntimeException e) {
            log.error("sth bad happened when logging outgoing request", e);
        }
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
