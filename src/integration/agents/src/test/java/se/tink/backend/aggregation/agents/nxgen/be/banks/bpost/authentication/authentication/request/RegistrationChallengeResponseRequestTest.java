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
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class RegistrationChallengeResponseRequestTest extends WireMockIntegrationTest {

    private BPostBankAuthContext authContextMock;
    private RequestBuilder requestBuilder;

    @Before
    public void init() {
        authContextMock = Mockito.mock(BPostBankAuthContext.class);
        requestBuilder =
                httpClient.request(getOrigin() + RegistrationChallengeResponseRequest.PATH);
    }

    @Test
    public void withBodyShouldCreteProperBody() throws JSONException {
        // given
        final String signCode = "12341234";
        RequestBuilder requestBuilderMock = Mockito.mock(RequestBuilder.class);
        Mockito.when(authContextMock.getDeviceRootedHash())
                .thenReturn("c82b8a1cfbc2eb985960cee62ac5853aca700a2cbf376b43bc5ac76d38bf81c9");
        Mockito.when(authContextMock.getLogin()).thenReturn("12345678");
        Mockito.when(authContextMock.getOrderReference()).thenReturn("92QIZ1MGKQXALB9I");
        RegistrationChallengeResponseRequest objectUnderTest =
                new RegistrationChallengeResponseRequest(authContextMock, signCode);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        objectUnderTest.withBody(requestBuilderMock);
        // then
        Mockito.verify(requestBuilderMock).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        Assert.assertEquals(signCode, body.getJSONObject("dataMap").getString("code"));
        Assert.assertEquals(authContextMock.getOrderReference(), body.getString("orderReference"));
        Assert.assertEquals(
                authContextMock.getDeviceRootedHash(), body.getString("deviceRootedHash"));
        Assert.assertEquals(
                authContextMock.getLogin(), body.getJSONObject("user").getString("loginName"));
    }

    @Test
    public void executeShouldReturnSuccessRegistrationResponseDTO() throws RequestException {
        // given
        final String responseBody =
                "{\"Order\": {\"credentials\": {\"challenge\": \"54302959\"},\"reference\": \"C0B11CWWR2993UJK\",\"isError\": false,\"state\": \"1000\",\"sessionToken\": \"NKCQK2EJJLWZH60A0MRW5TWK2SKMIV0R\"}}";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/bpb/services/rest/v2/registerauth"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "application/json")));
        RegistrationChallengeResponseRequest objectUnderTest =
                new RegistrationChallengeResponseRequest(authContextMock, "12345678");
        // when
        RegistrationResponseDTO result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertEquals("NKCQK2EJJLWZH60A0MRW5TWK2SKMIV0R", result.getSessionToken());
        Assert.assertEquals("C0B11CWWR2993UJK", result.getOrderReference());
        Assert.assertFalse(result.isError());
    }
}
