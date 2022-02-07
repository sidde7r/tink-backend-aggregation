package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_OFFSET;

import com.google.common.base.Preconditions;
import java.time.Period;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid.UkObDateCalculator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid.UkObTransactionPaginationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid.UkObTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.AccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.CreditCardAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.TransactionalAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range.DateRangeCalculator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.mapper.PrioritizedValueExtractor;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.model.UnleashContextWrapper;

@Slf4j
public class UkOpenBankingV31Ais implements UkOpenBankingAis {

    protected final UkOpenBankingAisConfig ukOpenBankingAisConfig;
    protected final PersistentStorage persistentStorage;
    protected final LocalDateTimeSource localDateTimeSource;
    private final AccountMapper<CreditCardAccount> creditCardAccountMapper;
    private final AccountMapper<TransactionalAccount> transactionalAccountMapper;
    private final ScaExpirationValidator scaValidator;
    private final TransactionPaginationHelper transactionPaginationHelper;

    public UkOpenBankingV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            TransactionPaginationHelper transactionPaginationHelper) {
        this(
                ukOpenBankingAisConfig,
                persistentStorage,
                localDateTimeSource,
                defaultCreditCardAccountMapper(),
                defaultTransactionalAccountMapper(),
                transactionPaginationHelper);
    }

    public UkOpenBankingV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            CreditCardAccountMapper creditCardAccountMapper,
            LocalDateTimeSource localDateTimeSource,
            TransactionPaginationHelper transactionPaginationHelper) {
        this(
                ukOpenBankingAisConfig,
                persistentStorage,
                localDateTimeSource,
                creditCardAccountMapper,
                defaultTransactionalAccountMapper(),
                transactionPaginationHelper);
    }

    public UkOpenBankingV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            AccountMapper<CreditCardAccount> creditCardAccountMapper,
            AccountMapper<TransactionalAccount> transactionalAccountMapper,
            TransactionPaginationHelper transactionPaginationHelper) {

        this.ukOpenBankingAisConfig = Preconditions.checkNotNull(ukOpenBankingAisConfig);
        this.persistentStorage = Preconditions.checkNotNull(persistentStorage);
        this.localDateTimeSource = Preconditions.checkNotNull(localDateTimeSource);
        this.creditCardAccountMapper = Preconditions.checkNotNull(creditCardAccountMapper);
        this.transactionalAccountMapper = Preconditions.checkNotNull(transactionalAccountMapper);
        this.scaValidator = getScaValidator();
        this.transactionPaginationHelper = transactionPaginationHelper;
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

    public static PartyV31Fetcher defaultPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage) {
        return new PartyV31Fetcher(apiClient, ukOpenBankingAisConfig, persistentStorage);
    }

    @Override
    public AccountFetcher<TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient) {

        return new TransactionalAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        defaultPartyFetcher(apiClient, ukOpenBankingAisConfig, persistentStorage),
                        transactionalAccountMapper));
    }

    @Override
    public TransactionPaginator<TransactionalAccount> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient,
            AgentComponentProvider componentProvider,
            Provider provider) {
        return makeAccountTransactionPaginatorControllerByToggleValue(
                apiClient, componentProvider, provider);
    }

    @Override
    public Optional<UkOpenBankingUpcomingTransactionFetcher<?>> makeUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient) {
        return Optional.empty();
        // TODO: Enable when this feature is mandatory for the banks to implement
        //        return Optional.of(new UkOpenBankingUpcomingTransactionFetcher<>(apiClient,
        //                UpcomingTransactionsV30Response.class,
        //                UpcomingTransactionsV30Response::toUpcomingTransactions))
    }

    @Override
    public AccountFetcher<CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return new CreditCardAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        defaultPartyFetcher(apiClient, ukOpenBankingAisConfig, persistentStorage),
                        creditCardAccountMapper));
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient,
            AgentComponentProvider componentProvider,
            Provider provider) {
        return makeCreditCardTransactionPaginatorControllerByToggleValue(
                apiClient, componentProvider, provider);
    }

    @Override
    public PartyFetcher makePartyFetcher(UkOpenBankingApiClient apiClient) {
        return defaultPartyFetcher(apiClient, ukOpenBankingAisConfig, persistentStorage);
    }

    protected <T extends Account> UkObDateCalculator<T> constructUkObDateCalculator() {
        return new UkObDateCalculator<>(
                scaValidator,
                new DateRangeCalculator<>(
                        localDateTimeSource, DEFAULT_OFFSET, transactionPaginationHelper),
                Period.ofDays(90),
                Period.ofYears(2));
    }

    protected Toggle createToggleForExperimentalTransactionPagination(
            AgentComponentProvider componentProvider) {
        String providerId = componentProvider.getContext().getProviderId();
        return Toggle.of("ukob-experimental-transaction-pagination")
                .unleashContextWrapper(
                        UnleashContextWrapper.builder().providerName(providerId).build())
                .build();
    }

    protected ScaExpirationValidator getScaValidator() {
        return new ScaExpirationValidator(
                persistentStorage, UkOpenBankingV31Constants.Limits.SCA_IN_MINUTES);
    }

    private TransactionPaginator<TransactionalAccount>
            makeAccountTransactionPaginatorControllerByToggleValue(
                    UkOpenBankingApiClient apiClient,
                    AgentComponentProvider componentProvider,
                    Provider provider) {
        Toggle trxToggle = createToggleForExperimentalTransactionPagination(componentProvider);

        if (componentProvider.getUnleashClient().isToggleEnabled(trxToggle)) {
            log.info(
                    "[ukob-experimental-transaction-pagination] New transaction pagination controller is used for transactional accounts.");
            return new UkObTransactionPaginationController<>(
                    new UkObTransactionPaginator<>(
                            apiClient,
                            AccountTransactionsV31Response.class,
                            ((response, account) ->
                                    AccountTransactionsV31Response
                                            .toAccountTransactionPaginationResponse(response))),
                    constructUkObDateCalculator(),
                    Period.ofYears(2));
        }

        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        componentProvider,
                        provider,
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (response, account) ->
                                AccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(response),
                        localDateTimeSource,
                        transactionPaginationHelper));
    }

    private TransactionPaginator<CreditCardAccount>
            makeCreditCardTransactionPaginatorControllerByToggleValue(
                    UkOpenBankingApiClient apiClient,
                    AgentComponentProvider componentProvider,
                    Provider provider) {
        Toggle trxToggle = createToggleForExperimentalTransactionPagination(componentProvider);

        if (componentProvider.getUnleashClient().isToggleEnabled(trxToggle)) {
            log.info(
                    "[ukob-experimental-transaction-pagination] New transaction pagination controller is used for credit cards.");
            return new UkObTransactionPaginationController<>(
                    new UkObTransactionPaginator<>(
                            apiClient,
                            AccountTransactionsV31Response.class,
                            (AccountTransactionsV31Response::toCreditCardPaginationResponse)),
                    constructUkObDateCalculator(),
                    Period.ofYears(2));
        }
        return new TransactionKeyPaginationController<>(
                new UkOpenBankingTransactionPaginator<>(
                        componentProvider,
                        provider,
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        AccountTransactionsV31Response::toCreditCardPaginationResponse,
                        localDateTimeSource,
                        transactionPaginationHelper));
    }
}
