package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ImageChallengeData {
    private String method;
    private String uri;

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }
}
