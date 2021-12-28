package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;

public interface ConsentController {

    String getConsentId();

    void requestConsent();

    ConsentStatus fetchConsentStatus();

    void clearConsentStorage();
}
