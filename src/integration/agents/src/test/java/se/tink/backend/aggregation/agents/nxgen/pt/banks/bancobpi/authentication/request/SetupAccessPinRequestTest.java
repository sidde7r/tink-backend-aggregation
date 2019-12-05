package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiUserState;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class SetupAccessPinRequestTest {

    private static final String REQUEST =
            "{\"versionInfo\": {\"moduleVersion\": \"gS+lXxFxC_wWYvNlPJM_Qw\",\"apiVersion\": \"kLvBIVZMfpTpRxE83Y5Hnw\"},\"viewName\": \"Fiabilizacao.PIN\",\"inputParameters\": {\"Pin\": \"1234\",\"IdDispositivo\": \"12345567890\",\"MobileChallengeResponse\": {\"Id\": \"\",\"Response\": \"\"}}}";
    private static final String RESPONSE_CORRECT =
            "{\"versionInfo\":{\"hasModuleVersionChanged\":false,\"hasApiVersionChanged\":false},\"data\":{\"TransactionStatus\":{\"TransactionStatus\":{\"OperationStatusId\":4,\"TransactionErrors\":{\"List\":[],\"EmptyListItem\":{\"TransactionError\":{\"Source\":\"\",\"Code\":\"\",\"Level\":0,\"Description\":\"\"}}},\"AuthStatusReason\":{\"List\":[{\"Status\":\"Failed\",\"Code\":\"AMGR_PUB_0001\",\"Description\":\"Pedido de autorização criado.\"}]}}},\"MobileChallenge\":{\"Id\":44257566,\"CreationDate\":\"1900-01-01T00:00:00\",\"UUID\":\"7ffccbca-bfa9-4257-af46-8e4f790d8434\",\"MobileChallengeRequestedToken\":{\"List\":[{\"UUID\":\"83035bfe-7fb7-464b-8f15-41c5e5a89140\",\"TokenDefinition\":\"{\\\"mobilePhoneNumber\\\":\\\"003519xxxx0065\\\",\\\"processedOn\\\":\\\"2019-10-10T11:06:54.924Z\\\",\\\"processedOnSpecified\\\":true,\\\"uuid\\\":\\\"83035bfe-7fb7-464b-8f15-41c5e5a89140\\\",\\\"replywith\\\":\\\"SMSResponseTokenType\\\"}\",\"ChallengeTokenType\":\"SMSChallengeTokenType\"}]}},\"RequestValid\":true}}";
    private static final String RESPONSE_INCORRECT =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"TransactionStatus\": {\"TransactionStatus\": {\"OperationStatusId\": 3,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [{\"Status\": \"Failed\",\"Code\": \"CIPL_0052\",\"Description\": \"PIN Incorrecto. Número de tentativas disponíveis: 3\"}]}}},\"BackgroundIdleTime\": 0,\"NIP\": 0,\"MostrarCondicoes\": false,\"rlContas\": {\"List\": [],\"EmptyListItem\": {\"IS_ContaSmall\": {\"nuc\": \"\",\"tipo\": \"\",\"ordem\": \"\"},\"ContaFormatada\": \"\",\"IBAN\": \"\",\"Moeda\": \"\",\"ContaContexto\": false,\"ClienteContaId\": 0}},\"NomeCliente\": \"\",\"PdfBackgroundIdleTime\": 0,\"Environment\": \"\",\"CardsGonow\": false,\"CardsVersion\": \"\",\"PagamentosOverlayCount\": 0,\"NumeroContratoAdesao\": \"\",\"Flag201802951PossibilidadeAlteracaoReferencia\": false,\"Flag201807854PartilhaMovimentos\": false,\"FundosOverlayCount\": 0,\"FinancasPessoaisOverlayCount\": 0,\"TransferenciasMBWayOverlayCount\": 0,\"TransferirTelemovelOverlayCount\": 0,\"Flag201808899ContaValor\": false,\"Flag201808899ContaValorListaNips\": \"\",\"VendaAssessoradaAtiva\": false,\"Flag201811169CreditoPessoalFaleConnosco\": false,\"AgendaFinanceiraOverlayCount\": 0,\"DMIFNumeroAcessosPagina\": 0,\"DMIFNumeroAcessosPaginaMinimo\": 0,\"DMIFPodeMostrarPagina\": false,\"FlagDebugLogs\": false,\"OsMeusContratosMobile\": false,\"HotLeads_QueroSerContactadoCreditoImediatoMobile\": false,\"HotLeads_QueroSerContactadoCartaoCreditoMobile\": false,\"HotLeads_QueroSerContactadoProdutoPrestigioMobile\": false,\"HotLeads_CreditoPessoalContratacaoMobile\": \"\",\"DesignacaoMobileCashAdvance\": \"\",\"DesignacaoCashAdvance\": \"\",\"RGPDRespondido\": true,\"TipoCliente\": \"\",\"ShowRGPDLogin\": false}}";
    private static final String RESPONSE_UNEXPECTED =
            "{\"data\": {},\"exception\": {\"name\": \"ServerException\",\"specificType\": \"OutSystems.RESTService.ErrorHandling.ExposeRestException\",\"message\": \"Invalid Login\"}}";
    private static final String PIN = "1234";
    private static final String DEVICE_UUID = "12345567890";
    private BancoBpiUserState userState;
    private RequestBuilder requestBuilder;
    private TinkHttpClient httpClient;
    private SetupAccessPinRequest objectUnderTest;

    @Before
    public void init() {
        userState = Mockito.mock(BancoBpiUserState.class);
        Mockito.when(userState.getAccessPin()).thenReturn(PIN);
        Mockito.when(userState.getDeviceUUID()).thenReturn(DEVICE_UUID);
        Mockito.when(userState.getSessionCSRFToken()).thenReturn("dlksfhewifhjwi");
        requestBuilder = Mockito.mock(RequestBuilder.class);
        httpClient = Mockito.mock(TinkHttpClient.class);
        objectUnderTest = new SetupAccessPinRequest(userState);
    }

    @Test
    public void withBodyShouldReturnProperBody() {
        // given
        RequestBuilder expectedRequestBuider = Mockito.mock(RequestBuilder.class);
        Mockito.when(requestBuilder.body(REQUEST)).thenReturn(expectedRequestBuider);
        // when
        RequestBuilder result = objectUnderTest.withBody(httpClient, requestBuilder);
        // then
        Assert.assertEquals(expectedRequestBuider, result);
    }

    @Test
    public void executeShouldReturnCorrectResponse() throws LoginException {
        // given
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_CORRECT);
        // when
        SetupAccessPinResponse result = objectUnderTest.execute(requestBuilder, httpClient);
        // then
        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals("44257566", result.getMobileChallengeRequestedToken().getId());
        Assert.assertEquals(
                "83035bfe-7fb7-464b-8f15-41c5e5a89140",
                result.getMobileChallengeRequestedToken().getUuid());
        Assert.assertEquals(
                "003519xxxx0065", result.getMobileChallengeRequestedToken().getPhoneNumber());
        Assert.assertEquals(
                "2019-10-10T11:06:54.924Z",
                result.getMobileChallengeRequestedToken().getProcessedOn());
        Assert.assertEquals(
                "SMSResponseTokenType", result.getMobileChallengeRequestedToken().getReplyWith());
    }

    @Test
    public void executeShouldReturnFailedResponse() throws LoginException {
        // given
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_INCORRECT);
        // when
        SetupAccessPinResponse result = objectUnderTest.execute(requestBuilder, httpClient);
        // then
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals("CIPL_0052", result.getCode());
    }

    @Test(expected = LoginException.class)
    public void executeShouldThrowLoginException() throws LoginException {
        // given
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_UNEXPECTED);
        // when
        objectUnderTest.execute(requestBuilder, httpClient);
    }
}
