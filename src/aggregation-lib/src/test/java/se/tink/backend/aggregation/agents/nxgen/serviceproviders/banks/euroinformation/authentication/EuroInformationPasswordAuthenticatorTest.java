package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.PfmInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationErrorCodes;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class EuroInformationPasswordAuthenticatorTest {

    public EuroInformationApiClient apiClient;
    public EuroInformationConfiguration config;
    public SessionStorage sessionStorage;
    public EuroInformationPasswordAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        this.apiClient = Mockito.mock(EuroInformationApiClient.class);
        this.config = Mockito.mock(EuroInformationConfiguration.class);
        this.sessionStorage = new SessionStorage();
        this.authenticator = EuroInformationPasswordAuthenticator.create(apiClient, sessionStorage, config);
    }

    @Test
    public void authenticate_properCodes() throws AuthenticationException, AuthorizationException {
        LoginResponse logon = Mockito.mock(LoginResponse.class);
        when(apiClient.logon(any(), any())).thenReturn(logon);
        when(logon.getReturnCode()).thenReturn(EuroInformationErrorCodes.SUCCESS.getCodeNumber());

        PfmInitResponse init = Mockito.mock(PfmInitResponse.class);
        when(apiClient.actionInit(any())).thenReturn(init);
        when(init.getReturnCode()).thenReturn(EuroInformationErrorCodes.SUCCESS.getCodeNumber());

        when(config.getInitEndpoint()).thenCallRealMethod();

        authenticator.authenticate("", "");
        assertTrue(sessionStorage.containsKey(EuroInformationConstants.Tags.PFM_ENABLED));
    }

    @Test(expected = SessionException.class)
    public void authenticate_errorCodeOnLogon() throws AuthenticationException, AuthorizationException {
        LoginResponse logon = Mockito.mock(LoginResponse.class);
        when(apiClient.logon(any(), any())).thenReturn(logon);
        when(logon.getReturnCode()).thenReturn(EuroInformationErrorCodes.NOT_LOGGED_IN.getCodeNumber());

        authenticator.authenticate("", "");
    }

    @Test
    public void authenticate_errorOnPmf() throws AuthenticationException, AuthorizationException {
        LoginResponse logon = Mockito.mock(LoginResponse.class);
        when(apiClient.logon(any(), any())).thenReturn(logon);
        when(logon.getReturnCode()).thenReturn(EuroInformationErrorCodes.SUCCESS.getCodeNumber());

        PfmInitResponse init = Mockito.mock(PfmInitResponse.class);
        when(apiClient.actionInit(any())).thenReturn(init);
        when(init.getReturnCode()).thenReturn(EuroInformationErrorCodes.TECHNICAL_PROBLEM.getCodeNumber());

        when(config.getInitEndpoint()).thenCallRealMethod();

        authenticator.authenticate("", "");
        assertFalse(sessionStorage.containsKey(EuroInformationConstants.Tags.PFM_ENABLED));
    }

    @Test
    public void authenticate_noPfmEndpoint() throws AuthenticationException, AuthorizationException {
        LoginResponse logon = Mockito.mock(LoginResponse.class);
        when(apiClient.logon(any(), any())).thenReturn(logon);
        when(logon.getReturnCode()).thenReturn(EuroInformationErrorCodes.SUCCESS.getCodeNumber());

        when(config.getInitEndpoint()).thenReturn(Optional.empty());

        authenticator.authenticate("", "");
        assertFalse(sessionStorage.containsKey(EuroInformationConstants.Tags.PFM_ENABLED));
    }
}
