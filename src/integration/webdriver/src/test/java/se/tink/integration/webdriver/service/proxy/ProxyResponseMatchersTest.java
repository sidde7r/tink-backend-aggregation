package se.tink.integration.webdriver.service.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.browserup.bup.util.HttpMessageInfo;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ProxyResponseMatchersTest {

    @Test
    @Parameters(method = "listenForUrlResponseTestParams")
    public void should_lister_for_response_with_correct_url(
            String responseUrl, String urlSubstringToListenFor, boolean expectedIsMatching) {
        // given
        ProxySaveResponseMatcher matcher =
                new ProxyResponseMatchers.ProxyUrlSubstringMatcher(urlSubstringToListenFor);
        ProxyResponse proxyResponse = proxyResponseWithUrl(responseUrl);

        // when
        boolean isMatching = matcher.matchesResponse(proxyResponse);

        // then
        assertThat(isMatching).isEqualTo(expectedIsMatching);
    }

    @SuppressWarnings("unused")
    private static Object[] listenForUrlResponseTestParams() {
        return new Object[] {
            asArray("https://example.com/", "https://example.com/", true),
            asArray("https://example.com/", "HTTPS://EXAMPLE.COM/", true),
            asArray("https://example.com/", "HTTPS://example.COM/", true),
            asArray("https://example.com/", "example.COM", true),
            asArray("https://example.com/", "example.com", true),
            asArray("https://example.com/", "http://example.com/", false),
            asArray("https://example.com/", "example.no", false)
        };
    }

    private static ProxyResponse proxyResponseWithUrl(String url) {
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        when(messageInfo.getUrl()).thenReturn(url);

        return ProxyResponse.builder().messageInfo(messageInfo).build();
    }

    private static Object[] asArray(Object... args) {
        return args;
    }
}
