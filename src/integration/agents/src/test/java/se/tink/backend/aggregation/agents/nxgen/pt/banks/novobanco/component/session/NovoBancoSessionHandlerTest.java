package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.session;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.NovoBancoSessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NovoBancoSessionHandlerTest {

    @Test(expected = SessionException.class)
    public void shouldThrowExceptionIfNoLongerAlive() throws SessionException {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.isAlive()).thenReturn(false);
        NovoBancoSessionHandler handler =
                new NovoBancoSessionHandler(apiClient, new SessionStorage());
        // when
        handler.keepAlive();
    }

    @Test
    public void shouldNotThrowIfSessionIsStillAlive() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.isAlive()).thenReturn(true);
        NovoBancoSessionHandler handler =
                new NovoBancoSessionHandler(apiClient, new SessionStorage());

        // when
        Throwable thrown = catchThrowable(handler::keepAlive);

        // then
        assertNull(thrown);
    }
}
