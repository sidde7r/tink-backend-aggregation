package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.EeIConsultationMovementsPostponedViewEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.RepositioningEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.TransactionsPaginationRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EvoBancoTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, RepositioningEntity> {

    private final EvoBancoApiClient bankClient;
    private final SessionStorage sessionStorage;

    public EvoBancoTransactionFetcher(EvoBancoApiClient bankClient, SessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransactionKeyPaginatorResponse<RepositioningEntity> getTransactionsFor(
            TransactionalAccount account, RepositioningEntity key) {

        EeIConsultationMovementsPostponedViewEntity eeIConsultationMovementsPostponedViewEntity =
                new EeIConsultationMovementsPostponedViewEntity.Builder()
                        .withUserbe(sessionStorage.get(Storage.USER_BE))
                        .withAgreement(account.getApiIdentifier())
                        .withRepositioning(key)
                        .withAgreementbe(sessionStorage.get(Storage.AGREEMENT_BE))
                        .withEntityCode(sessionStorage.get(Storage.ENTITY_CODE))
                        .build();

        TransactionsPaginationRequest request =
                new TransactionsPaginationRequest(eeIConsultationMovementsPostponedViewEntity);
        try {
            return bankClient.fetchTransactions(request);
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                TransactionsResponse errorResponse =
                        e.getResponse().getBody(TransactionsResponse.class);

                bankClient.setNextCodSecIpHeader(e.getResponse());

                errorResponse.handleReturnCode();

                return TransactionKeyPaginatorResponseImpl.createEmpty();
            } else {
                throw e;
            }
        }
    }
}
