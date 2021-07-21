package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator;

import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.SignSteps;
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

    public CitadeleBaseAuthenticator(
            CitadeleBaseApiClient apiClient,
            PersistentStorage persistentStorage,
            String locale,
            String market,
            StrongAuthenticationState strongAuthenticationState) {
        this.persistentStorage = persistentStorage;
        this.citadeleConsentManager =
                new CitadeleConsentManager(
                        apiClient, strongAuthenticationState, locale, market, persistentStorage);
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        if (authSteps.isEmpty()) {
            CitadeleThirdPartyAppRequestParamsProvider citadeleThirdPartyAppRequestParamsProvider =
                    new CitadeleThirdPartyAppRequestParamsProvider(
                            citadeleConsentManager, strongAuthenticationState);

            authSteps.add(
                    new ThirdPartyAppAuthenticationStep(
                            SignSteps.STEP_ID,
                            citadeleThirdPartyAppRequestParamsProvider,
                            citadeleThirdPartyAppRequestParamsProvider::processThirdPartyCallback));
        }
        return authSteps;
    }
}
