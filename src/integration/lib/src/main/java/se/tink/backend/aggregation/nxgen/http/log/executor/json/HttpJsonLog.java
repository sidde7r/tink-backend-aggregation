package se.tink.backend.aggregation.nxgen.http.log.executor.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogExchangeEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogMetaEntity;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class HttpJsonLog {

    private final HttpJsonLogMetaEntity meta;

    @Singular("addHttp")
    private final List<HttpJsonLogExchangeEntity> http;
}
