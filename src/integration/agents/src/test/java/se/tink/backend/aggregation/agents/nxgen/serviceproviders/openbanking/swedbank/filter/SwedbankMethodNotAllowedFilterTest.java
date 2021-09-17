package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SwedbankMethodNotAllowedFilterTest {
    SwedbankMethodNotAllowedFilter filter = new SwedbankMethodNotAllowedFilter();
    HttpRequest httpRequest = setupHttpRequest();
    Filter nextFilter = mock(Filter.class);

    @Test
    public void shouldThrowErrorWhenResponseStatusIs405() {
        HttpResponse response = setupHttpResponse(405);
        filter.setNext(nextFilter);
        when(nextFilter.handle(any())).thenReturn(response);

        Throwable throwable = catchThrowable(() -> filter.handle(httpRequest));

        assertThat(throwable).isExactlyInstanceOf(BankServiceException.class);
    }

    @Test
    public void shouldReturn428ResponseIfStatusIs428() {
        HttpResponse response = setupHttpResponse(428);
        filter.setNext(nextFilter);
        when(nextFilter.handle(any())).thenReturn(response);

        HttpResponse result = filter.handle(httpRequest);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.RESOURCE_PENDING);
    }

    @Test
    public void shouldReturn500ResponseIfStatusIs500() {
        HttpResponse response = setupHttpResponse(500);
        filter.setNext(nextFilter);
        when(nextFilter.handle(any())).thenReturn(response);

        HttpResponse result = filter.handle(httpRequest);

        assertThat(result.getStatus()).isEqualTo(500);
    }

    private HttpRequest setupHttpRequest() {
        URL url = new URL("fakeURL");
        return new HttpRequestImpl(HttpMethod.POST, url, null, null);
    }

    private HttpResponse setupHttpResponse(int number) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(number);
        when(httpResponse.getBody(String.class)).thenReturn("Fake body");
        return httpResponse;
    }
}
