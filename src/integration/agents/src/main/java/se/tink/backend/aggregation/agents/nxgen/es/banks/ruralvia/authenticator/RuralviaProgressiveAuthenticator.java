package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import java.util.List;
import lombok.AllArgsConstructor;
import org.assertj.core.util.Lists;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.WebDriverWrapper;

@AllArgsConstructor
public class RuralviaProgressiveAuthenticator extends StatelessProgressiveAuthenticator {

    private WebDriverWrapper webDriver;
    private Credentials credentials;
    private RuralviaApiClient apiClient;
    private AgentTemporaryStorage agentTemporaryStorage;

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return Lists.newArrayList(
                new AutomaticAuthenticationStep(
                        new OpenAuthenticationPageProcessor(
                                webDriver, "https://www.grupocajarural.es/es"),
                        "openLoginPage"),
                new AutomaticAuthenticationStep(
                        new LoginProcessor(webDriver, credentials), "loginProcessingStep"),
                new AutomaticAuthenticationStep(
                        new BeforeMainPageAdvertisementPageProcessor(webDriver),
                        "beforeMainPageAdvertisementPageStep"),
                new AutomaticAuthenticationStep(
                        new LoginResultValidationProcessor(webDriver), "loginValidationStep"),
                new AutomaticAuthenticationStep(
                        new PostLoginProcessor(webDriver, apiClient, agentTemporaryStorage),
                        "postLoginStep"));
    }
}
