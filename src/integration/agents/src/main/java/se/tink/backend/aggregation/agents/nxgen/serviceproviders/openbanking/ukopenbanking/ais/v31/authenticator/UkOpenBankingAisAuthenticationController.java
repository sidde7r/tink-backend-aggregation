package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class UkOpenBankingAisAuthenticationController extends OpenIdAuthenticationController {

    private final UkOpenBankingApiClient apiClient;
    private final ConsentStatusValidator consentStatusValidator;

    public UkOpenBankingAisAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            UkOpenBankingApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            RandomValueGenerator randomValueGenerator,
            OpenIdAuthenticationValidator authenticationValidator,
            ConsentStatusValidator consentStatusValidator) {
        super(
                persistentStorage,
                supplementalInformationHelper,
                apiClient,
                authenticator,
                credentials,
                strongAuthenticationState,
                callbackUri,
                randomValueGenerator,
                authenticationValidator);

        this.apiClient = apiClient;
        this.consentStatusValidator = consentStatusValidator;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token clientOAuth2Token = apiClient.requestClientCredentials(ClientMode.ACCOUNTS);
        if (!clientOAuth2Token.isValid()) {
            throw new IllegalArgumentException("Client access token is not valid.");
        }

        apiClient.instantiateAisAuthFilter(clientOAuth2Token);

        consentStatusValidator.validate();
        super.autoAuthenticate();
    }
}
