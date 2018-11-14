package se.tink.backend.aggregation.nxgen.agents.demo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.utils.demo.DemoDataUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.DemoData;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.core.Amount;
import se.tink.credentials.demo.DemoCredentials;

public class NextGenerationDemoFetcher implements AccountFetcher<TransactionalAccount>, TransactionDatePaginator<TransactionalAccount> {
    private static final String BASE_PATH = NextGenDemoConstants.BASE_PATH;
    private static final List<AccountTypes> TRANSACTIONAL_ACCOUNT_TYPES = ImmutableList.of(
            AccountTypes.CHECKING,
            AccountTypes.SAVINGS,
            AccountTypes.SAVINGS);
    private final Set<String> finishedAccountNumbers = Sets.newHashSet();
    private final Credentials credentials;

    public NextGenerationDemoFetcher(Credentials credentials) {
        this.credentials = credentials;
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
                                        a.getAccountNumber()) , a.getBalance()))
                                .setAccountNumber(a.getAccountNumber())
                                .setName(a.getName())
                                .setBankIdentifier(a.getBankId());
                        a.getIdentifiers().forEach(builder::addIdentifier);

                        return builder.build();
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        if (account.getType() == AccountTypes.LOAN || finishedAccountNumbers.contains(account.getAccountNumber())) {
            return PaginatorResponseImpl.createEmpty();
        }

        if (account.getType() == AccountTypes.CREDIT_CARD || account.getType() == AccountTypes.CHECKING) {
            try {
                account.getBalance().getCurrency();
                NextGenerationTransactionGenerator transactionGenerator =
                        new NextGenerationTransactionGenerator(BASE_PATH, account.getBalance().getCurrency());
                Collection<Transaction> transactions = transactionGenerator.generateTransactions(fromDate, toDate);
                return PaginatorResponseImpl.create(transactions);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        if (account.getType() == AccountTypes.SAVINGS) {
            //Add 3 years of savings, 36 months
            Double savedAmount = account.getBalance().getValue();
            savedAmount = savedAmount / 36;

        }
        return PaginatorResponseImpl.createEmpty();
    }
}
