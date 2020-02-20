package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AbstractRequestTest {

    @Test
    public void withHeadersShouldSetRequiredHeaders() throws RequestException {
        // given
        final String csrfToken = "csrfToken";
        BPostBankAuthContext authContext = Mockito.mock(BPostBankAuthContext.class);
        RequestBuilderArgumentCapture requestBuilder = new RequestBuilderArgumentCapture();
        Mockito.when(authContext.getCsrfToken()).thenReturn(csrfToken);
        AbstractRequest<Void> objectUnderTest =
                new AbstractRequest<Void>("path", authContext) {
                    @Override
                    public RequestBuilder withBody(RequestBuilder requestBuilder) {
                        return requestBuilder;
                    }

                    @Override
                    public Void execute(RequestBuilder requestBuilder) throws RequestException {
                        return null;
                    }
                };
        // when
        Map<String, Object> result =
                ((RequestBuilderArgumentCapture) objectUnderTest.withHeaders(requestBuilder))
                        .getHeaders();
        // then
        Assert.assertEquals("1", result.get("X-Device-Type"));
        Assert.assertEquals("nl-be", result.get("Accept-Language"));
        Assert.assertEquals("nl-BE", result.get("lang"));
        Assert.assertEquals(csrfToken, result.get("X-BBXSRF"));
    }

    @Test
    public void withUrlShouldSetBPostUrl() {
        // given
        final String path = "/bpb";
        final String url = "https://app.bpostbank.be" + path;
        TinkHttpClient httpClient = Mockito.mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        Mockito.when(httpClient.request(url)).thenReturn(requestBuilder);
        Mockito.when(httpClient.request(new URL(url))).thenReturn(requestBuilder);
        AbstractRequest<Void> objectUnderTest =
                new AbstractRequest<Void>(path) {
                    @Override
                    public RequestBuilder withBody(RequestBuilder requestBuilder) {
                        return requestBuilder;
                    }

                    @Override
                    public Void execute(RequestBuilder requestBuilder) throws RequestException {
                        return null;
                    }
                };
        // when
        RequestBuilder result = objectUnderTest.withUrl(httpClient);
        // then
        Assert.assertEquals(requestBuilder, result);
    }
}
