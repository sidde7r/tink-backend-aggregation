package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.CORRECT_PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createAuthenticationRequest;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createExpiredOAuth2Token;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createOAuth2Token;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createTokenResponseDto;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TokenResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class AktiaAccessTokenRetrieverTest {

    private AktiaAccessTokenRetriever accessTokenRetriever;

    private AktiaApiClient aktiaApiClientMock;

    private OAuth2TokenStorage tokenStorageMock;

    @Before
    public void setUp() {
        aktiaApiClientMock = mock(AktiaApiClient.class);
        final TokenResponseDto tokenResponseDto = createTokenResponseDto();
        when(aktiaApiClientMock.retrieveAccessToken(USERNAME, CORRECT_PASSWORD))
                .thenReturn(tokenResponseDto);

        tokenStorageMock = mock(OAuth2TokenStorage.class);

        accessTokenRetriever = new AktiaAccessTokenRetriever(aktiaApiClientMock, tokenStorageMock);
    }

    @Test
    public void shouldGetStatusForValidToken() {
        // given
        final OAuth2Token oAuth2Token = createOAuth2Token();

        when(tokenStorageMock.getToken()).thenReturn(Optional.of(oAuth2Token));

        // when
        final AccessTokenStatus returnedStatus = accessTokenRetriever.getStatusFromStorage();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.VALID);
    }

    @Test
    public void shouldGetStatusForExpiredToken() {
        // given
        final OAuth2Token oAuth2Token = createExpiredOAuth2Token();

        when(tokenStorageMock.getToken()).thenReturn(Optional.of(oAuth2Token));

        // when
        final AccessTokenStatus returnedStatus = accessTokenRetriever.getStatusFromStorage();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.EXPIRED);
    }

    @Test
    public void shouldGetStatusWhenTokenIsNotPresent() {
        // given
        when(tokenStorageMock.getToken()).thenReturn(Optional.empty());

        // when
        final AccessTokenStatus returnedStatus = accessTokenRetriever.getStatusFromStorage();

        // then
        assertThat(returnedStatus).isEqualTo(AccessTokenStatus.NOT_PRESENT);
    }

    @Test
    public void shouldRetrieveAndStoreAccessToken() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        // when
        final Throwable thrown =
                catchThrowable(
                        () -> accessTokenRetriever.getFromRequestAndStore(authenticationRequest));

        // then
        assertThat(thrown).isNull();

        verify(aktiaApiClientMock).retrieveAccessToken(USERNAME, CORRECT_PASSWORD);
        verify(tokenStorageMock).storeToken(any(OAuth2Token.class));
    }
}
