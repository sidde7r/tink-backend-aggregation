package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.transaction;

import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BancoBpiTransactionFetcherTest {

    private static final String RESPONSE =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"MovimentosConta\": {\"List\": [{\"dataMovimento\": \"2019-12-09\",\"descricao\": \"09/12 COMPRA ELEC 6330968/10 REPSOL ALTO DO VALE 2070 CARTAX\",\"valorMoedaConta\": \"-28.72\",\"moedaOperacao\": \"EUR\",\"valorOperacao\": \"0.0\",\"saldoMoedaConta\": \"276.42\"}, {\"dataMovimento\": \"2019-12-09\",\"descricao\": \"09/12 COMPRA ELEC 6330968/09 OURIQUENSE LDAVILA CHA DE\",\"valorMoedaConta\": \"-16.6\",\"moedaOperacao\": \"EUR\",\"valorOperacao\": \"0.0\",\"saldoMoedaConta\": \"305.14\"}]},\"SaldoMoedaConta\": \"EUR\",\"PaginacaoOut\": {\"uuid\": \"193648e9-ddc6-4092-a3a5-e10b69fa2cb8\",\"lastPage\": false,\"pageNumber\": 1,\"pageSize\": 10,\"currentPage\": \"\",\"recordCount\": \"\"},\"TransactionStatus\": {\"OperationStatusId\": 1,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [],\"EmptyListItem\": {\"Status\": \"\",\"Code\": \"\",\"Description\": \"\"}}},\"RequestValid\": true,\"CanceledBecauseFromBack\": false}}";

    private static final String MODULE_VERSION = "testModuleVersion";
    private static final String ACCOUNT_ORDER = "testAccountOrder";
    private static final String ACCOUNT_TYPE = "testAccountType";
    private static final String ACCOUNT_INTERNAL_ID = "12345";

    private RequestBuilder requestBuilder;
    private TinkHttpClient httpClient;
    private BancoBpiAuthContext authContext;
    private BancoBpiAccountsContext accountsContext;
    private BancoBpiEntityManager entityManager;
    private TransactionalAccount transactionalAccount;
    private BancoBpiTransactionFetcher objectUnderTest;

    @Before
    public void init() {
        httpClient = Mockito.mock(TinkHttpClient.class);
        requestBuilder = Mockito.mock(RequestBuilder.class);
        Mockito.when(httpClient.request(Mockito.anyString())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.header(Mockito.any(String.class), Mockito.any()))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.body(Mockito.anyString())).thenReturn(requestBuilder);
        authContext = Mockito.mock(BancoBpiAuthContext.class);
        accountsContext = new BancoBpiAccountsContext();
        entityManager = Mockito.mock(BancoBpiEntityManager.class);
        Mockito.when(entityManager.getAccountsContext()).thenReturn(accountsContext);
        Mockito.when(entityManager.getAuthContext()).thenReturn(authContext);
        objectUnderTest = new BancoBpiTransactionFetcher(entityManager, httpClient);
        initAccountObjects();
    }

    private void initAccountObjects() {
        transactionalAccount = Mockito.mock(TransactionalAccount.class);
        Mockito.when(transactionalAccount.getAccountNumber()).thenReturn(ACCOUNT_INTERNAL_ID);
        TransactionalAccountBaseInfo accountBaseInfo = new TransactionalAccountBaseInfo();
        accountBaseInfo.setInternalAccountId(ACCOUNT_INTERNAL_ID);
        accountBaseInfo.setOrder(ACCOUNT_ORDER);
        accountBaseInfo.setType(ACCOUNT_TYPE);
        accountsContext.getAccountInfo().add(accountBaseInfo);
    }

    @Test
    public void getTransactionsForShouldReturnTransactionsWithMoreAvailable() {
        // given
        Pattern emptyUUIDPattern = Pattern.compile("\"uuid\": \"\"");
        Pattern pageNumberPattern = Pattern.compile("\"pageNumber\": 1");
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        PaginatorResponse response = objectUnderTest.getTransactionsFor(transactionalAccount, 1);
        // then
        Mockito.verify(requestBuilder).body(stringArgumentCaptor.capture());
        Assert.assertTrue(emptyUUIDPattern.matcher(stringArgumentCaptor.getValue()).find());
        Assert.assertTrue(pageNumberPattern.matcher(stringArgumentCaptor.getValue()).find());
        Assert.assertFalse(response.getTinkTransactions().isEmpty());
        Assert.assertEquals(2, response.getTinkTransactions().size());
        Assert.assertTrue(response.canFetchMore().get());
    }

    @Test
    public void getTransactionsForShouldReturnTransactionsWithNoMoreAvailable() {
        // given
        Pattern emptyUUIDPattern =
                Pattern.compile("\"uuid\": \"193648e9-ddc6-4092-a3a5-e10b69fa2cb8\"");
        Pattern pageNumberPattern = Pattern.compile("\"pageNumber\": 2");
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        objectUnderTest.getTransactionsFor(transactionalAccount, 1);
        PaginatorResponse response = objectUnderTest.getTransactionsFor(transactionalAccount, 2);
        // then
        Mockito.verify(requestBuilder, Mockito.times(2)).body(stringArgumentCaptor.capture());
        Assert.assertTrue(emptyUUIDPattern.matcher(stringArgumentCaptor.getValue()).find());
        Assert.assertTrue(pageNumberPattern.matcher(stringArgumentCaptor.getValue()).find());
        Assert.assertFalse(response.getTinkTransactions().isEmpty());
        Assert.assertEquals(2, response.getTinkTransactions().size());
        Assert.assertTrue(response.canFetchMore().get());
    }
}
