package se.tink.agent.runtime.authentication;

import java.util.List;
import java.util.Optional;
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
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.environment.Utilities;

public class RuntimeAuthenticationApi {
    private final AgentInstance agentInstance;
    private final List<AuthenticationProcess<?>> predefinedAuthenticationProcesses;

    public RuntimeAuthenticationApi(AgentInstance agentInstance) {
        this.agentInstance = agentInstance;

        Utilities utilities = agentInstance.getUtilities();
        Operation operation = agentInstance.getOperation();

        predefinedAuthenticationProcesses =
                List.of(
                        new GenericAuthenticationProcess(),
                        new Oauth2AuthenticationProcess(utilities.getRandomGenerator()),
                        new Oauth2DecoupledAppAuthenticationProcess(utilities.getSleeper()),
                        new ThirdPartyAppAuthenticationProcess(utilities.getSleeper()),
                        new SwedishMobileBankIdAuthenticationProcess(utilities.getSleeper()),
                        new UsernameAndPasswordAuthenticationProcess(
                                operation.getStaticBankCredentials()),
                        new Oauth2DecoupledSwedishMobileBankIdAuthenticationProcess(
                                utilities.getSleeper()),
                        new BerlinGroupAuthenticationProcess(
                                utilities.getTimeGenerator(), utilities.getRandomGenerator()));
    }

    public NewConsentFlow getNewConsentFlow() throws AuthenticatorNotFoundException {
        return predefinedAuthenticationProcesses.stream()
                .map(this::getNewConsentFlow)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(AuthenticatorNotFoundException::new);
    }

    public ExistingConsentFlow getExistingConsentFlow() throws AuthenticatorNotFoundException {
        return predefinedAuthenticationProcesses.stream()
                .map(this::getExistingConsentFlow)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(AuthenticatorNotFoundException::new);
    }

    private <T> Optional<ExistingConsentFlow> getExistingConsentFlow(
            AuthenticationProcess<T> authProcess) {
        return authProcess
                .tryInstantiateAuthenticator(this.agentInstance)
                .map(authProcess::getUseExistingConsentFlow);
    }

    private <T> Optional<NewConsentFlow> getNewConsentFlow(AuthenticationProcess<T> authProcess) {
        return authProcess
                .tryInstantiateAuthenticator(this.agentInstance)
                .map(authProcess::getNewConsentFlow);
    }
}
