package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.AccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.CreditCardAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.IdentityDataV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.PartyDataV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.TransactionalAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.IdentityDataMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class UkOpenBankingV31Ais implements UkOpenBankingAis {

    protected final UkOpenBankingAisConfig ukOpenBankingAisConfig;
    protected final PersistentStorage persistentStorage;
    protected final LocalDateTimeSource localDateTimeSource;
    private final AccountMapper<CreditCardAccount> creditCardAccountMapper;
    private final AccountMapper<TransactionalAccount> transactionalAccountMapper;

    public UkOpenBankingV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource) {
        this(
                ukOpenBankingAisConfig,
                persistentStorage,
                localDateTimeSource,
                defaultCreditCardAccountMapper(),
                defaultTransactionalAccountMapper());
    }

    public UkOpenBankingV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            CreditCardAccountMapper creditCardAccountMapper,
            LocalDateTimeSource localDateTimeSource) {
        this(
                ukOpenBankingAisConfig,
                persistentStorage,
                localDateTimeSource,
                creditCardAccountMapper,
                defaultTransactionalAccountMapper());
    }

    public UkOpenBankingV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            AccountMapper<CreditCardAccount> creditCardAccountMapper,
            AccountMapper<TransactionalAccount> transactionalAccountMapper) {

        this.ukOpenBankingAisConfig = Preconditions.checkNotNull(ukOpenBankingAisConfig);
        this.persistentStorage = Preconditions.checkNotNull(persistentStorage);
        this.localDateTimeSource = Preconditions.checkNotNull(localDateTimeSource);
        this.creditCardAccountMapper = Preconditions.checkNotNull(creditCardAccountMapper);
        this.transactionalAccountMapper = Preconditions.checkNotNull(transactionalAccountMapper);
    }

    @Override
    public AccountFetcher<TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient, FetcherInstrumentationRegistry instrumentation) {

        return new TransactionalAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        defaultPartyDataFetcher(apiClient, ukOpenBankingAisConfig),
                        new AccountTypeMapper(ukOpenBankingAisConfig),
                        transactionalAccountMapper,
                        instrumentation));
    }

    @Override
    public TransactionPaginator<TransactionalAccount> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        AccountTransactionsV31Response::toAccountTransactionPaginationResponse,
                        localDateTimeSource));
    }

    @Override
    public Optional<UkOpenBankingUpcomingTransactionFetcher<?>> makeUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient) {
        return Optional.empty();
        // TODO: Enable when this feature is mandatory for the banks to implement
        //        return Optional.of(new UkOpenBankingUpcomingTransactionFetcher<>(apiClient,
        //                UpcomingTransactionsV30Response.class,
        //                UpcomingTransactionsV30Response::toUpcomingTransactions));
    }

    @Override
    public AccountFetcher<CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient, FetcherInstrumentationRegistry instrumentation) {
        return new CreditCardAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        defaultPartyDataFetcher(apiClient, ukOpenBankingAisConfig),
                        new AccountTypeMapper(ukOpenBankingAisConfig),
                        creditCardAccountMapper,
                        instrumentation));
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        AccountTransactionsV31Response::toCreditCardPaginationResponse,
                        localDateTimeSource));
    }

    @Override
    public IdentityDataFetcher makeIdentityDataFetcher(UkOpenBankingApiClient apiClient) {
        return defaultIdentityDataFetcher(apiClient, ukOpenBankingAisConfig);
    }

    public static CreditCardAccountMapper defaultCreditCardAccountMapper() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        return new CreditCardAccountMapper(
                new DefaultCreditCardBalanceMapper(valueExtractor),
                new DefaultIdentifierMapper(valueExtractor));
    }

    public static TransactionalAccountMapper defaultTransactionalAccountMapper() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        return new TransactionalAccountMapper(
                new TransactionalAccountBalanceMapper(valueExtractor),
                new DefaultIdentifierMapper(valueExtractor));
    }

    public static IdentityDataV31Fetcher defaultIdentityDataFetcher(
            UkOpenBankingApiClient apiClient, UkOpenBankingAisConfig ukOpenBankingAisConfig) {
        return new IdentityDataV31Fetcher(
                apiClient, ukOpenBankingAisConfig, new IdentityDataMapper());
    }

    public static PartyDataV31Fetcher defaultPartyDataFetcher(
            UkOpenBankingApiClient apiClient, UkOpenBankingAisConfig ukOpenBankingAisConfig) {
        return new PartyDataV31Fetcher(apiClient, ukOpenBankingAisConfig);
    }
}
