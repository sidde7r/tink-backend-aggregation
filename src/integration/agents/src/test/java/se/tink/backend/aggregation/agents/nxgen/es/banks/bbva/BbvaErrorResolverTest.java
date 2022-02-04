package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class BbvaErrorResolverTest {

    @Mock private HttpResponse httpResponse;

    @Mock private BbvaErrorResponse bbvaErrorResponse;

    @Test
    public void shouldResolveAndReturnDedicatedException() {
        // given
        final String errorCode = "100";
        BbvaErrorResolver objectUnderTest =
                new BbvaErrorResolver(
                        401, errorCode, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());
        when(bbvaErrorResponse.getErrorCode()).thenReturn(errorCode);
        when(httpResponse.getBody(BbvaErrorResponse.class)).thenReturn(bbvaErrorResponse);
        when(httpResponse.getStatus()).thenReturn(401);

        // when
        Optional<AgentException> result = objectUnderTest.resolve(httpResponse);

        // then
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getError())
                .isEqualTo(LoginError.CREDENTIALS_VERIFICATION_ERROR);
    }

    @Test
    public void shouldReturnEmptyResultWhenErrorCodeIsNotResolved() {
        // given
        BbvaErrorResolver objectUnderTest =
                new BbvaErrorResolver(
                        401, "101", LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());
        when(bbvaErrorResponse.getErrorCode()).thenReturn("100");
        when(httpResponse.getBody(BbvaErrorResponse.class)).thenReturn(bbvaErrorResponse);
        when(httpResponse.getStatus()).thenReturn(401);

        // when
        Optional<AgentException> result = objectUnderTest.resolve(httpResponse);

        // then
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyResultWhenErrorHttpStatusIsNotResolved() {
        // given
        final String errorCode = "100";
        BbvaErrorResolver objectUnderTest =
                new BbvaErrorResolver(
                        401, errorCode, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());
        when(httpResponse.getStatus()).thenReturn(403);

        // when
        Optional<AgentException> result = objectUnderTest.resolve(httpResponse);

        // then
        Assertions.assertThat(result).isEmpty();
    }
}
