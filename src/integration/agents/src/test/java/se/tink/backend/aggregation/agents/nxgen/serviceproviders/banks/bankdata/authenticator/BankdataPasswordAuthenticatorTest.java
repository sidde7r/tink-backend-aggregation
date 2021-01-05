package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.storage.BankdataStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;

public class BankdataPasswordAuthenticatorTest {

    private static final String USERNAME = "username";
    private static final String PIN_CODE = "pincode";
    private BankdataPasswordAuthenticator bankdataPasswordAuthenticator;
    private BankdataNemIdAuthenticator bankdataNemIdAuthenticator;
    private BankdataStorage bankdataStorage;

    @Before
    public void setup() {
        bankdataNemIdAuthenticator = mock(BankdataNemIdAuthenticator.class);
        bankdataStorage = mock(BankdataStorage.class);
        bankdataPasswordAuthenticator =
                new BankdataPasswordAuthenticator(
                        USERNAME, PIN_CODE, bankdataNemIdAuthenticator, bankdataStorage);
    }

    @Test
    public void shouldAutoAuthenticate()
            throws LoginException, AuthorizationException, SessionException {
        // Given
        when(bankdataStorage.getNemidInstallId())
                .thenReturn(Optional.of(NemIdConstants.Storage.NEMID_INSTALL_ID));

        // When
        bankdataPasswordAuthenticator.autoAuthenticate();

        // Then
        verify(bankdataStorage).getNemidInstallId();
        verify(bankdataNemIdAuthenticator)
                .authenticateUsingInstallId(
                        USERNAME, PIN_CODE, NemIdConstants.Storage.NEMID_INSTALL_ID);
    }

    @Test
    public void shouldThrowExceptionWhenInstallIdIsNull() {
        // Given
        when(bankdataStorage.getNemidInstallId()).thenReturn(Optional.empty());

        // When
        Throwable t = catchThrowable(bankdataPasswordAuthenticator::autoAuthenticate);

        // Then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }
}
