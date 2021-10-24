package se.tink.backend.aggregation.nxgen.http.log.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.nxgen.http.log.constants.HttpLoggingConstants;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLoggingExecutor;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogRequestEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogResponseEntity;

public class JsonHttpTrafficLoggingExecutorTest {

    private JsonHttpTrafficLogger jsonHttpTrafficLogger;
    private JsonHttpTrafficLoggingExecutor jsonLoggingExecutor;

    private static final List<String> NOT_SENSITIVE_HEADER_NAMES =
            new ArrayList<>(HttpLoggingConstants.NON_SENSITIVE_HEADER_FIELDS);

    @Before
    public void setup() {
        jsonHttpTrafficLogger = mock(JsonHttpTrafficLogger.class);
        jsonLoggingExecutor = new JsonHttpTrafficLoggingExecutor(jsonHttpTrafficLogger);
    }

    @Test
    public void
            should_correctly_map_common_request_model_to_json_request_entity_and_mask_sensitive_headers() {
        // given
        Map<String, String> originalHeaders = new HashMap<>();
        originalHeaders.put("sensitiveHeader1", "sensitiveHeaderValue1");
        originalHeaders.put("sensitiveHeader2", "sensitiveHeaderValue2");
        NOT_SENSITIVE_HEADER_NAMES.forEach(
                header -> originalHeaders.put(header, "not sensitive value of " + header));

        Map<String, String> expectedHeaders = new HashMap<>(originalHeaders);
        expectedHeaders.put("sensitiveHeader1", "***");
        expectedHeaders.put("sensitiveHeader2", "***");

        // when
        jsonLoggingExecutor.log(
                RequestLogEntry.builder()
                        .url("https://google.com/some/path?key1=value1")
                        .body("{\"field1\":\"value1\"}")
                        .method("GET")
                        .headers(originalHeaders)
                        .build());

        // then
        ArgumentCaptor<HttpJsonLogRequestEntity> jsonLogEntityCaptor =
                forClass(HttpJsonLogRequestEntity.class);
        verify(jsonHttpTrafficLogger).addRequestLog(jsonLogEntityCaptor.capture());
        verifyNoMoreInteractions(jsonHttpTrafficLogger);

        assertThat(jsonLogEntityCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(
                        HttpJsonLogRequestEntity.builder()
                                .url("https://google.com/some/path?key1=value1")
                                .body("{\"field1\":\"value1\"}")
                                .method("GET")
                                .headers(expectedHeaders)
                                .build());
    }

    @Test
    public void
            should_correctly_map_common_response_model_to_json_response_entity_and_mask_sensitive_headers() {
        // given
        Map<String, String> originalHeaders = new HashMap<>();
        originalHeaders.put("sensitiveHeader123", "sensitiveHeaderValue123");
        originalHeaders.put("sensitiveHeader1234", "sensitiveHeaderValue1234");
        NOT_SENSITIVE_HEADER_NAMES.forEach(
                header -> originalHeaders.put(header, "not sensitive value for " + header));

        Map<String, String> expectedHeaders = new HashMap<>(originalHeaders);
        expectedHeaders.put("sensitiveHeader123", "***");
        expectedHeaders.put("sensitiveHeader1234", "***");

        // when
        jsonLoggingExecutor.log(
                ResponseLogEntry.builder()
                        .status(404)
                        .body("{\"field11\":\"value11\"}")
                        .headers(originalHeaders)
                        .build());

        // then
        ArgumentCaptor<HttpJsonLogResponseEntity> jsonLogEntityCaptor =
                forClass(HttpJsonLogResponseEntity.class);
        verify(jsonHttpTrafficLogger).addResponseLog(jsonLogEntityCaptor.capture());
        verifyNoMoreInteractions(jsonHttpTrafficLogger);

        assertThat(jsonLogEntityCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(
                        HttpJsonLogResponseEntity.builder()
                                .status(404)
                                .body("{\"field11\":\"value11\"}")
                                .headers(expectedHeaders)
                                .build());
    }
}
