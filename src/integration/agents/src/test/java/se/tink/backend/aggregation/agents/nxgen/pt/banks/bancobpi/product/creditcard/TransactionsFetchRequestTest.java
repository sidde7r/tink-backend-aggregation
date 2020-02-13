package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class TransactionsFetchRequestTest {

    private TinkHttpClient httpClient;
    private RequestBuilder requestBuilder;
    private BancoBpiEntityManager entityManager;
    private BancoBpiAuthContext authContext;
    private BancoBpiAccountsContext accountsContext;
    private CreditCardAccount account;

    @Before
    public void init() {
        httpClient = Mockito.mock(TinkHttpClient.class);
        requestBuilder = Mockito.mock(RequestBuilder.class);
        entityManager = Mockito.mock(BancoBpiEntityManager.class);
        authContext = Mockito.mock(BancoBpiAuthContext.class);
        accountsContext = Mockito.mock(BancoBpiAccountsContext.class);
        Mockito.when(entityManager.getAuthContext()).thenReturn(authContext);
        Mockito.when(entityManager.getAccountsContext()).thenReturn(accountsContext);
        account = Mockito.mock(CreditCardAccount.class);
    }

    @Test
    public void withBodyShouldCreateProperBody() throws JSONException {
        // given
        final String expectedModuleVersion = "testModuleVersion";
        final String expectedNip = "1234567";
        final String cardNo = "1234123412341234";
        Mockito.when(authContext.getModuleVersion()).thenReturn(expectedModuleVersion);
        Mockito.when(accountsContext.getNip()).thenReturn(expectedNip);
        Mockito.when(account.getAccountNumber()).thenReturn(cardNo);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        TransactionsFetchRequest objectUnderTest =
                new TransactionsFetchRequest(entityManager, account, 1, "");
        // when
        objectUnderTest.withBody(requestBuilder);
        // then
        Mockito.verify(requestBuilder).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        Assert.assertEquals("BPI.ContasECartoes", body.getString("viewName"));
        JSONObject variables = body.getJSONObject("screenData").getJSONObject("variables");
        Assert.assertEquals(expectedNip, variables.getString("NIP"));
        Assert.assertEquals(
                cardNo,
                variables.getJSONObject("Cartao").getJSONObject("Cartao").getString("contaCartao"));
        Assert.assertEquals(1, variables.getJSONObject("Paginacao").getInt("pageNumber"));
        Assert.assertTrue(variables.getJSONObject("Paginacao").getString("uuid").isEmpty());
    }
}
