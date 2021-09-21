package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions.MonzoTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions.MonzoTransactionPaginator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MonzoV31Ais extends UkOpenBankingV31Ais {

    private final PartyFetcher monzoPartyFetcher;
    private final CredentialsRequest request;

    public MonzoV31Ais(
            UkOpenBankingAisConfig aisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            UkOpenBankingApiClient apiClient,
            CredentialsRequest request) {
        super(aisConfig, persistentStorage, localDateTimeSource);
        this.monzoPartyFetcher = new PartyV31Fetcher(apiClient, aisConfig, persistentStorage);
        this.request = request;
    }

    @Override
    public TransactionPaginator<TransactionalAccount> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new MonzoTransactionPaginator<>(
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (response, account) ->
                                AccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(
                                                response, new MonzoTransactionMapper()),
                        localDateTimeSource,
                        request));
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return new TransactionKeyPaginationController<>(
                new MonzoTransactionPaginator<>(
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (response, account) ->
                                AccountTransactionsV31Response.toCreditCardPaginationResponse(
                                        response, new MonzoTransactionMapper(), account),
                        localDateTimeSource,
                        request));
    }

    @Override
    public PartyFetcher makePartyFetcher(UkOpenBankingApiClient apiClient) {
        return monzoPartyFetcher;
    }
}
