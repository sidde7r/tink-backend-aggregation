package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators.LoginValidatorFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

public class LoginValidationStep<INPUT> extends AbstractAuthenticationStep {

    private List<LoginValidator> loginValidators;
    private LoginResponseProvider<INPUT> loginResponseProvider;

    public LoginValidationStep(
            LoginValidatorFactory loginValidatorsFactory,
            LoginResponseProvider loginResponseProvider) {
        this.loginValidators = loginValidatorsFactory.getValidators();
        this.loginResponseProvider = loginResponseProvider;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        if (loginValidators == null || loginValidators.isEmpty()) {
            throw new IllegalStateException("Validators list can't be empty");
        }

        INPUT validationInput = loginResponseProvider.getValidationInput();
        loginValidators.stream()
                .forEach(loginValidator -> loginValidator.validate(validationInput));

        return AuthenticationStepResponse.executeNextStep();
    }
}
