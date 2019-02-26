package se.tink.backend.aggregation.agents.utils.jersey;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.AbstractClientRequestAdapter;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientRequestAdapter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import se.tink.libraries.date.ThreadSafeDateFormat;

import javax.ws.rs.core.MultivaluedMap;
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
import java.util.logging.Logger;

/**
 * This is a copy of the logging filter that exist in com.sun.jersey.api.client.filter except that
 * it fixes a bug with that the whole response isn't logged. Problem is that stream.read() in
 * logResponse() isn't guaranteed to return all bytes. A problem that this class fixes.
 *
 * <p>The whole response is always logged.
 */
public class LoggingFilter extends ClientFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    private static final String NOTIFICATION_PREFIX = "* ";

    private static final String REQUEST_PREFIX = "> ";

    private static final String RESPONSE_PREFIX = "< ";

    private static final ImmutableList<String> SENSITIVE_HEADERS =
            ImmutableList.of("cookie", "set-cookie", "authorization");

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
    private final Logger logger;

    private long _id = 0;

    // Max size that we log is 0,5MB
    private final int maxSize = 500 * 1024;

    /**
     * Create a logging filter logging the request and response to a default JDK logger, named as
     * the fully qualified class name of this class.
     */
    public LoggingFilter() {
        this(LOGGER);
    }

    /**
     * Create a logging filter logging the request and response to a JDK logger.
     *
     * @param logger the logger to log requests and responses.
     */
    public LoggingFilter(Logger logger) {
        this(logger, null);
    }

    /**
     * Create a logging filter logging the request and response to print stream.
     *
     * @param loggingStream the print stream to log requests and responses.
     */
    public LoggingFilter(PrintStream loggingStream) {
        this(null, loggingStream);
    }

    private LoggingFilter(Logger logger, PrintStream loggingStream) {
        this.loggingStream = loggingStream;
        this.logger = logger;
    }

    private void log(StringBuilder b) {
        if (logger != null) {
            logger.info(b.toString());
        } else {
            loggingStream.print(b);
        }
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
        b.append(Long.toString(id)).append(" ");
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

            String value = null;
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
                    .append(censorHeaderValue(header, value))
                    .append("\n");
        }
    }

    private void logResponse(long id, ClientResponse response) {
        StringBuilder b = new StringBuilder();

        printResponseLine(b, id, response);
        printResponseHeaders(b, id, response.getHeaders());

        InputStream stream = response.getEntityInputStream();
        try {

            if (!response.getEntityInputStream().markSupported()) {
                stream = new BufferedInputStream(stream);
                response.setEntityInputStream(stream);
            }

            stream.mark(Integer.MAX_VALUE);

            StringBuilderWriter sw = new StringBuilderWriter();
            InputStreamReader in = new InputStreamReader(stream, Charsets.UTF_8);
            long charsCopied = IOUtils.copyLarge(in, sw, 0, maxSize);
            if (charsCopied == maxSize) {
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
        prefixId(b, id)
                .append(RESPONSE_PREFIX)
                .append(Integer.toString(response.getStatus()))
                .append("\n");
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
                        .append(censorHeaderValue(header, value))
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
