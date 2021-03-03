package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TestAccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.user.rpc.User;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RefreshControllersTest {
    @Mock private MetricRefreshController metricRefreshController;
    @Mock private UpdateController updateController;
    @Mock private AccountFetcher<TransactionalAccount> transactionalAccountFetcher;
    @Mock private AccountFetcher<CreditCardAccount> creditCardAccountFetcher;
    @Mock private AccountFetcher<InvestmentAccount> investmentFetcher;
    @Mock private AccountFetcher<LoanAccount> loanFetcher;
    @Mock private TransactionFetcher<TransactionalAccount> transactionFetcher;
    @Mock private TransactionFetcher<CreditCardAccount> creditCardTransactionFetcher;
    @Mock private EInvoiceFetcher eInvoiceFetcher;
    @Mock private TransferDestinationFetcher transferDestinationFetcher;
    @Mock private Provider provider;

    private List<Refresher> refreshers;

    private final List<TransactionalAccount> accounts =
            ImmutableList.<TransactionalAccount>builder()
                    .add(TestAccountBuilder.from(TransactionalAccount.class).build())
                    .build();
    private final List<CreditCardAccount> creditCards =
            ImmutableList.<CreditCardAccount>builder()
                    .add(TestAccountBuilder.from(CreditCardAccount.class).build())
                    .build();
    private final List<LoanAccount> loans =
            ImmutableList.<LoanAccount>builder()
                    .add(TestAccountBuilder.from(LoanAccount.class).build())
                    .build();
    private final List<InvestmentAccount> investments =
            ImmutableList.<InvestmentAccount>builder()
                    .add(TestAccountBuilder.from(InvestmentAccount.class).build())
                    .build();
    private final List<AggregationTransaction> transactions = Collections.emptyList();
    private final TransferDestinationsResponse transferDestinations =
            new TransferDestinationsResponse();
    private final List<Transfer> eInvoices = Collections.emptyList();
    private final User user = new User();

    private InOrder executionOrder;

    @Before
    public void setup() {
        Mockito.when(
                        metricRefreshController.buildAction(
                                Mockito.any(MetricId.class), Mockito.anyList()))
                .thenReturn(Mockito.mock(MetricRefreshAction.class));
        Mockito.when(transactionalAccountFetcher.fetchAccounts()).thenReturn(accounts);
        Mockito.when(creditCardAccountFetcher.fetchAccounts()).thenReturn(creditCards);
        Mockito.when(investmentFetcher.fetchAccounts()).thenReturn(investments);
        Mockito.when(loanFetcher.fetchAccounts()).thenReturn(loans);
        Mockito.when(transactionFetcher.fetchTransactionsFor(accounts.get(0)))
                .thenReturn(transactions);
        Mockito.when(creditCardTransactionFetcher.fetchTransactionsFor(creditCards.get(0)))
                .thenReturn(transactions);
        Mockito.when(
                        transferDestinationFetcher.fetchTransferDestinationsFor(
                                accounts.stream()
                                        .map(a -> a.toSystemAccount(user, provider))
                                        .collect(Collectors.toList())))
                .thenReturn(transferDestinations);
        Mockito.when(eInvoiceFetcher.fetchEInvoices()).thenReturn(eInvoices);

        executionOrder =
                Mockito.inOrder(
                        updateController,
                        transactionalAccountFetcher,
                        creditCardAccountFetcher,
                        investmentFetcher,
                        loanFetcher,
                        transactionFetcher,
                        creditCardTransactionFetcher,
                        transferDestinationFetcher,
                        eInvoiceFetcher);

        refreshers =
                ImmutableList.<Refresher>builder()
                        .add(
                                new TransactionalAccountRefreshController(
                                        metricRefreshController,
                                        updateController,
                                        transactionalAccountFetcher,
                                        transactionFetcher))
                        .add(
                                new CreditCardRefreshController(
                                        metricRefreshController,
                                        updateController,
                                        creditCardAccountFetcher,
                                        creditCardTransactionFetcher))
                        .add(
                                new InvestmentRefreshController(
                                        metricRefreshController,
                                        updateController,
                                        investmentFetcher))
                        .add(
                                new LoanRefreshController(
                                        metricRefreshController, updateController, loanFetcher))
                        .add(
                                new EInvoiceRefreshController(
                                        metricRefreshController, eInvoiceFetcher))
                        .add(
                                new TransferDestinationRefreshController(
                                        metricRefreshController, transferDestinationFetcher))
                        .build();
    }

    @Test
    public void ensureRefreshAccounts_fetchesAndUpdatesAccountsCreditCardsAndLoans() {
        getRefreshControllersOfType(AccountRefresher.class)
                .forEach(AccountRefresher::fetchAccounts);

        executionOrder.verify(transactionalAccountFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.times(accounts.size()))
                .updateAccount(Mockito.any(TransactionalAccount.class));

        executionOrder.verify(creditCardAccountFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.times(creditCards.size()))
                .updateAccount(Mockito.any(CreditCardAccount.class));

        executionOrder.verify(investmentFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.times(investments.size()))
                .updateAccount(Mockito.any(InvestmentAccount.class));

        executionOrder.verify(loanFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.times(loans.size()))
                .updateAccount(Mockito.any(LoanAccount.class));
    }

    @Test
    public void ensureNullAccountsCollection_isConvertedTo_emptyList() {
        Mockito.when(transactionalAccountFetcher.fetchAccounts()).thenReturn(null);

        getRefreshControllersOfType(AccountRefresher.class)
                .forEach(AccountRefresher::fetchAccounts);

        executionOrder.verify(transactionalAccountFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.never())
                .updateAccount(Mockito.any(TransactionalAccount.class));
    }

    @Test
    public void ensureNullCreditCardCollection_isConvertedTo_emptyList() {
        Mockito.when(creditCardAccountFetcher.fetchAccounts()).thenReturn(null);

        getRefreshControllersOfType(AccountRefresher.class)
                .forEach(AccountRefresher::fetchAccounts);

        executionOrder.verify(creditCardAccountFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.never())
                .updateAccount(Mockito.any(CreditCardAccount.class));
    }

    @Test
    public void ensureNullInvestmentCollection_isConvertedTo_emptyList() {
        Mockito.when(investmentFetcher.fetchAccounts()).thenReturn(null);

        getRefreshControllersOfType(AccountRefresher.class)
                .forEach(AccountRefresher::fetchAccounts);

        executionOrder.verify(investmentFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.never())
                .updateAccount(Mockito.any(InvestmentAccount.class));
    }

    @Test
    public void ensureNullLoanCollection_isConvertedTo_emptyMap() {
        Mockito.when(loanFetcher.fetchAccounts()).thenReturn(null);

        getRefreshControllersOfType(AccountRefresher.class)
                .forEach(AccountRefresher::fetchAccounts);

        executionOrder.verify(loanFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.never())
                .updateAccount(Mockito.any(LoanAccount.class));
    }

    @Test
    public void ensureNullTransactionsCollection_isConvertedTo_emptyList() {
        Mockito.when(transactionFetcher.fetchTransactionsFor(accounts.get(0))).thenReturn(null);

        getRefreshControllersOfType(TransactionRefresher.class)
                .forEach(TransactionRefresher::fetchTransactions);

        executionOrder.verify(transactionalAccountFetcher).fetchAccounts();
        executionOrder.verify(transactionFetcher).fetchTransactionsFor(accounts.get(0));
        executionOrder
                .verify(updateController, Mockito.never())
                .updateAccount(Mockito.any(TransactionalAccount.class));
    }

    @Test
    public void
            ensureRefreshTransactions_fetchesAccountsAndCreditCards_whenAccounts_haveNotAlreadyBeenFetched() {
        getRefreshControllersOfType(TransactionRefresher.class)
                .forEach(TransactionRefresher::fetchTransactions);
        executionOrder.verify(transactionalAccountFetcher).fetchAccounts();
        executionOrder.verify(transactionFetcher).fetchTransactionsFor(accounts.get(0));
        executionOrder.verify(updateController).updateTransactions(accounts.get(0), transactions);
        executionOrder.verify(creditCardAccountFetcher).fetchAccounts();
        executionOrder
                .verify(creditCardTransactionFetcher)
                .fetchTransactionsFor(creditCards.get(0));
        executionOrder
                .verify(updateController)
                .updateTransactions(creditCards.get(0), transactions);
    }

    @Test
    public void
            ensureRefreshTransactions_doesNotFetchAccountsAndCreditCards_whenAccountsHaveAlreadyBeenFetched() {
        getRefreshControllersOfType(AccountRefresher.class)
                .forEach(AccountRefresher::fetchAccounts);
        executionOrder.verify(transactionalAccountFetcher).fetchAccounts();
        accounts.forEach(a -> executionOrder.verify(updateController).updateAccount(a));

        getRefreshControllersOfType(TransactionRefresher.class)
                .forEach(TransactionRefresher::fetchTransactions);

        executionOrder.verify(transactionalAccountFetcher, Mockito.never()).fetchAccounts();
        executionOrder.verify(transactionFetcher).fetchTransactionsFor(accounts.get(0));
        executionOrder.verify(updateController).updateTransactions(accounts.get(0), transactions);
        executionOrder.verify(creditCardAccountFetcher, Mockito.never()).fetchAccounts();
        executionOrder
                .verify(creditCardTransactionFetcher)
                .fetchTransactionsFor(creditCards.get(0));
        executionOrder
                .verify(updateController)
                .updateTransactions(creditCards.get(0), transactions);
    }

    @Test
    public void
            ensureRefreshAccounts_doesNotFetchAccountsAndCreditCards_whenAccountsHaveAlreadyBeenFetched() {
        getRefreshControllersOfType(TransactionRefresher.class)
                .forEach(TransactionRefresher::fetchTransactions);

        executionOrder.verify(transactionalAccountFetcher).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.never())
                .updateAccount(Mockito.any(Account.class));
        executionOrder.verify(transactionFetcher).fetchTransactionsFor(accounts.get(0));
        executionOrder.verify(updateController).updateTransactions(accounts.get(0), transactions);

        getRefreshControllersOfType(AccountRefresher.class)
                .forEach(AccountRefresher::fetchAccounts);
        executionOrder.verify(transactionalAccountFetcher, Mockito.never()).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.times(accounts.size()))
                .updateAccount(Mockito.any(TransactionalAccount.class));
        executionOrder.verify(creditCardAccountFetcher, Mockito.never()).fetchAccounts();
        executionOrder
                .verify(updateController, Mockito.times(creditCards.size()))
                .updateAccount(Mockito.any(CreditCardAccount.class));
    }

    /*
    @Test
    // Test fail due to TransferDestinationFetcherController touches context
    public void ensureFetchTransferDestinations_fetchesTransferDestinations() {
        refreshers.forEach(r -> r.fetch(RefreshableItem.TRANSFER_DESTINATIONS));
        executionOrder.verify(transferDestinationFetcher).fetchTransferDestinationsFor(accounts.get(0));
    }
    */

    /*
    // Test fail due to TransferDestinationFetcherController touches context
    @Test
    public void ensureNullTransferDestinationFetcher_doesNotRenderNullPointer() {
        Mockito.when(transferDestinationFetcher.fetchTransferDestinationsFor(accounts.get(0))).thenReturn(null);

        refreshers.forEach(r -> r.refresh(RefreshableItem.TRANSFER_DESTINATIONS));
        executionOrder.verify(transferDestinationFetcher).fetchTransferDestinationsFor(accounts.get(0));
    }
    */

    @SuppressWarnings("unchecked")
    private <T extends Refresher> List<T> getRefreshControllersOfType(Class<T> cls) {
        return refreshers.stream()
                .filter(cls::isInstance)
                .map(refresher -> (T) refresher)
                .collect(Collectors.toList());
    }

    private <T extends Refresher> Optional<T> getRefreshController(Class<T> cls) {
        return getRefreshControllersOfType(cls).stream().findFirst();
    }
}
