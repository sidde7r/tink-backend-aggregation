package se.tink.backend.aggregation.nxgen.http.log.executor.json.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class HttpJsonLogExchangeEntity {

    private final HttpJsonLogRequestEntity request;
    private final HttpJsonLogResponseEntity response;
}
