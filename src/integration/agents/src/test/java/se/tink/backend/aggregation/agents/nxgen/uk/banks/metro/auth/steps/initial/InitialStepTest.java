package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.initial;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Security;
import java.util.HashMap;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.autoauthentication.AutoAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.getcredentials.CredentialsGetStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;

public class InitialStepTest {

    private InitialStep initialStep;

    private MetroDataAccessorFactory metroDataAccessorFactory;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void before() {
        ObjectMapperFactory factory = new ObjectMapperFactory();
        metroDataAccessorFactory = new MetroDataAccessorFactory(factory.getInstance());
        this.initialStep = new InitialStep(metroDataAccessorFactory);
    }

    @Test
    public void shouldCreateNewAuthDataAndSetNextStepAsCredentialsGetStep() {
        // given
        AgentAuthenticationPersistedData persistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());
        AgentAuthenticationRequest request =
                new AgentStartAuthenticationProcessRequest(persistedData, null);

        // when
        AgentAuthenticationResult execute = initialStep.execute(request);

        // then
        assertThat(execute).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        assertThat(execute.getAuthenticationProcessStepIdentifier().get().getValue())
                .isEqualTo(CredentialsGetStep.class.getSimpleName());
    }

    @Test
    public void shouldInitializeAuthDataAndSetNextStepAsAutoAuthenticationStep() {
        // given
        AgentAuthenticationPersistedData persistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());
        AgentAuthenticationRequest request =
                new AgentStartAuthenticationProcessRequest(persistedData, null);
        MetroPersistedDataAccessor persistedDataAccessor =
                metroDataAccessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        persistedDataAccessor.storeAuthenticationData(
                new MetroAuthenticationData().setAlreadyRegistered(true));

        // when
        AgentAuthenticationResult execute = initialStep.execute(request);

        // then
        assertThat(execute).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        assertThat(execute.getAuthenticationProcessStepIdentifier().get().getValue())
                .isEqualTo(AutoAuthenticationStep.class.getSimpleName());
    }
}
