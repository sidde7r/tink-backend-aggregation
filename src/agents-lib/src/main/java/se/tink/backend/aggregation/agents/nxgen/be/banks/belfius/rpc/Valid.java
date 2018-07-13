package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Valid {

    private String valid;

    public boolean isValid() {
        return valid == null || Boolean.valueOf(valid);
    }
}
