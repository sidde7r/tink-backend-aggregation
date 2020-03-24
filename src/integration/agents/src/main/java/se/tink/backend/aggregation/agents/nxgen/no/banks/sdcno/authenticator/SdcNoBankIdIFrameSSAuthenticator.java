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
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.PortalBankIframeInitializer;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers.AuthenticationType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.BankIdIframeSSAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.initializer.BankIdIframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.initializer.IframeInitializer;
import se.tink.libraries.selenium.WebDriverHelper;

public class SdcNoBankIdIFrameSSAuthenticator implements AutoAuthenticator, TypedAuthenticator {
    private final WebDriver driver;
    private final SdcNoConfiguration configuration;
    private final WebDriverHelper webDriverHelper;

    public SdcNoBankIdIFrameSSAuthenticator(SdcNoConfiguration configuration) {
        this.webDriverHelper = new WebDriverHelper();
        this.driver = webDriverHelper.constructPhantomJsWebDriver(WebScrapingConstants.USER_AGENT);
        this.configuration = configuration;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String username = credentials.getField(Key.USERNAME);
        String password = credentials.getField(Key.PASSWORD);
        IframeInitializer iframeInitializer = constructBankIdIframeInitializer(username);

        BankIdIframeSSAuthenticationController controller =
                new BankIdIframeSSAuthenticationController(
                        iframeInitializer, driver, webDriverHelper);

        driver.get(configuration.getBaseUrl());

        controller.doLogin(password);

        driver.close();
    }

    private IframeInitializer constructBankIdIframeInitializer(String username) {
        AuthenticationType authenticationType = configuration.getAuthenticationType();

        if ((AuthenticationType.NETTBANK).equals(authenticationType)) {
            return new BankIdIframeInitializer(username, driver, webDriverHelper);
        } else if ((AuthenticationType.PORTAL).equals(authenticationType)) {
            return new PortalBankIframeInitializer(username, driver, webDriverHelper);
        }
        throw new IllegalArgumentException(
                String.format("Unsupported Iframe Initializer for  %s", authenticationType));
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
