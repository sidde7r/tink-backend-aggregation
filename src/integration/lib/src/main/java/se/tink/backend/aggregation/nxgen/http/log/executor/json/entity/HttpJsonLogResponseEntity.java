package se.tink.backend.aggregation.nxgen.http.log.executor.json.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateTimeSerializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@EqualsAndHashCode
@JsonObject
@Builder
public class HttpJsonLogResponseEntity {

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime timestamp;

    private final int status;
    private final String body;
    private final Map<String, String> headers;
}
