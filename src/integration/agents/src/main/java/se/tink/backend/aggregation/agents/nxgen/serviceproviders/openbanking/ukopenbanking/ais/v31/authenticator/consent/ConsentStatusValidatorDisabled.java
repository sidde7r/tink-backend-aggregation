package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ConsentStatusValidatorDisabled extends ConsentStatusValidator {

    public ConsentStatusValidatorDisabled(
            UkOpenBankingApiClient apiClient, PersistentStorage storage) {
        super(apiClient, storage);
    }

    @Override
    public void validate() {
        // Do not validate
    }
}
