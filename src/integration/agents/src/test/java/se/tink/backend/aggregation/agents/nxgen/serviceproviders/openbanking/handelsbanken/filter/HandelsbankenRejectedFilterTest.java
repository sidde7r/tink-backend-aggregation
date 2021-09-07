package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.filter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class HandelsbankenRejectedFilterTest {

    private HandelsbankenRejectedFilter handelsbankenRejectedFilter;
    @Mock private Filter filter;
    @Mock private HttpRequest httpRequest;
    @Mock private HttpResponse httpResponse;

    @Before
    public void setup() {
        handelsbankenRejectedFilter = new HandelsbankenRejectedFilter();
        handelsbankenRejectedFilter.setNext(filter);
    }

    @Test
    public void shouldReturnBankServiceError() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_OK);
        given(httpResponse.getBody(String.class))
                .willReturn(
                        "Request Rejected "
                                + "The requested URL was rejected. Please consult with your administrator.");

        // when
        when(filter.handle(any())).thenReturn(httpResponse);

        // then
        assertThatThrownBy(() -> handelsbankenRejectedFilter.handle(httpRequest))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void shouldReturnResponse() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_OK);
        given(httpResponse.getBody(String.class)).willReturn("Successful request");

        // when
        when(filter.handle(any())).thenReturn(httpResponse);

        // then
        assertEquals(handelsbankenRejectedFilter.handle(httpRequest), httpResponse);
    }
}
