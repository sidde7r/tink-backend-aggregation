package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class CheckIfAccessTokenIsValidStepTest {

    @Test
    @Parameters(method = "getTokenAndConsentStatus")
    public void shouldReturnTrueWhenValidTokenAndValidConsent(
            OAuth2Token oAuth2Token, boolean isConsentValid, boolean isAuthenticationFinished) {

        // given
        SebBalticsApiClient apiClient = mock(SebBalticsApiClient.class);
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        CheckIfAccessTokenIsValidStep checkIfAccessTokenIsValidStep =
                new CheckIfAccessTokenIsValidStep(apiClient, persistentStorage);
        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(mock(Credentials.class));

        // when
        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(oAuth2Token));
        when(apiClient.isConsentValid()).thenReturn(isConsentValid);

        // then
        Assert.assertEquals(
                isAuthenticationFinished,
                checkIfAccessTokenIsValidStep
                        .execute(authenticationRequest)
                        .isAuthenticationFinished());
    }

    private Object[] getTokenAndConsentStatus() {
        return new Object[] {
            new Object[] {getOAuth2TokenWithMissingAccessToken(), true, false},
            new Object[] {getValidOAuth2Token(), false, false},
            new Object[] {getOAuth2TokenWithMissingAccessToken(), false, false},
            new Object[] {getValidOAuth2Token(), true, true}
        };
    }

    private OAuth2Token getOAuth2TokenWithMissingAccessToken() {
        TokenResponse tokenResponse =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "  \"token_type\": \"Bearer\",\n"
                                + "  \"refresh_token\": \"refreshToken1\",\n"
                                + "  \"refresh_token_expires_in\": 7775999,\n"
                                + "  \"expires_in\": 3599,\n"
                                + "  \"scope\": \"account.lists accounts consents\"\n"
                                + "}",
                        TokenResponse.class);
        return tokenResponse.toTinkToken();
    }

    private OAuth2Token getValidOAuth2Token() {
        TokenResponse tokenResponse =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "  \"access_token\": \"accessToken1\",\n"
                                + "  \"token_type\": \"Bearer\",\n"
                                + "  \"refresh_token\": \"refreshToken1\",\n"
                                + "  \"refresh_token_expires_in\": 7775999,\n"
                                + "  \"expires_in\": 3599,\n"
                                + "  \"scope\": \"account.lists accounts consents\"\n"
                                + "}",
                        TokenResponse.class);
        return tokenResponse.toTinkToken();
    }
}
