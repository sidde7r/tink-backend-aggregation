package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Ignore
public class TestHelper {
    public static TinkHttpClient mockHttpClient(
            RequestBuilder requestBuilder, HttpResponse response) {

        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.addBearerToken(any(OAuth2Token.class))).thenReturn(requestBuilder);
        when(requestBuilder.queryParam(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(HttpResponse.class)).thenReturn(response);
        return tinkHttpClient;
    }
}
