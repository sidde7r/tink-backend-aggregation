package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private ConsentApprovalEntity consentApproval;

    public String getAuthorizationUrl() {
        return consentApproval.getUrl();
    }
}
