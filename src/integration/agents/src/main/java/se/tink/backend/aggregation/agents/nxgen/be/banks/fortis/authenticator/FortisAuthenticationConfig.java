package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.AuthInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.AutoAuthStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.CheckLoginResultStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.ClientCredentialsGetStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.ClientCredentialsSaveStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.EasyPinActivationStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.EasyPinCreateStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.EasyPinProvisionStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.IdentAuthStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps.LegacyAutoAuthStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisRandomTokenGenerator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAbstractMultiStepsAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class FortisAuthenticationConfig {

    private final AgentPlatformFortisApiClient apiClient;
    private final FortisRandomTokenGenerator generatorUtils;
    private final ObjectMapper objectMapper;

    public AgentAuthenticationProcess createAuthProcess() {
        return new AgentAbstractMultiStepsAuthenticationProcess() {
            @Override
            public void registerSteps() {
                addStep(new ClientCredentialsGetStep());
                addStep(new ClientCredentialsSaveStep(getDataAccessorFactory()));
                addStep(new IdentAuthStep(apiClient, getDataAccessorFactory()));
                addStep(new CheckLoginResultStep(apiClient, getDataAccessorFactory()));
                addStep(new EasyPinCreateStep(apiClient, generatorUtils, getDataAccessorFactory()));
                addStep(new EasyPinProvisionStep(apiClient, getDataAccessorFactory()));
                addStep(new EasyPinActivationStep(apiClient, getDataAccessorFactory()));
                // the sign transaction step is not included and apparently not necessary
                addStep(new AutoAuthStep(apiClient, getDataAccessorFactory()));
                addStep(new LegacyAutoAuthStep(apiClient, getDataAccessorFactory()));
            }

            @Override
            public AgentAuthenticationProcessStep getStartStep() {
                return new AuthInitStep(getDataAccessorFactory());
            }
        };
    }

    private FortisDataAccessorFactory getDataAccessorFactory() {
        return new FortisDataAccessorFactory(objectMapper);
    }
}
