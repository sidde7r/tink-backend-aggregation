package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Sparebank1ApiClientTest {
    private Sparebank1ApiClient objUnderTest;
    private TinkHttpClient client;

    @Before
    public void init() {
        client = mockHttpClient(mock(RequestBuilder.class));
        objUnderTest = new Sparebank1ApiClient(client, "dummy");
    }

    @Test
    public void getSessionTokenShouldReturnSessionTokenIfAvailable() {
        // given
        when(client.getCookies())
                .thenReturn(
                        ImmutableList.of(
                                new BasicClientCookie("DSESSIONID", "dummySessionCookie")));

        // when
        objUnderTest.retrieveSessionCookie();

        // then
        assertThat(objUnderTest.getSessionToken()).isEqualTo("dummySessionCookie");
    }

    @Test
    public void getSessionTokenShouldThrowExceptionIfSessionTokenNotFound() {
        // given
        // when
        Throwable ex = catchThrowable(() -> objUnderTest.retrieveSessionCookie());

        // then
        assertThat(ex).isInstanceOf(IllegalStateException.class);
    }

    public static TinkHttpClient mockHttpClient(RequestBuilder requestBuilder) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);

        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.get(HttpResponse.class)).thenReturn(mock(HttpResponse.class));
        return tinkHttpClient;
    }
}
