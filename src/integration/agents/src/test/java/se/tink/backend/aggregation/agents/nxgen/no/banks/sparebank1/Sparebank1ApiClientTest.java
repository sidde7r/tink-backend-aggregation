package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Sparebank1ApiClientTest {
    private Sparebank1ApiClient sparebank1ApiClient;
    private TinkHttpClient client;

    @Test
    public void getSessionTokenShouldReturnSessionTokenIfAvailable() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getCookies())
                .thenReturn(Arrays.asList(new NewCookie("DSESSIONID", "dummySessionCookie")));
        client = mockHttpClient(mock(RequestBuilder.class), httpResponse);
        sparebank1ApiClient = new Sparebank1ApiClient(client, "bankId");

        // when
        sparebank1ApiClient.initLinks();

        // then
        assertThat(sparebank1ApiClient.getSessionToken()).isEqualTo("dummySessionCookie");
    }

    @Test
    public void getSessionTokenShouldThrowExceptionIfSessionTokenNotFound() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getCookies()).thenReturn(Collections.emptyList());
        client = mockHttpClient(mock(RequestBuilder.class), httpResponse);
        sparebank1ApiClient = new Sparebank1ApiClient(client, "bankId");

        // when
        Throwable ex = catchThrowable(() -> sparebank1ApiClient.initLinks());

        // then
        assertThat(ex).isInstanceOf(IllegalStateException.class);
    }

    public static TinkHttpClient mockHttpClient(
            RequestBuilder requestBuilder, HttpResponse httpResponse) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);

        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.queryParam(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.get(HttpResponse.class)).thenReturn(httpResponse);
        return tinkHttpClient;
    }
}
