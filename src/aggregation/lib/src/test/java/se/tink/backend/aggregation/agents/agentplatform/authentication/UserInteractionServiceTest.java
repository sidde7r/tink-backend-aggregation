package se.tink.backend.aggregation.agents.agentplatform.authentication;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.AgentFieldLabel;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.I18NFieldLabel;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UserInteractionServiceTest {

    private CredentialsRequest credentialsRequest;
    private Provider supplementalFieldsProvider;
    private SupplementalInformationController supplementalInformationController;
    private Credentials credentials;

    @Before
    public void init() throws SupplementalInfoException {
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        supplementalFieldsProvider = Mockito.mock(Provider.class);
        Mockito.when(credentialsRequest.getProvider()).thenReturn(supplementalFieldsProvider);
        supplementalInformationController = Mockito.mock(SupplementalInformationController.class);
        credentials = Mockito.mock(Credentials.class);
        Mockito.when(credentialsRequest.getCredentials()).thenReturn(credentials);
    }

    private Field mockField(String fieldName) {
        Field field = Mockito.mock(Field.class);
        Mockito.when(field.getName()).thenReturn(fieldName);
        return field;
    }

    @Test
    public void shouldGetFieldValuesFromCredentials() {
        // given
        final String usernameFieldId = "username";
        final String passwordFieldId = "password";
        AgentFieldDefinition usernameFieldDefinition = mockAgentFieldDefinition(usernameFieldId);
        AgentFieldDefinition passwordFieldDefinition = mockAgentFieldDefinition(passwordFieldId);
        Mockito.when(credentials.getField(usernameFieldId)).thenReturn("testUsername");
        Mockito.when(credentials.getField(passwordFieldId)).thenReturn("testPassword");
        UserInteractionService objectUnderTest =
                new UserInteractionService(supplementalInformationController, credentialsRequest);
        // when
        List<AgentFieldValue> result =
                objectUnderTest.requestForFields(
                        Lists.newArrayList(usernameFieldDefinition, passwordFieldDefinition));
        // then
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.contains(new AgentFieldValue(usernameFieldId, "testUsername")));
        Assert.assertTrue(result.contains(new AgentFieldValue(passwordFieldId, "testPassword")));
        Mockito.verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldRequestUserForFieldAndReturnValue() throws SupplementalInfoException {
        // given
        final String otpCodeFieldId = "otpCode";
        AgentFieldDefinition otpFieldDefinition = mockAgentFieldDefinition(otpCodeFieldId);
        Field otpField = mockField(otpCodeFieldId);
        Mockito.when(supplementalFieldsProvider.getSupplementalFields())
                .thenReturn(Lists.newArrayList(otpField));
        Map<String, String> userResponse = new HashMap<>();
        userResponse.put(otpCodeFieldId, "12345");
        Mockito.when(supplementalInformationController.askSupplementalInformation(otpField))
                .thenReturn(userResponse);
        UserInteractionService objectUnderTest =
                new UserInteractionService(supplementalInformationController, credentialsRequest);
        // when
        List<AgentFieldValue> result =
                objectUnderTest.requestForFields(Lists.newArrayList(otpFieldDefinition));
        // then
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.contains(new AgentFieldValue(otpCodeFieldId, "12345")));
    }

    @Test
    public void shouldRequestUSerForFieldAndReturnEmptyResponse() throws SupplementalInfoException {
        // given
        final String otpCodeFieldId = "otpCode";
        AgentFieldDefinition otpFieldDefinition = mockAgentFieldDefinition(otpCodeFieldId);
        Field otpField = mockField(otpCodeFieldId);
        Mockito.when(supplementalFieldsProvider.getSupplementalFields())
                .thenReturn(Lists.newArrayList(otpField));
        Map<String, String> userResponse = new HashMap<>();
        userResponse.put(otpCodeFieldId, "12345");
        Mockito.when(supplementalInformationController.askSupplementalInformation(otpField))
                .thenThrow(SupplementalInfoError.WAIT_TIMEOUT.exception());
        UserInteractionService objectUnderTest =
                new UserInteractionService(supplementalInformationController, credentialsRequest);
        // when
        List<AgentFieldValue> result =
                objectUnderTest.requestForFields(Lists.newArrayList(otpFieldDefinition));
        // then
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void shouldRequestForRedirectAndReturnCallbackResponse() {
        // given
        final String stateValue = "1234567890";
        final String redirectUrl = "http://wwww.somedomain.com?state=" + stateValue;
        final long waitFor = 9;
        final TimeUnit timeUnit = TimeUnit.MINUTES;
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("key1", "value1");
        Mockito.when(
                        supplementalInformationController.waitForSupplementalInformation(
                                "tpcb_" + stateValue, waitFor, timeUnit))
                .thenReturn(Optional.of(callbackData));
        ArgumentCaptor<ThirdPartyAppAuthenticationPayload>
                thirdPartyAppAuthenticationPayloadArgumentCaptor =
                        ArgumentCaptor.forClass(ThirdPartyAppAuthenticationPayload.class);
        UserInteractionService objectUnderTest =
                new UserInteractionService(supplementalInformationController, credentialsRequest);
        // when
        Optional<Map<String, String>> result =
                objectUnderTest.redirect(redirectUrl, AgentClientInfo.builder().build());
        // then
        Mockito.verify(supplementalInformationController)
                .openThirdPartyApp(thirdPartyAppAuthenticationPayloadArgumentCaptor.capture());
        Assert.assertEquals(
                redirectUrl,
                thirdPartyAppAuthenticationPayloadArgumentCaptor
                        .getValue()
                        .getIos()
                        .getDeepLinkUrl());
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("value1", result.get().get("key1"));
    }

    @Test
    public void shouldAddMissingStateToUrlAndRequestForRedirectAndReturnCallbackResponse() {
        // given
        final String redirectUrl = "http://wwww.somedomain.com";
        final long waitFor = 9;
        final TimeUnit timeUnit = TimeUnit.MINUTES;
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("key1", "value1");
        ArgumentCaptor<String> stateArgumentCapture = ArgumentCaptor.forClass(String.class);
        Mockito.when(
                        supplementalInformationController.waitForSupplementalInformation(
                                Mockito.any(), Mockito.eq(waitFor), Mockito.eq(timeUnit)))
                .thenReturn(Optional.of(callbackData));
        ArgumentCaptor<ThirdPartyAppAuthenticationPayload>
                thirdPartyAppAuthenticationPayloadArgumentCaptor =
                        ArgumentCaptor.forClass(ThirdPartyAppAuthenticationPayload.class);
        UserInteractionService objectUnderTest =
                new UserInteractionService(supplementalInformationController, credentialsRequest);
        // when
        Optional<Map<String, String>> result =
                objectUnderTest.redirect(redirectUrl, AgentClientInfo.builder().build());
        // then
        Mockito.verify(supplementalInformationController)
                .waitForSupplementalInformation(
                        stateArgumentCapture.capture(), Mockito.eq(waitFor), Mockito.eq(timeUnit));
        String stateValue = stateArgumentCapture.getValue().replace("tpcb_", "");
        Mockito.verify(supplementalInformationController)
                .openThirdPartyApp(thirdPartyAppAuthenticationPayloadArgumentCaptor.capture());
        Assert.assertEquals(
                redirectUrl + "?state=" + stateValue,
                thirdPartyAppAuthenticationPayloadArgumentCaptor
                        .getValue()
                        .getIos()
                        .getDeepLinkUrl());
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("value1", result.get().get("key1"));
    }

    @Test
    public void shouldRequestForRedirectAndReturnEmptyResponse() {
        // given
        final String stateValue = "1234567890";
        final String redirectUrl = "http://wwww.somedomain.com?state=" + stateValue;
        final long waitFor = 9;
        final TimeUnit timeUnit = TimeUnit.MINUTES;
        Mockito.when(
                        supplementalInformationController.waitForSupplementalInformation(
                                stateValue, waitFor, timeUnit))
                .thenReturn(Optional.empty());
        UserInteractionService objectUnderTest =
                new UserInteractionService(supplementalInformationController, credentialsRequest);
        // when
        Optional<Map<String, String>> result =
                objectUnderTest.redirect(redirectUrl, AgentClientInfo.builder().build());
        // then
        Assert.assertFalse(result.isPresent());
    }

    private AgentFieldDefinition mockAgentFieldDefinition(String id) {
        AgentFieldDefinition fieldDefinition = Mockito.mock(AgentFieldDefinition.class);
        Mockito.when(fieldDefinition.getFieldIdentifier()).thenReturn(id);
        AgentFieldLabel label = Mockito.mock(I18NFieldLabel.class);
        Mockito.when(fieldDefinition.getFieldLabel()).thenReturn(label);
        return fieldDefinition;
    }
}
