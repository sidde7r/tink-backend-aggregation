package se.tink.backend.aggregation.agents.utils.authentication.encap.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractResponse {
    private int code;
    private boolean outdated;

    public int getCode() {
        return code;
    }

    public boolean isOutdated() {
        return outdated;
    }
}
