package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.AccountType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.AccountClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.MetroAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.MetroTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.TransactionClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.TransactionMapper;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

public class MetroFetchersModule extends AbstractModule {

    @Inject
    @Singleton
    @Provides
    public TransactionalAccountRefreshControllerFactory accountRefreshController(
            AgentPlatformHttpClient httpClient, AccountType accountType) {
        AccountClient accountClient = new AccountClient(httpClient);
        AccountMapper accountMapper = new AccountMapper(accountTypeMapper());
        MetroAccountFetcher accountFetcher =
                new MetroAccountFetcher(accountClient, accountType, accountMapper);

        TransactionClient transactionClient = new TransactionClient(httpClient);
        TransactionMapper transactionMapper = new TransactionMapper();
        MetroTransactionFetcher transactionFetcher =
                new MetroTransactionFetcher(transactionClient, transactionMapper);
        return new TransactionalAccountRefreshControllerFactory(accountFetcher, transactionFetcher);
    }

    private static AccountTypeMapper accountTypeMapper() {
        return AccountTypeMapper.builder()
                .put(AccountTypes.CHECKING, "CURRENT_ACCOUNT")
                .put(AccountTypes.SAVINGS, "SAVINGS_ACCOUNT")
                .build();
    }
}
