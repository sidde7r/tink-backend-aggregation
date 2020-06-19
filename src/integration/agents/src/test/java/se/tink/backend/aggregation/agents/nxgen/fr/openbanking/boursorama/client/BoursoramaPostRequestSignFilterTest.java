package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.api.Psd2Headers.Keys;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BoursoramaPostRequestSignFilterTest {

    private BoursoramaPostRequestSignFilter filter;
    private BoursoramaSignatureHeaderGenerator signatureHeaderGenerator;

    @Before
    public void before() {
        signatureHeaderGenerator = mock(BoursoramaSignatureHeaderGenerator.class);

        filter = new BoursoramaPostRequestSignFilter(signatureHeaderGenerator);
        filter.setNext(mock(Filter.class));
    }

    @Test
    public void correctAdditionalHeadersAreAdded() {
        // given
        HttpRequestImpl request = new HttpRequestImpl(HttpMethod.POST, new URL("dummy.url"));

        // when
        filter.handle(request);

        // then
        assertThat(request.getHeaders().get(Keys.DATE))
                .as("single Date header is added to request")
                .hasSize(1);

        assertThat(request.getHeaders().get(Keys.X_REQUEST_ID))
                .as("single X-Request-ID header is added to request")
                .hasSize(1);
    }

    @Test
    public void singleSignatureHeaderIsAdded() {
        // given
        HttpRequestImpl request = new HttpRequestImpl(HttpMethod.POST, new URL("dummy.url"));

        // when
        when(signatureHeaderGenerator.getSignatureHeaderValueForPost(any(), any(), any(), any()))
                .thenReturn("SIGNATURE_HEADER_VALUE");

        filter.handle(request);

        // then
        assertThat(request.getHeaders().get(Keys.SIGNATURE))
                .as("single Signature header is added to request")
                .hasSize(1);

        assertThat(request.getHeaders().getFirst(Keys.SIGNATURE))
                .isEqualTo("SIGNATURE_HEADER_VALUE");
    }

    @Test
    public void correctDigestHeaderIsAdded() {
        // given
        String requestBody = "REQUEST_BODY";
        HttpRequestImpl request =
                new HttpRequestImpl(HttpMethod.POST, new URL("dummy.url"), requestBody);

        // when
        when(signatureHeaderGenerator.getDigestHeaderValue(any()))
                .thenReturn("DIGEST_HEADER_VALUE");

        filter.handle(request);

        // then
        assertThat(request.getHeaders().get(Keys.DIGEST))
                .as("single Digest header is added to request")
                .hasSize(1);

        assertThat(request.getHeaders().getFirst(Keys.DIGEST)).isEqualTo("DIGEST_HEADER_VALUE");
    }

    @Test
    public void singingWorks_EvenWhenBodyIsNotString() {
        // given
        List<Integer> requestBody = Collections.singletonList(123);
        HttpRequestImpl request =
                new HttpRequestImpl(HttpMethod.POST, new URL("dummy.url"), requestBody);

        // when
        filter.handle(request);

        // then
        ArgumentCaptor<String> digestInput = ArgumentCaptor.forClass(String.class);
        verify(signatureHeaderGenerator).getDigestHeaderValue(digestInput.capture());
        assertThat(digestInput.getValue()).isEqualTo(request.getBody());
    }

    @Test
    public void willDoNothingWhenHttpMethodIsNotPost() {
        // given
        HttpRequestImpl request = new HttpRequestImpl(HttpMethod.GET, new URL("dummy.url"));

        // when
        filter.handle(request);

        // then
        verify(signatureHeaderGenerator, never()).getDigestHeaderValue(anyString());
        verify(signatureHeaderGenerator, never())
                .getSignatureHeaderValueForGet(any(), anyString(), anyString(), anyString());
        assertThat(request.getHeaders().getFirst(Keys.DIGEST)).isNull();
        assertThat(request.getHeaders().getFirst(Keys.SIGNATURE)).isNull();
        assertThat(request.getHeaders().getFirst(Keys.DATE)).isNull();
    }
}
