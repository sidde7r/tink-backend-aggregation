package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
public class RabobankUserRefreshLimitExceededFilterTest {

    @Mock private Filter call;
    @Mock private HttpResponse response;

    private final RabobankUserRefreshLimitExceededFilter accessExceededFilter =
            new RabobankUserRefreshLimitExceededFilter();

    @Before
    public void setUp() {
        openMocks(this);
        configureMocks();

        accessExceededFilter.setNext(call);
    }

    @Test
    public void shouldThrowOnUserRefreshLimitExceededResponse() {
        // given
        givenResponse(
                429,
                "The maximum number of calls for unattended requests has been exceeded for account with ID [some-id]");

        // expect
        assertThatThrownBy(() -> accessExceededFilter.handle(mock(HttpRequest.class)))
                .isInstanceOf(BankServiceException.class)
                .hasMessageContaining(
                        "calls for unattended requests has been exceeded for account with ID [some-id]");
    }

    @Test
    @Parameters
    public void shouldNotThrowOnStatusesOtherThanTooManyRequests(int statusCode) {
        // given
        givenResponse(
                statusCode,
                "The maximum number of calls for unattended requests has been exceeded for account with ID [some-id]");

        // when
        HttpResponse response = accessExceededFilter.handle(mock(HttpRequest.class));

        // then
        assertThat(response).isSameAs(this.response);
    }

    @Test
    @Parameters
    public void shouldNotThrowOnNotMatchingResponseBodies(String body) {
        // given
        givenResponse(429, body);

        // when
        HttpResponse response = accessExceededFilter.handle(mock(HttpRequest.class));

        // then
        assertThat(response).isSameAs(this.response);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldNotThrowOnStatusDifferentThanAccessExceeded() {
        return new Object[] {200, 201, 202, 204, 301, 400, 401, 403, 404, 500, 501, 502, 503};
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldNotThrowOnNotMatchingConstraint() {
        return new Object[] {"other error message body", null};
    }

    private void configureMocks() {
        when(call.handle(any())).thenReturn(response);
    }

    private void givenResponse(int statusCode, String responseBody) {
        given(response.getStatus()).willReturn(statusCode);
        given(response.getBody(String.class)).willReturn(responseBody);
    }
}
