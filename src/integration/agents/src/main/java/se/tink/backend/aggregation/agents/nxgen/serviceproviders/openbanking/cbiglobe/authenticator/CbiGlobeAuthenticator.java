package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CbiGlobeAuthenticator extends StatelessProgressiveAuthenticator {

    protected final CbiGlobeApiClient apiClient;
    protected final List<AuthenticationStep> manualAuthenticationSteps = new LinkedList<>();
    protected final StrongAuthenticationState strongAuthenticationState;
    protected final CbiUserState userState;
    protected final ConsentManager consentManager;
    private final CbiGlobeConfiguration configuration;

    public CbiGlobeAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            CbiGlobeConfiguration configuration) {
        this.apiClient = apiClient;
        this.strongAuthenticationState = strongAuthenticationState;
        this.userState = userState;
        this.consentManager = new ConsentManager(apiClient, userState);
        this.configuration = configuration;
    }

    CbiGlobeAuthenticator(
            CbiGlobeApiClient apiClient,
            StrongAuthenticationState strongAuthenticationState,
            CbiUserState userState,
            ConsentManager consentManager,
            CbiGlobeConfiguration configuration) {
        this.apiClient = apiClient;
        this.strongAuthenticationState = strongAuthenticationState;
        this.userState = userState;
        this.consentManager = consentManager;
        this.configuration = configuration;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        if (isAutoAuthenticationPossible()) {
            return Collections.emptyList();
        }

        return getManualAuthenticationSteps();
    }

    protected List<AuthenticationStep> getManualAuthenticationSteps() {
        if (manualAuthenticationSteps.isEmpty()) {
            manualAuthenticationSteps.add(
                    new CbiThirdPartyAppAuthenticationStep(
                            new AccountsConsentRequestParamsProvider(
                                    this, consentManager, strongAuthenticationState),
                            ConsentType.ACCOUNT,
                            consentManager,
                            userState,
                            strongAuthenticationState));

            manualAuthenticationSteps.add(new AccountFetchingStep(apiClient, userState));

            manualAuthenticationSteps.add(
                    new CbiThirdPartyAppAuthenticationStep(
                            new TransactionsConsentRequestParamsProvider(
                                    this, consentManager, strongAuthenticationState),
                            ConsentType.BALANCE_TRANSACTION,
                            consentManager,
                            userState,
                            strongAuthenticationState));
        }

        return manualAuthenticationSteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return !isAutoAuthenticationPossible();
    }

    private boolean isAutoAuthenticationPossible() {
        try {
            return !userState.isManualAuthenticationInProgress()
                    && fetchToken()
                    && consentManager.verifyIfConsentIsAccepted();
        } catch (SessionException | LoginException e) {
            return false;
        }
    }

    protected CbiGlobeConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public URL getScaUrl(ConsentResponse consentResponse) {
        String url = consentResponse.getLinks().getAuthorizeUrl().getHref();

        return new URL(CbiGlobeUtils.encodeBlankSpaces(url));
    }

    private boolean fetchToken() {
        try {
            if (!apiClient.isTokenValid()) {
                getTokenAndPutIntoStorage();
            }
        } catch (IllegalStateException e) {
            String message = e.getMessage();
            if (message.contains(MessageCodes.NO_ACCESS_TOKEN_IN_STORAGE.name())) {
                getTokenAndPutIntoStorage();
            } else {
                throw e;
            }
        }

        return true;
    }

    private void getTokenAndPutIntoStorage() {
        GetTokenResponse getTokenResponse =
                apiClient.getToken(configuration.getClientId(), configuration.getClientSecret());
        OAuth2Token token = getTokenResponse.toTinkToken();
        userState.saveToken(token);
    }
}
