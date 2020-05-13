package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Tags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Predicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.InitializationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils.AmericanExpressV62Storage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.strings.StringUtils;

public class AmericanExpressV62PasswordAuthenticator implements PasswordAuthenticator {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AmericanExpressV62PasswordAuthenticator.class);
    private final AmericanExpressV62ApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final AmericanExpressV62Storage instanceStorage;

    public AmericanExpressV62PasswordAuthenticator(
            AmericanExpressV62ApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            final AmericanExpressV62Storage instanceStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.instanceStorage = instanceStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        prepareStorageBeforeAuth(username);
        sendInitializationRequests();
        processLogon(prepareLogon(username, password));
    }

    private LogonRequest prepareLogon(String username, String password) {
        String rememberMeToken = persistentStorage.get(Tags.REMEMBER_ME_TOKEN);
        String maskedUserId = persistentStorage.get(Tags.MASKED_USER_ID);
        if (rememberMeToken != null && maskedUserId != null) {
            return new LogonRequest(maskedUserId, password, rememberMeToken);
        }

        return new LogonRequest(username, password);
    }

    private void processLogon(LogonRequest request)
            throws AuthenticationException, AuthorizationException {
        LogonResponse response = apiClient.logon(request);
        if (!response.isSuccess()) {
            sessionStorage.clear();
            persistentStorage.clear();
            handleErrorResponse(response);
        }
        prepareStorageAfterAuth(response);
    }

    private void handleErrorResponse(LogonResponse response)
            throws AuthenticationException, AuthorizationException {

        String reportingCode = response.getStatus().getReportingCode().toUpperCase();
        switch (reportingCode) {
            case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_FIRST_ATTEMPT:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_SECOND_ATTEMPT:
                throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();
            case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_ACCOUNT_BLOCKED:
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_CONTENT_ERROR:
                // This error might be fixed by updating headers
                LOGGER.error(
                        String.format("%s: %s", reportingCode, response.getStatus().getMessage()));
                throw new IllegalStateException("Login failure - check headers");
            case AmericanExpressV62Constants.ReportingCode.BANKSIDE_TEMPORARY_ERROR:
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        "Error code: "
                                + reportingCode
                                + ", error message: "
                                + response.getStatus().getMessage());
            case AmericanExpressV62Constants.ReportingCode.UNSUPPORTED_MARKET:
                // Using the message sent from Amex as user message as it will be in the local
                // language and with
                // reference to the relevant market.
                throw LoginError.NOT_CUSTOMER.exception(
                        new LocalizableKey(response.getStatus().getMessage()));

            default:
                LOGGER.error(
                        String.format("%s: %s", reportingCode, response.getStatus().getMessage()));
                throw new IllegalStateException("Logon failure");
        }
    }

    private void sendInitializationRequests() {
        apiClient.fetchSaneIdCookie();
        InitializationResponse response = apiClient.initialization();
        if (response.getInitialization().getStatus().equals(0)) {
            persistentStorage.put(Tags.INIT_VERSION, response.getInitialization().getVersion());
        }
    }

    private void prepareStorageBeforeAuth(String username) {
        persistentStorage.computeIfAbsent(
                AmericanExpressV62Constants.Tags.HARDWARE_ID,
                k -> persistentStorage.put(k, StringUtils.hashAsUUID(username)));
        persistentStorage.computeIfAbsent(
                AmericanExpressV62Constants.Tags.INSTALLATION_ID,
                k -> persistentStorage.put(k, generateUUID()));
        persistentStorage.computeIfAbsent(
                AmericanExpressV62Constants.Tags.PROCESS_ID,
                k -> persistentStorage.put(k, generateUUID()));
    }

    private void prepareStorageAfterAuth(LogonResponse response) {
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.CUPCAKE, response.getLogonData().getCupcake());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.SESSION_ID,
                response.getLogonData().getAmexSession());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.USER_DATA,
                response.getSummaryData().getUserData());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.GATEKEEPER,
                response.getLogonData().getGateKeeperCookie());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.AUTHORIZATION,
                createAuthorizationHeaderValue(response));

        persistentStorage.putIfAbsent(
                AmericanExpressV62Constants.Tags.MASKED_USER_ID,
                response.getLogonData().getProfileData().getMaskedUserId());
        persistentStorage.putIfAbsent(
                AmericanExpressV62Constants.Tags.REMEMBER_ME_TOKEN,
                response.getLogonData().getProfileData().getData());
        persistentStorage.putIfAbsent(
                AmericanExpressV62Constants.Tags.PUBLIC_GUID,
                response.getLogonData().getPublicGuid());

        instanceStorage.saveCreditCardList(getCardList(response));
    }

    private List<CardEntity> getCardList(LogonResponse response) {
        return response.getSummaryData().getCardList().stream()
                .filter(AmericanExpressV62Predicates.cancelledCardsPredicate)
                // Double filtering for backward compatibility
                .filter(AmericanExpressV62Predicates.cancelledCardSummaryValuePredicate)
                .collect(Collectors.toList());
    }

    private String generateUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    private String createAuthorizationHeaderValue(LogonResponse response) {
        String rawToken = response.getLogonData().getJsonWebToken().getRawToken();
        return "Bearer " + rawToken;
    }
}
