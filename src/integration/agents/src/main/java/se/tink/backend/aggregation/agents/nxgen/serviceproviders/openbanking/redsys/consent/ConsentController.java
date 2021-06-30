package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;

public interface ConsentController {

    String getConsentId();

    boolean requestConsent();

    ConsentStatus fetchConsentStatus(String consentId);

    void clearConsentStorage();
}
