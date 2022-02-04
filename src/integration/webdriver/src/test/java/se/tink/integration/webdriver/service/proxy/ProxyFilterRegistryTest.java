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
public class ProxyFilterRegistryTest {

    /*
    Mocks
     */
    private ProxyFilter filter1;
    private ProxyFilter filter2;
    private ProxyFilter filter3;

    /*
    Real
     */
    private ProxyFilterRegistry proxyFilterRegistry;

    @Before
    public void setup() {
        filter1 = mock(ProxyFilter.class);
        filter2 = mock(ProxyFilter.class);
        filter3 = mock(ProxyFilter.class);

        proxyFilterRegistry = new ProxyFilterRegistry();
        proxyFilterRegistry.registerProxy("filter1", filter1);
        proxyFilterRegistry.registerProxy("filter2", filter2);
        proxyFilterRegistry.registerProxy("filter3", filter3);
    }

    @Test
    public void should_notify_all_filters_on_request_and_ignore_exceptions() {
        // given
        doThrow(new RuntimeException()).when(filter2).handleRequest(any());

        // when
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpMessageContents contents = mock(HttpMessageContents.class);
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        proxyFilterRegistry.filterRequest(httpRequest, contents, messageInfo);

        // then
        ProxyRequest expectedRequest = new ProxyRequest(httpRequest, contents, messageInfo);

        verify(contents).getTextContents();

        Stream.of(filter1, filter2, filter3)
                .forEach(
                        filter -> {
                            ArgumentCaptor<ProxyRequest> requestArgumentCaptor =
                                    ArgumentCaptor.forClass(ProxyRequest.class);
                            verify(filter).handleRequest(requestArgumentCaptor.capture());

                            ProxyRequest capturedRequest = requestArgumentCaptor.getValue();
                            assertThat(capturedRequest)
                                    .usingRecursiveComparison()
                                    .isEqualTo(expectedRequest);
                        });
    }

    @Test
    public void should_notify_all_filters_on_response_and_ignore_exceptions() {
        // given
        doThrow(new RuntimeException()).when(filter1).handleRequest(any());

        // when
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpMessageContents contents = mock(HttpMessageContents.class);
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        proxyFilterRegistry.filterResponse(httpResponse, contents, messageInfo);

        // then
        ProxyResponse expectedResponse = new ProxyResponse(httpResponse, contents, messageInfo);

        verify(contents).getTextContents();

        Stream.of(filter1, filter2, filter3)
                .forEach(
                        filter -> {
                            ArgumentCaptor<ProxyResponse> responseArgumentCaptor =
                                    ArgumentCaptor.forClass(ProxyResponse.class);
                            verify(filter).handleResponse(responseArgumentCaptor.capture());

                            ProxyResponse capturedResponse = responseArgumentCaptor.getValue();
                            assertThat(capturedResponse)
                                    .usingRecursiveComparison()
                                    .isEqualTo(expectedResponse);
                        });
    }
}
