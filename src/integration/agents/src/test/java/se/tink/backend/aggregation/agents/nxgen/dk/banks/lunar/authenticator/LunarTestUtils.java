package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class LunarTestUtils {

    public static AgentProceedNextStepAuthenticationRequest getProceedNextStepAuthRequest(
            LunarProcessStateAccessor stateAccessor,
            LunarAuthDataAccessor authDataAccessor,
            LunarProcessState processState,
            LunarAuthData authData) {

        return new AgentProceedNextStepAuthenticationRequest(
                AgentAuthenticationProcessStepIdentifier.of("test"),
                stateAccessor.storeState(processState),
                authDataAccessor.storeData(authData),
                AgentExtendedClientInfo.builder().build());
    }

    public static AgentStartAuthenticationProcessRequest getStartAuthProcessRequest(
            LunarAuthDataAccessor authDataAccessor, LunarAuthData authData) {

        return new AgentStartAuthenticationProcessRequest(
                authDataAccessor.storeData(authData), AgentExtendedClientInfo.builder().build());
    }

    public static LunarProcessStateAccessor getProcessStateAccessor(
            LunarDataAccessorFactory dataAccessorFactory, LunarProcessState processState) {
        return dataAccessorFactory.createProcessStateAccessor(
                new AgentAuthenticationProcessState(
                        new HashMap<>(
                                Collections.singletonMap(
                                        LunarConstants.Storage.PROCESS_STATE_KEY,
                                        SerializationUtils.serializeToString(processState)))));
    }

    public static LunarAuthDataAccessor getAuthDataAccessor(
            LunarDataAccessorFactory dataAccessorFactory, LunarAuthData authData) {

        return dataAccessorFactory.createAuthDataAccessor(
                new AgentAuthenticationPersistedData(
                        new HashMap<>(
                                Collections.singletonMap(
                                        LunarConstants.Storage.PERSISTED_DATA_KEY,
                                        SerializationUtils.serializeToString(authData)))));
    }

    public static void assertFailedResultEquals(
            AgentFailedAuthenticationResult expected, AgentFailedAuthenticationResult result) {
        assertThat(result.getError().getClass()).isEqualTo(expected.getError().getClass());
        assertThat(result.getError().getDetails())
                .isEqualToIgnoringGivenFields(expected.getError().getDetails(), "uniqueId");
        assertThat(result.getAuthenticationPersistedData())
                .isEqualToComparingFieldByFieldRecursively(
                        expected.getAuthenticationPersistedData());
    }

    public static AgentAuthenticationPersistedData toPersistedData(LunarAuthData expectedData) {
        return AgentAuthenticationPersistedData.of(
                LunarConstants.Storage.PERSISTED_DATA_KEY,
                SerializationUtils.serializeToString(expectedData));
    }

    public static AgentAuthenticationProcessState toProcessState(LunarProcessState expectedState) {
        return AgentAuthenticationProcessState.of(
                LunarConstants.Storage.PROCESS_STATE_KEY,
                SerializationUtils.serializeToString(expectedState));
    }

    public static NemIdParamsResponse getExpectedNemIdParamsResponse() {
        NemIdParamsResponse expected = new NemIdParamsResponse();
        expected.setClientflow("OCESLOGIN2");
        expected.setClientmode("LIMITED");
        expected.setDigestSignature("digestSignature");
        expected.setEnableAwaitingAppApprovalEvent("TRUE");
        expected.setParamsDigest("paramsDigest");
        expected.setSignProperties("challenge=1234567890123");
        expected.setSpCert("CoolCertificate");
        expected.setTimestamp("MjAyMS0wMS0xOSAxNDo0NDowMCswMDAw");
        return expected;
    }
}
