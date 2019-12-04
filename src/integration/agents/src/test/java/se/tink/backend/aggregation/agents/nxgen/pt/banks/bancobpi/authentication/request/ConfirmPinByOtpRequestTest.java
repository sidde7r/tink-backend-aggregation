package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.BancoBpiUserState;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.MobileChallengeRequestedToken;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class ConfirmPinByOtpRequestTest {

    private static final String MOBILE_ID = "MobileId";
    private static final String MOBILE_PHONE_NO = "123456789";
    private static final String MOBILE_PROCESS_ON = "processOn";
    private static final String MOBILE_UUID = "1234567890";
    private static final String MOBILE_REPLY_WITH = "ReplyWith";
    private static final String DEVICE_UUID = "123456123456";
    private static final String PIN = "1234";
    private static final String OTP = "123456";
    private static final String REQUEST_BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"gS+lXxFxC_wWYvNlPJM_Qw\",\"apiVersion\": \"jR8qd1rTdzHYUcSU5Wk3nA\"},\"viewName\": \"Fiabilizacao.Code\",\"inputParameters\": {\"IdDispositivo\": \"%s\",\"Pin\": \"%s\",\"MobileChallengeResponse\": {\"Id\": \"%s\",\"Response\": \"{\\\"id\\\":\\\"%s\\\",\\\"data\\\":[{\\\"requestedOTP\\\":\\\"%s\\\",\\\"mobilePhoneNumber\\\":\\\"%s\\\",\\\"processedOn\\\":\\\"%s\\\",\\\"processedOnSpecified\\\":true,\\\"uuid\\\":\\\"%s\\\",\\\"replywith\\\":\\\"%s\\\"}]}\"}}}";
    private static final String REQUEST =
            String.format(
                    REQUEST_BODY_TEMPLATE,
                    DEVICE_UUID,
                    PIN,
                    MOBILE_ID,
                    MOBILE_ID,
                    OTP,
                    MOBILE_PHONE_NO,
                    MOBILE_PROCESS_ON,
                    MOBILE_UUID,
                    MOBILE_REPLY_WITH);
    private static final String RESPONSE_CORRECT =
            "{\"versionInfo\":{\"hasModuleVersionChanged\":false,\"hasApiVersionChanged\":false},\"data\":{\"TransactionStatus\":{\"TransactionStatus\":{\"OperationStatusId\":1,\"TransactionErrors\":{\"List\":[],\"EmptyListItem\":{\"TransactionError\":{\"Source\":\"\",\"Code\":\"\",\"Level\":0,\"Description\":\"\"}}},\"AuthStatusReason\":{\"List\":[],\"EmptyListItem\":{\"Status\":\"\",\"Code\":\"\",\"Description\":\"\"}}}},\"MobileChallenge\":{\"Id\":0,\"CreationDate\":\"1900-01-01T00:00:00\",\"UUID\":\"\",\"MobileChallengeRequestedToken\":{\"List\":[],\"EmptyListItem\":{\"UUID\":\"\",\"TokenDefinition\":\"\",\"ChallengeTokenType\":\"\"}}},\"RequestValid\":true}}";
    private static final String RESPONSE_INCORRECT =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"TransactionStatus\": {\"TransactionStatus\": {\"OperationStatusId\": 3,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [{\"Status\": \"Failed\",\"Code\": \"CIPL_0001\",\"Description\": \"Acesso inválido (Identificador de dispositivo ou Pin errados).\"}]}}},\"IsMockUser\": false,\"EstadoRegisto\": 0,\"MudarPassword\": false}}";
    private static final String RESPONSE_UNEXPECTED =
            "{\"data\": {},\"exception\": {\"name\": \"ServerException\",\"specificType\": \"OutSystems.RESTService.ErrorHandling.ExposeRestException\",\"message\": \"Invalid Login\"}}";
    private RequestBuilder requestBuilder;
    private TinkHttpClient httpClient;
    private BancoBpiUserState userState;
    private ConfirmPinByOtpRequest objectUnderTest;
    private MobileChallengeRequestedToken mobileChallengeRequestedToken;

    @Before
    public void init() {
        requestBuilder = Mockito.mock(RequestBuilder.class);
        httpClient = Mockito.mock(TinkHttpClient.class);
        initMobileChallengeRequestedToken();
        initUserState();
        objectUnderTest = new ConfirmPinByOtpRequest(userState, OTP);
    }

    private void initUserState() {
        userState = Mockito.mock(BancoBpiUserState.class);
        Mockito.when(userState.getAccessPin()).thenReturn(PIN);
        Mockito.when(userState.getDeviceUUID()).thenReturn(DEVICE_UUID);
        Mockito.when(userState.getAccessPin()).thenReturn(PIN);
        Mockito.when(userState.getSessionCSRFToken()).thenReturn("ewporegoijvkldsjkcso");
        Mockito.when(userState.getMobileChallengeRequestedToken())
                .thenReturn(mobileChallengeRequestedToken);
    }

    private void initMobileChallengeRequestedToken() {
        mobileChallengeRequestedToken = new MobileChallengeRequestedToken();
        mobileChallengeRequestedToken.setReplyWith(MOBILE_REPLY_WITH);
        mobileChallengeRequestedToken.setProcessedOn(MOBILE_PROCESS_ON);
        mobileChallengeRequestedToken.setPhoneNumber(MOBILE_PHONE_NO);
        mobileChallengeRequestedToken.setId(MOBILE_ID);
        mobileChallengeRequestedToken.setUuid(MOBILE_UUID);
    }

    @Test
    public void withBodyShouldCreateProperBody() {
        // given
        RequestBuilder expectedResponseBuilder = Mockito.mock(RequestBuilder.class);
        Mockito.when(requestBuilder.body(REQUEST)).thenReturn(expectedResponseBuilder);
        // when
        RequestBuilder result = objectUnderTest.withBody(httpClient, requestBuilder);
        // then
        Assert.assertEquals(expectedResponseBuilder, result);
    }

    @Test
    public void executeShouldReturnSuccessResponse() throws LoginException {
        // given
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_CORRECT);
        // when
        AuthenticationResponse result = objectUnderTest.execute(requestBuilder, httpClient);
        // then
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void executeShouldReturnFailedResponse() throws LoginException {
        // given
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_INCORRECT);
        // when
        AuthenticationResponse result = objectUnderTest.execute(requestBuilder, httpClient);
        // then
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals("CIPL_0001", result.getCode());
    }

    @Test(expected = LoginException.class)
    public void executeShouldThrowLoginException() throws LoginException {
        // given
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_UNEXPECTED);
        // when
        objectUnderTest.execute(requestBuilder, httpClient);
    }
}
