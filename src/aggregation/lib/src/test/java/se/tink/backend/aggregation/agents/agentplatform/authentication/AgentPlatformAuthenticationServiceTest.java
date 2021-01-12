package se.tink.backend.aggregation.agents.agentplatform.authentication;

import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AgentPlatformAuthenticationServiceTest {

    private AgentPlatformAuthenticationService objectUnderTest;
    private AgentPlatformAuthenticator agentPlatformAuthenticator;
    private AgentAuthenticationProcess agentAuthenticationProcess;
    private UserInteractionService userInteractionService;
    private PersistentStorage persistentStorage;
    private CredentialsRequest credentialsRequest;
    private Credentials credentials;
    private final String appId = "dummyTestAppId";
    private AgentExtendedClientInfo agentExtendedClientInfo;

    @Before
    public void init() {
        userInteractionService = Mockito.mock(UserInteractionService.class);
        persistentStorage = new PersistentStorage();
        credentials = Mockito.mock(Credentials.class);
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        Mockito.when(credentialsRequest.getCredentials()).thenReturn(credentials);
        Mockito.when(credentialsRequest.getState()).thenReturn(appId);
        objectUnderTest =
                new AgentPlatformAuthenticationService(
                        userInteractionService,
                        new PersistentStorageService(persistentStorage),
                        credentialsRequest);
        agentPlatformAuthenticator = Mockito.mock(AgentPlatformAuthenticator.class);
        agentAuthenticationProcess = Mockito.mock(AgentAuthenticationProcess.class);
        Mockito.when(agentPlatformAuthenticator.getAuthenticationProcess())
                .thenReturn(agentAuthenticationProcess);

        agentExtendedClientInfo =
                AgentExtendedClientInfo.builder()
                        .clientInfo(AgentClientInfo.builder().appId(appId).build())
                        .build();
    }

    @Test
    public void
            shouldTravelThroughAllAuthenticationStepsAndFinishWithAuthenticationSuccessAndStorePersistentData()
                    throws AgentException {
        // given
        AgentAuthenticationProcessStepIdentifier lastStepIdentifier =
                AgentAuthenticationProcessStepIdentifier.of("lastStep");
        AgentUserInteractionDefinitionResult firstStepResult =
                new AgentUserInteractionDefinitionResult(
                                lastStepIdentifier,
                                new AgentAuthenticationPersistedData(new HashMap<>()))
                        .requireField(Mockito.mock(AgentFieldDefinition.class));
        AgentAuthenticationProcessStep firstStep =
                createAuthenticationStep(
                        new AgentStartAuthenticationProcessRequest(
                                new AgentAuthenticationPersistedData(new HashMap<>()),
                                agentExtendedClientInfo),
                        firstStepResult);
        Mockito.when(agentAuthenticationProcess.getStartStep()).thenReturn(firstStep);

        AgentFieldValue usernameFieldValue = Mockito.mock(AgentFieldValue.class);
        List<AgentFieldValue> agentFieldValues = Lists.newArrayList(usernameFieldValue);
        Mockito.when(
                        userInteractionService.requestForFields(
                                firstStepResult.getUserInteractionDefinition().getRequiredFields()))
                .thenReturn(agentFieldValues);

        Map<String, String> persistentData = new HashMap<>();
        persistentData.put("key", "value");
        Instant sessionExpiryDate = Instant.now();
        AgentAuthenticationProcessStep lastStep =
                createAuthenticationStep(
                        new AgentUserInteractionAuthenticationProcessRequest(
                                lastStepIdentifier,
                                new AgentAuthenticationPersistedData(new HashMap<>()),
                                new AgentAuthenticationProcessState(new HashMap<>()),
                                agentFieldValues,
                                agentExtendedClientInfo),
                        new AgentSucceededAuthenticationResult(
                                sessionExpiryDate,
                                new AgentAuthenticationPersistedData(persistentData)));
        Mockito.when(agentAuthenticationProcess.nextStep(Mockito.eq(lastStepIdentifier)))
                .thenReturn(lastStep);

        // when
        objectUnderTest.authenticate(agentPlatformAuthenticator);

        // then
        Assertions.assertThat(persistentStorage.get("key")).isNotNull();
        Mockito.verify(credentials).setSessionExpiryDate(Date.from(sessionExpiryDate));
    }

    @Test
    public void shouldFinishWithAuthenticationFailedAndCleanPersistentStorage() {
        // given
        Map<String, String> persistentData = new HashMap<>();
        String usernameValue = "testusername";
        persistentData.put("username", usernameValue);
        persistentStorage.put("username", usernameValue);
        AgentAuthenticationProcessStep firstStep =
                createAuthenticationStep(
                        new AgentStartAuthenticationProcessRequest(
                                new AgentAuthenticationPersistedData(persistentData),
                                agentExtendedClientInfo),
                        new AgentFailedAuthenticationResult(
                                new ServerError(),
                                new AgentAuthenticationPersistedData(new HashMap<>())));
        Mockito.when(agentAuthenticationProcess.getStartStep()).thenReturn(firstStep);

        // when
        Throwable thrown =
                Assertions.catchThrowable(
                        () -> objectUnderTest.authenticate(agentPlatformAuthenticator));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(AgentPlatformAuthenticationProcessException.class);
        Assertions.assertThat(persistentStorage.isEmpty()).isTrue();
    }

    private AgentAuthenticationProcessStep createAuthenticationStep(
            AgentAuthenticationRequest request, AgentAuthenticationResult result) {
        AgentAuthenticationProcessStep authStep =
                Mockito.mock(AgentAuthenticationProcessStep.class);
        Mockito.when(authStep.execute(Mockito.eq(request))).thenReturn(result);
        return authStep;
    }
}
