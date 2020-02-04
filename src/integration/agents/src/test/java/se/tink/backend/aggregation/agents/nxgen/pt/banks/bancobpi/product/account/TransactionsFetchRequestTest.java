package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class TransactionsFetchRequestTest {

    private static final String MODULE_VERSION = "testModuleVersion";
    private static final String ACCOUNT_ORDER = "testAccountOrder";
    private static final String ACCOUNT_TYPE = "testAccountType";
    private static final String ACCOUNT_INTERNAL_ID = "12345";

    private BancoBpiAuthContext authContext;
    private BancoBpiEntityManager entityManager;
    private TransactionalAccount transactionalAccount;
    private BancoBpiAccountsContext accountsContext;
    private TinkHttpClient httpClient;
    private RequestBuilder requestBuilder;
    private TransactionalAccountBaseInfo accountBaseInfo;

    @Before
    public void init() {
        httpClient = Mockito.mock(TinkHttpClient.class);
        requestBuilder = Mockito.mock(RequestBuilder.class);
        accountsContext = Mockito.mock(BancoBpiAccountsContext.class);
        authContext = Mockito.mock(BancoBpiAuthContext.class);
        entityManager = Mockito.mock(BancoBpiEntityManager.class);
        Mockito.when(entityManager.getAuthContext()).thenReturn(authContext);
        Mockito.when(entityManager.getAccountsContext()).thenReturn(accountsContext);
        Mockito.when(authContext.getModuleVersion()).thenReturn(MODULE_VERSION);
        initAccount();
    }

    private void initAccount() {
        transactionalAccount = Mockito.mock(TransactionalAccount.class);
        Mockito.when(transactionalAccount.getAccountNumber()).thenReturn(ACCOUNT_INTERNAL_ID);
        accountBaseInfo = new TransactionalAccountBaseInfo();
        accountBaseInfo.setInternalAccountId(ACCOUNT_INTERNAL_ID);
        accountBaseInfo.setOrder(ACCOUNT_ORDER);
        accountBaseInfo.setType(ACCOUNT_TYPE);
    }

    @Test
    public void withBodyShouldCreateProperBody() throws RequestException, JSONException {
        // given
        final int pageNo = 2;
        final String fetchingUUID = "fetchingUUID";
        Mockito.when(accountsContext.findAccountInfoByNumber(ACCOUNT_INTERNAL_ID))
                .thenReturn(Optional.of(accountBaseInfo));
        TransactionsFetchRequest objectUnderTest =
                new TransactionsFetchRequest(
                        entityManager, fetchingUUID, pageNo, transactionalAccount);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        objectUnderTest.withBody(httpClient, requestBuilder);
        // then
        Mockito.verify(requestBuilder).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        Assert.assertEquals(
                MODULE_VERSION, body.getJSONObject("versionInfo").getString("moduleVersion"));
        JSONObject variables = body.getJSONObject("screenData").getJSONObject("variables");
        Assert.assertEquals(pageNo, variables.getJSONObject("Paginacao").getInt("pageNumber"));
        Assert.assertEquals(fetchingUUID, variables.getJSONObject("Paginacao").getString("uuid"));
        JSONObject conta = variables.getJSONObject("Conta");
        Assert.assertEquals(ACCOUNT_INTERNAL_ID, conta.getString("nuc"));
        Assert.assertEquals(ACCOUNT_TYPE, conta.getString("tipo"));
        Assert.assertEquals(ACCOUNT_ORDER, conta.getString("ordem"));
    }

    @Test(expected = RequestException.class)
    public void shouldThrowExceptionWhenAccountDoesNotExist() throws RequestException {
        // given
        final int pageNo = 2;
        final String fetchingUUID = "fetchingUUID";
        Mockito.when(accountsContext.findAccountInfoByNumber(ACCOUNT_INTERNAL_ID))
                .thenReturn(Optional.empty());
        // when
        new TransactionsFetchRequest(entityManager, fetchingUUID, pageNo, transactionalAccount);
        // then
    }
}
