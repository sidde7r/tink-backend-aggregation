package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.error;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;

@Slf4j
class ArgentaErrorResponseHandler {

    private static final Map<String, ExceptionHandler> EXCEPTIONS_PROVIDERS =
            ImmutableMap.<String, ExceptionHandler>builder()
                    .put(
                            ErrorResponse.AUTHENTICATION,
                            errorResponse -> {
                                throw LoginError.INCORRECT_CREDENTIALS.exception();
                            })
                    .put(ErrorResponse.ERROR_CODE_SBB, new ExceptionHandler.SbbExceptionHandler())
                    .put(
                            ErrorResponse.ERROR_INVALID_REQUEST,
                            errorResponse -> {
                                // happens when app version is too old
                                throw BankServiceError.BANK_SIDE_FAILURE.exception();
                            })
                    .build();

    static void handleKnownErrorResponses(ArgentaErrorResponse argentaErrorResponse) {
        String errorCode = argentaErrorResponse.getCode();
        if (!Strings.isNullOrEmpty(errorCode)) {
            String value = errorCode.toLowerCase();
            EXCEPTIONS_PROVIDERS.keySet().stream()
                    .filter(value::startsWith)
                    .findFirst()
                    .ifPresent(
                            errorResponseKey -> {
                                log.info(
                                        "{} Error response code: {}",
                                        LogTags.ARGENTA_LOG_TAG,
                                        errorCode);
                                EXCEPTIONS_PROVIDERS
                                        .get(errorResponseKey)
                                        .handle(argentaErrorResponse);
                            });
        }
    }

    interface ExceptionHandler {

        void handle(ArgentaErrorResponse argentaErrorResponse);

        class SbbExceptionHandler implements ExceptionHandler {

            private static final Map<String, AgentException> ERROR_MESSAGE_TO_EXCEPTION_MAP =
                    ImmutableMap.<String, AgentException>builder()
                            .put(
                                    ErrorResponse.TOO_MANY_DEVICES,
                                    LoginError.REGISTER_DEVICE_ERROR.exception())
                            .put(
                                    ErrorResponse.AUTHENTICATION_ERROR,
                                    LoginError.INCORRECT_CREDENTIALS.exception())
                            .put(
                                    ErrorResponse.TOO_MANY_ATTEMPTS,
                                    LoginError.INCORRECT_CHALLENGE_RESPONSE.exception())
                            .put(
                                    ErrorResponse.ACCOUNT_BLOCKED,
                                    AuthorizationError.ACCOUNT_BLOCKED.exception())
                            .put(
                                    ErrorResponse.PROBLEM_SOLVING_IN_PROGRESS,
                                    BankServiceError.BANK_SIDE_FAILURE.exception())
                            .put(
                                    ErrorResponse.SOMETHING_WRONG,
                                    BankServiceError.BANK_SIDE_FAILURE.exception())
                            .build();

            @Override
            public void handle(ArgentaErrorResponse argentaErrorResponse) {
                String errorMessage = getErrorMessage(argentaErrorResponse);
                if (!Strings.isNullOrEmpty(errorMessage)) {
                    handleKnownErrorMessages(errorMessage.toLowerCase());
                }
            }

            private void handleKnownErrorMessages(String errorMessage) {
                ERROR_MESSAGE_TO_EXCEPTION_MAP.keySet().stream()
                        .filter(errorMessage::contains)
                        .findFirst()
                        .ifPresent(
                                errorResponseKey -> {
                                    log.info(
                                            "{} Error response message: {}",
                                            LogTags.ARGENTA_LOG_TAG,
                                            errorMessage);
                                    throw ERROR_MESSAGE_TO_EXCEPTION_MAP.get(errorResponseKey);
                                });
            }

            private String getErrorMessage(ArgentaErrorResponse argentaErrorResponse) {
                if (argentaErrorResponse.getFieldErrors() != null
                        && argentaErrorResponse.getFieldErrors().size() > 0) {
                    return argentaErrorResponse.getFieldErrors().get(0).getMessage();
                }
                return argentaErrorResponse.getMessage();
            }
        }
    }
}
