package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.LoginResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.agents.wiremock.WireMockIntegrationTest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class LoginPINInitRequestTest extends WireMockIntegrationTest {

    private BPostBankAuthContext authContextMock;
    private LoginPINInitRequest objectUnderTest;
    private RequestBuilder requestBuilder;

    @Before
    public void init() {
        authContextMock = Mockito.mock(BPostBankAuthContext.class);
        objectUnderTest = new LoginPINInitRequest(authContextMock);
        requestBuilder = httpClient.request(getOrigin() + LoginPINInitRequest.URL_PATH);
    }

    @Test
    public void withBodyShouldCreateProperBody() throws JSONException {
        // given
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(authContextMock.getDeviceInstallationId())
                .thenReturn("4TAOD0R2I278HHCI9EPN33HX6KY2YJ5B3RN3MPG812NK3GKT3SQFIX3YEQND2DS0");
        Mockito.when(authContextMock.getLogin()).thenReturn("12345678");
        Mockito.when(authContextMock.getDeviceUniqueId())
                .thenReturn("CF07551B-F88E-4BEB-BDFA-94343AA7D077");
        // when
        objectUnderTest.withBody(requestBuilder);
        // then
        Mockito.verify(requestBuilder).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        Assert.assertEquals(
                authContextMock.getLogin(), body.getJSONObject("user").getString("loginName"));
        JSONObject securityContext = body.getJSONObject("securityContext");
        Assert.assertEquals(
                "$DEVICE_INSTALLATION_ID", securityContext.getString("DeviceInstallationID"));
        Assert.assertEquals("3", securityContext.getString("authenticationType"));
        Assert.assertEquals(
                authContextMock.getDeviceInstallationId(),
                securityContext.getString("deviceInstallationID"));
        Assert.assertEquals(
                authContextMock.getDeviceUniqueId(), securityContext.getString("deviceUniqueID"));
        Assert.assertEquals("1", securityContext.getString("deviceType"));
        Assert.assertEquals("nl", body.getString("language"));
    }

    @Test
    public void executeShouldReturnSuccessLoginResponseDTO() throws RequestException {
        // given
        final String responseBody =
                "{\"UserSessionImpl\": {\"state\": \"3000\",\"isError\": false,\"credentials\": {},\"sessionId\": \"k5_l1FbANE2vOEI2MtMB6l4BF5rKADaExeHDNeK3\",\"sessionToken\": \"HYFVFKI5W1ORU4GP7XL4RWF196TGVRYM\",\"language\": \"nl\",\"securityContext\": {\"authenticationType\": \"PIN\",\"businessChannel\": \"B2C\",\"communicationMode\": \"MOB\",\"company\": \"1\",\"project\": \"SOPRABANKING\",\"deviceInstallationID\": \"4TAOD0R2I278HHCI9EPN33HX6KY2YJ5B3RN3MPG812NK3GKT3SQFIX3YEQND2DS0\",\"deviceType\": \"SPH\",\"deviceUniqueID\": \"CF07551B-F88E-4BEB-BDFA-94343AA7D077\"},\"contentLocked\": false,\"identificationToken\": {},\"auditData\": {\"startDate\": \"2020-02-11T12:59:29.476+0000\"},\"reportAvailable\": false,\"feedbackList\": {\"feedbacks\": []},\"user\": {\"loginName\": \"01975233\"}}}";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/bpb/services/rest/v2/loginPINinit"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "application/json")));
        // when
        LoginResponseDTO result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertEquals("HYFVFKI5W1ORU4GP7XL4RWF196TGVRYM", result.getSessionToken());
        Assert.assertEquals("k5_l1FbANE2vOEI2MtMB6l4BF5rKADaExeHDNeK3", result.getSessionId());
        Assert.assertFalse(result.isError());
    }
}
