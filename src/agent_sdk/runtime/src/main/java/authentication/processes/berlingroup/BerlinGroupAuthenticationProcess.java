package se.tink.agent.runtime.authentication.processes.berlingroup;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.steps.BerlinGroupOpenConsentAppStep;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.steps.BerlinGroupVerifyAuthorizedConsentStep;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.steps.BerlinGroupVerifyConsentStatusStep;
import se.tink.agent.sdk.authentication.features.AuthenticateBerlinGroup;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import se.tink.agent.sdk.operation.MultifactorAuthenticationState;
import se.tink.agent.sdk.utils.TimeGenerator;

public class BerlinGroupAuthenticationProcess
        implements AuthenticationProcess<BerlinGroupAuthenticator> {
    private final TimeGenerator timeGenerator;
    private final MultifactorAuthenticationState multifactorAuthenticationState;

    public BerlinGroupAuthenticationProcess(
            TimeGenerator timeGenerator,
            MultifactorAuthenticationState multifactorAuthenticationState) {
        this.timeGenerator = timeGenerator;
        this.multifactorAuthenticationState = multifactorAuthenticationState;
    }

    @Override
    public Optional<BerlinGroupAuthenticator> tryInstantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateBerlinGroup.class)
                .map(AuthenticateBerlinGroup::authenticator);
    }

    @Override
    public NewConsentFlow getNewConsentFlow(BerlinGroupAuthenticator authenticator) {
        return NewConsentFlow.builder()
                .startStep(
                        new BerlinGroupOpenConsentAppStep(
                                this.timeGenerator,
                                this.multifactorAuthenticationState,
                                authenticator,
                                authenticator,
                                BerlinGroupVerifyAuthorizedConsentStep.class))
                .addStep(new BerlinGroupVerifyAuthorizedConsentStep(authenticator, authenticator))
                .build();
    }

    @Override
    public ExistingConsentFlow getUseExistingConsentFlow(BerlinGroupAuthenticator authenticator) {
        return ExistingConsentFlow.builder()
                .startStep(new BerlinGroupVerifyConsentStatusStep(authenticator, authenticator))
                .build();
    }
}
