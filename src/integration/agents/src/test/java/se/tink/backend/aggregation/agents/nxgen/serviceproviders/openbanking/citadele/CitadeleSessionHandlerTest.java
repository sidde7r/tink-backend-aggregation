package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.sessionhandler.CitadeleSessionHandler;

@RunWith(JUnitParamsRunner.class)
public class CitadeleSessionHandlerTest {

    private CitadeleBaseApiClient apiClient;
    private CitadeleSessionHandler sessionHandler;

    @Before
    public void setUp() {
        apiClient = mock(CitadeleBaseApiClient.class);
    }

    @Test
    public void shouldKeepSessionAlive() {
        // given
        when(apiClient.getConsentStatus()).thenReturn(ConsentStatus.VALID);

        // when
        sessionHandler = new CitadeleSessionHandler(apiClient, LocalDateTime.now().plusDays(1));

        // then
        sessionHandler.keepAlive();
    }

    @Test
    @Parameters(method = "sessionParameters")
    public void shouldThrowSessionExpired(LocalDateTime expirationDate, String status) {
        // given
        when(apiClient.getConsentStatus()).thenReturn(status);

        // when
        sessionHandler = new CitadeleSessionHandler(apiClient, expirationDate);

        // then
        assertThatThrownBy(() -> sessionHandler.keepAlive()).isInstanceOf(SessionException.class);
    }

    private Object[] sessionParameters() {
        return new Object[] {
            new Object[] {LocalDateTime.now().plusDays(-1), ConsentStatus.VALID},
            new Object[] {LocalDateTime.now().plusDays(-1), "notValid"},
            new Object[] {LocalDateTime.now().plusDays(0), ConsentStatus.VALID},
            new Object[] {LocalDateTime.now().plusDays(1), "notValid"},
        };
    }
}
