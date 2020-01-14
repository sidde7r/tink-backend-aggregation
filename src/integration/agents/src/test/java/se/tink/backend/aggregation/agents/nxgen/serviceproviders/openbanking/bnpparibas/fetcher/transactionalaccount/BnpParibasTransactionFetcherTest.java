package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasSignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasTransactionFetcherTest {

    private BnpParibasApiBaseClient apiClient;
    private SessionStorage sessionStorage;
    private BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider;
    private String token;
    private BnpParibasConfiguration bnpParibasConfiguration;
    private String signature;
    private AccountsResponse accountsResponse;

    @Before
    public void init() {
        apiClient = mock(BnpParibasApiBaseClient.class);
        sessionStorage = mock(SessionStorage.class);
        bnpParibasSignatureHeaderProvider = mock(BnpParibasSignatureHeaderProvider.class);
        token = "token";
        bnpParibasConfiguration = mock(BnpParibasConfiguration.class);
        signature = "signature";
        accountsResponse = mock(AccountsResponse.class);

        when(sessionStorage.get(BnpParibasBaseConstants.StorageKeys.TOKEN)).thenReturn(token);
        when(apiClient.getBnpParibasConfiguration()).thenReturn(bnpParibasConfiguration);
        when(bnpParibasSignatureHeaderProvider.buildSignatureHeader(
                        any(), any(), any(), any(), any()))
                .thenReturn(signature);
    }

    @Test
    public void shouldgetTransactions() {
        // given
        TransactionalAccount account = mock(TransactionalAccount.class);
        String accountNumber = "accountNumber";
        Date fromDate = mock(Date.class);
        Date toDate = mock(Date.class);
        TransactionsResponse transactionsResponse = mock(TransactionsResponse.class);

        when(account.getAccountNumber()).thenReturn(accountNumber);
        when(apiClient.getTransactions(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(transactionsResponse);

        BnpParibasTransactionFetcher bnpParibasTransactionFetcher =
                new BnpParibasTransactionFetcher(
                        apiClient, sessionStorage, bnpParibasSignatureHeaderProvider);

        // when
        PaginatorResponse response =
                bnpParibasTransactionFetcher.getTransactionsFor(account, fromDate, toDate);

        // then
        assertNotNull(response);
        assertEquals(transactionsResponse, response);
    }
}
