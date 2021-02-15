package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarTestUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

public class AutoAuthenticationStepTest {

    private static final String DEVICE_ID = "some test id";
    private static final String LUNAR_PASSWORD = "1234";
    private static final String ACCESS_TOKEN = "test_token";
    private static final String USER_ID = "1234567890123";

    private AutoAuthenticationStep autoAuthenticationStep;
    private AgentProceedNextStepAuthenticationRequest request;

    @Before
    public void setup() {
        LunarDataAccessorFactory dataAccessorFactory =
                new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        autoAuthenticationStep = new AutoAuthenticationStep(dataAccessorFactory);

        LunarProcessState processState = new LunarProcessState();

        LunarAuthData initialData = getAuthData();

        LunarProcessStateAccessor stateAccessor =
                LunarTestUtils.getProcessStateAccessor(dataAccessorFactory, processState);
        LunarAuthDataAccessor authDataAccessor =
                LunarTestUtils.getAuthDataAccessor(dataAccessorFactory, initialData);

        request =
                LunarTestUtils.getProceedNextStepAuthRequest(
                        stateAccessor, authDataAccessor, processState, initialData);
    }

    @Test
    public void shouldSetAutoAuthToTrue() {
        // given
        LunarAuthData expectedData = getAuthData();

        // and
        LunarProcessState expectedState = new LunarProcessState();
        expectedState.setAutoAuth(true);

        // when
        AgentAuthenticationResult result = autoAuthenticationStep.execute(request);

        // then
        assertThat(result)
                .isEqualTo(
                        new AgentProceedNextStepAuthenticationResult(
                                AgentAuthenticationProcessStep.identifier(SignInToLunarStep.class),
                                LunarTestUtils.toProcessState(expectedState),
                                LunarTestUtils.toPersistedData(expectedData)));
    }

    private LunarAuthData getAuthData() {
        LunarAuthData authData = new LunarAuthData();
        authData.setUserId(USER_ID);
        authData.setLunarPassword(LUNAR_PASSWORD);
        authData.setAccessToken(ACCESS_TOKEN);
        authData.setDeviceId(DEVICE_ID);
        return authData;
    }
}
