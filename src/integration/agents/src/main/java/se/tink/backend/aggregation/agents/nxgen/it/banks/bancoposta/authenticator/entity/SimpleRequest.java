package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity;

import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SimpleRequest extends BaseRequest {
    private final Map<String, String> body = Collections.emptyMap();
}
