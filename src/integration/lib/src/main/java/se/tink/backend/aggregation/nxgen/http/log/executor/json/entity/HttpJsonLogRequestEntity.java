package se.tink.backend.aggregation.nxgen.http.log.executor.json.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.serializers.LocalDateTimeSerializer;

@Getter
@EqualsAndHashCode
@JsonObject
@Builder
public class HttpJsonLogRequestEntity {

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime timestamp;

    private final String method;
    private final String url;
    private final String body;
    private final Map<String, String> headers;
}
