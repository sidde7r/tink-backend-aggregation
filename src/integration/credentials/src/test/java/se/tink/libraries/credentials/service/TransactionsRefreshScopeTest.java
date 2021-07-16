package se.tink.libraries.credentials.service;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;

public class TransactionsRefreshScopeTest {

    @Test
    public void testGetTransactionBookedDateGteForAccountIdentifiersFromAccountScope() {
        Optional<LocalDate> expectedDate = Optional.of(LocalDate.parse("2021-07-13"));
        Set<String> accountIdentifiers = Collections.singleton("tink://accountIdentifier");

        AccountTransactionsRefreshScope accountTransactionsRefreshScope =
                new AccountTransactionsRefreshScope();
        accountTransactionsRefreshScope.setAccountIdentifiers(accountIdentifiers);
        accountTransactionsRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-07-13"));

        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        transactionsRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-07-14"));
        transactionsRefreshScope.setAccounts(
                Collections.singleton(accountTransactionsRefreshScope));

        assertEquals(
                expectedDate,
                transactionsRefreshScope.getTransactionBookedDateGteForAccountIdentifiers(
                        accountIdentifiers.stream()
                                .map(AccountIdentifier::createOrThrow)
                                .collect(Collectors.toSet())));
    }

    @Test
    public void testGetTransactionBookedDateGteForAccountIdentifiersWhenAccountsIsNull() {
        Optional<LocalDate> expectedDate = Optional.of(LocalDate.parse("2021-07-14"));
        Set<String> accountIdentifiers = Collections.singleton("tink://accountIdentifier");

        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        transactionsRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-07-14"));

        assertEquals(
                expectedDate,
                transactionsRefreshScope.getTransactionBookedDateGteForAccountIdentifiers(
                        accountIdentifiers.stream()
                                .map(AccountIdentifier::createOrThrow)
                                .collect(Collectors.toSet())));
    }

    @Test
    public void testGetTransactionBookedDateGteForAccountIdentifiersWhenAccountsIsEmpty() {
        Optional<LocalDate> expectedDate = Optional.of(LocalDate.parse("2021-07-14"));
        Set<String> accountIdentifiers = Collections.singleton("tink://accountIdentifier");

        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        transactionsRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-07-14"));
        transactionsRefreshScope.setAccounts(Collections.emptySet());

        assertEquals(
                expectedDate,
                transactionsRefreshScope.getTransactionBookedDateGteForAccountIdentifiers(
                        accountIdentifiers.stream()
                                .map(AccountIdentifier::createOrThrow)
                                .collect(Collectors.toSet())));
    }

    @Test
    public void
            testGetTransactionBookedDateGteForAccountIdentifiersWhenAccountsDoNotContainSpecificIdentifier() {
        Optional<LocalDate> expectedDate = Optional.of(LocalDate.parse("2021-07-14"));
        Set<String> accountIdentifiers = Collections.singleton("tink://accountIdentifier");

        AccountTransactionsRefreshScope accountTransactionsRefreshScope =
                new AccountTransactionsRefreshScope();
        accountTransactionsRefreshScope.setAccountIdentifiers(
                Collections.singleton("tink://otherAccountIdentifier"));
        accountTransactionsRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-07-13"));

        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        transactionsRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-07-14"));
        transactionsRefreshScope.setAccounts(
                Collections.singleton(accountTransactionsRefreshScope));

        assertEquals(
                expectedDate,
                transactionsRefreshScope.getTransactionBookedDateGteForAccountIdentifiers(
                        accountIdentifiers.stream()
                                .map(AccountIdentifier::createOrThrow)
                                .collect(Collectors.toSet())));
    }
}
