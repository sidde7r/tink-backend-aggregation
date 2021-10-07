package src.integration.agents.src.test.java.se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankMockSeBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.BankIdInitResponse;

public class DemobankMockSeBankIdAuthenticatorTest {

    private DemobankMockSeBankIdAuthenticator authenticator;
    private final DemobankApiClient apiClient = mock(DemobankApiClient.class);

    private static final String SE_TEST_SSN = "";

    @Before
    public void setup() {
        authenticator = new DemobankMockSeBankIdAuthenticator(apiClient);
    }

    @Test
    public void testGetAutostartToken() {
        String autostartToken = authenticator.getAutostartToken().get();
        assertEquals(36, autostartToken.length());
    }

    @Test
    public void testInitHappyPath() {
        // given
        BankIdInitResponse response = new BankIdInitResponse();
        String sessionId = UUID.randomUUID().toString();
        response.setSessionId(sessionId);

        // when
        when(apiClient.initBankIdSe(any())).thenReturn(response);
        String sid = authenticator.init(SE_TEST_SSN);

        // then
        assertEquals(sessionId, sid);
    }

    @Test
    public void shouldThrowBankIdErrorWhenInitAlreadyInProcess() {
        // given
        BankIdInitResponse response = new BankIdInitResponse();
        String sessionId = UUID.randomUUID().toString();
        response.setSessionId(sessionId);
        response.setErrorCode("ALREADY_IN_PROGRESS");

        // when
        when(apiClient.initBankIdSe(any())).thenReturn(response);
        BankIdException e =
                catchThrowableOfType(() -> authenticator.init(SE_TEST_SSN), BankIdException.class);

        // then
        assertNotNull(e);
        assertEquals(BankIdError.ALREADY_IN_PROGRESS, e.getError());
    }

    @Test
    public void testCollectCompletedStatus() {
        // given
        BankIdCollectResponse response = new BankIdCollectResponse();
        response.setCode("COMPLETED");

        // when
        when(apiClient.collectBankIdSe(any(), any())).thenReturn(response);
        BankIdStatus status = authenticator.collect("test");

        // then
        assertEquals(BankIdStatus.DONE, status);
        verify(apiClient).setTokenToStorage(any());
    }

    @Test
    public void testCollectWaitingStatus() {
        // given
        BankIdCollectResponse response = new BankIdCollectResponse();
        response.setCode("WAITING");

        // when
        when(apiClient.collectBankIdSe(any(), any())).thenReturn(response);
        BankIdStatus status = authenticator.collect("test");

        // then
        assertEquals(BankIdStatus.WAITING, status);
        verify(apiClient, never()).setTokenToStorage(any());
    }
}
