package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.siliconvalley.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.UkOpenBankingAisAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.entities.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SiliconValleyAisAuthenticationController
        extends UkOpenBankingAisAuthenticationController {
    private final UkOpenBankingApiClient apiClient;
    private final String strongAuthenticationState;
    private final String callbackUri;
    private final RandomValueGenerator randomValueGenerator;

    public SiliconValleyAisAuthenticationController(
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
                authenticationValidator,
                consentStatusValidator);

        this.apiClient = apiClient;
        this.strongAuthenticationState = strongAuthenticationState.getState();
        this.callbackUri = callbackUri;
        this.randomValueGenerator = randomValueGenerator;
    }

    // Prepare third party app payload containing authentication url
    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        String intentId = apiClient.createConsent();

        String nonce = randomValueGenerator.generateRandomHexEncoded(8);
        ClientInfo info = apiClient.getProviderConfiguration();
        SoftwareStatementAssertion ssa = apiClient.getSoftwareStatement();
        String redirectUrl = apiClient.getRedirectUrl();
        WellKnownResponse wellKnownConfig = apiClient.getWellKnownConfiguration();
        JwtSigner signer = apiClient.getSigner();

        URL authorizeUrl =
                apiClient
                        .buildAuthorizeUrl(
                                strongAuthenticationState, nonce, ClientMode.ACCOUNTS, callbackUri)
                        .queryParam(
                                OpenIdAuthenticatorConstants.Params.REQUEST,
                                AuthorizeRequest.create()
                                        .withAccountsScope()
                                        .withClientInfo(info)
                                        .withSoftwareStatement(ssa)
                                        .withRedirectUrl(redirectUrl)
                                        .withState(this.strongAuthenticationState)
                                        .withNonce(nonce)
                                        .withCallbackUri(this.callbackUri)
                                        .withWellKnownConfiguration(wellKnownConfig)
                                        .withIntentId(intentId)
                                        .build(signer));

        return getThirdPartyAppAuthenticationPayload(authorizeUrl);
    }
}
