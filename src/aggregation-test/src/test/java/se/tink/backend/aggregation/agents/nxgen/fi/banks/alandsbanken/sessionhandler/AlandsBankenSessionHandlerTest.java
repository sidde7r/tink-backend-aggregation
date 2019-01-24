package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.sessionhandler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.CrossKeySessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.rpc.LogoutResponse;
import se.tink.backend.mocks.ResultCaptor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;

public class AlandsBankenSessionHandlerTest extends AlandsBankenTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void refreshSessionSucceeds() throws Exception {
        ResultCaptor<KeepAliveResponse> resultCaptor = new ResultCaptor();
        doAnswer(resultCaptor).when(client).keepAlive();

        CrossKeySessionHandler sessionHandler = new CrossKeySessionHandler(client);
        sessionHandler.keepAlive();

        assertFalse(resultCaptor.getActual().isFailure());
    }

    @Test
    public void refreshSessionFailsWhenLoggedOut() throws Exception {
        SessionException expectedException = SessionError.SESSION_EXPIRED.exception();
        this.exception.expect(expectedException.getClass());
        this.exception.expectMessage(expectedException.getMessage());

        CrossKeySessionHandler sessionHandler = new CrossKeySessionHandler(client);
        sessionHandler.logout();
        sessionHandler.keepAlive();
    }

    @Test
    public void loggingOutExpiresSession() throws Exception {
        CrossKeySessionHandler sessionHandler = new CrossKeySessionHandler(client);
        sessionHandler.logout();

        ResultCaptor<LogoutResponse> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(client).logout();

        sessionHandler.logout();

        assertTrue(resultCaptor.getActual().sessionExpired());
    }
}
