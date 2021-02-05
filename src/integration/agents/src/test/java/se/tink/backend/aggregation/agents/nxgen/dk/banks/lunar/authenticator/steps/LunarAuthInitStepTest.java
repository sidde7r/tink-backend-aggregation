package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RunWith(JUnitParamsRunner.class)
public class LunarAuthInitStepTest {

    private static final String USER_ID = "Tester";
    private static final String DEVICE_ID = "some test id";
    private static final String LUNAR_PASSWORD = "1234";
    private static final String ACCESS_TOKEN = "test_token";

    private LunarAuthInitStep lunarAuthInitStep;
    private AgentStartAuthenticationProcessRequest request;
    private LunarDataAccessorFactory dataAccessorFactory;

    @Before
    public void setup() {
        dataAccessorFactory = new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        lunarAuthInitStep = new LunarAuthInitStep(dataAccessorFactory);
    }

    @Test
    @Parameters(method = "requestParams")
    public void shouldReturnManualAuthenticationStep(
            String userId, String lunarPassword, String token, String deviceId) {
        // given
        LunarAuthData initialData = getAuthData(userId, lunarPassword, token, deviceId);

        LunarAuthDataAccessor authDataAccessor =
                StepsUtils.getAuthDataAccessor(dataAccessorFactory, initialData);

        request = StepsUtils.getStartAuthProcessRequest(authDataAccessor, initialData);

        // and
        LunarAuthData expectedData = getAuthData(userId, lunarPassword, token, deviceId);

        // when
        AgentAuthenticationResult result = lunarAuthInitStep.execute(request);

        // then
        assertThat(result)
                .isEqualTo(
                        new AgentProceedNextStepAuthenticationResult(
                                AgentAuthenticationProcessStep.identifier(
                                        GetUserCredentialsStep.class),
                                StepsUtils.getExpectedPersistedData(expectedData)));
    }

    private LunarAuthData getAuthData(
            String userId, String lunarPassword, String token, String deviceId) {
        LunarAuthData authData = new LunarAuthData();
        authData.setUserId(userId);
        authData.setLunarPassword(lunarPassword);
        authData.setAccessToken(token);
        authData.setDeviceId(deviceId);
        return authData;
    }

    private Object[] requestParams() {
        return new Object[] {
            new Object[] {null, null, null, null},
            new Object[] {USER_ID, LUNAR_PASSWORD, ACCESS_TOKEN, null},
            new Object[] {USER_ID, LUNAR_PASSWORD, null, DEVICE_ID},
            new Object[] {USER_ID, null, ACCESS_TOKEN, DEVICE_ID},
            new Object[] {null, LUNAR_PASSWORD, ACCESS_TOKEN, DEVICE_ID},
            new Object[] {null, null, ACCESS_TOKEN, DEVICE_ID},
            new Object[] {USER_ID, LUNAR_PASSWORD, null, null},
            new Object[] {USER_ID, null, null, DEVICE_ID},
            new Object[] {null, LUNAR_PASSWORD, ACCESS_TOKEN, null},
            new Object[] {USER_ID, null, ACCESS_TOKEN, null},
            new Object[] {null, LUNAR_PASSWORD, null, DEVICE_ID},
            new Object[] {USER_ID, null, null, null},
            new Object[] {null, LUNAR_PASSWORD, null, null},
            new Object[] {null, null, ACCESS_TOKEN, null},
            new Object[] {null, null, null, DEVICE_ID},
        };
    }

    @Test
    public void shouldReturnAutoAuthenticationStepWhenUserHasCredentials() {
        // given
        LunarAuthData initialData = getAuthData(USER_ID, LUNAR_PASSWORD, ACCESS_TOKEN, DEVICE_ID);

        LunarAuthDataAccessor authDataAccessor =
                StepsUtils.getAuthDataAccessor(dataAccessorFactory, initialData);

        request = StepsUtils.getStartAuthProcessRequest(authDataAccessor, initialData);

        // and
        LunarAuthData expectedData = getAuthData(USER_ID, LUNAR_PASSWORD, ACCESS_TOKEN, DEVICE_ID);

        // when
        AgentAuthenticationResult result = lunarAuthInitStep.execute(request);

        // then
        assertThat(result)
                .isEqualTo(
                        new AgentProceedNextStepAuthenticationResult(
                                AgentAuthenticationProcessStep.identifier(
                                        AutoAuthenticationStep.class),
                                StepsUtils.getExpectedPersistedData(expectedData)));
    }
}
