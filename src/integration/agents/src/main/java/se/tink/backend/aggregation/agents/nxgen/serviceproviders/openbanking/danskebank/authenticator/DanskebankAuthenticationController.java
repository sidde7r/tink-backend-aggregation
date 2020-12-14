package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatedHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DanskebankAuthenticationController extends OpenIdAuthenticationController {

    private static final String AUTHORISED = "Authorised";

    private UkOpenBankingApiClient apiClient;

    public DanskebankAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            UkOpenBankingApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            RandomValueGenerator randomValueGenerator,
            OpenIdAuthenticationValidator openIdAuthenticationValidator) {
        super(
                persistentStorage,
                supplementalInformationHelper,
                apiClient,
                authenticator,
                credentials,
                strongAuthenticationState,
                callbackUri,
                randomValueGenerator,
                openIdAuthenticationValidator);

        this.apiClient = apiClient;
    }

    @Override
    public void autoAuthenticate() {
        super.autoAuthenticate();

        // Store account accessToken filter, as we need to use a different one for next check
        OpenIdAuthenticatedHttpFilter aisAuthFilter = apiClient.getAisAuthFilter();

        String consentId =
                persistentStorage
                        .get(
                                UkOpenBankingV31Constants.PersistentStorageKeys
                                        .AIS_ACCOUNT_CONSENT_ID,
                                String.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        // Generate new client_credentials token, needed to verify consent validity
        OAuth2Token clientCredentialsToken =
                apiClient.requestClientCredentials(authenticator.getClientCredentialScope());
        apiClient.instantiateAisAuthFilter(clientCredentialsToken);

        AccountPermissionResponse accountPermissionResponse =
                apiClient.fetchIntentDetails(consentId);

        if (!AUTHORISED.equalsIgnoreCase(accountPermissionResponse.getData().getStatus())) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        // Return proper filter with true access token
        apiClient.setAisAuthFilter(aisAuthFilter);
    }
}
