package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidationExecutor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidatorFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

public class LoginValidationStep<INPUT> extends AbstractAuthenticationStep {

    private LoginValidationExecutor<INPUT> loginValidationExecutor;
    private LoginResponseProvider<INPUT> loginResponseProvider;

    public LoginValidationStep(
            LoginValidatorFactory loginValidatorsFactory,
            LoginResponseProvider loginResponseProvider) {
        this.loginValidationExecutor = new LoginValidationExecutor<>(loginValidatorsFactory);
        this.loginResponseProvider = loginResponseProvider;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        INPUT validationInput = loginResponseProvider.getValidationInput();
        loginValidationExecutor.execute(validationInput);

        return AuthenticationStepResponse.executeNextStep();
    }
}
