package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private ConsentApprovalEntity consentApproval;

    public String getAuthorizationUrl() {
        return consentApproval.getUrl();
    }
}
