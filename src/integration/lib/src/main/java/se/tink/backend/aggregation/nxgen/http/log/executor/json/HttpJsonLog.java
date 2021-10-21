package se.tink.backend.aggregation.nxgen.http.log.executor.json;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogExchangeEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogMetaEntity;

@Getter
@JsonObject
@Builder
public class HttpJsonLog {

    private final HttpJsonLogMetaEntity meta;
    private final List<HttpJsonLogExchangeEntity> http;
}
