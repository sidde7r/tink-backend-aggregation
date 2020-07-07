package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class NordeaNoAuthenticator extends StatelessProgressiveAuthenticator {

    private NordeaNoStorage storage;
    private List<AuthenticationStep> mySteps;

    public NordeaNoAuthenticator(
            AuthenticationClient authenticationClient,
            NordeaNoStorage storage,
            RandomValueGenerator randomValueGenerator) {
        this.storage = storage;

        mySteps =
                ImmutableList.of(
                        new SetupSessionStep(authenticationClient, storage, randomValueGenerator),
                        new VerifySessionStep(authenticationClient, storage, randomValueGenerator));
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return mySteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return !storage.retrieveOauthToken().isPresent();
    }
}
