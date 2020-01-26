package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import javax.ws.rs.core.MediaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class CertificateIsRevokedExceptionRequestRepeaterTest {

    private IngBaseApiClient apiClient;
    private static final String payload = "payload";
    private RequestBuilder requestBuilder;
    private CertificateIsRevokedExceptionRequestRepeater objectUnderTest;

    @Before
    public void init() {
        apiClient = Mockito.mock(IngBaseApiClient.class);
        requestBuilder = Mockito.mock(RequestBuilder.class);
        mockApiClient();
        mockRequestBuilder();
        objectUnderTest = new CertificateIsRevokedExceptionRequestRepeater(apiClient, payload);
    }

    private void mockRequestBuilder() {
        Mockito.when(requestBuilder.addBearerToken(Mockito.any())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.body(payload, MediaType.APPLICATION_FORM_URLENCODED))
                .thenReturn(requestBuilder);
    }

    private void mockApiClient() {
        Mockito.when(
                        apiClient.buildRequestWithSignature(
                                IngBaseConstants.Urls.TOKEN,
                                IngBaseConstants.Signature.HTTP_METHOD_POST,
                                payload))
                .thenReturn(requestBuilder);
        Mockito.when(apiClient.getApplicationTokenFromSession())
                .thenReturn(Mockito.mock(OAuth2Token.class));
    }

    @Test
    public void requestShouldReturnResponseWhenHttpResponseIsCorrect() {
        // given
        TokenResponse tokenResponse = Mockito.mock(TokenResponse.class);
        Mockito.when(requestBuilder.post(TokenResponse.class)).thenReturn(tokenResponse);
        // when
        TokenResponse result = objectUnderTest.request();
        // then
        Assert.assertEquals(tokenResponse, result);
    }

    @Test
    public void checkIfRepeatShouldReturnTrue() {
        // given
        final String certificateRevokedResponseBody = " {\"message\" : \"Certificate is revoked\"}";
        HttpResponseException certificateRevokedException =
                Mockito.mock(HttpResponseException.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getStatus()).thenReturn(400);
        Mockito.when(response.getBody(String.class)).thenReturn(certificateRevokedResponseBody);
        Mockito.when(certificateRevokedException.getResponse()).thenReturn(response);
        // when
        boolean result = objectUnderTest.checkIfRepeat(certificateRevokedException);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void checkIfRepeatShouldReturnFalse() {
        // given
        final String certificateRevokedResponseBody = " {\"message\" : \"Some exception\"}";
        HttpResponseException certificateRevokedException =
                Mockito.mock(HttpResponseException.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getStatus()).thenReturn(400);
        Mockito.when(response.getBody(String.class)).thenReturn(certificateRevokedResponseBody);
        Mockito.when(certificateRevokedException.getResponse()).thenReturn(response);
        // when
        boolean result = objectUnderTest.checkIfRepeat(certificateRevokedException);
        // then
        Assert.assertFalse(result);
    }
}
