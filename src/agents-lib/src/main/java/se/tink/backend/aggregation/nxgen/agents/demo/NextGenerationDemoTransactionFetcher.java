package se.tink.backend.aggregation.nxgen.agents.demo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregation.agents.utils.demo.DemoDataUtils;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.PurchaseHistoryGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.TransactionalAccountGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import static java.util.stream.Collectors.toList;

public class NextGenerationDemoTransactionFetcher
        implements AccountFetcher<TransactionalAccount>,
        TransactionPaginator<TransactionalAccount> {
    private static final String BASE_PATH = NextGenDemoConstants.BASE_PATH;
    private final List<Account> accounts;
    private static final int YEARS_BACK_TO_FETCH = -3;
    private static final int CERTAIN_DATE_OFFSET_DAYS = 29;
    private final PurchaseHistoryGenerator purchaseHistoryGenerator;
    private final String currency;
    private final Catalog catalog;

    public NextGenerationDemoTransactionFetcher(List<Account> accounts, String currency, Catalog catalog) {
        this.accounts = accounts;
        this.purchaseHistoryGenerator = new PurchaseHistoryGenerator(BASE_PATH);
        this.currency = currency;
        this.catalog = catalog;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return TransactionalAccountGenerator.fetchTransactionalAccounts(currency, catalog);
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        if (account.getType() == AccountTypes.CREDIT_CARD || account.getType() == AccountTypes.CHECKING) {
            return purchaseHistoryGenerator.generateTransactions(getRefreshStartDate(account.getAccountNumber()),
                    DateUtils.getToday(),
                    account.getBalance().getCurrency());
        }

        if (account.getType() == AccountTypes.SAVINGS) {
            return purchaseHistoryGenerator.createSavingsAccountTransactions(account);
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
