package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.CreateNewConsentStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.GetTokensStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.InitStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.RefreshAccessTokenStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.configuration.SebBalticsConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebBalticsDecoupledAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> authenticationSteps;

    public SebBalticsDecoupledAuthenticator(
            SebBalticsBaseApiClient apiClient,
            AgentConfiguration<SebBalticsConfiguration> agentConfiguration,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            Credentials credentials) {

        SebBalticsConfiguration configuration =
                agentConfiguration.getProviderSpecificConfiguration();

        this.authenticationSteps =
                Arrays.asList(
                        new CheckIfAccessTokenIsValidStep(persistentStorage),
                        new RefreshAccessTokenStep(apiClient, persistentStorage),
                        new InitStep(apiClient, sessionStorage, configuration),
                        new GetTokensStep(
                                apiClient, sessionStorage, persistentStorage, credentials),
                        new CreateNewConsentStep(apiClient, persistentStorage));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }
}
