package se.tink.backend.aggregation.nxgen.controllers.authentication.webscraping.loginvalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.webscraping.loginvalidation.validators.LoginValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.webscraping.loginvalidation.validators.LoginValidatorFactory;

public class LoginValidationStepTest {
    LoginValidatorFactory loginValidatorsFactory;
    LoginResponseProvider callbackProcessor;
    AuthenticationRequest authenticationRequest;

    @Before
    public void init() {
        callbackProcessor = mock(LoginResponseProvider.class);
        loginValidatorsFactory = mock(LoginValidatorFactory.class);
        authenticationRequest = new AuthenticationRequest(mock(Credentials.class));
    }

    @Test
    public void execute_should_call_validate_method_in_LoginValidators_from_loginValidators_list() {
        // given
        LoginValidator loginValidator = mock(LoginValidator.class);
        List<LoginValidator> loginValidators = ImmutableList.of(loginValidator, loginValidator);
        when(loginValidatorsFactory.getValidators()).thenReturn(loginValidators);

        LoginValidationStep loginValidationStep =
                new LoginValidationStep(loginValidatorsFactory, callbackProcessor);

        // when
        loginValidationStep.execute(authenticationRequest);

        // then
        verify(loginValidator, times(2)).validate(any());
    }

    @Test
    public void execute_should_throw_exception_if_validators_are_null() {
        // given
        LoginValidationStep loginValidationStep =
                new LoginValidationStep(loginValidatorsFactory, callbackProcessor);

        // when
        Throwable throwable =
                catchThrowable(() -> loginValidationStep.execute(authenticationRequest));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);
    }
}
