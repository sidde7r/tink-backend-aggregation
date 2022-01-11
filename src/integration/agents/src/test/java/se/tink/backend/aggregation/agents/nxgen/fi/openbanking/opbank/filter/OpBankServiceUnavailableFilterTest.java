package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.filter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
public class OpBankServiceUnavailableFilterTest {

    private OpBankServiceUnavailableFilter opBankServiceUnavailableFilter;
    @Mock private Filter filter;
    @Mock private HttpRequest httpRequest;
    @Mock private HttpResponse httpResponse;

    @Before
    public void setup() {
        opBankServiceUnavailableFilter = new OpBankServiceUnavailableFilter();
        opBankServiceUnavailableFilter.setNext(filter);
    }

    @Test
    public void shouldReturnBankServiceErrorWhenStatusUnavailable() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);
        given(httpResponse.getBody(String.class)).willReturn("whatever");

        // when
        when(filter.handle(any())).thenReturn(httpResponse);

        // then
        assertThatThrownBy(() -> opBankServiceUnavailableFilter.handle(httpRequest))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void shouldReturnBankServiceErrorWhenMessageUnavailable() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_OK);

        final String responseBody =
                "{\n"
                        + "\t\t\"type\": \"TECHNICAL\",\n"
                        + "\t\t\"message\": \"Service unavailable.\",\n"
                        + "\t\t\"violations\": \"[]\"\n"
                        + "\t\t\"id\": \"G3e78febf-329c-4f55-859b-caa3e4f8312f\"\n"
                        + "}";

        given(httpResponse.getBody(String.class)).willReturn(responseBody);

        // when
        when(filter.handle(any())).thenReturn(httpResponse);

        // then
        assertThatThrownBy(() -> opBankServiceUnavailableFilter.handle(httpRequest))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }
}
