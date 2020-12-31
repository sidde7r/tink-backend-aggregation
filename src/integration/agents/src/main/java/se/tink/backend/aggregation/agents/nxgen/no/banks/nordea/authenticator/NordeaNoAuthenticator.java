package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.libraries.i18n.Catalog;

@Slf4j
public class NordeaNoAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> mySteps;

    public NordeaNoAuthenticator(
            AuthenticationClient authenticationClient,
            NordeaNoStorage storage,
            RandomValueGenerator randomValueGenerator,
            SupplementalRequester supplementalRequester,
            Catalog catalog) {
        mySteps =
                ImmutableList.of(
                        new SetupSessionStep(
                                authenticationClient,
                                storage,
                                randomValueGenerator,
                                supplementalRequester,
                                catalog),
                        new VerifySessionStep(authenticationClient, storage, randomValueGenerator));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return mySteps;
    }
}
