package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class ResultEntity {
    private boolean successful;
    private List<Object> failReasons;

    public boolean isSuccessful() {
        return successful;
    }

    public List<Object> getFailReasons() {
        return failReasons;
    }
}
