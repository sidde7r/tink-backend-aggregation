package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        sessionHandler = new CitadeleSessionHandler(apiClient);

        // then
        sessionHandler.keepAlive();
    }

    @Test
    @Parameters(method = "sessionParameters")
    public void shouldThrowSessionExpired(String status) {
        // given
        when(apiClient.getConsentStatus()).thenReturn(status);

        // when
        sessionHandler = new CitadeleSessionHandler(apiClient);

        // then
        assertThatThrownBy(() -> sessionHandler.keepAlive()).isInstanceOf(SessionException.class);
    }

    private Object[] sessionParameters() {
        return new Object[] {
            new Object[] {"received"}, new Object[] {"notValid"}, new Object[] {null},
        };
    }
}
