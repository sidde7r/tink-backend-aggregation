package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginValidatorEmpty<INPUT> implements LoginValidator<INPUT> {

    private static final LoginValidatorEmpty INSTANCE = new LoginValidatorEmpty();

    public static LoginValidatorEmpty getInstance() {
        return INSTANCE;
    }

    @Override
    public void validate(INPUT input) {
        // no validation needed
    }
}
