package se.tink.integration.webdriver.service.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

@RunWith(JUnitParamsRunner.class)
public class ProxyFilterTest {

    /*
    Mocks
     */
    private ProxyListener listener1;
    private ProxyListener listener2;
    private ProxyListener listener3;

    /*
    Real
     */
    private ProxyFilter proxyFilter;

    @Before
    public void setup() {
        listener1 = mock(ProxyListener.class);
        listener2 = mock(ProxyListener.class);
        listener3 = mock(ProxyListener.class);

        proxyFilter = new ProxyFilter();
        proxyFilter.addListener("listener1", listener1);
        proxyFilter.addListener("listener2", listener2);
        proxyFilter.addListener("listener3", listener3);
    }

    @Test
    public void should_notify_all_listeners_on_request_and_ignore_exceptions() {
        // given
        doThrow(new RuntimeException()).when(listener2).handleRequest(any());

        // when
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpMessageContents contents = mock(HttpMessageContents.class);
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        proxyFilter.filterRequest(httpRequest, contents, messageInfo);

        // then
        ProxyRequest expectedRequest = new ProxyRequest(httpRequest, contents, messageInfo);

        verify(contents).getTextContents();

        Stream.of(listener1, listener2, listener3)
                .forEach(
                        listener -> {
                            ArgumentCaptor<ProxyRequest> requestArgumentCaptor =
                                    ArgumentCaptor.forClass(ProxyRequest.class);
                            verify(listener).handleRequest(requestArgumentCaptor.capture());

                            ProxyRequest capturedRequest = requestArgumentCaptor.getValue();
                            assertThat(capturedRequest)
                                    .usingRecursiveComparison()
                                    .isEqualTo(expectedRequest);
                        });
    }

    @Test
    public void should_notify_all_listeners_on_response_and_ignore_exceptions() {
        // given
        doThrow(new RuntimeException()).when(listener1).handleRequest(any());

        // when
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpMessageContents contents = mock(HttpMessageContents.class);
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        proxyFilter.filterResponse(httpResponse, contents, messageInfo);

        // then
        ProxyResponse expectedResponse = new ProxyResponse(httpResponse, contents, messageInfo);

        verify(contents).getTextContents();

        Stream.of(listener1, listener2, listener3)
                .forEach(
                        listener -> {
                            ArgumentCaptor<ProxyResponse> responseArgumentCaptor =
                                    ArgumentCaptor.forClass(ProxyResponse.class);
                            verify(listener).handleResponse(responseArgumentCaptor.capture());

                            ProxyResponse capturedResponse = responseArgumentCaptor.getValue();
                            assertThat(capturedResponse)
                                    .usingRecursiveComparison()
                                    .isEqualTo(expectedResponse);
                        });
    }
}
