package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CollectTicketResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CreateTicketRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CreateTicketResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Desktop;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.i18n.LocalizableKey;

public class DemobankAppToAppAuthenticator
        implements ThirdPartyAppAuthenticator<CreateTicketResponse>, AutoAuthenticator {

    private final DemobankApiClient apiClient;
    private final String username;
    private final String password;
    private final String callbackUri;
    private final String state;

    private CreateTicketResponse createTicketResponse = null;

    public DemobankAppToAppAuthenticator(
            DemobankApiClient apiClient,
            String username,
            String password,
            String callbackUri,
            String state) {
        this.apiClient = apiClient;
        this.username = username;
        this.password = password;
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

        String token =
                reference
                        .getDeeplinkUrl()
                        .substring(reference.getDeeplinkUrl().lastIndexOf('/') + 1);
        CollectTicketResponse response = this.apiClient.collectAppToApp(token);
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
            apiClient.setTokenToSession(
                    OAuth2Token.createBearer(response.getToken(), response.getToken(), 3600));
            return new ThirdPartyAppResponse<CreateTicketResponse>() {
                @Override
                public ThirdPartyAppStatus getStatus() {
                    return ThirdPartyAppStatus.DONE;
                }

                @Override
                public CreateTicketResponse getReference() {
                    reference.setToken(response.getToken());
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
        ios.setAppStoreUrl("https://appstore.tink.com");
        ios.setDeepLinkUrl(createTicketResponse.getDeeplinkUrl());
        payload.setIos(ios);

        Android android = new Android();
        android.setIntent("intent");
        android.setPackageName("packagename");
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

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {}
}
