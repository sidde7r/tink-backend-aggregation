package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters.SwedbankHourRateLimitFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TppErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TppMessage;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SwedbankHourRateLimitFilterTest {
    private SwedbankHourRateLimitFilter filter;

    @Before
    public void setup() {
        filter = new SwedbankHourRateLimitFilter();
    }

    @Test
    public void shouldThrowBankServiceError() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429, "Reached hour requests limit (120000)");
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        filter.setNext(nextFilter);
        Throwable t = catchThrowable(() -> filter.handle(null));

        // then
        assertThat(t)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.ACCESS_EXCEEDED");
    }

    @Test
    public void shouldNotThrowBankServiceError() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockResponse(429, "Reached parallel requests limit: (60)");
        when(nextFilter.handle(any())).thenReturn(response);

        // when
        filter.setNext(nextFilter);
        filter.handle(null);

        // then
        verify(nextFilter, times(1)).handle(any());
    }

    private HttpResponse mockResponse(int status, String text) {
        TppMessage mockTppMessage = new TppMessage("ACCESS_EXCEEDED", text, "ERROR");
        TppErrorResponse mockErrorResponse =
                new TppErrorResponse(Collections.singletonList(mockTppMessage));

        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        when(mocked.getBody(TppErrorResponse.class)).thenReturn(mockErrorResponse);

        return mocked;
    }
}
