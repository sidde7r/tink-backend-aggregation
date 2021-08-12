package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.LinkDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class FiduciaTransactionalAccountFetcherTest {

    private static final String ACCOUNT_ID = "123";
    private static final String ACCOUNT_ID_2 = "456";
    private static final String IBAN = "DE11P03058016041005737885631";
    private static final String IBAN_2 = "DE22P03058016041005737885632";
    private static final String CURRENCY = "EUR";
    private static final String OWNER_NAME = "dummyOwnerName";
    private static final String SECOND_PAGE_PATH = "SomeAwesomePath";
    private static final String CREDITOR_NAME = "creditorName";
    private static final String DEBTOR_NAME = "debtorName";

    private FiduciaApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(FiduciaApiClient.class);

        when(apiClient.getAccounts()).thenReturn(getAccountsResponse());

        when(apiClient.getBalances(ACCOUNT_ID)).thenReturn(getBalancesResponse());
        when(apiClient.getBalances(ACCOUNT_ID_2)).thenReturn(getBalancesResponse());
        when(apiClient.getTransactions(any(TransactionalAccount.class)))
                .thenReturn(getTransactionsFirstPage());
        when(apiClient.getTransactions(SECOND_PAGE_PATH)).thenReturn(getTransactionsLastPage());
    }

    @Test
    public void fetchAccountsShouldReturnProperNumberOfAccounts() {
        // given
        FiduciaTransactionalAccountFetcher fetcher =
                new FiduciaTransactionalAccountFetcher(apiClient);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertEquals(2, accounts.size());
        verify(apiClient).getAccounts();
        verify(apiClient).getBalances(ACCOUNT_ID);
        verify(apiClient).getBalances(ACCOUNT_ID_2);
    }

    @Test
    public void getTransactionsForShouldReturnTransactions() {
        // given
        FiduciaTransactionalAccountFetcher fetcher =
                new FiduciaTransactionalAccountFetcher(apiClient);
        TransactionalAccount account = getTestAccount();

        // when
        GetTransactionsResponse transactions =
                (GetTransactionsResponse) fetcher.getTransactionsFor(account, null);

        // then
        assertEquals(1, transactions.getTinkTransactions().size());
        verify(apiClient).getTransactions(account);
    }

    @Test
    public void getTransactionsForShouldReturnTransactionsForSecondPage() {
        // given
        FiduciaTransactionalAccountFetcher fetcher =
                new FiduciaTransactionalAccountFetcher(apiClient);
        TransactionalAccount account = getTestAccount();

        // when
        GetTransactionsResponse transactions =
                (GetTransactionsResponse) fetcher.getTransactionsFor(account, SECOND_PAGE_PATH);

        // then
        assertEquals(1, transactions.getTinkTransactions().size());
        verify(apiClient).getTransactions(SECOND_PAGE_PATH);
    }

    private GetAccountsResponse getAccountsResponse() {
        return new GetAccountsResponse(
                Arrays.asList(
                        new AccountEntity(IBAN, ACCOUNT_ID, Collections.emptyList(), OWNER_NAME),
                        new AccountEntity(
                                IBAN_2, ACCOUNT_ID_2, Collections.emptyList(), OWNER_NAME)));
    }

    private GetBalancesResponse getBalancesResponse() {
        return new GetBalancesResponse(
                Collections.singletonList(
                        new BalanceEntity()
                                .setBalanceType("available")
                                .setBalanceAmount(
                                        new AmountEntity()
                                                .setCurrency(CURRENCY)
                                                .setAmount(BigDecimal.valueOf(10.0)))));
    }

    private GetTransactionsResponse getTransactionsFirstPage() {
        return new GetTransactionsResponse(
                new TransactionsEntity(
                        new LinksEntity(null, new LinkDetailsEntity(SECOND_PAGE_PATH)),
                        Collections.singletonList(
                                new TransactionEntity(
                                        new Date(),
                                        "to own account",
                                        new AmountEntity(CURRENCY, BigDecimal.valueOf(10.0)),
                                        CREDITOR_NAME,
                                        DEBTOR_NAME))));
    }

    private GetTransactionsResponse getTransactionsLastPage() {
        return new GetTransactionsResponse(
                new TransactionsEntity(
                        new LinksEntity(null, null),
                        Collections.singletonList(
                                new TransactionEntity(
                                        new Date(),
                                        "to own account",
                                        new AmountEntity(CURRENCY, BigDecimal.valueOf(10.0)),
                                        CREDITOR_NAME,
                                        DEBTOR_NAME))));
    }

    private TransactionalAccount getTestAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(new ExactCurrencyAmount(BigDecimal.TEN, CURRENCY)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(IBAN)
                                .withAccountNumber(IBAN)
                                .withAccountName(IBAN)
                                .addIdentifier(new IbanIdentifier(IBAN))
                                .build())
                .setApiIdentifier(ACCOUNT_ID)
                .build()
                .get();
    }
}
