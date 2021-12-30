package se.tink.backend.aggregation.nxgen.controllers.authentication.webscraping.loginvalidation.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class DefaultLoginValidatorFactoryTest {

    @Test
    public void getValidators_should_return_8_Login_Validators_by_default() {
        // given
        // when
        DefaultLoginValidatorFactory defaultLoginValidatorFactory =
                constructImplementationOfDefaultLoginValidatorFactory();

        // then
        assertThat(defaultLoginValidatorFactory.getValidators()).hasSize(9);
    }

    public DefaultLoginValidatorFactory constructImplementationOfDefaultLoginValidatorFactory() {
        LoginValidator loginValidator = mock(LoginValidator.class);

        return new DefaultLoginValidatorFactory() {
            @Override
            public LoginValidator createChangePasswordErrorValidator() {
                return loginValidator;
            }

            @Override
            public LoginValidator createAccountTypeErrorValidator() {
                return loginValidator;
            }

            @Override
            public LoginValidator createBankServiceErrorValidator() {
                return loginValidator;
            }

            @Override
            public LoginValidator createIncorrectCredentialsErrorValidator() {
                return loginValidator;
            }

            @Override
            public LoginValidator createNotActivatedErrorValidator() {
                return loginValidator;
            }

            @Override
            public LoginValidator createPendingConfirmationErrorValidator() {
                return loginValidator;
            }

            @Override
            public LoginValidator createScaErrorValidator() {
                return loginValidator;
            }

            @Override
            public LoginValidator createUserBlockedErrorValidator() {
                return loginValidator;
            }

            @Override
            public LoginValidator createLoginSuccessValidator() {
                return loginValidator;
            }
        };
    }
}
