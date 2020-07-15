package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitSecurityTokenChallengeResponse {
    private String challenge;
    private LinkEntity imageChallenge;
    private LinksEntity links;
    private boolean useOneTimePassword;

    public String getChallenge() {
        return challenge;
    }

    public LinkEntity getImageChallenge() {
        return imageChallenge;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public boolean isUseOneTimePassword() {
        return useOneTimePassword;
    }
}
