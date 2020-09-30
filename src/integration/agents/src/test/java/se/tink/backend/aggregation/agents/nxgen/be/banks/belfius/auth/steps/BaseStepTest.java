package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;

@Ignore
public class BaseStepTest {

    public static final String DEVICE_TOKEN = "deviceToken";
    public static final String SESSION_ID = "sessionId";
    public static final String MACHINE_ID = "machineId";
    public static final String PAN_NUMBER = "panNumber";
    public static final String CONTRACT_NUMBER = "contractNumber";
    public static final String DEVICE_TOKEN_HASHED = "deviceTokenHashed";
    public static final String DEVICE_TOKEN_HASHED_IOS_COMPARISON =
            "deviceTokenHashedIosComparison";
    public static final String PASSWORD = "password";
    public static final String CHALLENGE = "challenge";
    public static final String ENCRYPTED_PASSWORD = "encryptedPassword";
    public static final String CODE = "code";
    private static final ObjectMapper OBJECT_MAPPER_SINGLETON = new ObjectMapper();

    protected AgentProceedNextStepAuthenticationRequest
            createAgentProceedNextStepAuthenticationRequest(
                    BelfiusProcessState build1, BelfiusAuthenticationData build) {
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                createBelfiusPersistedDataAccessorFactory()
                        .createBelfiusPersistedDataAccessor(
                                new AgentAuthenticationPersistedData(new HashMap<>()))
                        .storeBelfiusAuthenticationData(build);
        HashMap<String, Object> values = new HashMap<>();
        values.put(BelfiusProcessState.KEY, build1);
        return new AgentProceedNextStepAuthenticationRequest(
                new AgentAuthenticationProcessStepIdentifier(""),
                new AgentAuthenticationProcessState(values),
                agentAuthenticationPersistedData);
    }

    protected AgentUserInteractionAuthenticationProcessRequest
            createAgentUserInteractionAuthenticationProcessRequest(
                    BelfiusProcessState build1,
                    BelfiusAuthenticationData build,
                    AgentFieldValue... agentFieldValues) {
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                createBelfiusPersistedDataAccessorFactory()
                        .createBelfiusPersistedDataAccessor(
                                new AgentAuthenticationPersistedData(new HashMap<>()))
                        .storeBelfiusAuthenticationData(build);
        HashMap<String, Object> values = new HashMap<>();
        values.put(BelfiusProcessState.KEY, build1);
        return new AgentUserInteractionAuthenticationProcessRequest(
                new AgentAuthenticationProcessStepIdentifier(""),
                agentAuthenticationPersistedData,
                new AgentAuthenticationProcessState(values),
                Arrays.asList(agentFieldValues));
    }

    protected BelfiusPersistedDataAccessorFactory createBelfiusPersistedDataAccessorFactory() {
        return new BelfiusPersistedDataAccessorFactory(OBJECT_MAPPER_SINGLETON);
    }
}
