package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import io.netty.handler.codec.http.HttpResponse;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ProxyResponseListenerTest {

    /*
    Mocks
     */
    private HttpResponse httpResponse;
    private HttpMessageContents httpContents;

    /*
    Real
     */
    private ProxyResponseListener proxyResponseListener;

    @Before
    public void setup() {
        httpResponse = mock(HttpResponse.class);
        httpContents = mock(HttpMessageContents.class);

        proxyResponseListener = new ProxyResponseListener();
    }

    @Test
    @Parameters(method = "listenForUrlResponseTestParams")
    public void should_listen_by_response_url_substring(
            String responseUrl, String responseUrlSubstring, boolean shouldRegisterResponse) {
        // given
        proxyResponseListener.listenByResponseUrlSubstring(responseUrlSubstring);

        HttpMessageInfo responseMessageInfo = httpMessageInfoWithUrl(responseUrl);

        // when
        proxyResponseListener.filterResponse(httpResponse, httpContents, responseMessageInfo);
        Optional<ResponseFromProxy> response = proxyResponseListener.getResponseFromProxy();

        // then
        if (shouldRegisterResponse) {
            assertThat(response).isPresent();
            assertThat(response.get().getResponse()).isEqualTo(httpResponse);
            assertThat(response.get().getContents()).isEqualTo(httpContents);
            assertThat(response.get().getMessageInfo()).isEqualTo(responseMessageInfo);

        } else {
            assertThat(response).isEmpty();
        }
    }

    @SuppressWarnings("unused")
    private static Object[] listenForUrlResponseTestParams() {
        return new Object[] {
            asArgs("https://example.com/", "https://example.com/", true),
            asArgs("https://example.com/", "HTTPS://EXAMPLE.COM/", true),
            asArgs("https://example.com/", "HTTPS://example.COM/", true),
            asArgs("https://example.com/", "example.COM", true),
            asArgs("https://example.com/", "example.com", true),
            asArgs("https://example.com/", "http://example.com/", false),
            asArgs("https://example.com/", "example.no", false)
        };
    }

    @Test
    public void should_listen_only_to_the_very_first_response() {
        // given
        proxyResponseListener.listenByResponseUrlSubstring("some.url");

        HttpMessageInfo messageInfo1 = httpMessageInfoWithUrl("https://some.url?param=1");
        HttpMessageInfo messageInfo2 = httpMessageInfoWithUrl("https://some.url?param=2");
        HttpMessageInfo messageInfo3 = httpMessageInfoWithUrl("https://some.url?param=3");

        // when
        proxyResponseListener.filterResponse(httpResponse, httpContents, messageInfo1);
        proxyResponseListener.filterResponse(httpResponse, httpContents, messageInfo2);
        proxyResponseListener.filterResponse(httpResponse, httpContents, messageInfo3);

        Optional<ResponseFromProxy> proxyResponse = proxyResponseListener.getResponseFromProxy();

        // then
        assertThat(proxyResponse).isPresent();
        assertThat(proxyResponse.get().getMessageInfo()).isEqualTo(messageInfo1);
    }

    private static HttpMessageInfo httpMessageInfoWithUrl(String url) {
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        when(messageInfo.getUrl()).thenReturn(url);
        return messageInfo;
    }

    private static Object[] asArgs(Object... args) {
        return args;
    }
}
