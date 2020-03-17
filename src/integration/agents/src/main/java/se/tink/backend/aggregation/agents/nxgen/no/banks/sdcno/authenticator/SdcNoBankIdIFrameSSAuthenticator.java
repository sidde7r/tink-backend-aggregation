package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import org.openqa.selenium.WebDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.BankIdIframeSSAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants;
import se.tink.libraries.selenium.WebDriverHelper;

public class SdcNoBankIdIFrameSSAuthenticator implements AutoAuthenticator, TypedAuthenticator {
    private final BankIdIframeSSAuthenticationController controller;
    private final WebDriver driver;
    private final String baseUrl;

    public SdcNoBankIdIFrameSSAuthenticator(String baseUrl) {
        WebDriverHelper webDriverHelper = new WebDriverHelper();
        this.driver = webDriverHelper.constructPhantomJsWebDriver(WebScrapingConstants.USER_AGENT);
        this.baseUrl = baseUrl;
        this.controller = new BankIdIframeSSAuthenticationController(webDriverHelper, driver);
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String username = credentials.getField(Key.USERNAME);
        String password = credentials.getField(Key.PASSWORD);

        driver.get(baseUrl);

        controller.doLogin(username, password);

        driver.close();
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
