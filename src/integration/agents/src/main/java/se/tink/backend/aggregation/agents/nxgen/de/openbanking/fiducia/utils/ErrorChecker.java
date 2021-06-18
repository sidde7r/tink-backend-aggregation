package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ErrorChecker {

    static Map<String, RuntimeException> exceptionMap = new HashMap<>();

    static {
        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.ERROR_KONF,
                LoginError.NOT_CUSTOMER.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.BANK_NO_LONGER_AVAILABLE_MESSAGE));

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.TAN_PLUS_BLOCKED,
                LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.BLOCKED_TAN_MESSAGE));

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.ONLINE_ACCESS_BLOCKED,
                AuthorizationError.ACCOUNT_BLOCKED.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.BLOCKED_ACCESS_MESSAGE));

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.PIN_CHANGE_REQUIRED,
                LoginError.PASSWORD_CHANGE_REQUIRED.exception());

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.NO_ACCOUNT_AVAILABLE,
                AuthorizationError.UNAUTHORIZED.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.UNAVAILABLE_ACCOUNT_MESSAGE));

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.ORDER_NOT_PROCESSED_OR_REJECTED,
                LoginError.DEFAULT_MESSAGE.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.ORDER_NOT_PROCESSED_MESSAGE));

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.PHONE_TAN_BLOCKED,
                LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.PHONE_TAN_BLOCKED_MESSAGE));

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.PSU_CREDENTIALS_INVALID,
                LoginError.INCORRECT_CREDENTIALS.exception());

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.SECURE_GO_BLOCKED,
                LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.SECURE_GO_BLOCKED_MESSAGE));

        exceptionMap.put(
                FiduciaConstants.ErrorMessageKeys.TAN_NOT_VALID,
                LoginError.NO_AVAILABLE_SCA_METHODS.exception(
                        FiduciaConstants.EndUserErrorMessageKeys.TAN_NOT_VALID_MESSAGE));
    }

    public static RuntimeException errorChecker(HttpResponseException httpResponseException) {
        String errorMessage = httpResponseException.getResponse().getBody(String.class);

        return exceptionMap.entrySet().stream()
                .filter(entry -> StringUtils.containsIgnoreCase(errorMessage, entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(httpResponseException);
    }
}
