package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.AccountTransactionsRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public class RefreshScopeTransactionPaginationHelper extends TransactionPaginationHelper {

    private final RefreshScope refreshScope;

    /**
     * Returns the lower limit (inclusive) date for this account. It can be specified by refresh
     * scope on all transaction or specifically on account transactions
     */
    @Override
    public Optional<Date> getTransactionDateLimit(Account account) {
        if (refreshScope == null || refreshScope.getTransactions() == null) {
            log.info("No transactions date limit, returning empty optional");
            return Optional.empty();
        }

        Optional<Date> defaultLimit =
                Optional.ofNullable(refreshScope.getTransactions().getTransactionBookedDateGte())
                        .map(RefreshScopeTransactionPaginationHelper::localDateToDate);
        if (refreshScope.getTransactions().getAccounts() == null) {
            log.info("Returning all transactions date limit {}", defaultLimit);
            return defaultLimit;
        }

        Optional<AccountTransactionsRefreshScope> accountRefreshScope =
                getAccountTransactionsRefreshScope(account);

        if (accountRefreshScope.isPresent()
                && accountRefreshScope.get().getTransactionBookedDateGte() != null) {
            Date accountLimit =
                    localDateToDate(accountRefreshScope.get().getTransactionBookedDateGte());
            log.info("Returning account transactions date limit {}", accountLimit);
            return Optional.of(accountLimit);
        }

        log.info("Returning all transactions date limit {}", defaultLimit);
        return defaultLimit;
    }

    private Optional<AccountTransactionsRefreshScope> getAccountTransactionsRefreshScope(
            Account account) {
        if (refreshScope == null
                || refreshScope.getTransactions() == null
                || refreshScope.getTransactions().getAccounts() == null) {
            return Optional.empty();
        }
        return refreshScope.getTransactions().getAccounts().stream()
                .filter(
                        it ->
                                accountTransactionsRefreshScopeContainsAnyIdentifier(
                                        it, account.getIdentifiers()))
                .findAny();
    }

    private static boolean accountTransactionsRefreshScopeContainsAnyIdentifier(
            AccountTransactionsRefreshScope scope,
            Collection<AccountIdentifier> accountIdentifiers) {
        Set<AccountIdentifier> refreshScopeAccountIdentifiers =
                scope.getAccountIdentifiers().stream()
                        .map(AccountIdentifier::createOrThrow)
                        .collect(Collectors.toSet());
        return refreshScopeAccountIdentifiers.removeAll(accountIdentifiers);
    }

    private static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
