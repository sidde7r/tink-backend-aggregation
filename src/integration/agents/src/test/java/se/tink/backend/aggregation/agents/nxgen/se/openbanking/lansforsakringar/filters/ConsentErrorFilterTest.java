package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ConsentErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class ConsentErrorFilterTest {

    @Mock private Filter filter;
    private ConsentErrorFilter consentErrorFilter;

    @Before
    public void setUp() {
        consentErrorFilter = new ConsentErrorFilter();
        consentErrorFilter.setNext(filter);
    }

    @Test
    public void shouldThrowSessionErrorWhenTokenExpired() {

        // given
        final String responseBody = "1012117 - Invalid token. The token has expired.";

        HttpResponse mockedResponse = mockResponse(401, responseBody);
        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenThrow(new HttpResponseException(null, mockedResponse));

        // when
        consentErrorFilter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> consentErrorFilter.handle(null));

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("1012117 - Invalid token. The token has expired.");
    }

    static HttpResponse mockResponse(int statusCode, String responseBody) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getBody(String.class)).thenReturn(responseBody);
        when(mocked.getStatus()).thenReturn(statusCode);
        return mocked;
    }
}
