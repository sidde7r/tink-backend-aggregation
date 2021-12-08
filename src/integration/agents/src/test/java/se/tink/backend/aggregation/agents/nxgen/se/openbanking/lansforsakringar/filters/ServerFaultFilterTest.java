package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filters;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ServerFaultFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc.FaultResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class ServerFaultFilterTest {

    @Mock private HttpRequest httpRequest;
    @Mock private Filter filter;
    @Mock private HttpResponse response;
    @Mock private FaultResponse faultResponse;
    private ServerFaultFilter serverFaultFilter;

    @Before
    public void setUp() {
        serverFaultFilter = new ServerFaultFilter();
        serverFaultFilter.setNext(filter);
    }

    @Test
    public void shouldThrowBankServiceErrorIfServerFault() {
        // given
        given(response.getStatus()).willReturn(HttpStatus.SC_UNAUTHORIZED);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(FaultResponse.class)).willReturn(faultResponse);
        given(response.getType()).willReturn(MediaType.APPLICATION_JSON_TYPE);
        given(faultResponse.isServerFault()).willReturn(true);

        // when
        when(filter.handle(any())).thenThrow(new HttpResponseException(null, response));

        // then
        assertThatThrownBy(() -> serverFaultFilter.handle(httpRequest))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void shouldReturnResponse() {
        // given
        given(response.getStatus()).willReturn(HttpStatus.SC_UNAUTHORIZED);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(FaultResponse.class)).willReturn(faultResponse);

        // when
        when(filter.handle(any())).thenReturn(response);

        // then
        assertEquals(serverFaultFilter.handle(httpRequest), response);
    }
}
