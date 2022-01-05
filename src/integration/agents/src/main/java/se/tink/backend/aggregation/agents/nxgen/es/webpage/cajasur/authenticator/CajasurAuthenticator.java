package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.CajasurLoginProcessor;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.CajasurLoginValidatorFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.LoginValidationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CredentialsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class CajasurAuthenticator extends StatelessProgressiveAuthenticator {

    private final CajasurAuthenticationApiClient cajasurApiClient;
    private final SessionStorage sessionStorage;

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return Lists.newArrayList(
                new CredentialsAuthenticationStep(
                        new CajasurLoginProcessor(cajasurApiClient, sessionStorage)),
                new LoginValidationStep<String>(
                        new CajasurLoginValidatorFactory(sessionStorage), this::loginResponse),
                new AutomaticAuthenticationStep(
                        new CajasurPostLoginProcessor(cajasurApiClient, sessionStorage),
                        "doPostLoginStep"),
                new AutomaticAuthenticationStep(
                        new CajasurRedirectToMainPageProcessor(cajasurApiClient, sessionStorage),
                        "redirectToMainPageStep"));
    }

    private String loginResponse() {
        return CajasurSessionState.getInstance(sessionStorage).getLoginResponse();
    }
}
