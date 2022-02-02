package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

import java.time.Period;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid.UkObTransactionPaginationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid.UkObTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.unleash.model.Toggle;

@Slf4j
public class DanskeBankV31Ais extends UkOpenBankingV31Ais {

    private final TransactionPaginationHelper paginationHelper;

    public DanskeBankV31Ais(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            AccountMapper<CreditCardAccount> creditCardAccountMapper,
            AccountMapper<TransactionalAccount> transactionalAccountMapper,
            TransactionPaginationHelper transactionPaginationHelper) {
        super(
                ukOpenBankingAisConfig,
                persistentStorage,
                localDateTimeSource,
                creditCardAccountMapper,
                transactionalAccountMapper,
                transactionPaginationHelper);
        this.paginationHelper = transactionPaginationHelper;
    }

    @Override
    public TransactionPaginator<TransactionalAccount> makeAccountTransactionPaginatorController(
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
                            DanskeBankAccountTransactionsV31Response.class,
                            ((response, account) ->
                                    DanskeBankAccountTransactionsV31Response
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
                        DanskeBankAccountTransactionsV31Response.class,
                        (response, account) ->
                                DanskeBankAccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(response),
                        localDateTimeSource,
                        paginationHelper));
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
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
                            DanskeBankAccountTransactionsV31Response.class,
                            (DanskeBankAccountTransactionsV31Response
                                    ::toCreditCardPaginationResponse)),
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
                        DanskeBankAccountTransactionsV31Response.class,
                        DanskeBankAccountTransactionsV31Response::toCreditCardPaginationResponse,
                        localDateTimeSource,
                        paginationHelper));
    }
}
