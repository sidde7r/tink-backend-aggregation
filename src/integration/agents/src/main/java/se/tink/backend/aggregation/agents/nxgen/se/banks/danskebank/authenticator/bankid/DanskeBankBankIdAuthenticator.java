package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.InitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.PollResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.DanskeBankWebDriverHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class DanskeBankBankIdAuthenticator implements BankIdAuthenticator<String> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DanskeBankSEApiClient apiClient;
    private final String deviceId;
    private final DanskeBankSEConfiguration configuration;
    private String dynamicBankIdJavascript;
    private String finalizePackage;
    private final Credentials credentials;

    public DanskeBankBankIdAuthenticator(
            DanskeBankSEApiClient apiClient,
            String deviceId,
            DanskeBankConfiguration configuration,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
        this.configuration = (DanskeBankSEConfiguration) configuration;
        this.credentials = credentials;
    }

    @Override
    public String init(String ssn) throws BankIdException, AuthorizationException {
        // Get the dynamic logon javascript
        HttpResponse getResponse =
                apiClient.collectDynamicLogonJavascript(
                        configuration.getSecuritySystem(), configuration.getBrand());

        // Add the authorization header from the response
        final String persistentAuth =
                getResponse
                        .getHeaders()
                        .getFirst(DanskeBankConstants.DanskeRequestHeaders.PERSISTENT_AUTH);
        // Store tokens in sensitive payload, so it will be masked from logs
        credentials.setSensitivePayload(
                DanskeBankConstants.DanskeRequestHeaders.AUTHORIZATION, persistentAuth);

        apiClient.addPersistentHeader(
                DanskeBankConstants.DanskeRequestHeaders.AUTHORIZATION, persistentAuth);

        // Add method to return device information string
        dynamicBankIdJavascript =
                DanskeBankConstants.Javascript.getDeviceInfo(
                                deviceId,
                                configuration.getMarketCode(),
                                configuration.getAppName(),
                                configuration.getAppVersion())
                        + getResponse.getBody(String.class);

        // Execute javascript to get encrypted logon package and finalize package
        WebDriver driver = null;
        try {
            driver = DanskeBankWebDriverHelper.constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    DanskeBankJavascriptStringFormatter.createBankIdJavascript(
                            dynamicBankIdJavascript, ssn));

            // Get encrypted logon package
            String logonPackage =
                    driver.findElement(By.tagName("body")).getAttribute("logonPackage");

            // Initiate bankid logon
            InitResponse bankIdInitResponse = apiClient.initiateBankIdLogin(logonPackage);

            if (bankIdInitResponse.getStatus() != null
                    && Objects.equals(
                            DanskeBankSEConfiguration.BankIdStatus.ALREADY_IN_PROGRESS,
                            bankIdInitResponse.getStatus().toLowerCase())) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }

            // Set the finalize package which will be used during finalization of the login
            this.finalizePackage =
                    driver.findElement(By.tagName("body")).getAttribute("finalizePackage");

            return bankIdInitResponse.getOrderReference();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        PollResponse pollResponse = apiClient.pollBankId(reference);

        if (pollResponse.getStatus() == null) {
            throw new IllegalStateException(
                    "Could not authenticate user - BankId poll returned null status");
        }

        switch (pollResponse.getStatus().toLowerCase()) {
            case DanskeBankSEConfiguration.BankIdStatus.OK:
                finalizeAuthentication();
                return BankIdStatus.DONE;
            case DanskeBankSEConfiguration.BankIdStatus.OUTSTANDING_TRANSACTION:
            case DanskeBankSEConfiguration.BankIdStatus.USER_SIGN:
                return BankIdStatus.WAITING;
            case DanskeBankSEConfiguration.BankIdStatus.NO_CLIENT:
                return BankIdStatus.NO_CLIENT;
            case DanskeBankSEConfiguration.BankIdStatus.CANCELLED:
                return BankIdStatus.INTERRUPTED;
            case DanskeBankSEConfiguration.BankIdStatus.USER_CANCEL:
                return BankIdStatus.CANCELLED;
            default:
                throw new IllegalStateException(pollResponse.getStatus());
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }

    private FinalizeAuthenticationResponse finalizeAuthentication() throws LoginException {
        if (finalizePackage == null) {
            throw new IllegalStateException("Finalize Package was null, aborting login");
        }

        // Get encrypted finalize package
        return apiClient.finalizeAuthentication(
                FinalizeAuthenticationRequest.createForBankId(finalizePackage));
    }
}
