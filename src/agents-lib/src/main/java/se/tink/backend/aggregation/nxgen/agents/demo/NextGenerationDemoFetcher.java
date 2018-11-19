package se.tink.backend.aggregation.nxgen.agents.demo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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


    public NextGenerationDemoFetcher(Credentials credentials, List<Account> accounts) {
        this.credentials = credentials;
        this.accounts = accounts;
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

    //TODO: Add nicer logic for generation of savings. Make sure to add up to the sum of the account
    private PaginatorResponse createSavingsAccountTransactions(Date now, TransactionalAccount account) {
        List<Transaction> transactions = IntStream.range(0, 36)
                .mapToObj(i -> Transaction.builder()
                        .setAmount(new Amount(account.getBalance().getCurrency(),
                                account.getBalance().getValue() / 36))
                        .setPending(false)
                        .setDescription("monthly savings")
                        .setDate(DateUtils.addMonths(now, -1)).build()
                )
                .collect(toList());

        return PaginatorResponseImpl.create(transactions, false);
    }

    private Date getLatestRefreshForAccount(String accountId) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountId) && Objects.nonNull(account.getCertainDate())) {
                return DateUtils.addDays(account.getCertainDate(), 29);
            }
        }

        return DateUtils.addMonths(DateUtils.getToday(), -3);
    }

    private PaginatorResponse fetchedGeneratedTransactions(TransactionalAccount account) {
        try {
            TransactionGenerator transactionGenerator = new TransactionGenerator(BASE_PATH,
                    account.getBalance().getCurrency());

            Collection<Transaction> transactions = transactionGenerator
                    .generateTransactions(getLatestRefreshForAccount(
                            account.getBankIdentifier()),
                            DateUtils.getToday());

            return PaginatorResponseImpl.create(transactions, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
            return PaginatorResponseImpl.createEmpty();
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        if (account.getType() == AccountTypes.LOAN || finishedAccountNumbers.contains(account.getAccountNumber())) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        if (account.getType() == AccountTypes.CREDIT_CARD || account.getType() == AccountTypes.CHECKING) {
            return fetchedGeneratedTransactions (account);
        }

        if (account.getType() == AccountTypes.SAVINGS) {
            return createSavingsAccountTransactions(DateUtils.getToday(), account);
        }

        return PaginatorResponseImpl.createEmpty(false);
    }
}
