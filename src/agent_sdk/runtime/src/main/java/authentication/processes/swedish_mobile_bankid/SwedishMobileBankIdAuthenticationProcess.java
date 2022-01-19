package se.tink.agent.runtime.authentication.processes.swedish_mobile_bankid;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.SwedishMobileBankIdAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.steps.SwedishMobileBankIdOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.common_steps.GetConsentLifetimeStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.features.AuthenticateSwedishMobileBankId;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;
import se.tink.agent.sdk.utils.Sleeper;

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
    public InteractiveExecutionFlow<Void, ConsentLifetime> getNewConsentFlow(
            SwedishMobileBankIdAuthenticator authenticator) {
        return InteractiveExecutionFlow.<Void, ConsentLifetime>startStep(
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
    public NonInteractiveExecutionFlow<Void, ConsentStatus> getUseExistingConsentFlow(
            SwedishMobileBankIdAuthenticator authenticator) {
        return NonInteractiveExecutionFlow.startStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
