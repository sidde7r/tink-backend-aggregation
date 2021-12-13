package se.tink.agent.runtime.authentication;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.berlingroup.BerlinGroupAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.generic.GenericAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.oauth2.Oauth2AuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.oauth2_decoupled_app.Oauth2DecoupledAppAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.oauth2_decoupled_swedish_mobile_bankid.Oauth2DecoupledSwedishMobileBankIdAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.swedish_mobile_bankid.SwedishMobileBankIdAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.thirdparty_app.ThirdPartyAppAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.username_password.UsernameAndPasswordAuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.runtime.operation.MultifactorAuthenticationStateImpl;
import se.tink.agent.sdk.authentication.authenticators.generic.AuthenticationFlow;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentRequest;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentResponse;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.environment.Utilities;
import se.tink.agent.sdk.operation.MultifactorAuthenticationState;

public class RuntimeAuthenticator {
    private final AgentInstance agentInstance;
    private final AuthenticationFlows authenticationFlows;

    public RuntimeAuthenticator(AgentInstance agentInstance) throws AuthenticatorNotFoundException {
        this.agentInstance = agentInstance;

        Utilities utilities = agentInstance.getEnvironment().getUtilities();

        MultifactorAuthenticationState multifactorAuthenticationState =
                new MultifactorAuthenticationStateImpl(
                        utilities.getRandomGenerator().randomUuidWithTinkTag());

        this.authenticationFlows =
                Stream.of(
                                getFlows(new GenericAuthenticationProcess()),
                                getFlows(
                                        new Oauth2AuthenticationProcess(
                                                multifactorAuthenticationState)),
                                getFlows(
                                        new Oauth2DecoupledAppAuthenticationProcess(
                                                utilities.getSleeper())),
                                getFlows(
                                        new ThirdPartyAppAuthenticationProcess(
                                                utilities.getSleeper())),
                                getFlows(
                                        new SwedishMobileBankIdAuthenticationProcess(
                                                utilities.getSleeper())),
                                getFlows(new UsernameAndPasswordAuthenticationProcess()),
                                getFlows(
                                        new Oauth2DecoupledSwedishMobileBankIdAuthenticationProcess(
                                                utilities.getSleeper())),
                                getFlows(
                                        new BerlinGroupAuthenticationProcess(
                                                utilities.getTimeGenerator(),
                                                multifactorAuthenticationState)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .orElseThrow(AuthenticatorNotFoundException::new);
    }

    public NewConsentResponse executeStep(@Nullable String stepId, NewConsentRequest request)
            throws AuthenticationStepNotFoundException {
        AuthenticationFlow<NewConsentStep> newConsentFlow =
                this.authenticationFlows.getNewConsentFlow();

        NewConsentStep newConsentStep =
                newConsentFlow
                        .getStep(stepId)
                        .orElseThrow(AuthenticationStepNotFoundException::new);

        return newConsentStep.execute(request);
    }

    public ExistingConsentResponse executeStep(
            @Nullable String stepId, ExistingConsentRequest request)
            throws AuthenticationStepNotFoundException {
        AuthenticationFlow<ExistingConsentStep> existingConsentFlow =
                this.authenticationFlows.getUseExistingConsentFlow();

        ExistingConsentStep existingConsentStep =
                existingConsentFlow
                        .getStep(stepId)
                        .orElseThrow(AuthenticationStepNotFoundException::new);

        return existingConsentStep.execute(request);
    }

    private <T> Optional<AuthenticationFlows> getFlows(AuthenticationProcess<T> authProcess) {
        return authProcess
                .tryInstantiateAuthenticator(this.agentInstance)
                .map(
                        agentAuthenticator -> {
                            AuthenticationFlow<NewConsentStep> newConsentFlow =
                                    authProcess.getNewConsentFlow(agentAuthenticator);
                            AuthenticationFlow<ExistingConsentStep> useExistingConsentFlow =
                                    authProcess.getUseExistingConsentFlow(agentAuthenticator);

                            return new AuthenticationFlows(newConsentFlow, useExistingConsentFlow);
                        });
    }

    private static class AuthenticationFlows {
        private final AuthenticationFlow<NewConsentStep> newConsentFlow;
        private final AuthenticationFlow<ExistingConsentStep> useExistingConsentFlow;

        public AuthenticationFlows(
                AuthenticationFlow<NewConsentStep> newConsentFlow,
                AuthenticationFlow<ExistingConsentStep> useExistingConsentFlow) {
            this.newConsentFlow = newConsentFlow;
            this.useExistingConsentFlow = useExistingConsentFlow;
        }

        public AuthenticationFlow<NewConsentStep> getNewConsentFlow() {
            return newConsentFlow;
        }

        public AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow() {
            return useExistingConsentFlow;
        }
    }

    public static class AuthenticatorNotFoundException extends Exception {}

    public static class AuthenticationStepNotFoundException extends Exception {}
}
