package se.tink.backend.aggregation.nxgen.controllers.payment.exception;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationFailedByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

// This class aims to translate (almost) all AgentExceptions into matching PaymentExceptions, that
// are handled and understood by payment exception handling.
// Some things are left not mapped on purpose. They either have their own handlers in payment
// exception handling, or will fall through to the default handler.
@Slf4j
public class PaymentControllerExceptionMapper implements PaymentControllerAgentExceptionMapper {

    @Override
    public Optional<PaymentException> tryToMapToPaymentException(
            AgentException agentException, PaymentControllerAgentExceptionMapperContext context) {
        PaymentException paymentException = null;
        boolean worryAboutNoMapping = true;

        if (agentException instanceof AuthorizationException) {
            paymentException = mapAuthorizationException((AuthorizationException) agentException);
        } else if (agentException instanceof BankIdException) {
            // This handling is referencing the "old" way of mapping exceptions, since I have no
            // ideas how to do it better
            paymentException =
                    PaymentControllerOldExceptionMapper.mapBankIdException(
                            (BankIdException) agentException);
        } else if (agentException instanceof BankIdNOException) {
            paymentException = mapBankIdNOException((BankIdNOException) agentException);
        } else if (agentException instanceof BankServiceException) {
            // This exception is handled by BankServiceExceptionHandler, no not need to map it here
            worryAboutNoMapping = false;
        } else if (agentException instanceof LoginException) {
            paymentException = mapLoginException((LoginException) agentException);
        } else if (agentException instanceof NemIdException) {
            paymentException = mapNemIdException((NemIdException) agentException);
        } else if (agentException instanceof SessionException) {
            paymentException = mapSessionException((SessionException) agentException);
        } else if (agentException instanceof SupplementalInfoException) {
            paymentException =
                    mapSupplementalInfoException((SupplementalInfoException) agentException);
        } else if (agentException instanceof ThirdPartyAppException) {
            paymentException = mapThirdPartyAppException((ThirdPartyAppException) agentException);
        } else if (agentException instanceof ThirdPartyException) {
            // This is rare error, only used in one agent.
            // Not mapping it, let it fall to the default handler.
            worryAboutNoMapping = false;
        }

        if (paymentException == null && worryAboutNoMapping) {
            log.warn(
                    "[Agent->Payment Exceptions] AgentException not mapped to any PaymentException! Class: "
                            + agentException.getClass().getSimpleName()
                            + " Error: "
                            + agentException.getError().name());
        }

        return Optional.ofNullable(paymentException);
    }

    private static PaymentException mapAuthorizationException(
            AuthorizationException authorizationException) {
        AuthorizationError authorizationError = authorizationException.getError();
        switch (authorizationError) {
            case UNAUTHORIZED:
            case NO_VALID_PROFILE:
            case ACCOUNT_BLOCKED:
                return new PaymentAuthorizationException(authorizationException);
            default:
                return null;
        }
    }

    private static PaymentException mapBankIdNOException(BankIdNOException bankIdNOException) {
        BankIdNOError bankIdNOError = bankIdNOException.getError();
        switch (bankIdNOError) {
            case INITIALIZATION_ERROR:
                return new PaymentException(bankIdNOError.userMessage().get(), bankIdNOException);
            case INVALID_SSN:
            case INVALID_SSN_FORMAT:
            case INVALID_SSN_OR_ONE_TIME_CODE:
            case INVALID_ONE_TIME_CODE:
            case INVALID_ONE_TIME_CODE_FORMAT:
            case INVALID_BANK_ID_PASSWORD_FORMAT:
            case INVALID_BANK_ID_PASSWORD:
                return new PaymentAuthorizationFailedByUserException(
                        bankIdNOError.userMessage().get(), bankIdNOException);
            case UNKNOWN_BANK_ID_ERROR:
            case MOBILE_BANK_ID_TIMEOUT_OR_REJECTED:
            case THIRD_PARTY_APP_BLOCKED:
                return new PaymentAuthenticationException(
                        bankIdNOError.userMessage().get(), bankIdNOException);
            case THIRD_PARTY_APP_TIMEOUT:
                return new PaymentAuthorizationTimeOutException(
                        bankIdNOError.userMessage().get(), bankIdNOException);
            case THIRD_PARTY_APP_REJECTED:
                return new PaymentAuthorizationCancelledByUserException(
                        bankIdNOError.userMessage().get(), bankIdNOException);
            default:
                return null;
        }
    }

    private static PaymentException mapLoginException(LoginException loginException) {
        LoginError loginError = loginException.getError();
        switch (loginError) {
            case NOT_CUSTOMER:
            case NOT_SUPPORTED:
            case INCORRECT_CREDENTIALS:
            case INCORRECT_CREDENTIALS_LAST_ATTEMPT:
            case INCORRECT_CHALLENGE_RESPONSE:
            case CREDENTIALS_VERIFICATION_ERROR:
            case NO_AVAILABLE_SCA_METHODS:
            case WRONG_ACTIVATION_CODE:
            case ACTIVATION_TIMED_OUT:
            case PASSWORD_CHANGE_REQUIRED:
            case NO_ACCOUNTS:
                return new PaymentAuthenticationException(
                        loginError.userMessage().get(), loginException);
            case WRONG_PHONENUMBER:
            case WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE:
            case ERROR_WITH_MOBILE_OPERATOR:
            case REGISTER_DEVICE_ERROR:
            case NO_ACCESS_TO_MOBILE_BANKING:
            case INVALIDATED_CREDENTIALS:
            case DEFAULT_MESSAGE:
                return new PaymentAuthenticationException(loginException);
            default:
                return null;
        }
    }

    private static PaymentException mapNemIdException(NemIdException nemIdException) {
        NemIdError nemIdError = nemIdException.getError();
        switch (nemIdError) {
            case TIMEOUT:
                return new PaymentAuthorizationTimeOutException(nemIdException);
            case INTERRUPTED:
                return new PaymentAuthenticationException(
                        nemIdError.userMessage().get(), nemIdException);
            case REJECTED:
                return new PaymentAuthorizationCancelledByUserException(nemIdException);
            case CODE_TOKEN_NOT_SUPPORTED:
            case SECOND_FACTOR_NOT_REGISTERED:
            case USE_NEW_CODE_CARD:
            case NEMID_LOCKED:
            case NEMID_BLOCKED:
            case NEMID_PASSWORD_BLOCKED:
            case LOCKED_PIN:
            case KEY_APP_NOT_READY_TO_USE:
            case RENEW_NEMID:
            case OLD_OTP_USED:
                return new PaymentAuthorizationException(
                        nemIdError.userMessage().get(), nemIdException);
            case INVALID_CODE_CARD_CODE:
            case INVALID_CODE_TOKEN_CODE:
                return new PaymentAuthorizationFailedByUserException(
                        nemIdError.userMessage().get(), nemIdException);
            default:
                return null;
        }
    }

    private static PaymentException mapSessionException(SessionException sessionException) {
        // With this one I'm not all that certain on what to map it to.
        // For now, keeping it very vague.
        SessionError sessionError = sessionException.getError();
        switch (sessionError) {
            case SESSION_EXPIRED:
            case SESSION_ALREADY_ACTIVE:
            case CONSENT_EXPIRED:
            case CONSENT_INVALID:
            case CONSENT_REVOKED_BY_USER:
            case CONSENT_REVOKED:
                return new PaymentAuthorizationException(sessionException);
            default:
                return null;
        }
    }

    private static PaymentException mapSupplementalInfoException(
            SupplementalInfoException supplementalInfoException) {
        SupplementalInfoError supplementalInfoError = supplementalInfoException.getError();
        switch (supplementalInfoError) {
            case WAIT_TIMEOUT:
            case NO_VALID_CODE:
            case ABORTED:
                return new PaymentAuthorizationCancelledByUserException(supplementalInfoException);
            case UNKNOWN:
                return new PaymentAuthorizationException(supplementalInfoException);
            default:
                return null;
        }
    }

    private static PaymentException mapThirdPartyAppException(
            ThirdPartyAppException thirdPartyAppException) {
        ThirdPartyAppError thirdPartyAppError = thirdPartyAppException.getError();
        switch (thirdPartyAppError) {
            case CANCELLED:
                return new PaymentAuthorizationCancelledByUserException(thirdPartyAppException);
            case TIMED_OUT:
                return new PaymentAuthorizationTimeOutException(thirdPartyAppException);
            case ALREADY_IN_PROGRESS:
            case AUTHENTICATION_ERROR:
                return new PaymentAuthenticationException(thirdPartyAppException);
            default:
                return null;
        }
    }
}
