package se.tink.backend.aggregation.nxgen.http.log.executor.json;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.log.constants.HttpLoggingConstants;
import se.tink.backend.aggregation.nxgen.http.log.executor.LoggingExecutor;
import se.tink.backend.aggregation.nxgen.http.log.executor.RequestLogEntry;
import se.tink.backend.aggregation.nxgen.http.log.executor.ResponseLogEntry;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogRequestEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogResponseEntity;

@RequiredArgsConstructor
public class JsonHttpTrafficLoggingExecutor implements LoggingExecutor {

    private final JsonHttpTrafficLogger jsonHttpTrafficLogger;

    @Override
    public void log(RequestLogEntry entry) {
        HttpJsonLogRequestEntity requestEntity =
                HttpJsonLogRequestEntity.builder()
                        .timestamp(LocalDateTime.now())
                        .method(entry.getMethod())
                        .url(entry.getUrl())
                        .body(entry.getBody())
                        .headers(maskSensitiveHeaders(entry.getHeaders()))
                        .build();
        jsonHttpTrafficLogger.addRequestLog(requestEntity);
    }

    @Override
    public void log(ResponseLogEntry entry) {
        HttpJsonLogResponseEntity requestEntity =
                HttpJsonLogResponseEntity.builder()
                        .timestamp(LocalDateTime.now())
                        .status(entry.getStatus())
                        .body(entry.getBody())
                        .headers(maskSensitiveHeaders(entry.getHeaders()))
                        .build();
        jsonHttpTrafficLogger.addResponseLog(requestEntity);
    }

    private static Map<String, String> maskSensitiveHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .collect(
                        toMap(
                                Map.Entry::getKey,
                                entry -> censorHeaderValue(entry.getKey(), entry.getValue())));
    }

    private static String censorHeaderValue(String key, String value) {
        if (HttpLoggingConstants.NON_SENSITIVE_HEADER_FIELDS.contains(key.toLowerCase())) {
            return value;
        }

        // Do not output sensitive information in our logs
        return "***";
    }
}
