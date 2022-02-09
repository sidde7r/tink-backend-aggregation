package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.payment.DuplicatePaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.ErrorMessageKeys;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class ErrorChecker {

    static Map<String, RuntimeException> exceptionMap = new HashMap<>();

    static {
        exceptionMap.put(
                ErrorMessageKeys.ERROR_KONF,
                LoginError.NOT_CUSTOMER.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.BANK_NO_LONGER_AVAILABLE_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.TAN_PLUS_BLOCKED,
                LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.BLOCKED_TAN_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.ONLINE_ACCESS_BLOCKED,
                AuthorizationError.ACCOUNT_BLOCKED.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.BLOCKED_ACCESS_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.PIN_CHANGE_REQUIRED,
                LoginError.PASSWORD_CHANGE_REQUIRED.exception());

        exceptionMap.put(
                ErrorMessageKeys.NO_ACCOUNT_AVAILABLE,
                AuthorizationError.UNAUTHORIZED.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.UNAVAILABLE_ACCOUNT_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.ORDER_NOT_PROCESSED_OR_REJECTED,
                LoginError.DEFAULT_MESSAGE.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.ORDER_NOT_PROCESSED_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.PHONE_TAN_BLOCKED,
                LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.PHONE_TAN_BLOCKED_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.PSU_CREDENTIALS_INVALID,
                LoginError.INCORRECT_CREDENTIALS.exception(
                        new LocalizableKey(
                                "Incorrect login credentials. Are you sure that you have selected a correct branch?")));

        exceptionMap.put(
                ErrorMessageKeys.SECURE_GO_BLOCKED,
                LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.SECURE_GO_BLOCKED_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.TAN_NOT_VALID,
                LoginError.NO_AVAILABLE_SCA_METHODS.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.TAN_NOT_VALID_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.ORDER_LIMIT_EXCEEDED,
                PaymentRejectedException.tooManyTransactions());

        exceptionMap.put(
                ErrorMessageKeys.ORDER_BLOCKED,
                LoginError.DEFAULT_MESSAGE.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.ORDER_NOT_PROCESSED_MESSAGE));

        exceptionMap.put(
                ErrorMessageKeys.ORDER_REJECTED,
                LoginError.DEFAULT_MESSAGE.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.ORDER_NOT_PROCESSED_MESSAGE));

        exceptionMap.put(ErrorMessageKeys.ORDER_DUPLICATED, new DuplicatePaymentException());

        exceptionMap.put(
                ErrorMessageKeys.NO_PAYMENT_AUTHORIZATION, new PaymentAuthorizationException());
    }

    public static RuntimeException mapError(HttpResponseException httpResponseException) {
        if (!hasNonEmptyResponseBody(httpResponseException)) {
            return new BankServiceException(
                    BankServiceError.BANK_SIDE_FAILURE, httpResponseException);
        }

        String errorMessage = httpResponseException.getResponse().getBody(String.class);

        if (toBeMappedWithThrowable(errorMessage)) {
            return getExceptionWithThrowable(httpResponseException);
        }

        return exceptionMap.entrySet().stream()
                .filter(entry -> StringUtils.containsIgnoreCase(errorMessage, entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(httpResponseException);
    }

    private static PaymentRejectedException getExceptionWithThrowable(HttpResponseException hre) {
        return new PaymentRejectedException(hre);
    }

    private static boolean toBeMappedWithThrowable(String errorMessage) {
        return StringUtils.containsIgnoreCase(errorMessage, ErrorMessageKeys.MISSING_COVERAGE)
                || StringUtils.containsIgnoreCase(errorMessage, ErrorMessageKeys.READ_TIME_OUT);
    }

    private static boolean hasNonEmptyResponseBody(HttpResponseException hre) {
        return Optional.ofNullable(hre.getResponse())
                .map(response -> response.getBody(String.class))
                .filter(body -> !body.isEmpty())
                .isPresent();
    }
}
