package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class N26ConsentPersistentData {
    private final String consentId;

    public N26ConsentPersistentData() {
        consentId = null;
    }
}
