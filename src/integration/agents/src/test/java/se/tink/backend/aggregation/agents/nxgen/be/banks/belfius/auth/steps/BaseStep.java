package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;

@Ignore
public class BaseStep {

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
                createBelfiusDataAccessorFactory()
                        .createBelfiusPersistedDataAccessor(
                                new AgentAuthenticationPersistedData(new HashMap<>()))
                        .storeBelfiusAuthenticationData(build);
        AgentAuthenticationProcessState agentAuthenticationProcessState =
                createBelfiusDataAccessorFactory()
                        .createBelfiusProcessStateAccessor(
                                new AgentAuthenticationProcessState(new HashMap<>()))
                        .storeBelfiusProcessState(build1);
        return new AgentProceedNextStepAuthenticationRequest(
                new AgentAuthenticationProcessStepIdentifier(""),
                agentAuthenticationProcessState,
                agentAuthenticationPersistedData);
    }

    protected AgentUserInteractionAuthenticationProcessRequest
            createAgentUserInteractionAuthenticationProcessRequest(
                    BelfiusProcessState build1,
                    BelfiusAuthenticationData build,
                    AgentFieldValue... agentFieldValues) {
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                createBelfiusDataAccessorFactory()
                        .createBelfiusPersistedDataAccessor(
                                new AgentAuthenticationPersistedData(new HashMap<>()))
                        .storeBelfiusAuthenticationData(build);
        AgentAuthenticationProcessState agentAuthenticationProcessState =
                createBelfiusDataAccessorFactory()
                        .createBelfiusProcessStateAccessor(
                                new AgentAuthenticationProcessState(new HashMap<>()))
                        .storeBelfiusProcessState(build1);
        return new AgentUserInteractionAuthenticationProcessRequest(
                new AgentAuthenticationProcessStepIdentifier(""),
                agentAuthenticationPersistedData,
                agentAuthenticationProcessState,
                Arrays.asList(agentFieldValues));
    }

    protected BelfiusDataAccessorFactory createBelfiusDataAccessorFactory() {
        return new BelfiusDataAccessorFactory(OBJECT_MAPPER_SINGLETON);
    }
}
