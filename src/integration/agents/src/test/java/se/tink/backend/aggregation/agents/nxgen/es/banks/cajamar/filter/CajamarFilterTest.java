package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.rpc.CajamarErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class CajamarFilterTest {

    private CajamarUnauthorizedFilter forbiddenFilter;

    @Before
    public void setup() {
        this.forbiddenFilter = new CajamarUnauthorizedFilter();
    }

    @Test
    public void shouldHandleSessionException() throws IOException {
        // given
        HttpResponse response = mockResponse(401, ErrorCodes.APP_PROBLEM);

        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenReturn(response);
        forbiddenFilter.setNext(nextFilter);

        // when
        Throwable throwable = catchThrowable(() -> forbiddenFilter.handle(null));

        // expected
        assertThat(throwable).isExactlyInstanceOf(SessionException.class);
    }

    @Test
    public void shouldHandleLoginException() throws IOException {
        // given
        HttpResponse response = mockResponse(401, "SYS01");

        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenReturn(response);
        forbiddenFilter.setNext(nextFilter);

        // when
        Throwable throwable = catchThrowable(() -> forbiddenFilter.handle(null));

        // expected
        assertThat(throwable).isExactlyInstanceOf(LoginException.class);
    }

    private HttpResponse mockResponse(int status, String codeError) throws IOException {
        CajamarErrorResponse errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\"code\": \"" + codeError + "\",\"message\": \"Error\"}",
                                CajamarErrorResponse.class);
        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(status);
        given(response.getBody(CajamarErrorResponse.class)).willReturn(errorResponse);

        return response;
    }
}
