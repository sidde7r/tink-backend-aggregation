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
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.environment.Utilities;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlow;

public class RuntimeAuthenticationApi {
    private final AgentInstance agentInstance;
    private final AuthenticationFlows authenticationFlows;

    public RuntimeAuthenticationApi(AgentInstance agentInstance) throws AuthenticatorNotFoundException {
        this.agentInstance = agentInstance;

        Utilities utilities = agentInstance.getUtilities();
        Operation operation = agentInstance.getOperation();

        this.authenticationFlows =
                Stream.of(
                                getFlows(new GenericAuthenticationProcess()),
                                getFlows(
                                        new Oauth2AuthenticationProcess(
                                                utilities.getRandomGenerator())),
                                getFlows(
                                        new Oauth2DecoupledAppAuthenticationProcess(
                                                utilities.getSleeper())),
                                getFlows(
                                        new ThirdPartyAppAuthenticationProcess(
                                                utilities.getSleeper())),
                                getFlows(
                                        new SwedishMobileBankIdAuthenticationProcess(
                                                utilities.getSleeper())),
                                getFlows(
                                        new UsernameAndPasswordAuthenticationProcess(
                                                operation.getStaticBankCredentials())),
                                getFlows(
                                        new Oauth2DecoupledSwedishMobileBankIdAuthenticationProcess(
                                                utilities.getSleeper())),
                                getFlows(
                                        new BerlinGroupAuthenticationProcess(
                                                utilities.getTimeGenerator(),
                                                utilities.getRandomGenerator())))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .orElseThrow(AuthenticatorNotFoundException::new);
    }

    public StepResponse<ConsentLifetime> executeNewConsentStep(
            @Nullable String stepId, StepRequest<Void> request)
            throws AuthenticationStepNotFoundException {

        ExecutionFlow<Void, ConsentLifetime> newConsentFlow =
                this.authenticationFlows.getNewConsentFlow();

        BaseStep<Void, ConsentLifetime> newConsentStep =
                newConsentFlow
                        .getStep(stepId)
                        .orElseThrow(AuthenticationStepNotFoundException::new);

        return newConsentStep.executeInternal(request);
    }

    public StepResponse<ConsentStatus> executeUseExistingConsentStep(
            @Nullable String stepId, StepRequest<Void> request)
            throws AuthenticationStepNotFoundException {

        ExecutionFlow<Void, ConsentStatus> useExistingConsentFlow =
                this.authenticationFlows.getUseExistingConsentFlow();

        BaseStep<Void, ConsentStatus> useExistingConsentStep =
                useExistingConsentFlow
                        .getStep(stepId)
                        .orElseThrow(AuthenticationStepNotFoundException::new);

        return useExistingConsentStep.executeInternal(request);
    }

    private <T> Optional<AuthenticationFlows> getFlows(AuthenticationProcess<T> authProcess) {
        return authProcess
                .tryInstantiateAuthenticator(this.agentInstance)
                .map(
                        agentAuthenticator -> {
                            ExecutionFlow<Void, ConsentLifetime> newConsentFlow =
                                    authProcess.getNewConsentFlow(agentAuthenticator);

                            ExecutionFlow<Void, ConsentStatus> useExistingConsentFlow =
                                    authProcess.getUseExistingConsentFlow(agentAuthenticator);

                            return new AuthenticationFlows(newConsentFlow, useExistingConsentFlow);
                        });
    }

    private static class AuthenticationFlows {
        private final ExecutionFlow<Void, ConsentLifetime> newConsentFlow;
        private final ExecutionFlow<Void, ConsentStatus> useExistingConsentFlow;

        public AuthenticationFlows(
                ExecutionFlow<Void, ConsentLifetime> newConsentFlow,
                ExecutionFlow<Void, ConsentStatus> useExistingConsentFlow) {
            this.newConsentFlow = newConsentFlow;
            this.useExistingConsentFlow = useExistingConsentFlow;
        }

        public ExecutionFlow<Void, ConsentLifetime> getNewConsentFlow() {
            return newConsentFlow;
        }

        public ExecutionFlow<Void, ConsentStatus> getUseExistingConsentFlow() {
            return useExistingConsentFlow;
        }
    }

    public static class AuthenticatorNotFoundException extends Exception {}

    public static class AuthenticationStepNotFoundException extends Exception {}
}
