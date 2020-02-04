package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import java.math.BigDecimal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class TransactionalAccountBalanceRequestTest {

    private static final String RESPONSE_EXPECTED =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"ContaSaldoInfo\": {\"Saldo\": {\"montanteCativo\": \"0.00\",\"moedaConta\": \"EUR\",\"saldoContabilisticoConta\": \"321.74\",\"saldoDisponivelConta\": \"321.74\",\"valoresPendentes\": \"0.00\",\"codigoBalcao\": \"32\"},\"Erro\": false},\"SaldoCartao\": {\"HasSaldoError\": false,\"SaldoDisponivel\": \"\",\"SaldoExtractoInfo\": {\"nome\": \"\",\"saldoActual\": \"0.0\",\"montanteDividaUltimoExtracto\": \"0.0\",\"montanteMinimoPagamento\": \"0.0\",\"totalAutorizacoesAprovadas\": 0,\"totalAutorizacoesDeclinadas\": 0,\"plafondActual\": 0,\"plafondDisponivel\": 0,\"quantidadeAutorizacoesCurso\": 0,\"montanteAutorizacoesCurso\": \"0.0\",\"plafondCash\": 0,\"montantePlafondExcedido\": \"0.0\",\"estadoContaCartao\": \"\",\"cicloExtracto\": 0,\"montanteCashDisponivel\": 0,\"indicadorDebitoConta\": false,\"opcaoPagamento\": 0}},\"IsFetched\": true}}";
    private static final String RESPONSE_UNEXPECTED =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {},\"exception\": {\"name\": \"NotRegisteredException\",\"specificType\": \"LB_MobileCore.NotExpiredSession\",\"message\": \"Expired Session\"} }";

    private static final String MODULE_VERSION = "moduleVersion1234567890";
    private BancoBpiAuthContext authContext;
    private TinkHttpClient httpClient;
    private RequestBuilder requestBuilder;

    @Before
    public void init() {
        authContext = Mockito.mock(BancoBpiAuthContext.class);
        Mockito.when(authContext.getModuleVersion()).thenReturn(MODULE_VERSION);
        httpClient = Mockito.mock(TinkHttpClient.class);
        requestBuilder = Mockito.mock(RequestBuilder.class);
    }

    @Test
    public void withBodyShouldCreateProperBody() throws JSONException {
        // given
        final String titulo = "Conta 5-4736917.000.001";
        final String contaNuc = "4736917";
        final String contaTipo = "000";
        final String contaOrdem = "001";
        TransactionalAccountBaseInfo accountBaseInfo =
                Mockito.mock(TransactionalAccountBaseInfo.class);
        Mockito.when(accountBaseInfo.getAccountName()).thenReturn(titulo);
        Mockito.when(accountBaseInfo.getInternalAccountId()).thenReturn(contaNuc);
        Mockito.when(accountBaseInfo.getType()).thenReturn(contaTipo);
        Mockito.when(accountBaseInfo.getOrder()).thenReturn(contaOrdem);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        RequestBuilder result =
                new TransactionalAccountBalanceRequest(authContext, accountBaseInfo)
                        .withBody(httpClient, requestBuilder);
        // then
        Mockito.verify(requestBuilder).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        JSONObject obj = body.getJSONObject("versionInfo");
        Assert.assertEquals(MODULE_VERSION, obj.getString("moduleVersion"));
        JSONArray carouselStructureArray =
                body.getJSONObject("screenData")
                        .getJSONObject("variables")
                        .getJSONObject("CarouselStructure")
                        .getJSONArray("List");
        Assert.assertEquals(1, carouselStructureArray.length());
        obj = carouselStructureArray.getJSONObject(0);
        Assert.assertEquals(titulo, obj.getString("Titulo"));
        obj = obj.getJSONObject("Conta").getJSONObject("Conta");
        Assert.assertEquals(contaNuc, obj.getString("nuc"));
        Assert.assertEquals(contaTipo, obj.getString("tipo"));
        Assert.assertEquals(contaOrdem, obj.getString("ordem"));
    }

    @Test
    public void executeShouldReturnAccountBalance() throws RequestException {
        // given
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_EXPECTED);
        TransactionalAccountBaseInfo accountBaseInfo =
                Mockito.mock(TransactionalAccountBaseInfo.class);
        // when
        BigDecimal result =
                new TransactionalAccountBalanceRequest(authContext, accountBaseInfo)
                        .execute(requestBuilder, httpClient);
        // then
        Assert.assertEquals(new BigDecimal("321.74"), result);
    }

    @Test(expected = RequestException.class)
    public void executeShouldThrowExceptionWhenUnexpectedResponse() throws RequestException {
        // given
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_UNEXPECTED);
        TransactionalAccountBaseInfo accountBaseInfo =
                Mockito.mock(TransactionalAccountBaseInfo.class);
        // when
        BigDecimal result =
                new TransactionalAccountBalanceRequest(authContext, accountBaseInfo)
                        .execute(requestBuilder, httpClient);
        // then
    }
}
