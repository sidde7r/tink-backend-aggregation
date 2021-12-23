package se.tink.backend.aggregation.nxgen.http.log.executor.json.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateTimeSerializer;

@Getter
@EqualsAndHashCode
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class HttpJsonLogResponseEntity {

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime timestamp;

    private final int status;
    private final String body;
    private final Map<String, String> headers;
}
