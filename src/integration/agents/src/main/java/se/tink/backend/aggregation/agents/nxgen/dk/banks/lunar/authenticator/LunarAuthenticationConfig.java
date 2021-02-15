package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.AutoAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.GetLunarAccessTokenStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.GetNemIdTokenStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.GetUserCredentialsStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.LunarAuthInitStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.SaveUserCredentialsStep;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps.SignInToLunarStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAbstractMultiStepsAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

@RequiredArgsConstructor
public class LunarAuthenticationConfig {

    private final AuthenticationApiClient apiClient;
    private final LunarDataAccessorFactory lunarDataAccessorFactory;
    private final RandomValueGenerator randomValueGenerator;
    private final NemIdIframeAttributes nemIdIframeAttributes;

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
                                nemIdIframeAttributes,
                                randomValueGenerator));
                addStep(new GetLunarAccessTokenStep(getDataAccessorFactory(), apiClient));
                addStep(new SignInToLunarStep(getDataAccessorFactory(), apiClient));

                addStep(new AutoAuthenticationStep(getDataAccessorFactory()));
            }

            @Override
            public AgentAuthenticationProcessStep getStartStep() {
                return new LunarAuthInitStep(getDataAccessorFactory());
            }
        };
    }

    private LunarDataAccessorFactory getDataAccessorFactory() {
        return lunarDataAccessorFactory;
    }
}
