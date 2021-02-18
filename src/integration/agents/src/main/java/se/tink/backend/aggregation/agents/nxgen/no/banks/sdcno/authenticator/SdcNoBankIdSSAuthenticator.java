package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import java.util.Optional;
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
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.AuthenticationType;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.BankIdMobilSSAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.integration.webdriver.WebDriverHelper;
import se.tink.integration.webdriver.WebDriverInitializer;
import se.tink.libraries.i18n.Catalog;

public class SdcNoBankIdSSAuthenticator implements AutoAuthenticator, TypedAuthenticator {
    private final WebDriver driver;
    private final SdcNoConfiguration configuration;
    private final WebDriverHelper webDriverHelper;
    private final PostAuthDriverProcessor postAuthDriverProcessor;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    public SdcNoBankIdSSAuthenticator(
            SdcNoConfiguration configuration,
            TinkHttpClient tinkHttpClient,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog) {
        this.webDriverHelper = new WebDriverHelper();
        this.driver = WebDriverInitializer.constructWebDriver(WebScrapingConstants.USER_AGENT);
        this.configuration = configuration;
        this.postAuthDriverProcessor =
                new PostAuthDriverProcessor(driver, webDriverHelper, tinkHttpClient, configuration);
        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;
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
                        webDriverHelper,
                        driver,
                        mobilInitializer,
                        supplementalInformationController,
                        catalog);

        driver.get(configuration.getLoginUrl());

        controller.doLogin();

        postAuthDriverProcessor.processLogonCasesAfterSuccessfulBankIdAuthentication();
        postAuthDriverProcessor.processWebDriver();

        driver.close();
    }

    private MobilInitializer constructMobilInitializer(
            String mobileNummer, String idNumber, AuthenticationType authenticationType) {

        return Optional.ofNullable(
                        authenticationType.getMobilBankIdInitializer(
                                mobileNummer, idNumber, driver, webDriverHelper))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        ("Unsupported bank id mobil Initializer for "
                                                + authenticationType)));
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
