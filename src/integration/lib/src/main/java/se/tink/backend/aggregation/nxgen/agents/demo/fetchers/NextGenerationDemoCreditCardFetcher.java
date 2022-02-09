package se.tink.backend.aggregation.nxgen.agents.demo.fetchers;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.DemoAccountFactory;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.PurchaseHistoryGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n_aggregation.Catalog;

public class NextGenerationDemoCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPaginator<CreditCardAccount> {

    private static final String BASE_PATH = DemoConstants.BASE_PATH;
    private final List<Account> accounts;
    private static final int MONTHS_BACK_TO_FETCH = -12;
    private static final int CERTAIN_DATE_OFFSET_DAYS = 10;
    private static final int MAX_NUMBER_OF_DAILY_TRANSACTIONS = 500;
    private final PurchaseHistoryGenerator purchaseHistoryGenerator;
    private final Catalog catalog;
    private final List<DemoCreditCardAccount> creditCardAccountDefinition;
    private final String currency;

    public NextGenerationDemoCreditCardFetcher(
            List<Account> accounts,
            String currency,
            Catalog catalog,
            List<DemoCreditCardAccount> creditCardAccountDefinition) {
        this.accounts = accounts;
        this.currency = currency;
        this.purchaseHistoryGenerator = new PurchaseHistoryGenerator(BASE_PATH);
        this.catalog = catalog;
        this.creditCardAccountDefinition = creditCardAccountDefinition;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return DemoAccountFactory.createCreditCardAccounts(
                currency, catalog, creditCardAccountDefinition);
    }

    @Override
    public void resetState() {}

    @Override
    public PaginatorResponse fetchTransactionsFor(CreditCardAccount account) {
        return purchaseHistoryGenerator.generateTransactions(
                getRefreshStartDate(account.getAccountNumber()),
                DateUtils.getToday(),
                account.getExactBalance().getCurrencyCode(),
                MAX_NUMBER_OF_DAILY_TRANSACTIONS);
    }

    private Date getRefreshStartDate(String accountId) {
        Optional<Account> previouslyRefreshedAccount =
                accounts.stream()
                        .filter(
                                account ->
                                        account.getAccountNumber().equals(accountId)
                                                && Objects.nonNull(account.getCertainDate()))
                        .findFirst();

        return previouslyRefreshedAccount
                .map(
                        account ->
                                DateUtils.addDays(
                                        account.getCertainDate(), CERTAIN_DATE_OFFSET_DAYS))
                .orElseGet(() -> DateUtils.addMonths(DateUtils.getToday(), MONTHS_BACK_TO_FETCH));
    }
}
