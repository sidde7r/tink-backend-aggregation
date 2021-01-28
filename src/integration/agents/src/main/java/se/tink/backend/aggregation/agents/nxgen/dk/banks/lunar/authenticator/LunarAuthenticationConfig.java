package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.AgentPlatformLunarApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.AutoAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.GetLunarAccessTokenStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.GetNemIdTokenStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.GetUserCredentialsStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.LunarAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.SaveUserCredentialsStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.SignInToLunarStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAbstractMultiStepsAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

@RequiredArgsConstructor
public class LunarAuthenticationConfig {

    private final AgentPlatformLunarApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final RandomValueGenerator randomValueGenerator;
    private final NemIdIframeControllerAttributes nemIdIframeControllerAttributes;

    public AgentAuthenticationProcess createAuthProcess() {
        return new AgentAbstractMultiStepsAuthenticationProcess() {
            @Override
            public void registerSteps() {
                addStep(new GetUserCredentialsStep());
                addStep(new SaveUserCredentialsStep(getDataAccessorFactory()));
                addStep(
                        new GetNemIdTokenStep(
                                getDataAccessorFactory(),
                                apiClient,
                                nemIdIframeControllerAttributes,
                                randomValueGenerator));
                addStep(new GetLunarAccessTokenStep(getDataAccessorFactory(), apiClient));
                addStep(new SignInToLunarStep(getDataAccessorFactory(), apiClient));

                addStep(new AutoAuthenticationStep(getDataAccessorFactory(), apiClient));
            }

            @Override
            public AgentAuthenticationProcessStep getStartStep() {
                return new LunarAuthenticationInitStep(getDataAccessorFactory());
            }
        };
    }

    private LunarDataAccessorFactory getDataAccessorFactory() {
        return new LunarDataAccessorFactory(objectMapper);
    }
}
