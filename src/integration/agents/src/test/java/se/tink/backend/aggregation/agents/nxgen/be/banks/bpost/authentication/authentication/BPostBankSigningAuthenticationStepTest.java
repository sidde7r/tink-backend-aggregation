package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class BPostBankSigningAuthenticationStepTest {

    private SupplementalInformationFormer supplementalInformationFormer;
    private BPostBankApiClient apiClient;
    private BPostBankAuthContext authContext;
    private Field signCodeDescriptionField;
    private Field signCodeInputField;
    private BPostBankSigningAuthenticationStep objectUnderTest;

    @Before
    public void init() {
        supplementalInformationFormer = Mockito.mock(SupplementalInformationFormer.class);
        apiClient = Mockito.mock(BPostBankApiClient.class);
        authContext = Mockito.mock(BPostBankAuthContext.class);
        initFields();
        objectUnderTest =
                new BPostBankSigningAuthenticationStep(
                        supplementalInformationFormer, apiClient, authContext);
    }

    private void initFields() {
        signCodeInputField = Mockito.mock(Field.class);
        signCodeDescriptionField =
                Field.builder()
                        .name(Field.Key.SIGN_CODE_DESCRIPTION.getFieldKey())
                        .description("description")
                        .build();
        Mockito.when(signCodeInputField.getName())
                .thenReturn(Field.Key.SIGN_CODE_INPUT.getFieldKey());
        Mockito.when(signCodeInputField.getDescription()).thenReturn("input description ");
        Mockito.when(supplementalInformationFormer.getField(Field.Key.SIGN_CODE_DESCRIPTION))
                .thenReturn(signCodeDescriptionField);
        Mockito.when(supplementalInformationFormer.getField(Field.Key.SIGN_CODE_INPUT))
                .thenReturn(signCodeInputField);
    }

    @Test
    public void shouldInitRegistrationAndRequestForSignCode()
            throws AuthenticationException, AuthorizationException {
        // given
        RegistrationResponseDTO responseDTO = Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(authContext.isRegistrationInitialized()).thenReturn(false);
        Mockito.when(authContext.getChallengeCode()).thenReturn("12341234");
        Mockito.when(apiClient.registrationInit(authContext)).thenReturn(responseDTO);
        Credentials credentials = Mockito.mock(Credentials.class);
        AuthenticationRequest request = new AuthenticationRequest(credentials);
        // when
        AuthenticationStepResponse response = objectUnderTest.execute(request);
        // then
        Assert.assertTrue(response.getSupplementInformationRequester().isPresent());
        Assert.assertEquals(
                2, response.getSupplementInformationRequester().get().getFields().get().size());
        Assert.assertEquals("1234 1234", signCodeDescriptionField.getValue());
    }

    @Test
    public void shouldCallSignRequestAndFinishItself()
            throws AuthenticationException, AuthorizationException {
        // given
        final String signCode = "12345678";
        RegistrationResponseDTO responseDTO = Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(authContext.isRegistrationInitialized()).thenReturn(true);
        Mockito.when(apiClient.registrationAuthorize(authContext, signCode))
                .thenReturn(responseDTO);
        Credentials credentials = Mockito.mock(Credentials.class);
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put(Field.Key.SIGN_CODE_INPUT.getFieldKey(), signCode);
        AuthenticationRequest request =
                new AuthenticationRequest(credentials).withUserInputs(callbackData);
        // when
        AuthenticationStepResponse response = objectUnderTest.execute(request);
        // then
        Assert.assertFalse(response.isAuthenticationFinished());
        Assert.assertFalse(response.getSupplementInformationRequester().isPresent());
        Assert.assertFalse(response.getNextStepId().isPresent());
        Mockito.verify(apiClient).registrationAuthorize(authContext, signCode);
    }

    @Test
    public void shouldRepeatRequestForSignCodeWhenPreviousWasIncorrect()
            throws AuthenticationException, AuthorizationException {
        // given
        final String signCode = "12345678";
        RegistrationResponseDTO responseDTO = Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(responseDTO.isErrorChallengeResponseIncorrect()).thenReturn(true);
        Mockito.when(apiClient.registrationAuthorize(authContext, signCode))
                .thenReturn(responseDTO);
        Mockito.when(authContext.isRegistrationInitialized()).thenReturn(true);
        Credentials credentials = Mockito.mock(Credentials.class);
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put(Field.Key.SIGN_CODE_INPUT.getFieldKey(), signCode);
        AuthenticationRequest request =
                new AuthenticationRequest(credentials).withUserInputs(callbackData);
        // when
        AuthenticationStepResponse response = objectUnderTest.execute(request);
        // then
        Assert.assertTrue(response.getNextStepId().isPresent());
        Assert.assertEquals(
                BPostBankSigningAuthenticationStep.STEP_ID, response.getNextStepId().get());
    }

    @Test(expected = AuthorizationException.class)
    public void shouldThrowAuthorizationExceptionWhenAccountIsBlocked()
            throws AuthenticationException, AuthorizationException {
        // given
        final String signCode = "12345678";
        RegistrationResponseDTO responseDTO = Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(responseDTO.isErrorAccountBlocked()).thenReturn(true);
        Mockito.when(apiClient.registrationAuthorize(authContext, signCode))
                .thenReturn(responseDTO);
        Mockito.when(authContext.isRegistrationInitialized()).thenReturn(true);
        Credentials credentials = Mockito.mock(Credentials.class);
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put(Field.Key.SIGN_CODE_INPUT.getFieldKey(), signCode);
        AuthenticationRequest request =
                new AuthenticationRequest(credentials).withUserInputs(callbackData);
        // when
        objectUnderTest.execute(request);
    }
}
