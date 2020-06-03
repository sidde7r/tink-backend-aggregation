package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class LinksEntity {
    private ConsentApprovalEntity consentApproval;

    public String getAuthorizationUrl() {
        return consentApproval.getUrl();
    }
}
