package se.tink.backend.aggregation.agents.agentplatform.authentication;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentAuthenticationException;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.framework.error.ServerError;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AgentPlatformAuthenticationServiceTest {

    private AgentPlatformAuthenticationService objectUnderTest;
    private AgentPlatformAuthenticator agentPlatformAuthenticator;
    private AgentAuthenticationProcess agentAuthenticationProcess;
    private UserInteractionService userInteractionService;
    private PersistentStorage persistentStorage;

    @Before
    public void init() {
        userInteractionService = Mockito.mock(UserInteractionService.class);
        persistentStorage = new PersistentStorage();
        objectUnderTest =
                new AgentPlatformAuthenticationService(
                        userInteractionService, new PersistentStorageService(persistentStorage));
        agentPlatformAuthenticator = Mockito.mock(AgentPlatformAuthenticator.class);
        agentAuthenticationProcess = Mockito.mock(AgentAuthenticationProcess.class);
        Mockito.when(agentPlatformAuthenticator.getAuthenticationProcess())
                .thenReturn(agentAuthenticationProcess);
    }

    @Test
    public void
            shouldTravelThroughAllAuthenticationStepsAndFinishWithAuthenticationSuccessAndStorePersistentData()
                    throws AgentException {
        // given
        AgentAuthenticationProcessStepIdentifier lastStepIdentifier =
                AgentAuthenticationProcessStepIdentifier.of("lastStep");
        AgentAuthenticationProcessStep firstStep =
                Mockito.mock(AgentAuthenticationProcessStep.class);
        Mockito.when(agentAuthenticationProcess.getStartStep()).thenReturn(firstStep);
        AgentUserInteractionDefinitionResult firstStepResult =
                new AgentUserInteractionDefinitionResult(
                        lastStepIdentifier, new AgentAuthenticationPersistedData(new HashMap<>()));
        AgentFieldDefinition usernameFieldDefinition = Mockito.mock(AgentFieldDefinition.class);
        firstStepResult.requireField(usernameFieldDefinition);
        Mockito.when(
                        firstStep.execute(
                                Mockito.eq(
                                        new AgentStartAuthenticationProcessRequest(
                                                new AgentAuthenticationPersistedData(
                                                        new HashMap<>())))))
                .thenReturn(firstStepResult);
        AgentFieldValue usernameFieldValue = Mockito.mock(AgentFieldValue.class);
        List<AgentFieldValue> agentFieldValues = Lists.newArrayList(usernameFieldValue);
        Mockito.when(
                        userInteractionService.requestForFields(
                                firstStepResult.getUserInteractionDefinition().getRequiredFields()))
                .thenReturn(agentFieldValues);

        AgentAuthenticationProcessStep lastStep =
                Mockito.mock(AgentAuthenticationProcessStep.class);
        Map<String, String> persistentData = new HashMap<>();
        persistentData.put("key", "value");
        AgentSucceededAuthenticationResult finalResult =
                new AgentSucceededAuthenticationResult(
                        new AgentAuthenticationPersistedData(persistentData));
        Mockito.when(
                        lastStep.execute(
                                Mockito.eq(
                                        new AgentUserInteractionAuthenticationProcessRequest(
                                                lastStepIdentifier,
                                                new AgentAuthenticationPersistedData(
                                                        new HashMap<>()),
                                                new AgentAuthenticationProcessState(
                                                        new HashMap<>()),
                                                agentFieldValues))))
                .thenReturn(finalResult);
        Mockito.when(agentAuthenticationProcess.nextStep(Mockito.eq(lastStepIdentifier)))
                .thenReturn(lastStep);

        // when
        objectUnderTest.authenticate(agentPlatformAuthenticator);

        // then
        Assertions.assertThat(persistentStorage.get("key")).isNotNull();
    }

    @Test
    public void shouldFinishWithAuthenticationFailedAndCleanPersistentStorage() {
        // given
        Map<String, String> persistentData = new HashMap<>();
        String usernameValue = "testusername";
        persistentData.put("username", usernameValue);
        persistentStorage.put("username", usernameValue);
        AgentAuthenticationProcessStep firstStep =
                Mockito.mock(AgentAuthenticationProcessStep.class);
        Mockito.when(agentAuthenticationProcess.getStartStep()).thenReturn(firstStep);
        AgentFailedAuthenticationResult failedAuthenticationResult =
                new AgentFailedAuthenticationResult(
                        new ServerError(), new AgentAuthenticationPersistedData(new HashMap<>()));
        AgentStartAuthenticationProcessRequest startAuthenticationProcessRequest =
                new AgentStartAuthenticationProcessRequest(
                        new AgentAuthenticationPersistedData(persistentData));
        Mockito.when(firstStep.execute(Mockito.eq(startAuthenticationProcessRequest)))
                .thenReturn(failedAuthenticationResult);

        // when
        Throwable thrown =
                Assertions.catchThrowable(
                        () -> objectUnderTest.authenticate(agentPlatformAuthenticator));

        // then
        Assertions.assertThat(thrown).isInstanceOf(AgentAuthenticationException.class);
        Assertions.assertThat(persistentStorage.isEmpty()).isTrue();
    }
}
