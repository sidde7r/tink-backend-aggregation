package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class ConsentStatusValidatorDisabled extends ConsentStatusValidator {

    public ConsentStatusValidatorDisabled(
            UkOpenBankingApiClient apiClient, PersistentStorage storage) {
        super(apiClient, storage);
    }

    @Override
    public void validate() {
        log.info("[CONSENT STATUS VALIDATOR] Disabled. No action");
    }
}
