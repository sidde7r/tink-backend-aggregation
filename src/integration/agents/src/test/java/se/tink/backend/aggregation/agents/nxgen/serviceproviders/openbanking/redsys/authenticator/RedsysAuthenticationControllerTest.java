package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.ConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import wiremock.com.google.common.collect.ImmutableMap;

public class RedsysAuthenticationControllerTest {
    private RedsysAuthenticationController redsysAuthenticationController;
    private PersistentStorage persistentStorage;
    private ConsentController consentController;
    private SupplementalInformationHelper supplementalInformationHelper;
    private OAuth2Authenticator oAuth2Authenticator;
    private static final String REFRESH_TOKEN = "dummyRefreshToken";
    private static final String STATE = "dummyState";
    private static final String VALUE = "dummyValue";

    @Before
    public void setup() {
        persistentStorage = mock(PersistentStorage.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        oAuth2Authenticator = mock(OAuth2Authenticator.class);
        consentController = mock(ConsentController.class);
        Credentials credentials = mock(Credentials.class);
        StrongAuthenticationState strongAuthenticationState = mock(StrongAuthenticationState.class);
        when(strongAuthenticationState.getSupplementalKey()).thenReturn(STATE);

        redsysAuthenticationController =
                new RedsysAuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        oAuth2Authenticator,
                        consentController,
                        credentials,
                        strongAuthenticationState);
    }

    @Test
    public void authenticator_should_refresh_token_after_succesful_authentication() {
        // given
        OAuth2Token token = mock(OAuth2Token.class);
        when(token.isBearer()).thenReturn(true);
        when(token.hasAccessExpired()).thenReturn(false);
        when(token.getOptionalRefreshToken()).thenReturn(Optional.of(REFRESH_TOKEN));
        when(token.isValid()).thenReturn(true);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        eq(STATE), anyLong(), any(TimeUnit.class)))
                .thenReturn(Optional.of(ImmutableMap.of("code", VALUE)));
        when(oAuth2Authenticator.exchangeAuthorizationCode(VALUE)).thenReturn(token);
        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(token));
        when(oAuth2Authenticator.refreshAccessToken(REFRESH_TOKEN)).thenReturn(token);
        when(consentController.fetchConsentStatus()).thenReturn(ConsentStatus.VALID);

        // when
        redsysAuthenticationController.collect("dummyValue");

        // then
        verify(oAuth2Authenticator, times(1)).refreshAccessToken(REFRESH_TOKEN);
    }
}
