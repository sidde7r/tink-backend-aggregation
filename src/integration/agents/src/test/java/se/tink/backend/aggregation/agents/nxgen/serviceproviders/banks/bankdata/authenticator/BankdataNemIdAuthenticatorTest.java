package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.CryptoHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.CompleteEnrollResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;

@RunWith(JUnitParamsRunner.class)
public class BankdataNemIdAuthenticatorTest {

    private static final String USER_ID = "userId";
    private static final String PIN_CODE = "pinCode";
    private static final String INSTALL_ID = "installId";

    private BankdataNemIdAuthenticator bankdataNemIdAuthenticator;
    private BankdataApiClient bankClient;
    private Storage mockedStorage;
    private Storage storage;
    private HttpResponse httpResponse;
    private NemIdParameters nemIdParameters;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        bankClient = mock(BankdataApiClient.class);
        mockedStorage = mock(Storage.class);

        httpResponse = mock(HttpResponse.class);
        bankdataNemIdAuthenticator = new BankdataNemIdAuthenticator(bankClient, mockedStorage);
        nemIdParameters = new NemIdParameters("testElements");

        storage = new Storage();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.genKeyPair();
        PublicKey publicKey = kp.getPublic();
        PrivateKey privateKey = kp.getPrivate();
        storage.put("KAY_PAIR_ID_STORAGE", "1234");
        storage.put("PUBLIC_KEY_STORAGE", publicKey.getEncoded());
        storage.put("PRIVATE_KEY_STORAGE", privateKey.getEncoded());
        storage.put("SESSION_KEY_STORAGE", "MTIzMTI=");
        storage.put("IV_STORAGE", "MTIzMTI=");
    }

    @Test
    public void shouldGetNemIdParameters() {
        // Given
        when(bankClient.portal()).thenReturn(httpResponse);
        when(bankClient.fetchNemIdParameters(httpResponse)).thenReturn(nemIdParameters);

        // When
        NemIdParameters result = bankdataNemIdAuthenticator.getNemIdParameters();

        // Then
        verify(bankClient).nemIdInit(any(CryptoHelper.class));
        verify(bankClient).portal();
        verify(bankClient).fetchNemIdParameters(httpResponse);
        assertThat(result).isEqualTo(nemIdParameters);
    }

    @Test
    public void shouldExchangeNemIdToken() throws IOException {
        // Given
        String json =
                "{ \"customerName\" : \"name\", \"nemIDNo\" : \"testIDNo\", \"installId\" : \"installId\" }";
        ObjectMapper objectMapper = new ObjectMapper();
        String nemIdToken = "nemIdtoken";
        CompleteEnrollResponse completeEnrollResponse =
                objectMapper.readValue(json, CompleteEnrollResponse.class);
        when(bankClient.completeEnrollment(any())).thenReturn(completeEnrollResponse);

        // When
        String result = bankdataNemIdAuthenticator.exchangeNemIdToken(nemIdToken);

        // Then
        verify(bankClient).eventDoContinue(nemIdToken);
        assertThat(result).isEqualTo(INSTALL_ID);
    }

    @Test
    public void shouldAuthenticateUsingInstallId()
            throws LoginException, AuthorizationException, SessionException {
        // Given
        bankdataNemIdAuthenticator = new BankdataNemIdAuthenticator(bankClient, storage);

        // When
        bankdataNemIdAuthenticator.authenticateUsingInstallId(USER_ID, PIN_CODE, INSTALL_ID);

        // Then
        verify(bankClient).nemIdInit(any(CryptoHelper.class));
        verify(bankClient)
                .loginWithInstallId(
                        eq(USER_ID), eq(PIN_CODE), eq(INSTALL_ID), any(CryptoHelper.class));
    }

    @Test
    @Parameters(method = "getValuesInstallId")
    public void shouldThrowSessionExceptionWhenInstallIdIsNullOrEmpty(String installIdParam) {
        // Given
        bankdataNemIdAuthenticator = new BankdataNemIdAuthenticator(bankClient, mockedStorage);

        // When
        Throwable t =
                catchThrowable(
                        () ->
                                bankdataNemIdAuthenticator.authenticateUsingInstallId(
                                        USER_ID, PIN_CODE, installIdParam));

        // Then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    private Object[] getValuesInstallId() {
        return new Object[] {
            new Object[] {
                INSTALL_ID,
            },
            new Object[] {null}
        };
    }

    @Test
    @Parameters(method = "getValuesForUserIdAndPinCode")
    public void shouldThrowLoginExceptionWhenUserIdOrPinCodeIsNullOrEmpty(
            String userId, String pinCode) {
        // Given
        bankdataNemIdAuthenticator = new BankdataNemIdAuthenticator(bankClient, mockedStorage);

        // When
        Throwable t =
                catchThrowable(
                        () ->
                                bankdataNemIdAuthenticator.authenticateUsingInstallId(
                                        userId, pinCode, INSTALL_ID));

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    private Object[] getValuesForUserIdAndPinCode() {
        return new Object[] {
            new Object[] {USER_ID, null},
            new Object[] {USER_ID, ""},
            new Object[] {null, PIN_CODE},
            new Object[] {"", PIN_CODE}
        };
    }

    @Test
    public void shouldThrowSessionExceptionWhenCryptoHelperIsOptionalEmpty() {
        // Given
        storage.remove("KAY_PAIR_ID_STORAGE");
        bankdataNemIdAuthenticator = new BankdataNemIdAuthenticator(bankClient, mockedStorage);

        // When
        Throwable t =
                catchThrowable(
                        () ->
                                bankdataNemIdAuthenticator.authenticateUsingInstallId(
                                        USER_ID, PIN_CODE, INSTALL_ID));

        // Then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }
}
