package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import com.google.common.collect.Lists;
import javax.ws.rs.core.NewCookie;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class PinAuthenticationRequestTest {

    private static final String REQUEST_EXPECTED =
            "{\"versionInfo\":{\"moduleVersion\":\"fkXs_sGDv6trPlpganAgKA\",\"apiVersion\":\"Px0uCRvJc6Tj7RQjvILrUg\"},\"viewName\":\"Common.Login\",\"inputParameters\":{\"Pin\":\"1234\",\"IdDispositivo\":\"1234567890\",\"Device\":{\"CordovaVersion\":\"4.5.5\",\"Model\":\"iPhone9,3\",\"Platform\":\"iOS\",\"UUID\":\"1234567890\",\"Version\":\"12.4\",\"Manufacturer\":\"Apple\",\"IsVirtual\":false,\"Serial\":\"unknown\"}}}";
    private static final String RESPONSE_CORRECT =
            "{\"versionInfo\":{\"hasModuleVersionChanged\":false,\"hasApiVersionChanged\":false},\"data\":{\"TransactionStatus\":{\"TransactionStatus\":{\"OperationStatusId\":1,\"TransactionErrors\":{\"List\":[],\"EmptyListItem\":{\"TransactionError\":{\"Source\":\"\",\"Code\":\"\",\"Level\":0,\"Description\":\"\"}}},\"AuthStatusReason\":{\"List\":[],\"EmptyListItem\":{\"Status\":\"\",\"Code\":\"\",\"Description\":\"\"}}}},\"BackgroundIdleTime\":90,\"NIP\":7052707,\"MostrarCondicoes\":false,\"rlContas\":{\"List\":[{\"IS_ContaSmall\":{\"nuc\":\"4736917\",\"tipo\":\"000\",\"ordem\":\"001\"},\"ContaFormatada\":\"5-4736917.000.001\",\"IBAN\":\"PT50\",\"Moeda\":\"EUR\",\"ContaContexto\":true,\"ClienteContaId\":201423633}]},\"NomeCliente\":\"ANDRE CANHA\",\"PdfBackgroundIdleTime\":300,\"Environment\":\"\",\"CardsGonow\":true,\"CardsVersion\":\"\",\"PagamentosOverlayCount\":0,\"NumeroContratoAdesao\":\"7052707\",\"Flag201802951PossibilidadeAlteracaoReferencia\":true,\"Flag201807854PartilhaMovimentos\":true,\"FundosOverlayCount\":2,\"FinancasPessoaisOverlayCount\":1,\"TransferenciasMBWayOverlayCount\":2,\"TransferirTelemovelOverlayCount\":1,\"Flag201808899ContaValor\":true,\"Flag201808899ContaValorListaNips\":\"\",\"VendaAssessoradaAtiva\":true,\"Flag201811169CreditoPessoalFaleConnosco\":false,\"AgendaFinanceiraOverlayCount\":2,\"DMIFNumeroAcessosPagina\":0,\"DMIFNumeroAcessosPaginaMinimo\":1,\"DMIFPodeMostrarPagina\":true,\"FlagDebugLogs\":false,\"OsMeusContratosMobile\":false,\"HotLeads_QueroSerContactadoCreditoImediatoMobile\":true,\"HotLeads_QueroSerContactadoCartaoCreditoMobile\":true,\"HotLeads_QueroSerContactadoProdutoPrestigioMobile\":true,\"HotLeads_CreditoPessoalContratacaoMobile\":\"False\",\"DesignacaoMobileCashAdvance\":\"Adiantamento a Crédito\",\"DesignacaoCashAdvance\":\"Adiantamento de numerário a crédito (Cash Advance)\",\"RGPDRespondido\":true,\"TipoCliente\":\"Particulares\",\"ShowRGPDLogin\":false},\"rolesInfo\":\"gWKLCzXOu0yEcDnK4Y4jUQ*VAHDPVL_JkaI4lJqaY79lg,or+aCwDRMkuqChXJqxTLPw*w3cYTVWz7EWtQf74Jch5+g,or+aCwDRMkuqChXJqxTLPw*nAmOk1KoXUmsSFCJ+irTpg,or+aCwDRMkuqChXJqxTLPw*lD0ixuC+p06ZqP+DLM4H+w,4HBXEhdjO0C6wkxQjoynNA*QskFZaN0KE6Mdm_sZamA4g,4HBXEhdjO0C6wkxQjoynNA*WGXCvZIyzkiT3q0+Z7wB3w,FTw_GrZqkEeROCD+a+mg2A*qbUEagifkEeyGELFOqcTiw,FTw_GrZqkEeROCD+a+mg2A*5Z6K1WyCQUula94HQSwD8A,9cgKKQujt0yYG8dYMtfK5w*cVXhAWU3vEesp2lftBq9Vw,okJdM75+oUqtuAZ823kOTA*dNT_PRwuZUGlT5jU7oRnmg,okJdM75+oUqtuAZ823kOTA*LeX_9AmFTkqm5ZruOHD21w,t8kNPqDUm0a5HRCCDvDUrA*IjhgMNNaakeGBH3oAIhvWA,t8kNPqDUm0a5HRCCDvDUrA*9u06nfXtX0G9wDcyaqEHCw,t8kNPqDUm0a5HRCCDvDUrA*KidtKZhpZ0ytglJEXgM28w,7Gw1VgiT7kuaOvGpepxQpw*KWluZmb1I0CpQ28ZLH_u6w,7Gw1VgiT7kuaOvGpepxQpw*nBPsnhk7S0uKIc+13Ely1A,7Gw1VgiT7kuaOvGpepxQpw*KufM+l1i2EiY2OOpNUOxNQ,4mz9aKXbDEewnLtzY7KJkw*91EdAcVZqUuIobXHDN3Yyg,X1zhfXRt8E6pQ3twspQehg*bBdHWeFiLUmO6Ct5MvERwA,KBP9mDjK7EGenMsJkv1Nsw*XYPtBOyxpUO3mkJlh2Z0ZQ,KBP9mDjK7EGenMsJkv1Nsw*wJkuuyC2b0SQLBwFqP4vdg,84vXq3EoD0C9wQkILPLThg*USAotu2iikCf2Ynwsnsj2A,84vXq3EoD0C9wQkILPLThg*gHvnu6vf6Eq7zCjLptR45w,84vXq3EoD0C9wQkILPLThg*PAPZu2BOxUGT85V3gLwG1w,l02Org0JuEevnmsIQ94rig*gtujwpaCEkOraLE9fvEgQg,l02Org0JuEevnmsIQ94rig*VNKa9Cz0t0OcywzxzVEmaw,a3g+utukWkCGocm01mIe4g*mFzAWtTLDEm2d8kZOJzSDw,FKkI1ty0KUe3VA0O89lijA*KYp9iqg07UO4MMpifJzMiA,FKkI1ty0KUe3VA0O89lijA*h1BVwPMIYUS7Cfa+uTJWEA,FKkI1ty0KUe3VA0O89lijA*SaRw3CMz70SQfgF4Lt6h3g,\"}";
    private static final String RESPONSE_INCORRECT =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"TransactionStatus\": {\"TransactionStatus\": {\"OperationStatusId\": 3,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [{\"Status\": \"Failed\",\"Code\": \"CIPL_0052\",\"Description\": \"PIN Incorrecto. Número de tentativas disponíveis: 3\"}]}}},\"BackgroundIdleTime\": 0,\"NIP\": 0,\"MostrarCondicoes\": false,\"rlContas\": {\"List\": [],\"EmptyListItem\": {\"IS_ContaSmall\": {\"nuc\": \"\",\"tipo\": \"\",\"ordem\": \"\"},\"ContaFormatada\": \"\",\"IBAN\": \"\",\"Moeda\": \"\",\"ContaContexto\": false,\"ClienteContaId\": 0}},\"NomeCliente\": \"\",\"PdfBackgroundIdleTime\": 0,\"Environment\": \"\",\"CardsGonow\": false,\"CardsVersion\": \"\",\"PagamentosOverlayCount\": 0,\"NumeroContratoAdesao\": \"\",\"Flag201802951PossibilidadeAlteracaoReferencia\": false,\"Flag201807854PartilhaMovimentos\": false,\"FundosOverlayCount\": 0,\"FinancasPessoaisOverlayCount\": 0,\"TransferenciasMBWayOverlayCount\": 0,\"TransferirTelemovelOverlayCount\": 0,\"Flag201808899ContaValor\": false,\"Flag201808899ContaValorListaNips\": \"\",\"VendaAssessoradaAtiva\": false,\"Flag201811169CreditoPessoalFaleConnosco\": false,\"AgendaFinanceiraOverlayCount\": 0,\"DMIFNumeroAcessosPagina\": 0,\"DMIFNumeroAcessosPaginaMinimo\": 0,\"DMIFPodeMostrarPagina\": false,\"FlagDebugLogs\": false,\"OsMeusContratosMobile\": false,\"HotLeads_QueroSerContactadoCreditoImediatoMobile\": false,\"HotLeads_QueroSerContactadoCartaoCreditoMobile\": false,\"HotLeads_QueroSerContactadoProdutoPrestigioMobile\": false,\"HotLeads_CreditoPessoalContratacaoMobile\": \"\",\"DesignacaoMobileCashAdvance\": \"\",\"DesignacaoCashAdvance\": \"\",\"RGPDRespondido\": true,\"TipoCliente\": \"\",\"ShowRGPDLogin\": false}}";
    private static final String RESPONSE_UNEXPECTED =
            "{\"data\": {},\"exception\": {\"name\": \"ServerException\",\"specificType\": \"OutSystems.RESTService.ErrorHandling.ExposeRestException\",\"message\": \"Invalid Login\"}}";
    private static final String PIN = "1234";
    private static final String DEVICE_UUID = "1234567890";
    private static final String MODULE_VERSION = "fkXs_sGDv6trPlpganAgKA";

    private RequestBuilder requestBuilder;
    private BancoBpiAuthContext authContext;
    private BancoBpiAccountsContext transactionalAccountsInfo;
    private PinAuthenticationRequest objectUnderTest;

    @Before
    public void init() {
        requestBuilder = Mockito.mock(RequestBuilder.class);
        transactionalAccountsInfo = Mockito.mock(BancoBpiAccountsContext.class);
        initUserState();
        BancoBpiEntityManager entityManager = Mockito.mock(BancoBpiEntityManager.class);
        Mockito.when(entityManager.getAuthContext()).thenReturn(authContext);
        Mockito.when(entityManager.getAccountsContext()).thenReturn(transactionalAccountsInfo);
        objectUnderTest = new PinAuthenticationRequest(entityManager);
    }

    private void initUserState() {
        authContext = Mockito.mock(BancoBpiAuthContext.class);
        Mockito.when(authContext.getAccessPin()).thenReturn(PIN);
        Mockito.when(authContext.getDeviceUUID()).thenReturn(DEVICE_UUID);
        Mockito.when(authContext.getModuleVersion()).thenReturn(MODULE_VERSION);
    }

    @Test
    public void withBodyShouldPrepareProperBody() {
        // given
        RequestBuilder expectedRequestBuilder = Mockito.mock(RequestBuilder.class);
        Mockito.when(requestBuilder.body(REQUEST_EXPECTED)).thenReturn(expectedRequestBuilder);
        // when
        RequestBuilder result = objectUnderTest.withBody(requestBuilder);
        // then
        Assert.assertEquals(expectedRequestBuilder, result);
    }

    @Test
    public void executeShouldReturnSuccessResponse() throws RequestException {
        // given
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.getBody(String.class)).thenReturn(RESPONSE_CORRECT);
        Mockito.when(requestBuilder.post(HttpResponse.class)).thenReturn(httpResponse);
        NewCookie cookie = Mockito.mock(NewCookie.class);
        Mockito.when(cookie.getName()).thenReturn("nr2LB_BPIParticulares");
        Mockito.when(cookie.getValue())
                .thenReturn(
                        "crf%3dZ%2fQLQRJ71kPfSpIPm%2bwQD5HlenI%3d%3buid%3d531646%3bunm%3d7791041");
        Mockito.when(httpResponse.getCookies()).thenReturn(Lists.newArrayList(cookie));
        // when
        LoginResponse result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals("Z/QLQRJ71kPfSpIPm+wQD5HlenI=", result.getCsrfToken());
    }

    @Test
    public void executeShouldReturnFailedResponse() throws RequestException {
        // given
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.getBody(String.class)).thenReturn(RESPONSE_INCORRECT);
        Mockito.when(requestBuilder.post(HttpResponse.class)).thenReturn(httpResponse);
        NewCookie cookie = Mockito.mock(NewCookie.class);
        Mockito.when(cookie.getName()).thenReturn("nr2LB_BPIParticulares");
        Mockito.when(cookie.getValue())
                .thenReturn(
                        "crf%3dZ%2fQLQRJ71kPfSpIPm%2bwQD5HlenI%3d%3buid%3d531646%3bunm%3d7791041");
        Mockito.when(httpResponse.getCookies()).thenReturn(Lists.newArrayList(cookie));
        // when
        LoginResponse result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals("Z/QLQRJ71kPfSpIPm+wQD5HlenI=", result.getCsrfToken());
    }

    @Test(expected = RequestException.class)
    public void executeShouldThrowLoginException() throws RequestException {
        // given
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.getBody(String.class)).thenReturn(RESPONSE_UNEXPECTED);
        Mockito.when(requestBuilder.post(HttpResponse.class)).thenReturn(httpResponse);
        // when
        objectUnderTest.execute(requestBuilder);
    }
}
