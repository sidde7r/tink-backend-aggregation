package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitSecurityTokenChallengeResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankTokenGeneratorAuthenticationControllerTest {

    private static final String CHALLENGE = "11111111";
    private static final String CHALLENGE_RESPONSE = "12345678";

    private SupplementalInformationHelper supplementalInformationHelperMock;

    private SwedbankTokenGeneratorAuthenticationController
            swedbankTokenGeneratorAuthenticationController;

    @Before
    public void setup() throws AuthenticationException, NoSuchMethodException {
        supplementalInformationHelperMock = createSupplementalInformationHelperMock();

        swedbankTokenGeneratorAuthenticationController =
                new SwedbankTokenGeneratorAuthenticationController(
                        mock(SwedbankDefaultApiClient.class),
                        new SessionStorage(),
                        supplementalInformationHelperMock);
    }

    @Test
    public void assertCorrectFlowForOTPLogin() throws SupplementalInfoException, LoginException {
        InitSecurityTokenChallengeResponse initSecurityTokenChallengeResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTokenGeneratorAuthenticationControllerTestData.SECURITY_TOKEN_OTP,
                        InitSecurityTokenChallengeResponse.class);

        swedbankTokenGeneratorAuthenticationController.getChallengeResponse(
                initSecurityTokenChallengeResponse);

        verify(supplementalInformationHelperMock, times(1)).waitForLoginInput();
    }

    @Test
    public void assertCorrectFlowForChallengeExchangeLogin()
            throws SupplementalInfoException, LoginException {
        InitSecurityTokenChallengeResponse initSecurityTokenChallengeResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTokenGeneratorAuthenticationControllerTestData
                                .SECURITY_TOKEN_CHALLENGE,
                        InitSecurityTokenChallengeResponse.class);

        swedbankTokenGeneratorAuthenticationController.getChallengeResponse(
                initSecurityTokenChallengeResponse);

        verify(supplementalInformationHelperMock, times(1))
                .waitForSignCodeChallengeResponse(CHALLENGE);
    }

    @Test(expected = LoginException.class)
    public void assertLoginExceptionIsThrownForImageChallenge()
            throws LoginException, SupplementalInfoException {
        InitSecurityTokenChallengeResponse initSecurityTokenChallengeResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTokenGeneratorAuthenticationControllerTestData.SECURITY_TOKEN_IMAGE,
                        InitSecurityTokenChallengeResponse.class);

        swedbankTokenGeneratorAuthenticationController.executeChallengeExchangeFlow(
                initSecurityTokenChallengeResponse);
    }

    @Test(expected = IllegalStateException.class)
    public void assertExceptionIsThrownWhenChallengeNoPresent()
            throws LoginException, SupplementalInfoException {
        InitSecurityTokenChallengeResponse initSecurityTokenChallengeResponse =
                SerializationUtils.deserializeFromString(
                        SwedbankTokenGeneratorAuthenticationControllerTestData
                                .SECURITY_TOKEN_NO_CHALLENGE_PRESENT,
                        InitSecurityTokenChallengeResponse.class);

        swedbankTokenGeneratorAuthenticationController.executeChallengeExchangeFlow(
                initSecurityTokenChallengeResponse);
    }

    private static SupplementalInformationHelper createSupplementalInformationHelperMock()
            throws SupplementalInfoException {
        SupplementalInformationHelper supplementalInformationHelperMock =
                mock(SupplementalInformationHelper.class);

        when(supplementalInformationHelperMock.waitForLoginInput()).thenReturn(CHALLENGE_RESPONSE);
        when(supplementalInformationHelperMock.waitForSignCodeChallengeResponse(CHALLENGE))
                .thenReturn(CHALLENGE_RESPONSE);

        return supplementalInformationHelperMock;
    }
}
