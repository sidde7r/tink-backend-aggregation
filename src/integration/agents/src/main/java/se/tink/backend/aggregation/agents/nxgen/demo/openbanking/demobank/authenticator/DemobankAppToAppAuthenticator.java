package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CollectTicketResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CreateTicketRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CreateTicketResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Desktop;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.i18n.LocalizableKey;

public class DemobankAppToAppAuthenticator
        implements ThirdPartyAppAuthenticator<CreateTicketResponse> {

    private final DemobankApiClient apiClient;
    private final String username;
    private final String password;
    private final String callbackUri;
    private final String state;
    private final Credentials credentials;

    private CreateTicketResponse createTicketResponse = null;

    public DemobankAppToAppAuthenticator(
            DemobankApiClient apiClient,
            Credentials credentials,
            String callbackUri,
            String state) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.username = credentials.getField("username");
        this.password = credentials.getField("password");
        this.callbackUri = callbackUri;
        this.state = state;
    }

    @Override
    public ThirdPartyAppResponse<CreateTicketResponse> init() {
        this.createTicketResponse =
                apiClient.initAppToApp(
                        new CreateTicketRequest(username, password, callbackUri, state));

        return new ThirdPartyAppResponse<CreateTicketResponse>() {
            @Override
            public ThirdPartyAppStatus getStatus() {
                return ThirdPartyAppStatus.WAITING;
            }

            @Override
            public CreateTicketResponse getReference() {
                return createTicketResponse;
            }
        };
    }

    @Override
    public ThirdPartyAppResponse<CreateTicketResponse> collect(CreateTicketResponse reference)
            throws AuthenticationException, AuthorizationException {
        CollectTicketResponse response = this.apiClient.collectAppToApp(reference.getTicket());
        if (response.getStatus().equals("PENDING")) {
            return new ThirdPartyAppResponse<CreateTicketResponse>() {
                @Override
                public ThirdPartyAppStatus getStatus() {
                    return ThirdPartyAppStatus.WAITING;
                }

                @Override
                public CreateTicketResponse getReference() {
                    return reference;
                }
            };
        } else if (response.getStatus().equals("REJECTED")) {
            return new ThirdPartyAppResponse<CreateTicketResponse>() {
                @Override
                public ThirdPartyAppStatus getStatus() {
                    return ThirdPartyAppStatus.AUTHENTICATION_ERROR;
                }

                @Override
                public CreateTicketResponse getReference() {
                    return reference;
                }
            };
        } else {
            OAuth2Token token = response.getOAuthToken();
            credentials.setSessionExpiryDate(
                    OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                            token,
                            DemobankConstants.DEFAULT_OB_TOKEN_LIFETIME,
                            DemobankConstants.DEFAULT_OB_TOKEN_LIFETIME_UNIT));
            apiClient.setTokenToStorage(token);
            return new ThirdPartyAppResponse<CreateTicketResponse>() {
                @Override
                public ThirdPartyAppStatus getStatus() {
                    return ThirdPartyAppStatus.DONE;
                }

                @Override
                public CreateTicketResponse getReference() {
                    return reference;
                }
            };
        }
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        Ios ios = new Ios();
        ios.setAppScheme("tink-demobank-auth");
        ios.setAppStoreUrl("https://demobank.production.global.tink.se/appstore");
        ios.setDeepLinkUrl(createTicketResponse.getDeeplinkUrl());
        payload.setIos(ios);

        Android android = new Android();
        android.setIntent(createTicketResponse.getDeeplinkUrl());
        android.setPackageName("com.tink.demobank.authenticator");
        payload.setAndroid(android);

        Desktop desktop = new Desktop();
        desktop.setUrl(createTicketResponse.getDeeplinkUrl());
        payload.setDesktop(desktop);

        payload.setDesktop(desktop);
        payload.setDownloadMessage("download message");
        payload.setDownloadTitle("Download title");
        payload.setUpgradeMessage("up message");
        payload.setUpgradeTitle("up title");

        return payload;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
