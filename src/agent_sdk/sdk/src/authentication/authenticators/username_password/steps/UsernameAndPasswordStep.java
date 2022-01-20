package se.tink.agent.sdk.authentication.authenticators.username_password.steps;

import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordLogin;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentStep;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;

public class UsernameAndPasswordStep extends NewConsentStep {

    private final StaticBankCredentials staticBankCredentials;
    private final UsernameAndPasswordLogin agentUsernameAndPasswordLogin;

    public UsernameAndPasswordStep(
            StaticBankCredentials staticBankCredentials,
            UsernameAndPasswordLogin agentUsernameAndPasswordLogin) {
        this.staticBankCredentials = staticBankCredentials;
        this.agentUsernameAndPasswordLogin = agentUsernameAndPasswordLogin;
    }

    @Override
    public InteractiveStepResponse<ConsentLifetime> execute(StepRequest<Void> request) {
        String username =
                this.staticBankCredentials
                        .tryGet(Field.Key.USERNAME)
                        .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);
        String password =
                this.staticBankCredentials
                        .tryGet(Field.Key.PASSWORD)
                        .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);

        ConsentLifetime consentLifetime =
                this.agentUsernameAndPasswordLogin.login(username, password);
        return InteractiveStepResponse.done(consentLifetime);
    }
}
