package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
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
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class UkOpenBankingAisAuthenticationController extends OpenIdAuthenticationController {

    private final UkOpenBankingApiClient apiClient;
    private final String strongAuthenticationState;
    private final String callbackUri;
    private final ConsentStatusValidator consentStatusValidator;
    private final RandomValueGenerator randomValueGenerator;

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
        this.strongAuthenticationState = strongAuthenticationState.getState();
        this.callbackUri = callbackUri;
        this.randomValueGenerator = randomValueGenerator;
        this.consentStatusValidator = consentStatusValidator;
    }

    // Prepare third party app payload containing authentication url
    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        String intentId = apiClient.fetchIntentIdString();

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
                                        .withMaxAge(OpenIdAuthenticatorConstants.MAX_AGE)
                                        .build(signer));

        return getThirdPartyAppAuthenticationPayload(authorizeUrl);
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

    private ThirdPartyAppAuthenticationPayload getThirdPartyAppAuthenticationPayload(
            URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        payload.setAndroid(getAndroidPayload(authorizeUrl));
        payload.setIos(getIosPayload(authorizeUrl));
        payload.setDesktop(getDesktopPayload(authorizeUrl));
        return payload;
    }

    private ThirdPartyAppAuthenticationPayload.Desktop getDesktopPayload(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload.Desktop desktop =
                new ThirdPartyAppAuthenticationPayload.Desktop();
        desktop.setUrl(authorizeUrl.get());
        return desktop;
    }

    private ThirdPartyAppAuthenticationPayload.Ios getIosPayload(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(authorizeUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());
        return iOsPayload;
    }

    private ThirdPartyAppAuthenticationPayload.Android getAndroidPayload(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(authorizeUrl.get());
        return androidPayload;
    }
}
