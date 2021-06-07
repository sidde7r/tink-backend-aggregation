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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.AllPsd2;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

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
                    new AllPsd2ConsentAuthenticationStep(
                            consentManager,
                            strongAuthenticationState,
                            userState,
                            AllPsd2.ALL_ACCOUNTS_WITH_OWNER_NAME));

            manualAuthenticationSteps.add(
                    new AllPsd2ConsentAuthenticationStep(
                            consentManager,
                            strongAuthenticationState,
                            userState,
                            AllPsd2.ALL_ACCOUNTS));

            manualAuthenticationSteps.add(
                    new AccountsConsentAuthenticationStep(
                            consentManager, strongAuthenticationState, userState));

            manualAuthenticationSteps.add(
                    new CbiThirdPartyAppAuthenticationStep(
                            userState,
                            ConsentType.ACCOUNT,
                            consentManager,
                            strongAuthenticationState));

            manualAuthenticationSteps.add(new AccountFetchingStep(apiClient, userState));

            manualAuthenticationSteps.add(
                    new TransactionsConsentAuthenticationStep(
                            consentManager, strongAuthenticationState, userState));

            manualAuthenticationSteps.add(
                    new CbiThirdPartyAppAuthenticationStep(
                            userState,
                            ConsentType.BALANCE_TRANSACTION,
                            consentManager,
                            strongAuthenticationState));

            manualAuthenticationSteps.add(
                    new CbiThirdPartyFinishAuthenticationStep(consentManager, userState));
        }

        return manualAuthenticationSteps;
    }

    private boolean isAutoAuthenticationPossible() {
        try {
            fetchToken();

            return !userState.isManualAuthenticationInProgress()
                    && consentManager.verifyIfConsentIsAccepted();
        } catch (SessionException | LoginException e) {
            return false;
        }
    }

    protected CbiGlobeConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private void fetchToken() {
        try {
            if (isTokenInStorageExpired()) {
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
    }

    private boolean isTokenInStorageExpired() {
        return !apiClient.isTokenValid();
    }

    private void getTokenAndPutIntoStorage() {
        GetTokenResponse getTokenResponse =
                apiClient.getToken(configuration.getClientId(), configuration.getClientSecret());
        OAuth2Token token = getTokenResponse.toTinkToken();
        userState.saveToken(token);
    }
}
