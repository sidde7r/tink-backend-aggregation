package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.GET;

import javax.ws.rs.core.MultivaluedMap;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.NextGenRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AbstractRequestTest {

    @Test
    public void shouldSetRequiredHeaders() throws RequestException {
        // given
        final String csrfToken = "csrfToken";
        BPostBankAuthContext authContext = mock(BPostBankAuthContext.class);
        RequestBuilder requestBuilder = new NextGenRequestBuilder(emptyList(), null, null);
        Mockito.when(authContext.getCsrfToken()).thenReturn(csrfToken);
        AbstractRequest<Void> objectUnderTest = createDummyObjectUnderTest(authContext);
        // when
        MultivaluedMap<String, Object> result =
                objectUnderTest.withHeaders(requestBuilder).build(GET).getHeaders();
        // then
        assertThat(result.get("X-Device-Type")).containsOnly("1");
        assertThat(result.get("Accept-Language")).containsOnly("nl-be");
        assertThat(result.get("lang")).containsOnly("nl-BE");
        assertThat(result.get("X-BBXSRF")).containsOnly(csrfToken);
    }

    @Test
    public void shouldSetBPostUrl() {
        // given
        final String path = "/bpb";
        final String url = "https://app.bpostbank.be" + path;
        TinkHttpClient httpClient = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        Mockito.when(httpClient.request(url)).thenReturn(requestBuilder);
        Mockito.when(httpClient.request(new URL(url))).thenReturn(requestBuilder);
        AbstractRequest<Void> objectUnderTest = createDummyObjectUnderTest(path);
        // when
        RequestBuilder result = objectUnderTest.withUrl(httpClient);
        // then
        Assert.assertEquals(requestBuilder, result);
    }

    private AbstractRequest<Void> createDummyObjectUnderTest(BPostBankAuthContext authContext) {
        return new AbstractRequest<Void>("path", authContext) {
            @Override
            public RequestBuilder withBody(RequestBuilder requestBuilder) {
                return requestBuilder;
            }

            @Override
            public Void execute(RequestBuilder requestBuilder) throws RequestException {
                return null;
            }
        };
    }

    private AbstractRequest<Void> createDummyObjectUnderTest(String path) {
        return new AbstractRequest<Void>(path) {
            @Override
            public RequestBuilder withBody(RequestBuilder requestBuilder) {
                return requestBuilder;
            }

            @Override
            public Void execute(RequestBuilder requestBuilder) throws RequestException {
                return null;
            }
        };
    }
}
