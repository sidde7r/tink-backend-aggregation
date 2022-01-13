package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators;

import com.google.common.base.Preconditions;
import java.util.List;

public class LoginValidationExecutor<INPUT> {

    private List<LoginValidator<INPUT>> validators;

    public LoginValidationExecutor(LoginValidatorFactory<INPUT> loginValidatorFactory) {
        validators = loginValidatorFactory.getValidators();
    }

    public void execute(INPUT loginResponse) {
        Preconditions.checkState(
                validators != null && !validators.isEmpty(), "Validators list can't be empty");
        validators.stream().forEach(loginValidator -> loginValidator.validate(loginResponse));
    }
}
