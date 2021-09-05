package se.tink.backend.aggregation.nxgen.http.log.executor.json.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
@AllArgsConstructor
public class HttpJsonLogExchangeEntity {

    private final HttpJsonLogRequestEntity request;
    private final HttpJsonLogResponseEntity response;
}
