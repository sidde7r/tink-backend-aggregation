package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetConsentResponse {

    private List<String> authorisationIds;

    public List<String> getAuthorisationIds() {
        return authorisationIds;
    }
}
