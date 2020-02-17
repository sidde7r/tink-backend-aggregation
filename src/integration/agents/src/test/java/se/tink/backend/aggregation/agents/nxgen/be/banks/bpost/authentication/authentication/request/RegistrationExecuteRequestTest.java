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
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.WireMockIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.BPostBankAuthContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class RegistrationExecuteRequestTest extends WireMockIntegrationTest {

    private BPostBankAuthContext authContextMock;
    private RequestBuilder requestBuilder;
    private RegistrationExecuteRequest objectUnderTest;

    @Before
    public void init() {
        authContextMock = Mockito.mock(BPostBankAuthContext.class);
        requestBuilder = httpClient.request(getOrigin() + RegistrationExecuteRequest.URL_PATH);
        objectUnderTest = new RegistrationExecuteRequest(authContextMock);
    }

    @Test
    public void withBodyShouldCreateCorrectBody() throws JSONException {
        // given
        RequestBuilder requestBuilderMock = Mockito.mock(RequestBuilder.class);
        Mockito.when(authContextMock.getLogin()).thenReturn("12345678");
        Mockito.when(authContextMock.getOrderReference()).thenReturn("3VYTR7JTBOUBAAMJ");
        Mockito.when(authContextMock.getDeviceUniqueId())
                .thenReturn("544907C2-C487-4928-9BE8-482A09F3729C");
        Mockito.when(authContextMock.getEmail()).thenReturn("someone@tink.se");
        Mockito.when(authContextMock.getDeviceCredential())
                .thenReturn("eea700b93d1e52364ab4c467d68a11b68be89f0a12c03e3d073def7becd3150c");
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        objectUnderTest.withBody(requestBuilderMock);
        // then
        Mockito.verify(requestBuilderMock).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        Assert.assertEquals(authContextMock.getLogin(), body.getString("userID"));
        Assert.assertNotNull(body.getString("deviceVersion"));
        Assert.assertEquals(authContextMock.getOrderReference(), body.getString("orderReference"));
        Assert.assertEquals("1", body.getString("mobileRegistrationType"));
        Assert.assertEquals(authContextMock.getDeviceUniqueId(), body.getString("deviceUniqueID"));
        Assert.assertEquals(authContextMock.getEmail(), body.getString("bpoEmailAddress"));
        Assert.assertNotNull(body.getString("devicePlatform"));
        Assert.assertEquals("1", body.getString("deviceType"));
        Assert.assertNotNull(body.getString("deviceName"));
        Assert.assertEquals("nl", body.get("language"));
        Assert.assertEquals(
                authContextMock.getDeviceCredential(), body.getString("deviceCredential"));
        Assert.assertNotNull(body.getString("deviceAlias"));
    }

    @Test
    public void executeShouldReturnCorrectRegistrationResponseDTO() throws RequestException {
        // given
        final String responseBody =
                "{\"Order\": {\"credentials\": {\"challenge\": \"20553032\"},\"reference\": \"C0B03CWLY24K9ER2\",\"isError\": false,\"state\": \"9000\",\"sessionToken\": \"430K2HLBLDM9SYR3LX55Y7W6E7AL72VL\",\"goal\": {\"deviceInstallationID\": \"EYSRHMU69X3NQXW9SDVDZ84FX188WZXKUZS6FNS5RGYYBCMMSHMMQYWN59Q4MXZ6\"}}}";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/bpb/services/rest/v2/register/execute"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "application/json")));
        // when
        RegistrationResponseDTO result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertEquals("430K2HLBLDM9SYR3LX55Y7W6E7AL72VL", result.getSessionToken());
        Assert.assertEquals("C0B03CWLY24K9ER2", result.getOrderReference());
        Assert.assertEquals(
                "EYSRHMU69X3NQXW9SDVDZ84FX188WZXKUZS6FNS5RGYYBCMMSHMMQYWN59Q4MXZ6",
                result.getDeviceInstallationID());
        Assert.assertFalse(result.isError());
    }
}
