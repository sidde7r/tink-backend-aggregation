package se.tink.agent.runtime.authentication.processes.berlingroup;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.steps.BerlinGroupOpenConsentAppStep;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.steps.BerlinGroupVerifyAuthorizedConsentStep;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.steps.BerlinGroupVerifyConsentStatusStep;
import se.tink.agent.sdk.authentication.authenticators.generic.AuthenticationFlow;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.features.AuthenticateBerlinGroup;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
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
    public AuthenticationFlow<NewConsentStep> getNewConsentFlow(
            BerlinGroupAuthenticator authenticator) {
        return AuthenticationFlow.builder(
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
    public AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow(
            BerlinGroupAuthenticator authenticator) {
        return AuthenticationFlow.builder(
                        new BerlinGroupVerifyConsentStatusStep(authenticator, authenticator))
                .build();
    }
}
