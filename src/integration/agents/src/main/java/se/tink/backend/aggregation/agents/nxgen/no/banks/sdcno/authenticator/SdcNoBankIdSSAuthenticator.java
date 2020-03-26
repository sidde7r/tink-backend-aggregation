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
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.PortalBankIdMobilInitializer;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers.AuthenticationType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.BankIdMobilSSAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.BankIdMobilInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.libraries.selenium.WebDriverHelper;

public class SdcNoBankIdSSAuthenticator implements AutoAuthenticator, TypedAuthenticator {
    private final WebDriver driver;
    private final SdcNoConfiguration configuration;
    private final WebDriverHelper webDriverHelper;

    public SdcNoBankIdSSAuthenticator(SdcNoConfiguration configuration) {
        this.webDriverHelper = new WebDriverHelper();
        this.driver = webDriverHelper.constructPhantomJsWebDriver(WebScrapingConstants.USER_AGENT);
        this.configuration = configuration;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        AuthenticationType authenticationType = configuration.getAuthenticationType();
        Key credentialsAdditionalKey = authenticationType.getCredentialsAdditionalKey();

        String mobileNumber = credentials.getField(Key.MOBILENUMBER);
        String idNumber = credentials.getField(credentialsAdditionalKey);

        MobilInitializer mobilInitializer =
                constructMobilInitializer(mobileNumber, idNumber, authenticationType);

        BankIdMobilSSAuthenticationController controller =
                new BankIdMobilSSAuthenticationController(
                        mobilInitializer, driver, webDriverHelper);

        driver.get(configuration.getBaseUrl());

        controller.doLogin();

        driver.close();
    }

    private MobilInitializer constructMobilInitializer(
            String mobileNummer, String idNumber, AuthenticationType authenticationType) {
        if ((AuthenticationType.NETTBANK).equals(authenticationType)) {
            return new BankIdMobilInitializer(mobileNummer, idNumber, driver, webDriverHelper);
        }
        if ((AuthenticationType.PORTAL).equals(authenticationType)) {
            return new PortalBankIdMobilInitializer(
                    mobileNummer, idNumber, driver, webDriverHelper);
        }

        throw new IllegalArgumentException(
                String.format("Unsupported bank id mobil Initializer for  %s", authenticationType));
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
