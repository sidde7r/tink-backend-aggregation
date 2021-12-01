package se.tink.agent.sdk.authentication.authenticators.username_password.steps;

import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordLogin;
import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.environment.StaticBankCredentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;

public class UsernameAndPasswordStep implements NewConsentStep {

    private final UsernameAndPasswordLogin agentUsernameAndPasswordLogin;

    public UsernameAndPasswordStep(UsernameAndPasswordLogin agentUsernameAndPasswordLogin) {
        this.agentUsernameAndPasswordLogin = agentUsernameAndPasswordLogin;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        StaticBankCredentials staticBankCredentials = request.getStaticBankCredentials();

        String username =
                staticBankCredentials
                        .tryGet(Field.Key.USERNAME)
                        .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);
        String password =
                staticBankCredentials
                        .tryGet(Field.Key.PASSWORD)
                        .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);

        ConsentLifetime consentLifetime =
                this.agentUsernameAndPasswordLogin.login(username, password);
        return NewConsentResponse.done(consentLifetime);
    }
}
