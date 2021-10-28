package se.tink.backend.aggregation.nxgen.http.filter.filters;

import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.SPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientHandlerException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.log.constants.HttpLoggingConstants;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.date.ThreadSafeDateFormat;

@FilterOrder(category = FilterPhases.SEND, order = Integer.MIN_VALUE)
public class RestIoLoggingFilter extends Filter {

    private static final String NOTIFICATION_PREFIX = "* ";
    private static final String REQUEST_PREFIX = "> ";
    private static final String RESPONSE_PREFIX = "< ";

    private final PrintStream loggingStream;
    private final LogMasker logMasker;
    private final LoggingMode loggingMode;

    private long _id = 0;

    // Max size that we log is 0,5MB
    private static final int MAX_SIZE = 500 * 1024;

    /**
     * Takes a logMasker that masks sensitive values from logs, the loggingMode parameter should *
     * only be passed with the value LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the
     * * logMasker handles the sensitive values in the provider. use {@link *
     * se.tink.backend.aggregation.logmasker.LogMasker#shouldLog(Provider)} if you can.
     *
     * @param loggingStream
     * @param logMasker Masks values from logs.
     * @param loggingMode determines if logs should be outputted at all.
     */
    public RestIoLoggingFilter(
            PrintStream loggingStream, LogMasker logMasker, LoggingMode loggingMode) {
        this.loggingStream = loggingStream;
        this.logMasker = logMasker;
        this.loggingMode = loggingMode;
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
        if (LoggingMode.LOGGING_MASKER_COVERS_SECRETS.equals(loggingMode)) {
            loggingStream.print(logMasker.mask(b.toString()));
        }
    }

    private static String censorHeaderValue(String key, String value) {
        if (HttpLoggingConstants.NON_SENSITIVE_HEADER_FIELDS.contains(key.toLowerCase())) {
            return value;
        }
        return "*** MASKED ***";
    }

    private static StringBuilder prefixId(StringBuilder b, long id) {
        b.append(id).append(" ");
        return b;
    }

    private void logRequest(long id, HttpRequest request) {
        StringBuilder b = new StringBuilder();
        appendRequestLine(b, id, request);
        appendRequestHeaders(b, id, request.getHeaders());
        appendRequestBody(b, request.getBody());
        log(b);
    }

    private void appendRequestBody(StringBuilder b, Object body) {
        if (body == null) {
            return;
        }

        if (body instanceof String) {
            b.append(body).append(LF);
            return;
        }

        ObjectMapper om = new ObjectMapper();
        try {
            b.append(om.writeValueAsString(body)).append(LF);
        } catch (JsonProcessingException e) {
            b.append("Error during body serialization '").append(body).append("'").append(LF);
        }
    }

    private void appendRequestLine(StringBuilder b, long id, HttpRequest request) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client out-bound request").append(LF);
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()))
                .append(LF);
        prefixId(b, id)
                .append(REQUEST_PREFIX)
                .append(request.getMethod())
                .append(SPACE)
                .append(request.getURI().toASCIIString())
                .append(LF);
    }

    private void appendRequestHeaders(
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
                    .append(censorHeaderValue(header, value))
                    .append(LF);
        }
    }

    private void logResponse(long id, HttpResponse response) {
        StringBuilder b = new StringBuilder();

        appendResponseLine(b, id, response);
        appendResponseHeaders(b, id, response.getHeaders());

        InputStream stream = response.getBodyInputStream();
        try (StringBuilderWriter sw = new StringBuilderWriter()) {

            if (!response.getBodyInputStream().markSupported()) {
                stream = new BufferedInputStream(stream);
                response.getInternalResponse().setEntityInputStream(stream);
            }

            stream.mark(Integer.MAX_VALUE);

            InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
            long charsCopied = IOUtils.copyLarge(in, sw, 0, MAX_SIZE);
            if (charsCopied == MAX_SIZE) {
                sw.write(" ... more ...");
            }
            String content = sw.toString();

            b.append(content);
            b.append(LF);
            stream.reset();

        } catch (IOException ex) {
            throw new ClientHandlerException(ex);
        }
        log(b);
    }

    private void appendResponseLine(StringBuilder b, long id, HttpResponse response) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client in-bound response").append(LF);
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()))
                .append(LF);
        prefixId(b, id).append(RESPONSE_PREFIX).append(response.getStatus()).append(LF);
    }

    private void appendResponseHeaders(
            StringBuilder b, long id, MultivaluedMap<String, String> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String header = e.getKey();
            for (String value : e.getValue()) {
                prefixId(b, id)
                        .append(RESPONSE_PREFIX)
                        .append(header)
                        .append(": ")
                        .append(censorHeaderValue(header, value))
                        .append(LF);
            }
        }
        prefixId(b, id).append(RESPONSE_PREFIX).append(LF);
    }
}
