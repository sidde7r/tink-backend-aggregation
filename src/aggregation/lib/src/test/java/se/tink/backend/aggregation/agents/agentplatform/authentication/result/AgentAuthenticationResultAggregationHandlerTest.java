package se.tink.backend.aggregation.agents.agentplatform.authentication.result;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.agentplatform.authentication.UserInteractionService;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentAuthenticationError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentRemoteInteractionAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentRedirectAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentUsernameFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.framework.error.InvalidCredentialsError;

public class AgentAuthenticationResultAggregationHandlerTest {

    private UserInteractionService userInteractionService;
    private PersistentStorageService persistentStorageService;
    private AgentAuthenticationResultAggregationHandler objectUnderTest;

    @Before
    public void init() {
        userInteractionService = Mockito.mock(UserInteractionService.class);
        persistentStorageService = Mockito.mock(PersistentStorageService.class);
        objectUnderTest =
                new AgentAuthenticationResultAggregationHandler(
                        userInteractionService, persistentStorageService);
    }

    @Test
    public void
            forSuccessAuthenticationShouldStoreAuthenticationPersistentDataAndReturnEmptyHandlingResult() {
        // given
        Map<String, String> data = new HashMap<>();
        data.put("accessToken", "tokenBody");
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(data);
        AgentSucceededAuthenticationResult succeededAuthenticationResult =
                new AgentSucceededAuthenticationResult(agentAuthenticationPersistedData);
        // when
        AgentAuthenticationResultHandlingResult result =
                objectUnderTest.handle(succeededAuthenticationResult);
        // then
        Mockito.verify(persistentStorageService)
                .writeToAgentPersistentStorage(agentAuthenticationPersistedData);
        Assert.assertFalse(result.getAuthenticationError().isPresent());
        Assert.assertFalse(result.getAgentAuthenticationNextRequest().isPresent());
    }

    @Test
    public void
            forSuccessAuthenticationShouldStoreAuthenticationPersistentDataAndReturnErrorHandlingResult() {
        // given
        Map<String, String> data = new HashMap<>();
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(data);
        AgentFailedAuthenticationResult failedAuthenticationResult =
                new AgentFailedAuthenticationResult(
                        new InvalidCredentialsError(), agentAuthenticationPersistedData);
        // when
        AgentAuthenticationResultHandlingResult result =
                objectUnderTest.handle(failedAuthenticationResult);
        // then
        Mockito.verify(persistentStorageService)
                .writeToAgentPersistentStorage(agentAuthenticationPersistedData);
        Assert.assertTrue(result.getAuthenticationError().isPresent());
        Assert.assertEquals(
                new AgentAuthenticationError(new InvalidCredentialsError()),
                result.getAuthenticationError().get());
        Assert.assertFalse(result.getAgentAuthenticationNextRequest().isPresent());
    }

    @Test
    public void forUserInteractionAuthenticationResultShouldRequestUserAndPassData() {
        // given
        AgentAuthenticationProcessState agentAuthenticationProcessState =
                Mockito.mock(AgentAuthenticationProcessState.class);
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                Mockito.mock(AgentAuthenticationPersistedData.class);
        AgentAuthenticationProcessStepIdentifier agentAuthenticationProcessStepIdentifier =
                Mockito.mock(AgentAuthenticationProcessStepIdentifier.class);
        AgentUserInteractionDefinitionResult authenticationResult =
                new AgentUserInteractionDefinitionResult(
                        agentAuthenticationProcessStepIdentifier,
                        agentAuthenticationPersistedData,
                        agentAuthenticationProcessState);
        authenticationResult.requireField(AgentUsernameFieldDefinition.of());
        AgentFieldValue usernameValue =
                new AgentFieldValue(AgentUsernameFieldDefinition.id(), "XXX");
        Mockito.when(
                        userInteractionService.requestForFields(
                                authenticationResult
                                        .getUserInteractionDefinition()
                                        .getRequiredFields()))
                .thenReturn(Lists.newArrayList(usernameValue));
        // when
        AgentAuthenticationResultHandlingResult result =
                objectUnderTest.handle(authenticationResult);
        // then
        Assertions.assertThat(result.getAgentAuthenticationNextRequest().get())
                .isExactlyInstanceOf(AgentUserInteractionAuthenticationProcessRequest.class);
        AgentUserInteractionAuthenticationProcessRequest userInteractionRequest =
                (AgentUserInteractionAuthenticationProcessRequest)
                        result.getAgentAuthenticationNextRequest().get();
        Assertions.assertThat(userInteractionRequest.getAuthenticationProcessState())
                .isEqualTo(agentAuthenticationProcessState);
        Assertions.assertThat(userInteractionRequest.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(agentAuthenticationProcessStepIdentifier);
        Assertions.assertThat(
                        userInteractionRequest
                                .getUserInteractionData()
                                .getFieldValue(AgentUsernameFieldDefinition.id()))
                .isEqualTo("XXX");
    }

    @Test
    public void forRedirectAuthenticationResultShouldMakeARedirectAndPassCallbackData() {
        // given
        final String redirectUrl = "http://somedomain.com";
        AgentAuthenticationProcessStepIdentifier agentAuthenticationProcessStepIdentifier =
                Mockito.mock(AgentAuthenticationProcessStepIdentifier.class);
        AgentRedirectAuthenticationResult authenticationResult =
                new AgentRedirectAuthenticationResult(
                        redirectUrl,
                        agentAuthenticationProcessStepIdentifier,
                        Mockito.mock(AgentAuthenticationPersistedData.class),
                        Mockito.mock(AgentAuthenticationProcessState.class));
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("key1", "value1");
        Mockito.when(userInteractionService.redirect(redirectUrl))
                .thenReturn(Optional.of(callbackData));
        // when
        AgentAuthenticationResultHandlingResult result =
                objectUnderTest.handle(authenticationResult);
        // then
        Assertions.assertThat(result.getAgentAuthenticationNextRequest().isPresent()).isTrue();
        Assertions.assertThat(result.getAgentAuthenticationNextRequest().get())
                .isExactlyInstanceOf(AgentRemoteInteractionAuthenticationRequest.class);
        AgentRemoteInteractionAuthenticationRequest remoteInteractionRequest =
                (AgentRemoteInteractionAuthenticationRequest)
                        result.getAgentAuthenticationNextRequest().get();
        Assertions.assertThat(
                        remoteInteractionRequest.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(agentAuthenticationProcessStepIdentifier);
        Assertions.assertThat(remoteInteractionRequest.getRemoteInteractionData().getValue("key1"))
                .isEqualTo("value1");
    }
}
