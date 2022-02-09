package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.NordeaFIAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateCode;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateTokenResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.Catalog;

@RunWith(MockitoJUnitRunner.class)
public class NordeaFIAuthenticatorTest {
    @Mock NordeaFIApiClient apiClient;
    @Mock SessionStorage sessionStorage;
    @Mock SupplementalInformationController supplementalInformationController;
    @Mock Catalog catalog;
    @Mock RandomValueGenerator randomValueGenerator;
    @Mock Credentials credentials;
    @Mock AuthenticateResponse authenticateInitResponse;
    @Mock AuthenticateResponse authenticateResponse;
    @Mock AuthenticateCode authenticateCode;
    @Mock AuthenticateTokenResponse authenticateTokenResponse;

    private NordeaFIAuthenticator nordeaFIAuthenticator;

    private static final String RANDOM_CODE =
            "bElkNm0xWTBEVFBaTXg0TE12TmVFVHpCNEtOQ2VYc3FCc3RVZkU4WTNmVTJtcndXSHpBUFFmQ1IwaHVuazhaVjYzdm9sR0pWS2dyekFSbUhTSWVHTzdSNlpkaXNiYTJ4";

    @Before
    public void init() {
        nordeaFIAuthenticator =
                new NordeaFIAuthenticator(
                        apiClient,
                        sessionStorage,
                        supplementalInformationController,
                        catalog,
                        randomValueGenerator);
    }

    @Test
    public void shouldAuthenticateSuccessfully() {
        // given
        when(randomValueGenerator.generateRandomBase64UrlEncoded(96)).thenReturn(RANDOM_CODE);
        when(credentials.getField(Field.Key.USERNAME)).thenReturn("mockUsername");

        when(authenticateInitResponse.getStatus()).thenReturn(ThirdPartyAppStatus.DONE);
        when(apiClient.initAuthentication(eq("mockUsername"), any()))
                .thenReturn(authenticateInitResponse);
        when(authenticateResponse.getStatus())
                .thenReturn(ThirdPartyAppStatus.WAITING)
                .thenReturn(ThirdPartyAppStatus.WAITING)
                .thenReturn(ThirdPartyAppStatus.DONE);
        when(authenticateResponse.getCode()).thenReturn("mockCode1");
        when(apiClient.getAuthenticationStatus(any())).thenReturn(authenticateResponse);

        when(authenticateCode.getCode()).thenReturn("mockCode2");
        when(apiClient.getAuthenticateCode(any())).thenReturn(authenticateCode);

        when(apiClient.getAuthenticationToken(anyString(), anyString()))
                .thenReturn(authenticateTokenResponse);

        // when
        nordeaFIAuthenticator.authenticate(credentials);

        // then
        verify(supplementalInformationController, times(1)).askSupplementalInformationSync(any());
        verify(authenticateTokenResponse, times(1)).storeTokens(sessionStorage);
    }

    @Test
    public void shouldHandleAuthenticateInitError() {
        // given
        when(randomValueGenerator.generateRandomBase64UrlEncoded(96)).thenReturn(RANDOM_CODE);
        when(credentials.getField(Field.Key.USERNAME)).thenReturn("mockUsername");

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        AuthenticateErrorResponse authenticateErrorResponse = mock(AuthenticateErrorResponse.class);
        when(httpResponse.getBody(AuthenticateErrorResponse.class))
                .thenReturn(authenticateErrorResponse);
        when(authenticateErrorResponse.getStatus())
                .thenReturn(ThirdPartyAppStatus.AUTHENTICATION_ERROR);

        when(apiClient.initAuthentication(eq("mockUsername"), any()))
                .thenThrow(httpResponseException);

        // when
        Throwable throwable = catchThrowable(() -> nordeaFIAuthenticator.authenticate(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessageContaining("ThirdPartyAppError.AUTHENTICATION_ERROR");
    }

    @Test
    public void shouldHandleAuthenticateStatusError() {
        // given
        when(randomValueGenerator.generateRandomBase64UrlEncoded(96)).thenReturn(RANDOM_CODE);
        when(credentials.getField(Field.Key.USERNAME)).thenReturn("mockUsername");

        when(authenticateInitResponse.getStatus()).thenReturn(ThirdPartyAppStatus.DONE);
        when(apiClient.initAuthentication(eq("mockUsername"), any()))
                .thenReturn(authenticateInitResponse);
        when(authenticateResponse.getStatus())
                .thenReturn(ThirdPartyAppStatus.WAITING)
                .thenReturn(ThirdPartyAppStatus.WAITING)
                .thenReturn(ThirdPartyAppStatus.DONE);
        when(authenticateResponse.getCode()).thenReturn("mockCode1");

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        AuthenticateErrorResponse authenticateErrorResponse = mock(AuthenticateErrorResponse.class);
        when(authenticateErrorResponse.getStatus())
                .thenReturn(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
        when(apiClient.getAuthenticationStatus(any())).thenThrow(httpResponseException);

        // when
        Throwable throwable = catchThrowable(() -> nordeaFIAuthenticator.authenticate(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessageContaining("ThirdPartyAppError.AUTHENTICATION_ERROR");
    }
}
