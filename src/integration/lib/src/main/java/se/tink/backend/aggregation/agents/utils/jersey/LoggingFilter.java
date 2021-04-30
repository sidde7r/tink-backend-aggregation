package se.tink.backend.aggregation.agents.utils.jersey;

import com.google.common.base.Charsets;
import com.sun.jersey.api.client.AbstractClientRequestAdapter;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientRequestAdapter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.log.constants.HttpLoggingConstants;
import se.tink.libraries.date.ThreadSafeDateFormat;

/**
 * This was originally a copy of the logging filter that exist in com.sun.jersey.api.client.filter
 * except that it fixes a bug with that the whole response isn't logged. The problem is that
 * stream.read() in logResponse() isn't guaranteed to return all bytes. A problem that this class
 * fixes.
 *
 * <p>The whole response is always logged.
 */
public class LoggingFilter extends ClientFilter {

    private static final String NOTIFICATION_PREFIX = "* ";

    private static final String REQUEST_PREFIX = "> ";

    private static final String RESPONSE_PREFIX = "< ";

    private final LogMasker logMasker;
    private final LoggingMode loggingMode;

    private final class Adapter extends AbstractClientRequestAdapter {
        private final StringBuilder b;

        Adapter(ClientRequestAdapter cra, StringBuilder b) {
            super(cra);
            this.b = b;
        }

        public OutputStream adapt(ClientRequest request, OutputStream out) throws IOException {
            return new LoggingOutputStream(getAdapter().adapt(request, out), b);
        }
    }

    private final class LoggingOutputStream extends OutputStream {
        private final OutputStream out;

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        private final StringBuilder b;

        LoggingOutputStream(OutputStream out, StringBuilder b) {
            this.out = out;
            this.b = b;
        }

        @Override
        public void write(byte[] b) throws IOException {
            baos.write(b);
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            baos.write(b, off, len);
            out.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            baos.write(b);
            out.write(b);
        }

        @Override
        public void close() throws IOException {
            printEntity(b, baos.toByteArray());
            log(b);
            out.close();
        }
    }

    private final PrintStream loggingStream;

    private long _id = 0;

    // Max size that we log is 0,5MB
    private static final int MAX_SIZE = 500 * 1024;
    private boolean censorSensitiveHeaders = true; // Default

    /**
     * Create a logging filter logging the request and response to print stream. Takes a logMasker
     * that masks sensitive values from logs, the loggingMode parameter should only be passed with
     * the value LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the logMasker handles
     * the sensitive values in the provider. use {@link LogMaskerImpl#shouldLog(Provider)} if you
     * can.
     *
     * @param loggingStream the print stream to log requests and responses.
     * @param logMasker Masks values from logs.
     * @param loggingMode determines if logs should be outputted at all.
     */
    public LoggingFilter(PrintStream loggingStream, LogMasker logMasker, LoggingMode loggingMode) {
        this.loggingStream = loggingStream;
        this.logMasker = logMasker;
        this.loggingMode = loggingMode;
    }

    public LoggingFilter(
            PrintStream loggingStream,
            LogMasker logMasker,
            boolean censorSensitiveHeaders,
            LoggingMode loggingMode) {
        this.loggingStream = loggingStream;
        this.censorSensitiveHeaders = censorSensitiveHeaders;
        this.logMasker = logMasker;
        this.loggingMode = loggingMode;
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

        // Do not output sensitive information in our logs
        return "***";
    }

    private static StringBuilder prefixId(StringBuilder b, long id) {
        b.append(id).append(" ");
        return b;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        long id = ++this._id;

        logRequest(id, request);

        ClientResponse response = getNext().handle(request);

        logResponse(id, response);

        return response;
    }

    private void logRequest(long id, ClientRequest request) {
        StringBuilder b = new StringBuilder();

        printRequestLine(b, id, request);
        printRequestHeaders(b, id, request.getHeaders());

        if (request.getEntity() != null) {
            request.setAdapter(new Adapter(request.getAdapter(), b));
        } else {
            log(b);
        }
    }

    private void printRequestLine(StringBuilder b, long id, ClientRequest request) {
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
                value = ClientRequest.getHeaderValue(val.get(0));
            } else {
                StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (Object o : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    sb.append(ClientRequest.getHeaderValue(o));
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

    private void logResponse(long id, ClientResponse response) {
        StringBuilder b = new StringBuilder();

        printResponseLine(b, id, response);
        printResponseHeaders(b, id, response.getHeaders());

        InputStream stream = response.getEntityInputStream();
        try (StringBuilderWriter sw = new StringBuilderWriter()) {

            if (!response.getEntityInputStream().markSupported()) {
                stream = new BufferedInputStream(stream);
                response.setEntityInputStream(stream);
            }

            stream.mark(Integer.MAX_VALUE);
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

    private void printResponseLine(StringBuilder b, long id, ClientResponse response) {
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
