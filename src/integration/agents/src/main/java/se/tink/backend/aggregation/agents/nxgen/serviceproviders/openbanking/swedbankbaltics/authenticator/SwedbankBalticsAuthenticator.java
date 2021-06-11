package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.CollectStatusStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.ExchangeCodeForTokenStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.InitStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.RefreshAccessTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SwedbankBalticsAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> authenticationSteps;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public SwedbankBalticsAuthenticator(
            SwedbankApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {

        this.authenticationSteps =
                Arrays.asList(
                        new CheckIfAccessTokenIsValidStep(persistentStorage),
                        new RefreshAccessTokenStep(apiClient, persistentStorage),
                        new InitStep(apiClient, persistentStorage, sessionStorage),
                        new CollectStatusStep(apiClient, persistentStorage, sessionStorage),
                        new ExchangeCodeForTokenStep(apiClient, persistentStorage, sessionStorage));
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }
    
    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }
}
