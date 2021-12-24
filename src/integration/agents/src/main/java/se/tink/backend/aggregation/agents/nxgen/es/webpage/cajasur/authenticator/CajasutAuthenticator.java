package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CredentialsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class CajasutAuthenticator extends StatelessProgressiveAuthenticator {

    private final CajasurAuthenticationApiClient cajasurApiClient;
    private final SessionStorage sessionStorage;

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return Lists.newArrayList(
                new CredentialsAuthenticationStep(
                        new CajasurAuthenticationRequestBodyPreparerProcessor(
                                cajasurApiClient, sessionStorage)),
                new AutomaticAuthenticationStep(
                        new CajasurDoLoginProcessor(cajasurApiClient, sessionStorage),
                        "doLoginStep"),
                new AutomaticAuthenticationStep(
                        new CajasurRedirectToMainPageProcessor(cajasurApiClient, sessionStorage),
                        "redirectToMainPageStep"));
    }
}
