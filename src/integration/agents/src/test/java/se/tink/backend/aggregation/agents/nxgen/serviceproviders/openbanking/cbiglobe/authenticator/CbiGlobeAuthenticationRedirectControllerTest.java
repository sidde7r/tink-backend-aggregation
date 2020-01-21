package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CbiGlobeAuthenticationRedirectControllerTest {

    private CbiGlobeAuthenticationController objectToTest;
    private SupplementalInformationHelper supplementalInformationHelper =
            mock(SupplementalInformationHelper.class);
    private CbiGlobeAuthenticator authenticator = mock(CbiGlobeAuthenticator.class);
    private StrongAuthenticationState consentState = mock(StrongAuthenticationState.class);

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        objectToTest =
                new CbiGlobeAuthenticationRedirectController(
                        supplementalInformationHelper, authenticator, consentState);

        doNothing().when(supplementalInformationHelper).openThirdPartyApp(any());
        when(authenticator.createRedirectUrl(any(), any())).thenReturn("asdf");
        when(authenticator.buildAuthorizeUrl(any(), any())).thenReturn(new URL("asdf"));
    }

    @Test
    public void whenNoSupplementalInformationReturnedShouldThrowLoginException()
            throws AuthenticationException, AuthorizationException {

        Optional<Map<String, String>> optionalToReturn = Optional.empty();
        when(supplementalInformationHelper.waitForSupplementalInformation(any(), anyLong(), any()))
                .thenReturn(optionalToReturn);

        thrown.expect(LoginException.class);

        objectToTest.authenticate(null);
    }

    @Test
    public void whenSomeSupplementalInfoReturnedShouldCompleteSuccessfully()
            throws AuthenticationException, AuthorizationException {

        doNothing().when(supplementalInformationHelper).openThirdPartyApp(any());
        when(authenticator.createRedirectUrl(any(), any())).thenReturn("asdf");
        when(authenticator.buildAuthorizeUrl(any(), any())).thenReturn(new URL("asdf"));

        Optional<Map<String, String>> optionalToReturn = Optional.of(new HashMap<>());
        when(supplementalInformationHelper.waitForSupplementalInformation(any(), anyLong(), any()))
                .thenReturn(optionalToReturn);

        objectToTest.authenticate(null);
    }
}
