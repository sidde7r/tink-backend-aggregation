package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClientResponseMockFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BPostBankAuthenticatorTest {

    private static final String EMAIL = "someone@tink.se";
    private static final String LOGIN = "8765432100";
    private BPostBankApiClient apiClient;
    private CredentialsRequest credentialsRequest;
    private Credentials credentials;

    @Before
    public void init() {
        apiClient = Mockito.mock(BPostBankApiClient.class);
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        initSupplementalFields();
        initCredentials();
    }

    private void initSupplementalFields() {
        Provider provider = Mockito.mock(Provider.class);
        Field signCodeDescriptionField =
                Field.builder()
                        .name(Field.Key.SIGN_CODE_DESCRIPTION.getFieldKey())
                        .description("sign code description field")
                        .build();
        Field signCodeInputField =
                Field.builder()
                        .name(Field.Key.SIGN_CODE_INPUT.getFieldKey())
                        .description("sign code input field")
                        .build();
        Mockito.when(provider.getSupplementalFields())
                .thenReturn(Lists.newArrayList(signCodeDescriptionField, signCodeInputField));
        Mockito.when(credentialsRequest.getProvider()).thenReturn(provider);
    }

    private void initCredentials() {
        credentials = Mockito.mock(Credentials.class);
        Mockito.when(credentials.getField(Field.Key.USERNAME)).thenReturn(LOGIN);
        Mockito.when(credentials.getField(Field.Key.EMAIL)).thenReturn(EMAIL);
    }

    @Test
    public void loginShouldDoAutomaticAuthentication()
            throws AuthenticationException, AuthorizationException {
        // given
        BPostBankAuthContext authContext = Mockito.mock(BPostBankAuthContext.class);
        Mockito.when(authContext.isRegistrationCompleted()).thenReturn(true);
        BPostBankApiClientResponseMockFactory.mockInitSessionAndGetCSRFToken(apiClient);
        BPostBankApiClientResponseMockFactory.mockLoginPINInit(apiClient);
        BPostBankApiClientResponseMockFactory.mockLoginPINAuth(apiClient);
        BPostBankAuthenticator objectUnderTest =
                new BPostBankAuthenticator(apiClient, authContext, credentialsRequest);
        // when
        SteppableAuthenticationResponse response =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(credentials));
        // then
        Assert.assertFalse(response.getStepIdentifier().isPresent());
    }

    @Test
    public void loginShouldDoManualAuthentication()
            throws AuthenticationException, AuthorizationException {
        // given
        BPostBankAuthContext authContext = new BPostBankAuthContext();
        BPostBankApiClientResponseMockFactory.mockInitSessionAndGetCSRFToken(apiClient);
        BPostBankApiClientResponseMockFactory.mockRegistrationInit(apiClient);
        BPostBankApiClientResponseMockFactory.mockRegistrationAuthorize(apiClient);
        BPostBankApiClientResponseMockFactory.mockRegistrationExecute(apiClient);
        BPostBankApiClientResponseMockFactory.mockLoginPINInit(apiClient);
        BPostBankApiClientResponseMockFactory.mockLoginPINAuth(apiClient);
        BPostBankAuthenticator objectUnderTest =
                new BPostBankAuthenticator(apiClient, authContext, credentialsRequest);
        Map<String, String> userInputs = new HashMap<>();
        userInputs.put(
                Field.Key.SIGN_CODE_INPUT.getFieldKey(),
                BPostBankApiClientResponseMockFactory.CHALLENGE_SING_CODE);
        // when
        SteppableAuthenticationResponse response1 =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(credentials));
        SteppableAuthenticationResponse response2 =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.subsequentRequest(
                                response1.getStepIdentifier().get(),
                                new AuthenticationRequest(Mockito.mock(Credentials.class))
                                        .withUserInputs(userInputs)));
        // then
        Mockito.verify(apiClient).initSessionAndGetCSRFToken();
        Assert.assertTrue(response1.getStepIdentifier().isPresent());
        Assert.assertFalse(response2.getStepIdentifier().isPresent());
        Assert.assertEquals(EMAIL, authContext.getEmail());
        Assert.assertEquals(
                BPostBankApiClientResponseMockFactory.ORDER_REFERENCE,
                authContext.getOrderReference());
        Assert.assertTrue(LOGIN.startsWith(authContext.getLogin()));
        Assert.assertEquals(
                BPostBankApiClientResponseMockFactory.DEVICE_INSTALLATION_ID,
                authContext.getDeviceInstallationId());
        Assert.assertEquals(
                BPostBankApiClientResponseMockFactory.SESSION_TOKEN_LOGIN,
                authContext.getSessionToken());
        Assert.assertEquals(
                BPostBankApiClientResponseMockFactory.CRF_TOKEN, authContext.getCsrfToken());
    }

    @Test
    public void loginShouldExecuteManualAuthenticationFlowWhenAutomaticFailed()
            throws AuthenticationException, AuthorizationException {
        // given
        BPostBankAuthContext authContext = new BPostBankAuthContext();
        RegistrationResponseDTO registrationResponseDTO =
                Mockito.mock(RegistrationResponseDTO.class);
        Mockito.when(registrationResponseDTO.getDeviceInstallationID()).thenReturn("1233456767880");
        authContext.completeRegistration(registrationResponseDTO);
        BPostBankApiClientResponseMockFactory.mockLoginPINInit(apiClient);
        BPostBankApiClientResponseMockFactory.mockLoginPINAuthMobileAccessDeleted(apiClient);
        BPostBankApiClientResponseMockFactory.mockRegistrationInit(apiClient);
        BPostBankAuthenticator objectUnderTest =
                new BPostBankAuthenticator(apiClient, authContext, credentialsRequest);
        // when
        SteppableAuthenticationResponse response =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(credentials));
        // then
        Assert.assertTrue(response.getStepIdentifier().isPresent());
        Assert.assertEquals(
                BPostBankSigningAuthenticationStep.STEP_ID, response.getStepIdentifier().get());
    }

    @Test
    public void shouldRepeatSignWhenPreviousChallengeResponseCodeWasIncorrect()
            throws AuthenticationException, AuthorizationException {
        // given
        BPostBankAuthContext authContext = new BPostBankAuthContext();
        BPostBankApiClientResponseMockFactory.mockRegistrationInit(apiClient);
        BPostBankApiClientResponseMockFactory.mockRegistrationAuthorizeWithIncorrectChallengeCode(
                apiClient);
        authContext.initRegistration(apiClient.registrationInit(authContext), credentials);
        BPostBankAuthenticator objectUnderTest =
                new BPostBankAuthenticator(apiClient, authContext, credentialsRequest);
        Map<String, String> userInputs = new HashMap<>();
        userInputs.put(Field.Key.SIGN_CODE_INPUT.getFieldKey(), "12341234");
        // when
        SteppableAuthenticationResponse response =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.subsequentRequest(
                                BPostBankSigningAuthenticationStep.STEP_ID,
                                new AuthenticationRequest(credentials).withUserInputs(userInputs)));
        // then
        Assert.assertTrue(response.getStepIdentifier().isPresent());
        Assert.assertEquals(
                BPostBankSigningAuthenticationStep.STEP_ID, response.getStepIdentifier().get());
    }
}
