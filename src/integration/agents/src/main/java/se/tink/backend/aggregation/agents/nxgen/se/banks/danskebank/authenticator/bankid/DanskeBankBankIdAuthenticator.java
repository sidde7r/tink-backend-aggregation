package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.InitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.PollResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankJavascriptStringFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.DanskeBankAbstractAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.agents.rpc.Credentials;

import java.util.Objects;
import java.util.Optional;

public class DanskeBankBankIdAuthenticator extends DanskeBankAbstractAuthenticator implements BankIdAuthenticator<String> {
    private static final AggregationLogger log = new AggregationLogger(DanskeBankBankIdAuthenticator.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DanskeBankSEApiClient apiClient;
    private final String deviceId;
    private final DanskeBankSEConfiguration configuration;
    private String dynamicBankIdJavascript;
    private String finalizePackage;

    public DanskeBankBankIdAuthenticator(DanskeBankSEApiClient apiClient, String deviceId,
                                         DanskeBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
        this.configuration = (DanskeBankSEConfiguration) configuration;
    }

    @Override
    public String init(String ssn) throws BankIdException, AuthorizationException {
        // Get the dynamic logon javascript
        HttpResponse getResponse = apiClient.collectDynamicLogonJavascript(configuration.getSecuritySystem(),
                configuration.getBrand());

        // Add the authorization header from the response
        apiClient.addPersistentHeader("Authorization", getResponse.getHeaders().getFirst("Persistent-Auth"));

        // Add method to return device information string
        dynamicBankIdJavascript = DanskeBankConstants.Javascript.getDeviceInfo(deviceId, configuration.getMarketCode(),
                configuration.getAppName(), configuration.getAppVersion()) + getResponse.getBody(String.class);

        // Execute javascript to get encrypted logon package and finalize package
        WebDriver driver = null;
        try {
            driver = constructWebDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(DanskeBankJavascriptStringFormatter.createBankIdJavascript(dynamicBankIdJavascript, ssn));

            // Get encrypted logon package
            String logonPackage = driver.findElement(By.tagName("body")).getAttribute("logonPackage");

            // Initiate bankid logon
            InitResponse bankIdInitResponse = apiClient.initiateBankIdLogin(logonPackage);

            if (bankIdInitResponse.getStatus() != null && Objects.equals(
                    DanskeBankSEConfiguration.BankIdStatus.ALREADY_IN_PROGRESS, bankIdInitResponse.getStatus().toLowerCase())) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }

            // Set the finalize package which will be used during finalization of the login
            this.finalizePackage = driver.findElement(By.tagName("body")).getAttribute("finalizePackage");

            return bankIdInitResponse.getOrderReference();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

    }

    @Override
    public BankIdStatus collect(String reference) throws AuthenticationException, AuthorizationException {
        PollResponse pollResponse = apiClient.pollBankId(reference);

        if (pollResponse.getStatus() == null) {
            throw new IllegalStateException("Could not authenticate user - BankId poll returned null status");
        }

        switch (pollResponse.getStatus().toLowerCase()) {
            case DanskeBankSEConfiguration.BankIdStatus.OK:
                // Finalize authentication
                FinalizeAuthenticationResponse finalizeAuthenticationResponse = finalizeAuthentication();

                // TODO: In about three months, check the logging and remove this block after implementation
                if (finalizeAuthenticationResponse.getSessionStatus() != 200) {
                    try {
                        log.infoExtraLong(mapper.writeValueAsString(finalizeAuthenticationResponse),
                                DanskeBankConstants.LogTags.AUTHENTICATION_BANKID);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Could not serialize response", e);
                    }
                }

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
    protected FinalizeAuthenticationResponse finalizeAuthentication() {
        if (finalizePackage == null) {
            throw new IllegalStateException("Finalize Package was null, aborting login");
        }

        // Get encrypted finalize package
        return apiClient.finalizeAuthentication(
                FinalizeAuthenticationRequest.createForBankId(finalizePackage));

    }


    private String createJavascriptString(String ssn) {
        String javascriptString =
                "var e = %s;\n" +
                        "var nt = new Function(\"eval(\" + JSON.stringify(e) + \"); return {\\n                  init: (typeof init === 'function' ? init : function () {}),\\n                  initSeBankIdLogon: initSeBankIdLogon,\\n                  pollSeBankId: pollSeBankId,\\n                  cleanup: (typeof cleanup === 'function' ? cleanup : function () {})\\n                 };\")();\n" +
                        "function getLogonPackage(logonPackage, failMethod) {\n" +
                        "    document.body.setAttribute(\"logonPackage\", logonPackage);" +
                        "}\n" +
                        "function getFinalizePackage(finalizePackage, failMethod) {\n" +
                        "    document.body.setAttribute(\"finalizePackage\", finalizePackage)" +
                        "}\n" +
                        "function failMethod(arg1) {}\n" +
                        "nt.initSeBankIdLogon(\"%s\", getLogonPackage, failMethod);\n" +
                        "nt.pollSeBankId(\"OK\", getFinalizePackage, failMethod)";

        return String.format(javascriptString, dynamicBankIdJavascript, ssn);
    }
}
