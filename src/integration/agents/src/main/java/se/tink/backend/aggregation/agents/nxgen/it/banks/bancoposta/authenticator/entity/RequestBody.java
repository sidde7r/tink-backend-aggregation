package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class RequestBody {
    @JsonProperty("body")
    private Map<String, String> body;

    @JsonProperty("header")
    private Map<String, String> header;

    public RequestBody(Map<String, String> body) {
        this.header =
                ImmutableMap.<String, String>builder()
                        .put("clientId", UUID.randomUUID().toString())
                        .put("requestId", "")
                        .build();
        this.body = body;
    }
}
