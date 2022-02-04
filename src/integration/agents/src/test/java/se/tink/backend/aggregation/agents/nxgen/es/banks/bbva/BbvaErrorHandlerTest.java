package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class BbvaErrorHandlerTest {

    @Mock private HttpResponseException responseException;

    @Mock private HttpResponse httpResponse;

    @Mock private BbvaErrorResponse errorResponse;

    @Before
    public void init() {
        when(responseException.getResponse()).thenReturn(httpResponse);
    }

    @Test
    public void shouldHandleInvalidOtpCodeError() {
        // given
        when(httpResponse.getStatus()).thenReturn(401);
        when(errorResponse.getErrorCode()).thenReturn("168");
        when(httpResponse.getBody(BbvaErrorResponse.class)).thenReturn(errorResponse);

        // when
        Optional<AgentException> result = BbvaErrorHandler.handle(responseException);

        // then
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getError())
                .isEqualTo(SupplementalInfoError.NO_VALID_CODE);
    }

    @Test
    public void shouldHandleOtpTimeoutError() {
        // given
        when(httpResponse.getStatus()).thenReturn(403);
        when(errorResponse.getErrorCode()).thenReturn("362");
        when(httpResponse.getBody(BbvaErrorResponse.class)).thenReturn(errorResponse);

        // when
        Optional<AgentException> result = BbvaErrorHandler.handle(responseException);

        // then
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getError())
                .isEqualTo(SupplementalInfoError.WAIT_TIMEOUT);
    }

    @Test
    public void shouldNotHandleWhenErrorIsNotResolved() {
        // given
        when(httpResponse.getStatus()).thenReturn(403);
        when(errorResponse.getErrorCode()).thenReturn("0");
        when(httpResponse.getBody(BbvaErrorResponse.class)).thenReturn(errorResponse);

        // when
        Optional<AgentException> result = BbvaErrorHandler.handle(responseException);

        // then
        Assertions.assertThat(result).isEmpty();
    }
}
