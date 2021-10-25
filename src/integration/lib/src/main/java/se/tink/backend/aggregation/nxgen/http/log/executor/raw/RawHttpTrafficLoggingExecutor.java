package se.tink.backend.aggregation.nxgen.http.log.executor.raw;

import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.log.constants.HttpLoggingConstants;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;
import se.tink.backend.aggregation.nxgen.http.log.executor.RequestLogEntry;
import se.tink.backend.aggregation.nxgen.http.log.executor.ResponseLogEntry;
import se.tink.libraries.date.ThreadSafeDateFormat;

/**
 * This is a class responsible to log logs to output stream (s3 file) using masker It's a modified
 * version of {@link se.tink.backend.aggregation.agents.utils.jersey.LoggingFilter} It can be reused
 * by different adapters to preserve requests numeration
 */
@RequiredArgsConstructor
public class RawHttpTrafficLoggingExecutor implements LoggingExecutor {

    private static final String NOTIFICATION_PREFIX = "* ";
    private static final String REQUEST_PREFIX = "> ";
    private static final String RESPONSE_PREFIX = "< ";

    private final RawHttpTrafficLogger rawHttpTrafficLogger;
    private final LogMasker logMasker;
    private final LoggingMode loggingMode;

    private long requestId = 0;

    @Override
    public void log(RequestLogEntry entry) {
        ++this.requestId;
        logRequest(entry);
    }

    @Override
    public void log(ResponseLogEntry entry) {
        logResponse(entry);
    }

    private void logResponse(ResponseLogEntry response) {
        StringBuilder b = new StringBuilder();

        printResponseLine(b, requestId, response);
        printResponseHeaders(b, requestId, response.getHeaders());

        b.append(response.getBody());
        b.append("\n");

        rawHttpTrafficLogger.log(b.toString(), logMasker, loggingMode);
    }

    private void printResponseLine(StringBuilder b, long id, ResponseLogEntry response) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client in-bound response").append("\n");
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()))
                .append("\n");
        prefixId(b, id).append(RESPONSE_PREFIX).append(response.getStatus()).append("\n");
    }

    private void printResponseHeaders(StringBuilder b, long id, Map<String, String> headers) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            String header = e.getKey();
            String value = e.getValue();
            prefixId(b, id)
                    .append(RESPONSE_PREFIX)
                    .append(header)
                    .append(": ")
                    .append(censorHeaderValue(header, value))
                    .append("\n");
        }
        prefixId(b, id).append(RESPONSE_PREFIX).append("\n");
    }

    private void logRequest(RequestLogEntry request) {
        StringBuilder b = new StringBuilder();

        printRequestLine(b, requestId, request);
        printRequestHeaders(b, requestId, request.getHeaders());

        if (request.getBody() != null) {
            b.append(request.getBody());
            b.append("\n");
        }

        rawHttpTrafficLogger.log(b.toString(), logMasker, loggingMode);
    }

    private void printRequestLine(StringBuilder b, long id, RequestLogEntry request) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client out-bound request").append("\n");
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()))
                .append("\n");
        prefixId(b, id)
                .append(REQUEST_PREFIX)
                .append(request.getMethod())
                .append(" ")
                .append(request.getUrl())
                .append("\n");
    }

    private void printRequestHeaders(StringBuilder b, long id, Map<String, String> headers) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            String header = e.getKey();

            String value = e.getValue();

            prefixId(b, id)
                    .append(REQUEST_PREFIX)
                    .append(header)
                    .append(": ")
                    .append(censorHeaderValue(header, value))
                    .append("\n");
        }
    }

    private static String censorHeaderValue(String key, String value) {

        if (HttpLoggingConstants.NON_SENSITIVE_HEADER_FIELDS.contains(key.toLowerCase())) {
            return value;
        }

        // Do not output sensitive information in our logs
        return "***";
    }

    private static StringBuilder prefixId(StringBuilder b, long id) {
        b.append(id).append(" ");
        return b;
    }
}
