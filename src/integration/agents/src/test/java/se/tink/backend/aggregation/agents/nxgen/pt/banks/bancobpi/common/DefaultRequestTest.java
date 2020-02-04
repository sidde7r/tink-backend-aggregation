package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class DefaultRequestTest {

    private static final String SPECIFIC_HEADER_NAME = "specificHeaderName";
    private static final String SPECIFIC_HEADER_VALUE = "specificHeaderValue";
    private static final String CSRF_TOKEN = "token";
    private static final String DEVICE_UUID = "deviceUUID";
    private static final String URL = "http://localhost";
    private static final String HEADER_CSRF_TOKEN = "X-CSRFToken";
    private static final String HEADER_DEVICE_UUID = "OutSystems-device-uuid";
    private BancoBpiAuthContext userState;
    private TinkHttpClient httpClient;

    @Before
    public void init() {
        userState = Mockito.mock(BancoBpiAuthContext.class);
        Mockito.when(userState.getDeviceUUID()).thenReturn(DEVICE_UUID);
        Mockito.when(userState.getSessionCSRFToken()).thenReturn(CSRF_TOKEN);
        httpClient = Mockito.mock(TinkHttpClient.class);
    }

    @Test
    public void withHeadersShouldPutMandatoryAndSpecificHeaders() throws RequestException {
        // given
        TestDefaultRequest objectUnderTest = new TestDefaultRequest(userState, URL);
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        Mockito.when(requestBuilder.header(HEADER_CSRF_TOKEN, CSRF_TOKEN))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.header(HEADER_DEVICE_UUID, DEVICE_UUID))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.header(SPECIFIC_HEADER_NAME, SPECIFIC_HEADER_VALUE))
                .thenReturn(requestBuilder);
        // when
        RequestBuilder result = objectUnderTest.withHeaders(httpClient, requestBuilder);
        // then
        Assert.assertEquals(requestBuilder, result);
        Mockito.verify(requestBuilder).header(HEADER_CSRF_TOKEN, CSRF_TOKEN);
        Mockito.verify(requestBuilder).header(HEADER_DEVICE_UUID, DEVICE_UUID);
        Mockito.verify(requestBuilder).header(SPECIFIC_HEADER_NAME, SPECIFIC_HEADER_VALUE);
    }

    @Test
    public void withUrlShouldCreateProperRequestBuilder() {
        // given
        TestDefaultRequest objectUnderTest = new TestDefaultRequest(userState, URL);
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        Mockito.when(httpClient.request(URL)).thenReturn(requestBuilder);
        // when
        RequestBuilder result = objectUnderTest.withUrl(httpClient);
        // then
        Assert.assertEquals(requestBuilder, result);
    }

    private class TestDefaultRequest extends DefaultRequest {

        public TestDefaultRequest(BancoBpiAuthContext userState, String url) {
            super(userState, url);
        }

        @Override
        protected RequestBuilder withSpecificHeaders(
                TinkHttpClient httpClient, RequestBuilder requestBuilder) {
            return requestBuilder.header(SPECIFIC_HEADER_NAME, SPECIFIC_HEADER_VALUE);
        }

        @Override
        public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
            return null;
        }

        @Override
        public Object execute(RequestBuilder requestBuilder, TinkHttpClient httpClient)
                throws RequestException {
            return null;
        }
    }
}
