package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankAuthenticatorTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    private SparebankApiClient apiClient;
    private SparebankAuthenticator authenticator;

    @Before
    public void setup() {
        apiClient = mock(SparebankApiClient.class);
        authenticator = new SparebankAuthenticator(apiClient);
    }

    @Test
    public void shouldThrowWhenNoScaRedirectReturnedFromClient() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(any())).thenReturn("");
        HttpResponseException httpResponseException =
                new HttpResponseException("", null, httpResponse);
        when(apiClient.getScaRedirect(anyString())).thenThrow(httpResponseException);

        thrown.expect(IllegalStateException.class);

        authenticator.buildAuthorizeUrl("");
    }

    @Test
    public void shouldFallBackOnHttpResponseExceptionBodyWhenCaught() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(String.class)).thenReturn(getScaResponseString());
        when(httpResponse.getBody(ScaResponse.class)).thenReturn(getScaResponse());
        HttpResponseException httpResponseException =
                new HttpResponseException("", null, httpResponse);
        when(apiClient.getScaRedirect(anyString())).thenThrow(httpResponseException);

        URL url = authenticator.buildAuthorizeUrl("");

        assertEquals(new URL("http://example.com"), url.getUrl());
    }

    @Test
    public void shouldReturnProperScaRedirectURL() {
        when(apiClient.getScaRedirect(anyString())).thenReturn(getScaResponse());

        URL url = authenticator.buildAuthorizeUrl("");

        assertEquals(new URL("http://example.com"), url.getUrl());
    }

    private String getScaResponseString() {
        return "{\"_links\": {\"scaRedirect\": {\"href\": \"http://example.com\"}}}";
    }

    private ScaResponse getScaResponse() {
        return SerializationUtils.deserializeFromString(getScaResponseString(), ScaResponse.class);
    }
}
