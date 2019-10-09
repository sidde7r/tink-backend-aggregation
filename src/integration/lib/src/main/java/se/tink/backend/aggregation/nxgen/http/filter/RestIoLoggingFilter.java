package se.tink.backend.aggregation.nxgen.http.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientHandlerException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.libraries.date.ThreadSafeDateFormat;

@FilterOrder(category = FilterPhases.SEND, order = Integer.MIN_VALUE)
public class RestIoLoggingFilter extends Filter {

    private static final String NOTIFICATION_PREFIX = "* ";

    private static final String REQUEST_PREFIX = "> ";

    private static final String RESPONSE_PREFIX = "< ";

    private static final ImmutableList<String> SENSITIVE_HEADERS =
            ImmutableList.of("cookie", "set-cookie", "authorization");

    private final PrintStream loggingStream;

    private long _id = 0;

    // Max size that we log is 0,5MB
    private static final int MAX_SIZE = 500 * 1024;
    private boolean censorSensitiveHeaders = true; // Default

    public RestIoLoggingFilter(PrintStream loggingStream) {
        this(loggingStream, false);
    }

    public RestIoLoggingFilter(PrintStream loggingStream, boolean censorSensitiveHeaders) {
        this.censorSensitiveHeaders = censorSensitiveHeaders;
        this.loggingStream = loggingStream;
    }

    public void setCensorSensitiveHeaders(boolean censorSensitiveHeaders) {
        this.censorSensitiveHeaders = censorSensitiveHeaders;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        long id = ++this._id;

        logRequest(id, httpRequest);

        HttpResponse response = getNext().handle(httpRequest);

        logResponse(id, response);

        return response;
    }

    private void log(StringBuilder b) {
        loggingStream.print(b);
    }

    private static String censorHeaderValue(String key, String value) {
        // do not output sensitive information in our logs
        for (String sensitiveHeader : SENSITIVE_HEADERS) {
            // http header keys are case insensitive
            if (key.toLowerCase().equals(sensitiveHeader)) {
                return "***";
            }
        }
        return value;
    }

    private static StringBuilder prefixId(StringBuilder b, long id) {
        b.append(id).append(" ");
        return b;
    }

    private void logRequest(long id, HttpRequest request) {
        StringBuilder b = new StringBuilder();

        printRequestLine(b, id, request);
        printRequestHeaders(b, id, request.getHeaders());
        printRequestBody(b, id, request.getBody());
        log(b);
    }

    private void printRequestBody(StringBuilder b, long id, Object body) {
        if (body == null) {
            return;
        }

        if (body instanceof String) {
            b.append(body).append("\n");
            return;
        }

        ObjectMapper om = new ObjectMapper();
        try {
            b.append(om.writeValueAsString(body)).append("\n");
        } catch (JsonProcessingException e) {
            b.append("Error during body serialization '").append(body).append("'").append("\n");
        }
    }

    private void printRequestLine(StringBuilder b, long id, HttpRequest request) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client out-bound request").append("\n");
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()))
                .append("\n");
        prefixId(b, id)
                .append(REQUEST_PREFIX)
                .append(request.getMethod())
                .append(" ")
                .append(request.getURI().toASCIIString())
                .append("\n");
    }

    private void printRequestHeaders(
            StringBuilder b, long id, MultivaluedMap<String, Object> headers) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            List<Object> val = e.getValue();
            String header = e.getKey();

            String value;
            if (val.size() == 1) {
                value = val.get(0).toString();
            } else {
                StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (Object o : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    sb.append(o.toString());
                }
                value = sb.toString();
            }

            prefixId(b, id)
                    .append(REQUEST_PREFIX)
                    .append(header)
                    .append(": ")
                    .append(censorSensitiveHeaders ? censorHeaderValue(header, value) : value)
                    .append("\n");
        }
    }

    private void logResponse(long id, HttpResponse response) {
        StringBuilder b = new StringBuilder();

        printResponseLine(b, id, response);
        printResponseHeaders(b, id, response.getHeaders());

        InputStream stream = response.getBodyInputStream();
        try {

            if (!response.getBodyInputStream().markSupported()) {
                stream = new BufferedInputStream(stream);
                response.getInternalResponse().setEntityInputStream(stream);
            }

            stream.mark(Integer.MAX_VALUE);

            StringBuilderWriter sw = new StringBuilderWriter();
            InputStreamReader in = new InputStreamReader(stream, Charsets.UTF_8);
            long charsCopied = IOUtils.copyLarge(in, sw, 0, MAX_SIZE);
            if (charsCopied == MAX_SIZE) {
                sw.write(" ... more ...");
            }
            String content = sw.toString();

            b.append(content);

            b.append("\n");

            stream.reset();

        } catch (IOException ex) {
            throw new ClientHandlerException(ex);
        }
        log(b);
    }

    private void printResponseLine(StringBuilder b, long id, HttpResponse response) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client in-bound response").append("\n");
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()))
                .append("\n");
        prefixId(b, id).append(RESPONSE_PREFIX).append(response.getStatus()).append("\n");
    }

    private void printResponseHeaders(
            StringBuilder b, long id, MultivaluedMap<String, String> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String header = e.getKey();
            for (String value : e.getValue()) {
                prefixId(b, id)
                        .append(RESPONSE_PREFIX)
                        .append(header)
                        .append(": ")
                        .append(censorSensitiveHeaders ? censorHeaderValue(header, value) : value)
                        .append("\n");
            }
        }
        prefixId(b, id).append(RESPONSE_PREFIX).append("\n");
    }

    private void printEntity(StringBuilder b, byte[] entity) {
        if (entity.length == 0) {
            return;
        }
        b.append(new String(entity)).append("\n");
    }
}
