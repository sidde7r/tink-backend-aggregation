package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps;

import java.net.URI;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class N26ProcessStateData {
    private URI authorizationUri;
    private String codeVerifier;
    private short consentRetryCounter;

    public N26ProcessStateData() {
        this.consentRetryCounter = 0;
    }

    public N26ProcessStateData(URI authorizationUri, String codeVerifier) {
        this.authorizationUri = authorizationUri;
        this.codeVerifier = codeVerifier;
        consentRetryCounter = 0;
    }

    public void incrementConsentRetryCounter() {
        consentRetryCounter++;
    }
}
