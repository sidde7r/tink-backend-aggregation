package se.tink.agent.sdk.authentication.authenticators.username_password.steps;

import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordLogin;
import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.environment.StaticBankCredentials;
import se.tink.backend.agents.rpc.Field;

public class UsernameAndPasswordStep implements NewConsentStep {

    private final UsernameAndPasswordLogin agentUsernameAndPasswordLogin;

    public UsernameAndPasswordStep(UsernameAndPasswordLogin agentUsernameAndPasswordLogin) {
        this.agentUsernameAndPasswordLogin = agentUsernameAndPasswordLogin;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        StaticBankCredentials staticBankCredentials = request.getStaticBankCredentials();

        // TODO: use correct exception.
        // throw LoginError.INCORRECT_CREDENTIALS.exception();
        String username =
                staticBankCredentials
                        .tryGet(Field.Key.USERNAME)
                        .orElseThrow(() -> new IllegalStateException("INCORRECT_CREDENTIALS"));
        String password =
                staticBankCredentials
                        .tryGet(Field.Key.PASSWORD)
                        .orElseThrow(() -> new IllegalStateException("INCORRECT_CREDENTIALS"));

        ConsentLifetime consentLifetime =
                this.agentUsernameAndPasswordLogin.login(username, password);
        return NewConsentResponse.done(consentLifetime);
    }
}
