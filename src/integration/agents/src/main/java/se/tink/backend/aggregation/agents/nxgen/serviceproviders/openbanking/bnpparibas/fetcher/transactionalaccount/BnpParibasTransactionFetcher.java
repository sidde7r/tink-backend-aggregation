package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import java.util.Date;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasSignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final BnpParibasApiBaseClient apiClient;
    private final SessionStorage sessionStorage;
    private final BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;

    public BnpParibasTransactionFetcher(
            BnpParibasApiBaseClient apiClient,
            SessionStorage sessionStorage,
            BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.bnpParibasSignatureHeaderProvider = bnpParibasSignatureHeaderProvider;
    }

    public void setEidasProxyConfiguration(
            EidasProxyConfiguration eidasProxyConfiguration, EidasIdentity eidasIdentity) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        String reqId = UUID.randomUUID().toString();
        String signature =
                bnpParibasSignatureHeaderProvider.buildSignatureHeader(
                        eidasProxyConfiguration,
                        eidasIdentity,
                        sessionStorage.get(BnpParibasBaseConstants.StorageKeys.TOKEN),
                        reqId,
                        apiClient.getBnpParibasConfiguration());

        return apiClient.getTransactions(
                account.getAccountNumber(), signature, reqId, fromDate, toDate);
    }
}
