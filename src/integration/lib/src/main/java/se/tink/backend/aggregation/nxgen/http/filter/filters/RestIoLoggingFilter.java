package se.tink.backend.aggregation.nxgen.http.filter.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
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
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.log.HttpLoggingConstants;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
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
    private boolean censorSensitiveHeaders;

    public RestIoLoggingFilter(
            PrintStream loggingStream, LogMasker logMasker, LoggingMode loggingMode) {
        this(loggingStream, logMasker, true, loggingMode);
    }

    /**
     * Takes a logMasker that masks sensitive values from logs, the loggingMode parameter should *
     * only be passed with the value LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the
     * * logMasker handles the sensitive values in the provider. use {@link *
     * se.tink.backend.aggregation.log.LogMasker#shouldLog(Provider)} if you can.
     *
     * @param loggingStream
     * @param logMasker Masks values from logs.
     * @param censorSensitiveHeaders
     * @param loggingMode determines if logs should be outputted at all.
     */
    public RestIoLoggingFilter(
            PrintStream loggingStream,
            LogMasker logMasker,
            boolean censorSensitiveHeaders,
            LoggingMode loggingMode) {
        this.censorSensitiveHeaders = censorSensitiveHeaders;
        this.loggingStream = loggingStream;
        this.logMasker = logMasker;
        this.loggingMode = loggingMode;
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
        if (LoggingMode.LOGGING_MASKER_COVERS_SECRETS.equals(loggingMode)) {
            loggingStream.print(logMasker.mask(b.toString()));
        }
    }

    private static String censorHeaderValue(String key, String value) {
        // do not output sensitive information in our logs
        if (HttpLoggingConstants.NON_SENSITIVE_HEADER_FIELDS.contains(key.toLowerCase())) {
            return "*** MASKED ***";
        }
        return value;
    }

    private static StringBuilder prefixId(StringBuilder b, long id) {
        b.append(id).append(" ");
        return b;
    }

    private void logRequest(long id, HttpRequest request) {
        StringBuilder b = new StringBuilder();

        appendRequestLine(b, id, request);
        appendRequestHeaders(b, id, request.getHeaders());
        appendRequestBody(b, id, request.getBody());
        log(b);
    }

    private void appendRequestBody(StringBuilder b, long id, Object body) {
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

    private void appendRequestLine(StringBuilder b, long id, HttpRequest request) {
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
                    .append(censorSensitiveHeaders ? censorHeaderValue(header, value) : value)
                    .append("\n");
        }
    }

    private void logResponse(long id, HttpResponse response) {
        StringBuilder b = new StringBuilder();

        appendResponseLine(b, id, response);
        appendResponseHeaders(b, id, response.getHeaders());

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

    private void appendResponseLine(StringBuilder b, long id, HttpResponse response) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client in-bound response").append("\n");
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()))
                .append("\n");
        prefixId(b, id).append(RESPONSE_PREFIX).append(response.getStatus()).append("\n");
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
