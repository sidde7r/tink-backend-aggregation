package se.tink.agent.runtime.authentication;

import com.google.common.collect.ImmutableList;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.generic.GenericAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.oauth2.Oauth2AuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.oauth2_decoupled_app.Oauth2DecoupledAppAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.thirdparty_app.ThirdPartyAppAuthenticationProcess;
import se.tink.agent.runtime.authentication.processes.username_password.UsernameAndPasswordAuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.operation.MultifactorAuthenticationState;
import se.tink.agent.sdk.storage.Storage;

public class AgentAuthenticator {
    private final AgentInstance agentInstance;
    private final Storage authenticationStorage;
    private final ImmutableList<AuthenticationProcess<?>> availableProcesses;

    public AgentAuthenticator(
            AgentInstance agentInstance,
            Storage authenticationStorage,
            MultifactorAuthenticationState multifactorAuthenticationState) {
        this.agentInstance = agentInstance;
        this.authenticationStorage = authenticationStorage;
        this.availableProcesses =
                ImmutableList.of(
                        new GenericAuthenticationProcess(),
                        new Oauth2AuthenticationProcess(multifactorAuthenticationState),
                        new Oauth2DecoupledAppAuthenticationProcess(),
                        new ThirdPartyAppAuthenticationProcess(),
                        new UsernameAndPasswordAuthenticationProcess());
    }

    public void authenticate(String request) {}
}
