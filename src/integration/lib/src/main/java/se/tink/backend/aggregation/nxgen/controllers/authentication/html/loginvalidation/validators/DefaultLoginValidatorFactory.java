package se.tink.backend.aggregation.nxgen.controllers.authentication.html.loginvalidation.validators;

import com.google.common.collect.ImmutableList;
import java.util.List;

public abstract class DefaultLoginValidatorFactory<INPUT> implements LoginValidatorFactory {

    @Override
    public List<LoginValidator> getValidators() {
        return ImmutableList.of(
                createChangePasswordErrorValidator(),
                createAccountTypeErrorValidator(),
                createBankServiceErrorValidator(),
                createIncorrectCredentialsErrorValidator(),
                createNotActivatedErrorValidator(),
                createPendingConfirmationErrorValidator(),
                createScaErrorValidator(),
                createUserBlockedErrorValidator(),
                createLoginSuccessValidator());
    }

    public abstract LoginValidator<INPUT> createChangePasswordErrorValidator();

    public abstract LoginValidator<INPUT> createAccountTypeErrorValidator();

    public abstract LoginValidator<INPUT> createBankServiceErrorValidator();

    public abstract LoginValidator<INPUT> createIncorrectCredentialsErrorValidator();

    public abstract LoginValidator<INPUT> createNotActivatedErrorValidator();

    public abstract LoginValidator<INPUT> createPendingConfirmationErrorValidator();

    public abstract LoginValidator<INPUT> createScaErrorValidator();

    public abstract LoginValidator<INPUT> createUserBlockedErrorValidator();

    public abstract LoginValidator<INPUT> createLoginSuccessValidator();
}
