package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.CONSENT_INVALID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.CONSENT_UNKNOWN;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.NO_SCA_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.OTP_WRONG_FORMAT;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.OTP_WRONG_LENGTH;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.PSU_CREDENTIALS_INVALID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.PSU_TOO_LONG;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.PsuErrorMessages;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.payment.DuplicatePaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@Slf4j
public class SparkassenErrorHandler {

    public enum ErrorSource {
        AUTHORISATION_USERNAME_PASSWORD,
        AUTHORISATION_OTP,
        AUTHORISATION_SELECT_METHOD,
        CONSENT_DETAILS,
        CREATE_PAYMENT,
        FETCH_PAYMENT
    }

    public static void handeHttpException(
            HttpResponseException httpResponseException, ErrorSource errorSource) {
        Optional<ErrorResponse> maybeErrorResponse =
                ErrorResponse.fromHttpException(httpResponseException);

        if (maybeErrorResponse.isPresent()) {
            ErrorResponse errorResponse = maybeErrorResponse.get();
            AgentError error = null;
            PaymentException paymentException = null;

            switch (errorSource) {
                case AUTHORISATION_OTP:
                    error = handleAuthorisationOtpErrors(errorResponse);
                    break;
                case AUTHORISATION_USERNAME_PASSWORD:
                    error = handleUsernamePasswordErrors(errorResponse);
                    break;
                case AUTHORISATION_SELECT_METHOD:
                    error = handleSelectMethodErrors(errorResponse);
                    break;
                case CONSENT_DETAILS:
                    error = handleConsentDetailsErrors(errorResponse);
                    break;
                case CREATE_PAYMENT:
                case FETCH_PAYMENT:
                    paymentException =
                            handlePaymentContextErrors(errorResponse, httpResponseException);
                    break;
                default:
                    return;
            }

            if (error == null) {
                error = handleGeneralContextErrors(errorResponse);
            }

            if (error != null) {
                if (error == LoginError.NOT_CUSTOMER) {
                    throw error.exception(
                            new LocalizableKey(
                                    "Bank couldn't find such a user in the system. "
                                            + "Are you sure that you have selected a correct branch or entered a correct username?"),
                            httpResponseException);
                } else {
                    throw error.exception(httpResponseException);
                }
            }

            if (paymentException != null) {
                throw paymentException;
            }
        }
    }

    private static AgentError handleAuthorisationOtpErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .or(ErrorResponse.anyTppMessageMatchesPredicate(OTP_WRONG_FORMAT))
                .or(ErrorResponse.anyTppMessageMatchesPredicate(OTP_WRONG_LENGTH))
                .test(errorResponse)) {
            return LoginError.INCORRECT_CHALLENGE_RESPONSE;
        }
        return null;
    }

    private static AgentError handleUsernamePasswordErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.TEMPORARILY_BLOCKED_ACCOUNT)
                .or(ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.BLOCKED_ACCOUNT))
                .test(errorResponse)) {
            return AuthorizationError.ACCOUNT_BLOCKED;
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.CUSTOMER_NOT_FOUND)
                .test(errorResponse)) {
            return LoginError.NOT_CUSTOMER;
        }
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .or(ErrorResponse.anyTppMessageMatchesPredicate(PSU_TOO_LONG))
                .test(errorResponse)) {
            return LoginError.INCORRECT_CREDENTIALS;
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.NO_ACTIVE_TAN_MEDIUM)
                .test(errorResponse)) {
            return LoginError.NO_AVAILABLE_SCA_METHODS;
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.PLEASE_CHANGE_PIN)
                .test(errorResponse)) {
            return LoginError.PASSWORD_CHANGE_REQUIRED;
        }
        if (ErrorResponse.anyTppMessageMatchesPredicate(NO_SCA_METHOD).test(errorResponse)) {
            return LoginError.NO_AVAILABLE_SCA_METHODS;
        }
        if (ErrorResponse.anyTppMessageMatchesPredicate(CONSENT_INVALID).test(errorResponse)) {
            return AuthorizationError.UNAUTHORIZED;
        }
        return null;
    }

    private static AgentError handleSelectMethodErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(NO_SCA_METHOD).test(errorResponse)) {
            // NZG-486 This in theory shouldn't happen, but it does. Extra log line to gather such
            // cases and ask bank about it
            log.info("Sparkassen error - no usable SCA - during sca method selection occurred!");
            return LoginError.NO_AVAILABLE_SCA_METHODS;
        }
        return null;
    }

    private static AgentError handleConsentDetailsErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(CONSENT_UNKNOWN).test(errorResponse)) {
            return SessionError.SESSION_EXPIRED;
        }
        return null;
    }

    private static AgentError handleGeneralContextErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.INCORRECT_TAN)
                .test(errorResponse)) {
            return LoginError.INCORRECT_CHALLENGE_RESPONSE;
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.ENTER_LOGIN_AND_PIN)
                .test(errorResponse)) {
            return AuthorizationError.UNAUTHORIZED;
        }

        return null;
    }

    private static PaymentException handlePaymentContextErrors(
            ErrorResponse errorResponse, HttpResponseException exception) {
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.JOB_NOT_EXECUTED_DUPLICATE)
                .test(errorResponse)) {
            return new DuplicatePaymentException();
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.NO_ORDER_AUTHORIZATION)
                .test(errorResponse)) {
            return new PaymentAuthorizationException(exception);
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.JOB_NOT_EXECUTED)
                .test(errorResponse)) {
            return new PaymentRejectedException(exception);
        }
        if (ErrorResponse.anyTppMessageMatchesPredicate(
                        SparkassenKnownErrors.PAYMENT_STATUS_UNKNOWN)
                .test(errorResponse)) {
            return new PaymentAuthorizationException(exception);
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.PAYMENT_LIMIT_EXCEEDED)
                .test(errorResponse)) {
            return PaymentRejectedException.tooManyTransactions();
        }
        if (ErrorResponse.psuMessageContainsPredicate(PsuErrorMessages.ORDER_NOT_EXECUTED)
                .test(errorResponse)) {
            return new InsufficientFundsException();
        }

        return null;
    }
}
