package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.WebDriverWrapper;

@AllArgsConstructor
public class PostLoginProcessor implements CallbackProcessorEmpty {

    private final WebDriverWrapper webDriver;
    private final RuralviaApiClient apiClient;
    private final AgentTemporaryStorage agentTemporaryStorage;

    @Override
    public AuthenticationStepResponse process()
            throws AuthenticationException, AuthorizationException {
        apiClient.storeLoginCookies(webDriver.manage().getCookies());
        apiClient.setGlobalPositionHtml(webDriver.getPageSource());
        apiClient.setLogged(true);
        agentTemporaryStorage.remove(webDriver.getDriverId());
        return AuthenticationStepResponse.authenticationSucceeded();
    }
}
