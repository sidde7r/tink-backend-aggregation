package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.pair.Pair;

public interface ConsentController {
    enum ConsentStatus {
        RECEIVED,
        VALID,
        OTHER
    }

    boolean storedConsentIsValid();

    Pair<String, URL> requestConsent();

    ConsentStatus getConsentStatus(String consentId);

    void useConsentId(String consentId);
}
