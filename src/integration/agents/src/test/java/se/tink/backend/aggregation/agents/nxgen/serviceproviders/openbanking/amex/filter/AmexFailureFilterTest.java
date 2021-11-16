package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;

@RunWith(MockitoJUnitRunner.class)
public class AmexFailureFilterTest {

    private Filter nextFilter;
    private AmexFailureFilter failureFilter;

    @Before
    public void setup() {
        failureFilter = new AmexFailureFilter();
        nextFilter = mock(Filter.class);
    }

    @Test
    public void shouldFilterAndThrowProperException() {
        // given
        final String exceptionMessage = "api2s.americanexpress.com:443 failed to respond";
        HttpClientException exception = new HttpClientException(exceptionMessage, null);

        // and
        when(nextFilter.handle(any())).thenThrow(exception);

        // when
        failureFilter.setNext(nextFilter);
        Throwable throwable = catchThrowable(() -> failureFilter.handle(any()));

        // then
        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
    }
}
