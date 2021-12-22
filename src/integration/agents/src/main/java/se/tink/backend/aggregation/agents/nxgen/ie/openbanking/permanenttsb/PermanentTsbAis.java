package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.permanenttsb;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PermanentTsbAis extends UkOpenBankingV31Ais {

    protected PermanentTsbAis(
            UkOpenBankingAisConfig aisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            TransactionPaginationHelper transactionPaginationHelper) {
        super(aisConfig, persistentStorage, localDateTimeSource, transactionPaginationHelper);
    }

    @Override
    public TransactionPaginator<TransactionalAccount> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient,
            AgentComponentProvider componentProvider,
            Provider provider) {
        return new TransactionKeyPaginationController<>(
                new PermanentTsbTransactionPaginator<>(
                        componentProvider,
                        provider,
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (response, account) ->
                                AccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(response),
                        localDateTimeSource));
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient,
            AgentComponentProvider componentProvider,
            Provider provider) {
        return new TransactionKeyPaginationController<>(
                new PermanentTsbTransactionPaginator<>(
                        componentProvider,
                        provider,
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        AccountTransactionsV31Response::toCreditCardPaginationResponse,
                        localDateTimeSource));
    }
}
