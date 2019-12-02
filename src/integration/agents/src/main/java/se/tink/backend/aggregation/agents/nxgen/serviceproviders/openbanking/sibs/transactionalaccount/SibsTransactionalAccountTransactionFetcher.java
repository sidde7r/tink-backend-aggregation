package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SibsTransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final int DAYS_BACK_TO_FETCH_TRANSACTIONS_WHEN_CONSENT_OLD = 89;
    private static final String PAGINATION_DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(PAGINATION_DATE_FORMAT);
    private static final LocalDate TRANSACTIONS_FROM_BEGINNING = LocalDate.of(1970, 1, 1);

    private final SibsBaseApiClient apiClient;
    private final CredentialsRequest credentialsRequest;
    private final SibsUserState userState;

    private static final String ENCODED_SPACE = "%20";

    public SibsTransactionalAccountTransactionFetcher(
            SibsBaseApiClient apiClient,
            CredentialsRequest credentialsRequest,
            SibsUserState userState) {
        this.apiClient = apiClient;
        this.credentialsRequest = credentialsRequest;
        this.userState = userState;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        if (StringUtils.isNotEmpty(key)) {
            key = key.replaceAll(StringUtils.SPACE, ENCODED_SPACE);
        }
        return Optional.ofNullable(key)
                .map(apiClient::getTransactionsForKey)
                .orElseGet(
                        () ->
                                apiClient.getAccountTransactions(
                                        account, getTransactionsFetchBeginDate(account)));
    }

    LocalDate getTransactionsFetchBeginDate(final Account account) {
        Optional<LocalDate> accountCertainDate = getCertainDate(account);
        LocalDate updateDate = accountCertainDate.orElse(TRANSACTIONS_FROM_BEGINNING);
        if (isDateABigBang(updateDate) && userState.getConsent().isConsentOlderThan30Minutes()) {
            updateDate =
                    LocalDate.now().minusDays(DAYS_BACK_TO_FETCH_TRANSACTIONS_WHEN_CONSENT_OLD);
        }
        return updateDate;
    }

    private boolean isDateABigBang(LocalDate date) {
        return date.getYear() == TRANSACTIONS_FROM_BEGINNING.getYear();
    }

    private Optional<LocalDate> getCertainDate(Account account) {
        return credentialsRequest.getAccounts().stream()
                .filter(rpcAccount -> account.isUniqueIdentifierEqual(rpcAccount.getBankId()))
                .findAny()
                .map(a -> a.getCertainDate())
                .map(d -> new java.sql.Date(d.getTime()).toLocalDate());
    }
}
