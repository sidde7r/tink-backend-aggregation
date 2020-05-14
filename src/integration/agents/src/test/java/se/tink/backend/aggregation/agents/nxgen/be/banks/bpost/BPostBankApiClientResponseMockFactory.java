package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.LoginResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;

public class BPostBankApiClientResponseMockFactory {

    public static final String CHALLENGE_SING_CODE = "12341234";
    public static final String ORDER_REFERENCE = "3VYTR7JTBOUBAAMJ";
    public static final String DEVICE_INSTALLATION_ID =
            "EYSRHMU69X3NQXW9SDVDZ84FX188WZXKUZS6FNS5RGYYBCMMSHMMQYWN59Q4MXZ6";
    public static final String SESSION_TOKEN_REGISTRATION = "430K2HLBLDM9SYR3LX55Y7W6E7AL72VL";
    public static final String SESSION_TOKEN_LOGIN = "I8HSIWFNU0T7RNAXZE8NG86QHPZWZXJI";
    public static final String CRF_TOKEN = "crfToken";

    public static void mockInitSessionAndGetCSRFToken(BPostBankApiClient apiClientMock)
            throws AuthenticationException {
        Mockito.when(apiClientMock.initSessionAndGetCSRFToken()).thenReturn(CRF_TOKEN);
    }

    public static void mockRegistrationInit(BPostBankApiClient apiClientMock)
            throws AuthenticationException {
        RegistrationResponseDTO responseDTO = Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(responseDTO.getChallengeCode()).thenReturn(CHALLENGE_SING_CODE);
        Mockito.when(responseDTO.getSessionToken()).thenReturn(SESSION_TOKEN_REGISTRATION);
        Mockito.when(responseDTO.getOrderReference()).thenReturn(ORDER_REFERENCE);
        Mockito.when(responseDTO.isError()).thenReturn(false);
        Mockito.when(apiClientMock.registrationInit(Mockito.any())).thenReturn(responseDTO);
    }

    public static void mockRegistrationAuthorize(BPostBankApiClient apiClientMock)
            throws AuthenticationException {
        RegistrationResponseDTO responseDTO = Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(responseDTO.getSessionToken()).thenReturn(SESSION_TOKEN_REGISTRATION);
        Mockito.when(responseDTO.getOrderReference()).thenReturn(ORDER_REFERENCE);
        Mockito.when(responseDTO.isError()).thenReturn(false);
        Mockito.when(
                        apiClientMock.registrationAuthorize(
                                Mockito.any(), Mockito.eq(CHALLENGE_SING_CODE)))
                .thenReturn(responseDTO);
    }

    public static void mockRegistrationAuthorizeWithIncorrectChallengeCode(
            BPostBankApiClient apiClientMock) throws AuthenticationException {
        RegistrationResponseDTO responseDTO = Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(responseDTO.isErrorChallengeResponseIncorrect()).thenReturn(true);
        Mockito.when(
                        apiClientMock.registrationAuthorize(
                                Mockito.any(), Mockito.eq(CHALLENGE_SING_CODE)))
                .thenReturn(responseDTO);
    }

    public static void mockRegistrationExecute(BPostBankApiClient apiClientMock)
            throws AuthenticationException {
        RegistrationResponseDTO responseDTO = Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(responseDTO.getSessionToken()).thenReturn(SESSION_TOKEN_REGISTRATION);
        Mockito.when(responseDTO.getOrderReference()).thenReturn(ORDER_REFERENCE);
        Mockito.when(responseDTO.getDeviceInstallationID()).thenReturn(DEVICE_INSTALLATION_ID);
        Mockito.when(responseDTO.isError()).thenReturn(false);
        Mockito.when(apiClientMock.registrationExecute(Mockito.any())).thenReturn(responseDTO);
    }

    public static void mockLoginPINInit(BPostBankApiClient apiClientMock)
            throws AuthenticationException {
        LoginResponseDTO responseDTO = Mockito.mock(LoginResponseDTO.class);
        Mockito.when(responseDTO.getSessionToken()).thenReturn(SESSION_TOKEN_LOGIN);
        Mockito.when(responseDTO.getSessionId())
                .thenReturn("FSgZmJu7EuccC_Sc-3uUbqMRLllIl_YZTfbUUW5D");
        Mockito.when(apiClientMock.loginPINInit(Mockito.any())).thenReturn(responseDTO);
    }

    public static void mockLoginPINAuth(BPostBankApiClient apiClientMock)
            throws AuthenticationException {
        LoginResponseDTO responseDTO = Mockito.mock(LoginResponseDTO.class);
        Mockito.when(responseDTO.getSessionToken()).thenReturn(SESSION_TOKEN_LOGIN);
        Mockito.when(responseDTO.getSessionId())
                .thenReturn("FSgZmJu7EuccC_Sc-3uUbqMRLllIl_YZTfbUUW5D");
        Mockito.when(apiClientMock.loginPINAuth(Mockito.any())).thenReturn(responseDTO);
    }

    public static void mockLoginPINAuthMobileAccessDeleted(BPostBankApiClient apiClientMock)
            throws AuthenticationException {
        LoginResponseDTO responseDTO = Mockito.mock(LoginResponseDTO.class);
        Mockito.when(responseDTO.isMobileAccessDeletedError()).thenReturn(true);
        Mockito.when(apiClientMock.loginPINAuth(Mockito.any())).thenReturn(responseDTO);
    }
}
