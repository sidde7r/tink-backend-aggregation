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

public class RegistrationInitRequestTest extends WireMockIntegrationTest {

    private BPostBankAuthContext authContextMock;
    private RequestBuilder requestBuilder;
    private RegistrationInitRequest objectUnderTest;

    @Before
    public void init() {
        authContextMock = Mockito.mock(BPostBankAuthContext.class);
        requestBuilder = httpClient.request(getOrigin() + RegistrationInitRequest.URL_PATH);
        objectUnderTest = new RegistrationInitRequest(authContextMock);
    }

    @Test
    public void withBodyShouldCreateCorrectBody() throws JSONException {
        // given
        RequestBuilder requestBuilderMock = Mockito.mock(RequestBuilder.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        objectUnderTest.withBody(requestBuilderMock);
        // then
        Mockito.verify(requestBuilderMock).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        Assert.assertEquals("nl", body.getString("language"));
        JSONObject securityContext = body.getJSONObject("securityContext");
        Assert.assertEquals("1", securityContext.getString("authenticationType"));
        Assert.assertEquals("1", securityContext.getString("deviceType"));
    }

    @Test
    public void executeShouldReturnCorrectRegistrationResponseDTO() throws RequestException {
        // given
        final String responseBody =
                "{\"Order\": {\"credentials\": {\"challenge\": \"20553032\"},\"reference\": \"3VYTR7JTBOUBAAMJ\",\"isError\": false,\"state\": \"0500\",\"sessionToken\": \"430K2HLBLDM9SYR3LX55Y7W6E7AL72VL\"}}";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/bpb/services/rest/v2/registerinit"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "application/json")));
        // when
        RegistrationResponseDTO result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertEquals("430K2HLBLDM9SYR3LX55Y7W6E7AL72VL", result.getSessionToken());
        Assert.assertEquals("3VYTR7JTBOUBAAMJ", result.getOrderReference());
        Assert.assertEquals("20553032", result.getChallengeCode());
        Assert.assertFalse(result.isError());
    }
}
