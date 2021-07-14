package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator;

import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.SignSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration.CitadeleMarketConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class CitadeleBaseAuthenticator extends StatelessProgressiveAuthenticator {

    private final PersistentStorage persistentStorage;
    private final List<AuthenticationStep> authSteps = new LinkedList<>();
    private final CitadeleConsentManager citadeleConsentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Credentials credentials;

    public CitadeleBaseAuthenticator(
            CitadeleBaseApiClient apiClient,
            PersistentStorage persistentStorage,
            CitadeleMarketConfiguration baseConfiguration,
            String providerMarket,
            StrongAuthenticationState strongAuthenticationState,
            Credentials credentials) {
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.citadeleConsentManager =
                new CitadeleConsentManager(
                        apiClient,
                        strongAuthenticationState,
                        providerMarket,
                        baseConfiguration,
                        persistentStorage);
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        if (authSteps.isEmpty()) {
            CitadeleThirdPartyAppRequestParamsProvider citadeleThirdPartyAppRequestParamsProvider =
                    new CitadeleThirdPartyAppRequestParamsProvider(
                            citadeleConsentManager,
                            strongAuthenticationState,
                            persistentStorage,
                            credentials);

            authSteps.add(
                    new ThirdPartyAppAuthenticationStep(
                            SignSteps.STEP_ID,
                            citadeleThirdPartyAppRequestParamsProvider,
                            citadeleThirdPartyAppRequestParamsProvider::processThirdPartyCallback));
        }
        return authSteps;
    }
}
