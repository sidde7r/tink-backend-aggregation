package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.filter;

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
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.filter.entity.EvoBancoErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class EvoBancoInvalidTokenFilterTest {

    private EvoBancoTokenInvalidFilter invalidTokenFilter;

    @Before
    public void setup() {
        invalidTokenFilter = new EvoBancoTokenInvalidFilter();
    }

    @Test
    public void shouldHandleSessionException() throws IOException {
        // given
        HttpResponse response = mockResponse();

        Filter nextFilter = mock(Filter.class);
        when(nextFilter.handle(any())).thenReturn(response);
        invalidTokenFilter.setNext(nextFilter);

        // when
        Throwable throwable = catchThrowable(() -> invalidTokenFilter.handle(null));

        // expected
        assertThat(throwable).isExactlyInstanceOf(SupplementalInfoException.class);
    }

    private HttpResponse mockResponse() throws IOException {
        EvoBancoErrorResponse errorResponse =
                new ObjectMapper()
                        .readValue(
                                "{\"response\":{\"message\":\"Token de acceso inv?lido\",\"codigo\":\"KO\"}}",
                                EvoBancoErrorResponse.class);

        HttpResponse response = mock(HttpResponse.class);
        given(response.getStatus()).willReturn(401);
        given(response.getBody(EvoBancoErrorResponse.class)).willReturn(errorResponse);

        return response;
    }
}
