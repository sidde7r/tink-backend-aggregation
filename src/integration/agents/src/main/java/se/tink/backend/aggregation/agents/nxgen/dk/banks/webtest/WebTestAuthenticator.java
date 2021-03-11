package se.tink.backend.aggregation.agents.nxgen.dk.banks.webtest;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
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
import se.tink.integration.webdriver.ChromeDriverInitializer;

@Slf4j
public class WebTestAuthenticator implements TypedAuthenticator, AutoAuthenticator {

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        WebDriver chromeDriver = ChromeDriverInitializer.constructChromeDriver();
        chromeDriver.get("https://tink.com/");
        // simple check if chrome driver did the job and scrape title of website
        log.info(chromeDriver.getTitle());
        ChromeDriverInitializer.quitChromeDriver(chromeDriver);
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
