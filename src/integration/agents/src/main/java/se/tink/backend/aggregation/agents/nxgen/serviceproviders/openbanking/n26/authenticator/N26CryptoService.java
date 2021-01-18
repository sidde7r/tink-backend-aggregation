package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.utils.CryptoUtils;

public class N26CryptoService {

    public String generateCodeVerifier() {
        return CryptoUtils.getCodeVerifier();
    }

    public String generateCodeChallenge(String codeVerifier) {
        return CryptoUtils.getCodeChallenge(codeVerifier);
    }
}
