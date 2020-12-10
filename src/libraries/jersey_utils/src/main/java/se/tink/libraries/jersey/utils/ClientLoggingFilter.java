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
import se.tink.libraries.jersey.utils.masker.ClientSensitiveDataMasker;

@Slf4j
public class ClientLoggingFilter extends ClientFilter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public ClientResponse handle(ClientRequest request) {
        ClientResponse response = getNext() == null ? null : getNext().handle(request);
        logRequestAndResponse(request, response);
        return response;
    }

    private void logRequestAndResponse(ClientRequest request, ClientResponse response) {
        try {
            log.info(
                    new StringBuilder("OUTGOING REQUEST:\n")
                            .append(formatRequest(request))
                            .append("\n\n")
                            .append("INCOMING RESPONSE:\n")
                            .append(formatResponse(response))
                            .toString());
        } catch (RuntimeException e) {
            log.error("sth bad happened when logging outgoing traffic");
        }
    }

    private StringBuilder formatResponse(ClientResponse response) {
        if (response == null) {
            return new StringBuilder(" response is null");
        }
        StringBuilder sb = new StringBuilder();
        byte[] rawBody = response.getEntity(byte[].class);
        if (rawBody != null) {
            // Put the body back into the input stream
            response.setEntityInputStream(new ByteArrayInputStream(rawBody));
        }
        String body = rawBody == null ? null : new String(rawBody);
        body = ClientSensitiveDataMasker.mask(body);

        return sb.append("status: ")
                .append(response.getStatus())
                .append("\n")
                .append("headers:\n")
                .append(formatHeaders(response.getHeaders()))
                .append("\n")
                .append("body:\n")
                .append(prettify(body));
    }

    private StringBuilder formatRequest(ClientRequest request) {
        StringBuilder sb =
                new StringBuilder()
                        .append(request.getMethod())
                        .append(" ")
                        .append(request.getURI())
                        .append("\n")
                        .append("headers:\n")
                        .append(formatHeaders(request.getHeaders()))
                        .append("\n")
                        .append("body:\n");

        Object entity = request.getEntity();
        if (entity instanceof SafelyLoggable) {
            sb.append(((SafelyLoggable) entity).toSafeString());
        } else if (entity != null) {
            sb.append("the class: ")
                    .append(entity.getClass().getName())
                    .append(" doesnt implement ")
                    .append(SafelyLoggable.class.getName())
                    .append(
                            " interface and cannot be safely printed. If you want to see the body of that request please implement that interface");
        }
        return sb;
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

        return "\t"
                + headers.entrySet().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("\n\t"));
    }
}
