package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filters;

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
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ServiceBlockedFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc.TppErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBlockedFilterTest {

    @Mock private SystemUpdater systemUpdater;
    @Mock private Credentials credentials;
    @Mock private HttpRequest httpRequest;
    @Mock private Filter filter;
    @Mock private HttpResponse response;
    @Mock private TppErrorResponse tppErrorResponse;
    private ServiceBlockedFilter serviceBlockedFilter;

    @Before
    public void setUp() {
        serviceBlockedFilter = new ServiceBlockedFilter(systemUpdater, credentials);
        serviceBlockedFilter.setNext(filter);
    }

    @Test
    public void shouldReturnBankServiceError() {
        // given
        given(response.getStatus()).willReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);

        // when
        when(filter.handle(any())).thenReturn(response);

        // then
        assertThatThrownBy(() -> serviceBlockedFilter.handle(httpRequest))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void shouldReturnSessionError() {
        // given
        given(response.getStatus()).willReturn(HttpStatus.SC_FORBIDDEN);
        given(response.getBody(TppErrorResponse.class)).willReturn(tppErrorResponse);
        given(tppErrorResponse.isAnyServiceBlocked()).willReturn(true);

        // when
        when(filter.handle(any())).thenReturn(response);

        // then
        assertThatThrownBy(() -> serviceBlockedFilter.handle(httpRequest))
                .isInstanceOf(SessionError.CONSENT_EXPIRED.exception().getClass());
    }

    @Test
    public void shouldReturnResponse() {
        // given
        given(response.getStatus()).willReturn(HttpStatus.SC_FORBIDDEN);
        given(response.getBody(TppErrorResponse.class)).willReturn(tppErrorResponse);
        given(tppErrorResponse.isAnyServiceBlocked()).willReturn(false);

        // when
        when(filter.handle(any())).thenReturn(response);

        // then
        assertEquals(serviceBlockedFilter.handle(httpRequest), response);
    }
}
