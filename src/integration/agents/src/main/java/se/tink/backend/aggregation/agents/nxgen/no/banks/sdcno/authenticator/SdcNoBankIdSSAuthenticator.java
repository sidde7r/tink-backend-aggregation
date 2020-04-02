package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import java.util.Optional;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
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
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers.AuthenticationType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.BankIdMobilSSAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.selenium.WebDriverHelper;

public class SdcNoBankIdSSAuthenticator implements AutoAuthenticator, TypedAuthenticator {
    private final WebDriver driver;
    private final SdcNoConfiguration configuration;
    private final WebDriverHelper webDriverHelper;
    private final TinkHttpClient tinkHttpClient;

    public SdcNoBankIdSSAuthenticator(
            SdcNoConfiguration configuration, TinkHttpClient tinkHttpClient) {
        this.webDriverHelper = new WebDriverHelper();
        this.driver = webDriverHelper.constructPhantomJsWebDriver(WebScrapingConstants.USER_AGENT);
        this.configuration = configuration;
        this.tinkHttpClient = tinkHttpClient;
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

        driver.manage()
                .getCookies()
                .forEach(cookie -> tinkHttpClient.addCookie(toTinkCookie(cookie)));

        driver.close();
    }

    private BasicClientCookie toTinkCookie(final Cookie cookie) {

        BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        clientCookie.setDomain(cookie.getDomain());
        clientCookie.setExpiryDate(cookie.getExpiry());
        clientCookie.setPath(cookie.getPath());
        clientCookie.setSecure(cookie.isSecure());
        return clientCookie;
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
