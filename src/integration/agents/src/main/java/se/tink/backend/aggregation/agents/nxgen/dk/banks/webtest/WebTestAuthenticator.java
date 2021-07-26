package se.tink.backend.aggregation.agents.nxgen.dk.banks.webtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

@Slf4j
@RequiredArgsConstructor
public class WebTestAuthenticator implements TypedAuthenticator, AutoAuthenticator {

    private final AgentTemporaryStorage agentTemporaryStorage;

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        WebDriverWrapper chromeDriver =
                ChromeDriverInitializer.constructChromeDriver(agentTemporaryStorage);

        chromeDriver.get("https://tink.com/");
        // simple check if chrome driver did the job and scrape title of website
        log.info(chromeDriver.getTitle());

        agentTemporaryStorage.remove(chromeDriver.getDriverId());
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
