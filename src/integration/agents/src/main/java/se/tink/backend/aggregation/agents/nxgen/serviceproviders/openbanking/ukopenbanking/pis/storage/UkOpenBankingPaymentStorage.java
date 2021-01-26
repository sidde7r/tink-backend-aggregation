package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.SigningAlgorithm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.UkPisAuthToken;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class UkOpenBankingPaymentStorage {

    private static final String TOKEN_KEY = "TOKEN_KEY";
    private static final String PREFERRED_SIGNING_ALG = "PREFERRED_SIGNING_ALG";

    private final Storage storage = new Storage();

    public UkPisAuthToken getToken() {
        return storage.get(TOKEN_KEY, UkPisAuthToken.class)
                .orElseThrow(() -> new IllegalArgumentException("Token not found in the storage"));
    }

    public void storeToken(UkPisAuthToken token) {
        storage.put(TOKEN_KEY, token);
    }

    public boolean hasToken() {
        return storage.containsKey(TOKEN_KEY);
    }

    public SigningAlgorithm getPreferredSigningAlgorithm() {
        return storage.get(PREFERRED_SIGNING_ALG, SigningAlgorithm.class)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "WellKnown Configuration not found in the storage"));
    }

    public void storePreferredSigningAlgorithm(SigningAlgorithm signingAlgorithm) {
        if (!storage.containsKey(PREFERRED_SIGNING_ALG)) {
            storage.put(PREFERRED_SIGNING_ALG, signingAlgorithm);
        }
    }
}
