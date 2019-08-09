package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import java.util.Date;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasTransactionFetcher implements TransactionDatePaginator {

    private final BnpParibasApiBaseClient apiClient;
    private final SessionStorage sessionStorage;
    private EidasProxyConfiguration eidasProxyConfiguration;

    public BnpParibasTransactionFetcher(
            BnpParibasApiBaseClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public void setEidasProxyConfiguration(EidasProxyConfiguration eidasProxyConfiguration) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
    }

    @Override
    public PaginatorResponse getTransactionsFor(Account account, Date fromDate, Date toDate) {
        String reqId = UUID.randomUUID().toString();
        String signature =
                BnpParibasUtils.buildSignatureHeader(
                        eidasProxyConfiguration,
                        sessionStorage.get(BnpParibasBaseConstants.StorageKeys.TOKEN),
                        reqId,
                        apiClient.getBnpParibasConfiguration());

        return apiClient.getTransactions(
                account.getAccountNumber(), signature, reqId, fromDate, toDate);
    }
}
