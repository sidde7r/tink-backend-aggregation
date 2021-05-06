package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.savecredentials;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition.FetchSeedPositionStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;

public class CredentialsSaveStepTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SECURITY_NUMBER = "12341234";
    private MetroDataAccessorFactory metroDataAccessorFactory;

    @Before
    public void before() {
        ObjectMapperFactory factory = new ObjectMapperFactory();
        metroDataAccessorFactory = new MetroDataAccessorFactory(factory.getInstance());
    }

    @Test
    public void shouldSaveUsersCredentialsAndSetNextStepAsFetchSeedPositionStep() {
        // given
        AgentAuthenticationPersistedData persistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());
        List<AgentFieldValue> filledUpAgentFields =
                Arrays.asList(
                        new AgentFieldValue(Key.USERNAME.getFieldKey(), USERNAME),
                        new AgentFieldValue(Key.PASSWORD.getFieldKey(), PASSWORD),
                        new AgentFieldValue(Key.SECURITY_NUMBER.getFieldKey(), SECURITY_NUMBER));
        AgentAuthenticationRequest request =
                new AgentUserInteractionAuthenticationProcessRequest(
                        null, persistedData, null, filledUpAgentFields, null);
        CredentialsSaveStep credentialsGetStep = new CredentialsSaveStep(metroDataAccessorFactory);

        // when
        AgentAuthenticationResult execute = credentialsGetStep.execute(request);

        // then
        assertThat(execute).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        assertThat(execute.getAuthenticationProcessStepIdentifier().get().getValue())
                .isEqualTo(FetchSeedPositionStep.class.getSimpleName());
        MetroAuthenticationData authenticationData =
                metroDataAccessorFactory
                        .createPersistedDataAccessor(execute.getAuthenticationPersistedData())
                        .getAuthenticationData();
        assertThat(authenticationData.getPassword()).isEqualTo(PASSWORD);
        assertThat(authenticationData.getSecuredNumber()).isEqualTo(SECURITY_NUMBER);
        assertThat(authenticationData.getUserId()).isEqualTo(USERNAME);
    }
}
