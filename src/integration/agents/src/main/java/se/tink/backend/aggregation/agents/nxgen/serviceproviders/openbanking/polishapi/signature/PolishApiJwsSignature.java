package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.signature;

import java.util.TreeMap;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class PolishApiJwsSignature {
    private final String uri;
    private final TreeMap<String, String> headers;
    private final String body;
}
