package se.tink.backend.aggregation.nxgen.agents.demo.finovate;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.TransactionalAccountGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;

public class NextGenerationDemoTransactionFetcher
        implements AccountFetcher<TransactionalAccount>,
        TransactionPaginator<TransactionalAccount> {
    private static final String BASE_PATH = DemoConstants.BASE_PATH;
    private final List<Account> accounts;
    private static final int YEARS_BACK_TO_FETCH = -3;
    private static final int CERTAIN_DATE_OFFSET_DAYS = 29;
    private final PurchaseHistoryGenerator purchaseHistoryGenerator;
    private final String currency;
    private final Catalog catalog;
    private final DemoTransactionAccount transactionAccountDefinition;
    private final DemoSavingsAccount savingsAccountDefinition;

    public NextGenerationDemoTransactionFetcher(List<Account> accounts, String currency,
            Catalog catalog,
            DemoTransactionAccount transactionAccountDefinition,
            DemoSavingsAccount savingsAccountDefinition) {
        this.accounts = accounts;
        this.purchaseHistoryGenerator = new PurchaseHistoryGenerator(BASE_PATH);
        this.currency = currency;
        this.catalog = catalog;
        this.transactionAccountDefinition = transactionAccountDefinition;
        this.savingsAccountDefinition = savingsAccountDefinition;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return TransactionalAccountGenerator
                .fetchTransactionalAccounts(currency, catalog, transactionAccountDefinition, savingsAccountDefinition);
    }

    @Override
    public void resetState() {

    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        if (account.getType() == AccountTypes.CREDIT_CARD || account.getType() == AccountTypes.CHECKING) {
            return purchaseHistoryGenerator.generateTransactions(
                    getRefreshStartDate(account.getAccountNumber()),
                    DateUtils.getToday(),
                    account.getBalance().getCurrency());
        }

        if (account.getType() == AccountTypes.SAVINGS) {
            return purchaseHistoryGenerator.generateSavingsAccountTransactions(account,
                    getRefreshStartDate(account.getAccountNumber()),
                    DateUtils.getToday());
        }

        return PaginatorResponseImpl.createEmpty(false);
    }

    private Date getRefreshStartDate(String accountId) {
        Optional<Account> previouslyRefreshedAccount = accounts.stream()
                .filter(account -> account.getAccountNumber().equals(accountId)
                        && Objects.nonNull(account.getCertainDate()))
                .findFirst();

        return (previouslyRefreshedAccount.isPresent()) ?
                DateUtils.addDays(previouslyRefreshedAccount.get().getCertainDate(), CERTAIN_DATE_OFFSET_DAYS) :
                DateUtils.addYears(DateUtils.getToday(), YEARS_BACK_TO_FETCH);
    }
}
