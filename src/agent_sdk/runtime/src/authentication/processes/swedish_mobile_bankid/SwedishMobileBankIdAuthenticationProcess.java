package src.agent_sdk.runtime.src.authentication.processes.swedish_mobile_bankid;

import java.util.Optional;
import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.SwedishMobileBankIdAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.steps.SwedishMobileBankIdOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.common_steps.GetConsentLifetimeStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.features.AuthenticateSwedishMobileBankId;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import se.tink.agent.sdk.utils.Sleeper;
import src.agent_sdk.runtime.src.authentication.processes.AuthenticationProcess;
import src.agent_sdk.runtime.src.instance.AgentInstance;

public class SwedishMobileBankIdAuthenticationProcess
        implements AuthenticationProcess<SwedishMobileBankIdAuthenticator> {

    private final Sleeper sleeper;

    public SwedishMobileBankIdAuthenticationProcess(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    @Override
    public Optional<SwedishMobileBankIdAuthenticator> tryInstantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateSwedishMobileBankId.class)
                .map(AuthenticateSwedishMobileBankId::authenticator);
    }

    @Override
    public NewConsentFlow getNewConsentFlow(SwedishMobileBankIdAuthenticator authenticator) {
        return NewConsentFlow.builder()
                .startStep(
                        new ThirdPartyAppInitStep(
                                authenticator, SwedishMobileBankIdOpenAppStep.class))
                .addStep(
                        new SwedishMobileBankIdOpenAppStep(
                                authenticator, ThirdPartyAppPollStep.class))
                .addStep(
                        new ThirdPartyAppPollStep(
                                authenticator,
                                this.sleeper,
                                SwedishMobileBankIdOpenAppStep.class,
                                GetConsentLifetimeStep.class))
                .addStep(new GetConsentLifetimeStep(authenticator))
                .build();
    }

    @Override
    public ExistingConsentFlow getUseExistingConsentFlow(
            SwedishMobileBankIdAuthenticator authenticator) {
        return ExistingConsentFlow.builder()
                .startStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
