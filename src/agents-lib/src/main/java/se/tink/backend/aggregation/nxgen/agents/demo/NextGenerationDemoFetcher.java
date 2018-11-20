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
import java.util.stream.IntStream;
import se.tink.backend.aggregation.agents.utils.demo.DemoDataUtils;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.TransactionGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.DateUtils;
import static java.util.stream.Collectors.toList;

public class NextGenerationDemoFetcher
        implements AccountFetcher<TransactionalAccount>,
        TransactionPaginator<TransactionalAccount> {
    private static final String BASE_PATH = NextGenDemoConstants.BASE_PATH;
    private static final List<AccountTypes> TRANSACTIONAL_ACCOUNT_TYPES = ImmutableList.of(
            AccountTypes.CHECKING,
            AccountTypes.SAVINGS,
            AccountTypes.SAVINGS);
    private final Set<String> finishedAccountNumbers = Sets.newHashSet();
    private final Credentials credentials;
    private final List<Account> accounts;
    private static final int YEARS_BACK_TO_FETCH = -3;
    private static final int CERTAIN_DATE_OFFSET_DAYS = 29;
    private final TransactionGenerator transactionGenerator;

    public NextGenerationDemoFetcher(Credentials credentials, List<Account> accounts) {
        this.credentials = credentials;
        this.accounts = accounts;
        this.transactionGenerator = new TransactionGenerator(BASE_PATH);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            File accountsFile = new File(BASE_PATH + File.separator + NextGenDemoConstants.ACCOUNT_FILE);

            return DemoDataUtils.readAggregationAccounts(accountsFile, credentials).stream()
                    .filter(a -> TRANSACTIONAL_ACCOUNT_TYPES.contains(a.getType()))
                    .map(a -> {
                        TransactionalAccount.Builder builder = TransactionalAccount.builder(a.getType(),
                                a.getBankId(), new Amount(DemoDataUtils.getCurrencyForDemoAccount(accountsFile,
                                        a.getAccountNumber()), a.getBalance()))
                                .setAccountNumber(a.getAccountNumber())
                                .setName(a.getName())
                                .setBankIdentifier(a.getBankId());
                        a.getIdentifiers().forEach(builder::addIdentifier);

                        return builder.build();
                    }).collect(toList());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        if (account.getType() == AccountTypes.LOAN || finishedAccountNumbers.contains(account.getAccountNumber())) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        if (account.getType() == AccountTypes.CREDIT_CARD || account.getType() == AccountTypes.CHECKING) {
            return transactionGenerator.generateTransactions(getRefreshStartDate(account.getAccountNumber()),
                    DateUtils.getToday(),
                    account.getBalance().getCurrency());
        }

        if (account.getType() == AccountTypes.SAVINGS) {
            return transactionGenerator.createSavingsAccountTransactions(account);
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
