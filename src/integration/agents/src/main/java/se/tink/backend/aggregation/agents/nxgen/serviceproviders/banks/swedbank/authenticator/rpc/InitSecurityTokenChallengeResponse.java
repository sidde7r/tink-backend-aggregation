package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitSecurityTokenChallengeResponse {
    private String challenge;
    private LinksEntity links;
    private boolean useOneTimePassword;

    public LinksEntity getLinks() {
        return links;
    }
}
