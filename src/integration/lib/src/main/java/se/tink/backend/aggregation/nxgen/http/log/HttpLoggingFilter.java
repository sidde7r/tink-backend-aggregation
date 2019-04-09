package se.tink.backend.aggregation.nxgen.http.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Functions;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.ReaderWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregation.agents.HttpLoggableExecutor;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.utils.MapValueMasker;
import se.tink.backend.aggregation.utils.MapValueMaskerImpl;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class HttpLoggingFilter extends ClientFilter {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final AggregationLogger log;
    private final String logTag;
    private final Iterable<StringMasker> stringMaskers;
    private final Class<?> agentClass;
    private final MapValueMasker headerMasker;
    private static final String LOG_FORMAT = "HTTP(%s) %s@{%d:%d}=%s";
    private long requestCount;
    private static final LogTag GENERIC_HTTP_LOGGER = LogTag.from("http_logging_filter");

    private static final Set<String> NON_SENSITIVE_HEADER_FIELDS =
            ImmutableSet.of(
                    "Accept",
                    "Accept-Charset",
                    "Accept-Datetime",
                    "Accept-Encoding",
                    "Accept-Language",
                    "Accept-Ranges",
                    "Access-Control-Allow-Origin",
                    "Age",
                    "Allow",
                    "Cache-Control",
                    "Connection",
                    "Content-Encoding",
                    "Content-Language",
                    "Content-Length",
                    "Content-Type",
                    "Date",
                    "Expires",
                    "Forwarded",
                    "If-Modified-Since",
                    "If-Unmodified-Since",
                    "Host",
                    "Language",
                    "Last-Modified",
                    "Pragma",
                    "Proxy-Connection",
                    "Referer",
                    "Server",
                    "Status",
                    "Transfer-Encoding",
                    "User-Agent",
                    "Vary",
                    "Via",
                    "X-Forwarded-For",
                    "X-Forwarded-Host",
                    "X-Powered-By");

    public HttpLoggingFilter(
            AggregationLogger log,
            String logTag,
            Iterable<StringMasker> stringMaskers,
            Class<? extends HttpLoggableExecutor> agentClass) {
        this.log = log;
        this.logTag = logTag;
        this.stringMaskers = stringMaskers;
        this.headerMasker = new MapValueMaskerImpl(Optional.of(NON_SENSITIVE_HEADER_FIELDS));
        this.agentClass = agentClass;
        this.requestCount = 0;
    }

    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
        requestCount++;

        log(requestCount, createRequestLogEntry(clientRequest));
        ClientResponse clientResponse = getNext().handle(clientRequest);
        log(requestCount, createResponseLogEntry(clientResponse));

        return clientResponse;
    }

    private void log(long requestCount, HttpLogEntry logEntry) {
        int lineNumber = 0;
        try {
            String logEntryJsonString = MAPPER.writeValueAsString(logEntry);
            Iterable<String> logLines = getLogLinesForEntity(logEntryJsonString);

            for (String logLine : logLines) {
                log.infoExtraLong(
                        String.format(
                                Locale.ENGLISH,
                                LOG_FORMAT,
                                logTag,
                                logEntry.getEntryType(),
                                requestCount,
                                lineNumber,
                                logLine),
                        GENERIC_HTTP_LOGGER);
                lineNumber++;
            }
        } catch (IOException exception) {
            log.error(
                    String.format(
                            Locale.ENGLISH,
                            LOG_FORMAT,
                            logTag,
                            logEntry.getEntryType(),
                            requestCount,
                            lineNumber,
                            exception.getMessage()),
                    exception);
        }
    }

    private Iterable<String> getLogLinesForEntity(String logEntryJsonString) {
        // Needing to split long entities up since the log server will truncate lines that are too
        // long
        // (I've seen one line being truncated after 8179 characters).
        Splitter fixedMaxLineLengthSplitter = Splitter.fixedLength(6144);
        return fixedMaxLineLengthSplitter.split(logEntryJsonString);
    }

    private HttpRequestLogEntry createRequestLogEntry(ClientRequest clientRequest) {
        HttpRequestLogEntry logEntry = new HttpRequestLogEntry();
        logEntry.setAgent(agentClass.getName());
        logEntry.setMaskedBody(getMaskedBody(clientRequest));
        logEntry.setMaskedHeader(getHeaderStringMasked(clientRequest));
        logEntry.setMaskedUri(getMaskedUrl(clientRequest));
        logEntry.setMethod(clientRequest.getMethod());
        logEntry.setTimestamp(ThreadSafeDateFormat.FORMATTER_LOGGING.format(new Date()));
        return logEntry;
    }

    private HttpResponseLogEntry createResponseLogEntry(ClientResponse clientResponse) {
        HttpResponseLogEntry logEntry = new HttpResponseLogEntry();
        logEntry.setAgent(agentClass.getName());
        logEntry.setMaskedHeader(getHeaderStringMasked(clientResponse));
        logEntry.setMaskedBody(getMaskedBody(clientResponse));
        logEntry.setMaskedLocation(getMaskedLocation(clientResponse));
        logEntry.setStatusCode(String.valueOf(clientResponse.getStatus()));
        logEntry.setStatusInfo(clientResponse.getStatusInfo().toString());
        logEntry.setTimestamp(ThreadSafeDateFormat.FORMATTER_LOGGING.format(new Date()));
        return logEntry;
    }

    private String getMaskedUrl(ClientRequest clientRequest) {
        if (clientRequest.getURI() != null) {
            return mask(clientRequest.getURI().toString());
        } else {
            return null;
        }
    }

    private String getMaskedLocation(ClientResponse clientResponse) {
        if (clientResponse.getLocation() != null) {
            return mask(clientResponse.getLocation().toString());
        } else {
            return null;
        }
    }

    private String getMaskedBody(ClientRequest clientRequest) {
        if (clientRequest.getEntity() != null) {
            try {
                String entity = MAPPER.writeValueAsString(clientRequest.getEntity());
                return mask(entity);
            } catch (IOException e) {
                return "Could not serialize request body to String";
            }
        } else {
            return null;
        }
    }

    private String getMaskedBody(ClientResponse clientResponse) {
        String entity = getEntityAsStringAndSetupNewInputStream(clientResponse);
        if (entity != null) {
            return mask(entity);
        } else {
            return null;
        }
    }

    private String getEntityAsStringAndSetupNewInputStream(ClientResponse response) {
        ByteArrayOutputStream entityInputCopy = new ByteArrayOutputStream();
        final StringBuilder entityStringBuilder = new StringBuilder();

        try (InputStream entityInputStream = response.getEntityInputStream()) {
            if (entityInputStream != null && entityInputStream.available() > 0) {
                ReaderWriter.writeTo(entityInputStream, entityInputCopy);

                byte[] requestEntity = entityInputCopy.toByteArray();
                appendEntityBytes(entityStringBuilder, requestEntity);

                response.setEntityInputStream(new ByteArrayInputStream(requestEntity));
            }
        } catch (IOException ex) {
            return "Could not serialize response entity to String";
        }

        return entityStringBuilder.toString();
    }

    private void appendEntityBytes(StringBuilder entityStringBuilder, byte[] entity) {
        if (entity == null || entity.length == 0) {
            return;
        }

        entityStringBuilder.append(new String(entity)).append("\n");
    }

    private String getHeaderStringMasked(ClientRequest clientRequest) {
        if (clientRequest.getHeaders() == null) {
            return null;
        }

        Map<String, List<String>> headerStrings = getRequestHeaderMap(clientRequest);

        try {
            String maskedHeaderJson =
                    MAPPER.writeValueAsString(headerMasker.copyAndMaskMultiValues(headerStrings));
            return mask(maskedHeaderJson);
        } catch (IOException e) {
            return "Could not serialize header map from request";
        }
    }

    private Map<String, List<String>> getRequestHeaderMap(ClientRequest clientRequest) {
        ImmutableMap<String, Map.Entry<String, List<Object>>> headerMap =
                FluentIterable.from(clientRequest.getHeaders().entrySet())
                        .uniqueIndex(Map.Entry::getKey);

        return Maps.transformValues(
                headerMap,
                entry ->
                        FluentIterable.from(entry.getValue())
                                .transform(Functions.toStringFunction())
                                .toList());
    }

    private String getHeaderStringMasked(ClientResponse clientResponse) {
        if (clientResponse.getHeaders() == null) {
            return null;
        }

        Map<String, List<String>> headerStrings = getResponseHeaderMap(clientResponse);

        try {
            String maskedHeaderJson =
                    MAPPER.writeValueAsString(headerMasker.copyAndMaskMultiValues(headerStrings));
            return mask(maskedHeaderJson);
        } catch (IOException e) {
            return "Could not serialize header map from response";
        }
    }

    private Map<String, List<String>> getResponseHeaderMap(ClientResponse clientResponse) {
        ImmutableMap<String, Map.Entry<String, List<String>>> headerMap =
                FluentIterable.from(clientResponse.getHeaders().entrySet())
                        .uniqueIndex(Map.Entry::getKey);

        return Maps.transformValues(headerMap, Map.Entry::getValue);
    }

    private String mask(String string) {
        if (string == null) {
            return null;
        }

        String masked = string;

        for (StringMasker masker : stringMaskers) {
            masked = masker.getMasked(masked);
        }

        return masked;
    }
}
