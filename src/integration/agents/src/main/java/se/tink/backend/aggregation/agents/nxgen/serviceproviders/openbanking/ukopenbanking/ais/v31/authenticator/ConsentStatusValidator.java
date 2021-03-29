package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountPermissionsDataResponseEntity;

public class ConsentStatusValidator {

    private final UkOpenBankingApiClient apiClient;

    public ConsentStatusValidator(UkOpenBankingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public boolean isInvalid(String consentId) {
        return isInvalidWithRetry(consentId, 1);
    }

    public boolean isInvalidWithRetry(String consentId, int maxAttempts) {
        return awaitAuthorisation(consentId, maxAttempts).isNotAuthorised();
    }

    public AccountPermissionsDataResponseEntity awaitAuthorisation(
            String consentId, int maxAttempts) {

        int attemptsLeft = maxAttempts;
        AccountPermissionsDataResponseEntity consent;

        do {
            consent = apiClient.fetchIntentDetails(consentId).getData();

            if (consent.isAuthorised()) {
                break;
            }

            attemptsLeft--;

        } while (consent.isAwaitingAuthorisation() && attemptsLeft > 0);

        return consent;
    }
}
