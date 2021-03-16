package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import io.netty.handler.codec.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ProxyResponseListener;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ResponseFromProxy;

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
    public void should_find_response_when_its_filtered_first_and_then_we_wait_for_it() {
        // given
        proxyResponseListener.changeUrlSubstringToListenFor("part.of.url");

        // when
        proxyResponseListener.filterResponse(
                httpResponse, httpContents, httpMessageInfoWithUrl("https://www.part.of.url"));
        Optional<ResponseFromProxy> response =
                proxyResponseListener.waitForResponse(1, TimeUnit.MILLISECONDS);

        // then
        assertThat(response).isPresent();
    }

    @Test
    public void should_find_response_when_we_wait_for_it_first_and_its_filtered_later() {
        // given
        proxyResponseListener.changeUrlSubstringToListenFor("another.part.of.url");

        // when
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(
                () ->
                        proxyResponseListener.filterResponse(
                                httpResponse,
                                httpContents,
                                httpMessageInfoWithUrl("https://www.another.part.of.url")),
                100,
                TimeUnit.MILLISECONDS);

        Optional<ResponseFromProxy> response =
                proxyResponseListener.waitForResponse(200, TimeUnit.MILLISECONDS);

        // then
        assertThat(response).isPresent();
    }

    @Test
    @Parameters(method = "listenForUrlResponseTestParams")
    public void should_listen_by_response_url_substring(
            String responseUrl, String responseUrlSubstring, boolean shouldRegisterResponse) {
        // given
        proxyResponseListener.changeUrlSubstringToListenFor(responseUrlSubstring);

        HttpMessageInfo responseMessageInfo = httpMessageInfoWithUrl(responseUrl);

        // when
        proxyResponseListener.filterResponse(httpResponse, httpContents, responseMessageInfo);
        Optional<ResponseFromProxy> response =
                proxyResponseListener.waitForResponse(1, TimeUnit.MILLISECONDS);

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
        proxyResponseListener.changeUrlSubstringToListenFor("some.url");

        HttpMessageInfo messageInfo1 = httpMessageInfoWithUrl("https://some.url?param=1");
        HttpMessageInfo messageInfo2 = httpMessageInfoWithUrl("https://some.url?param=2");
        HttpMessageInfo messageInfo3 = httpMessageInfoWithUrl("https://some.url?param=3");

        // when
        proxyResponseListener.filterResponse(httpResponse, httpContents, messageInfo1);
        proxyResponseListener.filterResponse(httpResponse, httpContents, messageInfo2);
        proxyResponseListener.filterResponse(httpResponse, httpContents, messageInfo3);

        Optional<ResponseFromProxy> proxyResponse =
                proxyResponseListener.waitForResponse(1, TimeUnit.MILLISECONDS);

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
