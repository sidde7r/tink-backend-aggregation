package se.tink.backend.aggregation.nxgen.controllers.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentConstants.BankId;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;

@RunWith(JUnitParamsRunner.class)
public class PaymentControllerTest {

    private PaymentExecutor paymentExecutor;

    @Before
    public void setup() {
        paymentExecutor = mock(PaymentExecutor.class);
    }

    @Test
    @Parameters(method = "exceptionsForDefault")
    public void shouldRethrowExceptionsWhenNotAuthExceptionAndUsingDefaultBehaviorInSignMethod(
            AgentException exception) {
        // given
        given(paymentExecutor.sign(null)).willThrow(exception);
        PaymentController paymentController = new PaymentController(paymentExecutor);

        // when
        Throwable throwable = catchThrowable(() -> paymentController.sign(null));

        // then
        assertThat(throwable).isEqualTo(exception);
    }

    private Object[] exceptionsForDefault() {
        return new Object[] {
            AuthorizationError.UNAUTHORIZED.exception(),
            BankServiceError.ACCESS_EXCEEDED.exception(),
            ThirdPartyError.INCORRECT_SECRETS.exception()
        };
    }

    @Test
    @Parameters(method = "exceptionsForDefaultAuth")
    public void
            shouldMapAuthExceptionsToDefaultPaymentExceptionWhenUsingDefaultBehaviorInSignMethod(
                    AgentException exception) {
        // given
        given(paymentExecutor.sign(null)).willThrow(exception);
        PaymentController paymentController = new PaymentController(paymentExecutor);

        // when
        Throwable throwable = catchThrowable(() -> paymentController.sign(null));

        // then
        assertThat(throwable)
                .isInstanceOf(PaymentAuthorizationException.class)
                .hasMessage("Payment could not be signed");
    }

    private Object[] exceptionsForDefaultAuth() {
        return new Object[] {
            LoginError.DEFAULT_MESSAGE.exception(),
            SessionError.SESSION_EXPIRED.exception(),
            BankIdNOError.INITIALIZATION_ERROR.exception(),
            SupplementalInfoError.NO_VALID_CODE.exception()
        };
    }

    @Test
    @Parameters(method = "exceptionsForDefaultBankId")
    public void
            shouldMapBankIdExceptionsToFittingPaymentExceptionWhenUsingDefaultBehaviorInSignMethod(
                    AgentException exception, String expectedMessage) {
        // given
        given(paymentExecutor.sign(null)).willThrow(exception);
        PaymentController paymentController = new PaymentController(paymentExecutor);

        // when
        Throwable throwable = catchThrowable(() -> paymentController.sign(null));

        // then
        assertThat(throwable)
                .isInstanceOf(PaymentAuthorizationException.class)
                .hasMessage(expectedMessage);
    }

    private Object[] exceptionsForDefaultBankId() {
        return new Object[] {
            new Object[] {BankIdError.ACTIVATE_EXTENDED_BANKID.exception(), BankId.NO_EXTENDED_USE},
            new Object[] {BankIdError.INTERRUPTED.exception(), BankId.INTERRUPTED},
            new Object[] {BankIdError.TIMEOUT.exception(), BankId.TIMEOUT},
            new Object[] {BankIdError.NO_CLIENT.exception(), BankId.NO_CLIENT},
            new Object[] {BankIdError.CANCELLED.exception(), BankId.CANCELLED},
            new Object[] {BankIdError.UNKNOWN.exception(), BankId.UNKNOWN}
        };
    }

    @Test
    @Parameters(method = "exceptionsForDefaultCreate")
    public void shouldRethrowAllExceptionsWhenUsingDefaultBehaviorInCreateMethod(
            AgentException exception) {
        // given
        given(paymentExecutor.create(null)).willThrow(exception);
        PaymentController paymentController = new PaymentController(paymentExecutor);

        // when
        Throwable throwable = catchThrowable(() -> paymentController.create(null));

        // then
        assertThat(throwable).isEqualTo(exception);
    }

    private Object[] exceptionsForDefaultCreate() {
        return new Object[] {
            AuthorizationError.UNAUTHORIZED.exception(),
            BankServiceError.ACCESS_EXCEEDED.exception(),
            ThirdPartyError.INCORRECT_SECRETS.exception(),
            LoginError.DEFAULT_MESSAGE.exception(),
            SessionError.SESSION_EXPIRED.exception(),
            BankIdNOError.INITIALIZATION_ERROR.exception(),
            SupplementalInfoError.NO_VALID_CODE.exception(),
            BankIdError.ACTIVATE_EXTENDED_BANKID.exception()
        };
    }

    @Test
    @Parameters(method = "exceptionsForNew")
    // This test is not exhaustive, it just checks few things.
    public void shouldMapExceptionProperlyWhenUsingNewStrategy(
            AgentException exception, Class<PaymentException> expectedClass) {
        // given
        given(paymentExecutor.create(null)).willThrow(exception);
        given(paymentExecutor.sign(null)).willThrow(exception);
        PaymentController paymentController =
                new PaymentController(
                        paymentExecutor, null, new PaymentControllerExceptionMapper());

        // when
        Throwable throwableSign = catchThrowable(() -> paymentController.sign(null));
        Throwable throwableCreate = catchThrowable(() -> paymentController.create(null));

        // then
        assertThat(throwableSign).isInstanceOf(expectedClass);
        assertThat(throwableCreate).isInstanceOf(expectedClass);
    }

    private Object[] exceptionsForNew() {
        return new Object[] {
            new Object[] {
                AuthorizationError.UNAUTHORIZED.exception(), PaymentAuthorizationException.class
            },
            new Object[] {
                LoginError.DEFAULT_MESSAGE.exception(), PaymentAuthenticationException.class
            },
            new Object[] {
                SessionError.SESSION_EXPIRED.exception(), PaymentAuthorizationException.class
            },
            new Object[] {BankIdNOError.INITIALIZATION_ERROR.exception(), PaymentException.class},
            new Object[] {
                SupplementalInfoError.NO_VALID_CODE.exception(),
                PaymentAuthorizationCancelledByUserException.class
            },
            new Object[] {
                BankIdError.ACTIVATE_EXTENDED_BANKID.exception(),
                PaymentAuthorizationException.class
            }
        };
    }
}
