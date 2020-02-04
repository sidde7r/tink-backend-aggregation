package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class ProductDetailsRequestTest {

    private static final String MODULE_VERSION = "testModuleVersion";
    private static final String NIP = "23456789";
    private static final String NUC = "12345";
    private RequestBuilder requestBuilder;
    private TinkHttpClient httpClient;
    private BancoBpiEntityManager entityManager;
    private BancoBpiAccountsContext accountsContext;
    private BancoBpiAuthContext authContext;
    private BancoBpiProductData productData;
    private ProductDetailsRequest objectUnderTest;

    @Before
    public void init() {
        requestBuilder = Mockito.mock(RequestBuilder.class);
        httpClient = Mockito.mock(TinkHttpClient.class);
        entityManager = Mockito.mock(BancoBpiEntityManager.class);
        accountsContext = Mockito.mock(BancoBpiAccountsContext.class);
        authContext = Mockito.mock(BancoBpiAuthContext.class);
        Mockito.when(entityManager.getAccountsContext()).thenReturn(accountsContext);
        Mockito.when(entityManager.getAuthContext()).thenReturn(authContext);
        Mockito.when(authContext.getModuleVersion()).thenReturn(MODULE_VERSION);
        Mockito.when(accountsContext.getNip()).thenReturn(NIP);
        Mockito.when(accountsContext.getNuc()).thenReturn(NUC);
        productData = Mockito.mock(BancoBpiProductData.class);
        objectUnderTest = new ProductDetailsRequest(entityManager, productData);
    }

    @Test
    public void withBodyShouldCreateProperBody() throws JSONException {
        // given
        Mockito.when(productData.getCodeFamily()).thenReturn("codeFamily");
        Mockito.when(productData.getCode()).thenReturn("code");
        Mockito.when(productData.getCodeSubFamily()).thenReturn("codeSubFamily");
        Mockito.when(productData.getNumber()).thenReturn("number");
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        objectUnderTest.withBody(httpClient, requestBuilder);
        // then
        Mockito.verify(requestBuilder).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        Assert.assertEquals(
                MODULE_VERSION, body.getJSONObject("versionInfo").getString("moduleVersion"));
        Assert.assertEquals("Reconciliacao.DetalheDeProduto", body.getString("viewName"));
        JSONObject productDados =
                body.getJSONObject("inputParameters").getJSONObject("ProdutoDados");
        Assert.assertEquals(productData.getCodeFamily(), productDados.getString("CodigoFamilia"));
        Assert.assertEquals(NUC, productDados.getString("NUC"));
        Assert.assertEquals(NIP, productDados.getString("NIP"));
        Assert.assertEquals(
                productData.getCodeSubFamily(), productDados.getString("CodigoSubFamilia"));
        Assert.assertEquals(productData.getCode(), productDados.getString("CodigoProduto"));
        Assert.assertEquals(productData.getNumber(), productDados.getString("NumeroOperacao"));
    }

    @Test
    public void executeShouldReturnExpectedResponse() throws RequestException {
        // given
        Mockito.when(requestBuilder.post(String.class))
                .thenReturn(ProductDetailsResponseTest.RESPONSE_EXPECTED);
        // when
        ProductDetailsResponse response = objectUnderTest.execute(requestBuilder, httpClient);
        // then
        Assert.assertEquals(LocalDate.parse("2047-04-04"), response.getFinalDate());
        Assert.assertEquals(LocalDate.parse("2007-04-03"), response.getInitialDate());
        Assert.assertEquals(new BigDecimal("88500.00"), response.getInitialBalance());
        Assert.assertEquals("NAME SURNAME", response.getOwner());
    }
}
