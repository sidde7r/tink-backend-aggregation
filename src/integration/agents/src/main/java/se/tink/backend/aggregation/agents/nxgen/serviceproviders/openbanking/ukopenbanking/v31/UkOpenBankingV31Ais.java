package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.IdentityDataV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.account.AccountBalanceV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.account.AccountsV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.PrioritizedValueExtractor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.CreditCardAccountExtractor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountExtractor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UkOpenBankingV31Ais implements UkOpenBankingAis {

    private final UkOpenBankingAisConfig ukOpenBankingAisConfig;
    private final PersistentStorage persistentStorage;
    private final LocalDateTimeSource localDateTimeSource;
    private final CreditCardAccountMapper creditCardAccountMapper;
    private final TransactionalAccountMapper transactionalAccountMapper;

    public UkOpenBankingV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig, PersistentStorage persistentStorage) {
        this(
                ukOpenBankingAisConfig,
                persistentStorage,
                new ActualLocalDateTimeSource(),
                defaultCreditCardAccountMapper(),
                defaultTransactionalAccountMapper());
    }

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
            CreditCardAccountMapper creditCardAccountMapper) {
        this(
                ukOpenBankingAisConfig,
                persistentStorage,
                new ActualLocalDateTimeSource(),
                creditCardAccountMapper,
                defaultTransactionalAccountMapper());
    }

    public UkOpenBankingV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            CreditCardAccountMapper creditCardAccountMapper,
            TransactionalAccountMapper transactionalAccountMapper) {
        this.ukOpenBankingAisConfig = ukOpenBankingAisConfig;
        this.persistentStorage = persistentStorage;
        this.localDateTimeSource = localDateTimeSource;
        this.creditCardAccountMapper = creditCardAccountMapper;
        this.transactionalAccountMapper = transactionalAccountMapper;
    }

    @Override
    public UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient) {

        TransactionalAccountExtractor extractor =
                new TransactionalAccountExtractor(transactionalAccountMapper);

        return new UkOpenBankingAccountFetcher<>(
                ukOpenBankingAisConfig,
                apiClient,
                AccountsV31Response.class,
                AccountBalanceV31Response.class,
                extractor::toTransactionalAccount,
                new IdentityDataV31Fetcher(apiClient));
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
    public UkOpenBankingAccountFetcher<?, ?, CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        CreditCardAccountExtractor extractor =
                new CreditCardAccountExtractor(creditCardAccountMapper);

        return new UkOpenBankingAccountFetcher<>(
                ukOpenBankingAisConfig,
                apiClient,
                AccountsV31Response.class,
                AccountBalanceV31Response.class,
                extractor::toCreditCardAccount,
                new IdentityDataV31Fetcher(apiClient));
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

    private static CreditCardAccountMapper defaultCreditCardAccountMapper() {
        return new CreditCardAccountMapper(
                new DefaultCreditCardBalanceMapper(new PrioritizedValueExtractor()));
    }

    private static TransactionalAccountMapper defaultTransactionalAccountMapper() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        return new TransactionalAccountMapper(
                new TransactionalAccountBalanceMapper(valueExtractor),
                new IdentifierMapper(valueExtractor));
    }
}
