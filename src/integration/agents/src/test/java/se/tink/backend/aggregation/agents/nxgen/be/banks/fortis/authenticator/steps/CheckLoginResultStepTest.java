package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.PhonenumberInputField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.CheckLoginResultResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.UserInfoResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;

@RunWith(MockitoJUnitRunner.class)
public class CheckLoginResultStepTest extends AbstractStepTest {

    @Mock private AgentPlatformFortisApiClient fortisApiClient;

    private CheckLoginResultStep step;

    @Before
    public void setup() {
        step =
                new CheckLoginResultStep(
                        fortisApiClient, new FortisDataAccessorFactory(objectMapper));
    }

    @Test
    public void shouldCheckLoginAndRequestPhoneNumber() throws Exception {
        // given
        FortisAuthData fortisAuthData = new FortisAuthData();
        fortisAuthData.setUsername("67030416123412340");
        fortisAuthData.setClientNumber("1234012340");

        when(fortisApiClient.checkLoginResult(any(), any()))
                .thenReturn(new CheckLoginResultResponse());

        when(fortisApiClient.getUserInfo()).thenReturn(new UserInfoResponse());

        AgentUserInteractionAuthenticationProcessRequest request =
                new AgentUserInteractionAuthenticationProcessRequest(
                        AgentAuthenticationProcessStepIdentifier.of("doesnt-matter"),
                        AgentAuthenticationPersistedData.of(
                                "FortisAuthData", objectMapper.writeValueAsString(fortisAuthData)),
                        AgentAuthenticationProcessState.of("FortisProcessState", "{}"),
                        Collections.singletonList(
                                new AgentFieldValue(
                                        CardReaderLoginInputAgentField.id(), "55556666")));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, EasyPinCreateStep.class);

        assertFieldRequested(result, PhonenumberInputField.ID);

        verify(fortisApiClient).checkLoginResult(eq("1234012340"), eq("55556666"));
        verify(fortisApiClient).doEbewAppLogin(eq("1234012340"), eq("08"));
        verify(fortisApiClient).getUserInfo();
        verify(fortisApiClient).getCountryList();
    }
}
