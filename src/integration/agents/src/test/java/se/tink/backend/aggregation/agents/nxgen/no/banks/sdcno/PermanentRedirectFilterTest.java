package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class PermanentRedirectFilterTest {

    private Filter next;

    private PermanentRedirectFilter filter = new PermanentRedirectFilter();

    @Before
    public void setUp() {
        filter = new PermanentRedirectFilter();

        // and set mocked filter
        next = mock(Filter.class);
        filter.setNext(next);
    }

    @Test
    public void handleWhenResponseIsNot308ShouldReturnInitialResponse() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);
        // and
        HttpResponse httpResponse = mock(HttpResponse.class);
        given(httpResponse.getStatus()).willReturn(200);
        // and
        given(next.handle(httpRequest)).willReturn(httpResponse);

        // when
        HttpResponse result = filter.handle(httpRequest);

        // then
        assertThat(result).isEqualTo(httpResponse);
        // and
        verify(next).handle(httpRequest);
        verifyNoMoreInteractions(next);
    }

    @Test
    public void handleWhenResponseHasNoLocationShouldReturnInitialResponse() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);
        // and
        HttpResponse httpResponse = mock(HttpResponse.class, Answers.RETURNS_DEEP_STUBS);
        given(httpResponse.getStatus()).willReturn(308);
        given(httpResponse.getHeaders().getFirst("Location")).willReturn(null);
        // and
        given(next.handle(httpRequest)).willReturn(httpResponse);

        // when
        HttpResponse result = filter.handle(httpRequest);

        // then
        assertThat(result).isEqualTo(httpResponse);
        // and
        verify(next).handle(httpRequest);
        verifyNoMoreInteractions(next);
    }

    @Test
    public void handle308ResponseShouldTryAdditionalUrl() {
        // given
        HttpRequest httpRequest = mock(HttpRequest.class);

        // and
        HttpResponse httpResponse = mock(HttpResponse.class, Answers.RETURNS_DEEP_STUBS);
        given(httpResponse.getStatus()).willReturn(308);
        given(httpResponse.getHeaders().getFirst("Location")).willReturn("/second/url");
        // and
        HttpResponse httpResponse2 = mock(HttpResponse.class, Answers.RETURNS_DEEP_STUBS);

        // and
        given(next.handle(httpRequest)).willReturn(httpResponse).willReturn(httpResponse2);

        // when
        HttpResponse result = filter.handle(httpRequest);

        // then
        assertThat(result).isEqualTo(httpResponse2);
        // and
        verify(next, times(2)).handle(httpRequest);
        verify(httpRequest).setUrl(new URL("/second/url"));
        verifyNoMoreInteractions(next);
    }
}
