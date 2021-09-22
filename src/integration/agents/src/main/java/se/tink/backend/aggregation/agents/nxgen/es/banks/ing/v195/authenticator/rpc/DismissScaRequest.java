package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DismissScaRequest {
    private String personId;
    private int channel = 83;
    private String reason = "Rooted mobile";

    public static DismissScaRequest create(String personId) {
        DismissScaRequest dismissScaRequest = new DismissScaRequest();
        dismissScaRequest.personId = personId;
        return dismissScaRequest;
    }
}
