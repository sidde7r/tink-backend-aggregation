package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Predicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
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

    public AmericanExpressV62PasswordAuthenticator(
            AmericanExpressV62ApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        LogonRequest logonRequest = new LogonRequest();
        logonRequest.setUsernameAndPassword(username, password);

        Optional.ofNullable(persistentStorage.get(AmericanExpressV62Constants.Tags.HARDWARE_ID))
                .orElseGet(
                        () ->
                                generateAndStoreUuidFromValue(
                                        AmericanExpressV62Constants.Tags.HARDWARE_ID, username));
        Optional.ofNullable(persistentStorage.get(AmericanExpressV62Constants.Tags.INSTALLATION_ID))
                .orElseGet(
                        () ->
                                generateAndStoreUuidFromValue(
                                        AmericanExpressV62Constants.Tags.INSTALLATION_ID,
                                        password));

        LogonResponse logonResponse = apiClient.logon(logonRequest);

        if (!logonResponse.isSuccess()) {
            String reportingCode = logonResponse.getStatus().getReportingCode().toUpperCase();

            switch (reportingCode) {
                case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_FIRST_ATTEMPT:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_SECOND_ATTEMPT:
                    throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();
                case AmericanExpressV62Constants.ReportingCode.LOGON_FAIL_ACCOUNT_BLOCKED:
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                case AmericanExpressV62Constants.ReportingCode.BANKSIDE_TEMPORARY_ERROR:
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                case AmericanExpressV62Constants.ReportingCode.UNSUPPORTED_MARKET:
                    // Using the message sent from Amex as user message as it will be in the local
                    // language and with
                    // reference to the relevant market.
                    throw LoginError.NOT_CUSTOMER.exception(
                            new LocalizableKey(logonResponse.getStatus().getMessage()));

                default:
                    LOGGER.error(
                            String.format(
                                    "%s: %s",
                                    reportingCode, logonResponse.getStatus().getMessage()));
                    throw new IllegalStateException("Logon failure");
            }
        }

        sessionStorage.put(
                AmericanExpressV62Constants.Tags.CUPCAKE,
                logonResponse.getLogonData().getCupcake());
        sessionStorage.put(
                AmericanExpressV62Constants.Tags.SESSION_ID,
                logonResponse.getLogonData().getAmexSession());

        List<CardEntity> cardList =
                logonResponse.getSummaryData().getCardList().stream()
                        .filter(AmericanExpressV62Predicates.cancelledCardsPredicate)
                        // Double filtering for backward compatibility
                        .filter(AmericanExpressV62Predicates.cancelledCardSummaryValuePredicate)
                        .collect(Collectors.toList());
        sessionStorage.put(AmericanExpressConstants.Tags.CARD_LIST, cardList);
    }

    private String generateAndStoreUuidFromValue(String tag, String value) {
        String generatedUuid = StringUtils.hashAsUUID(value);
        persistentStorage.put(tag, generatedUuid);
        return generatedUuid;
    }
}
