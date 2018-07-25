package se.tink.backend.aggregation.nxgen.agents.demo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.DemoData;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.agents.utils.demo.DemoDataUtils;
import se.tink.backend.core.Amount;
import se.tink.credentials.demo.DemoCredentials;

public class NextGenerationDemoFetcher implements AccountFetcher<TransactionalAccount>, TransactionDatePaginator<TransactionalAccount> {
    private static final String BASE_PATH = "data/demo";
    private static final Integer NUMBER_OF_TRANSACTIONS_TO_RANDOMIZE = 3;

    private static final List<AccountTypes> TRANSACTIONAL_ACCOUNT_TYPES = ImmutableList.<AccountTypes>builder()
            .add(AccountTypes.CHECKING)
            .add(AccountTypes.SAVINGS)
            .add(AccountTypes.SAVINGS)
            .build();

    private final String userPath;

    private final Set<String> finishedAccountNumbers = Sets.newHashSet();

    private final DemoCredentials demoCredentials;
    private final Credentials credentials;

    public NextGenerationDemoFetcher(Credentials credentials) {
        this.credentials = credentials;
        String userName = credentials.getField(Field.Key.USERNAME);
        this.demoCredentials = DemoCredentials.byUsername(userName);
        userPath = BASE_PATH + File.separator + userName;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            File accountsFile = new File(userPath + File.separator + "accounts.txt");

            return DemoDataUtils.readAggregationAccounts(accountsFile, credentials).stream()
                    .filter(a -> TRANSACTIONAL_ACCOUNT_TYPES.contains(a.getType()))
                    .map(a -> {
                        TransactionalAccount.Builder builder = TransactionalAccount.builder(a.getType(),
                                a.getBankId(), Amount.inSEK(a.getBalance()))
                                .setAccountNumber(a.getAccountNumber())
                                .setName(a.getName());

                        a.getIdentifiers().forEach(builder::addIdentifier);

                        return builder.build();
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Collection<Transaction> getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        if (account.getType() == AccountTypes.LOAN || finishedAccountNumbers.contains(account.getAccountNumber())) {
            return Collections.emptyList();
        }

        try {
            File transactionsFile = new File(userPath + File.separator + account.getBankIdentifier() + ".txt");
            finishedAccountNumbers.add(account.getAccountNumber());

            if (demoCredentials != null && demoCredentials
                    .hasFeature(DemoCredentials.DemoUserFeature.RANDOMIZE_TRANSACTIONS)) {
                return DemoData.readTransactionsWithRandomization(demoCredentials, transactionsFile,
                        account.toSystemAccount(), NUMBER_OF_TRANSACTIONS_TO_RANDOMIZE);
            } else {
                return DemoData.readTransactions(demoCredentials, transactionsFile, account.toSystemAccount());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
