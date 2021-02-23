package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;

@Slf4j
public class AuthenticationExceptionHandler {

    private static final Map<String, AuthenticationExceptionMapper> EXCEPTIONS_MAPPERS =
            ImmutableMap.<String, AuthenticationExceptionMapper>builder()
                    .put(
                            LoginException.class.getSimpleName(),
                            new AuthenticationExceptionMapper.LoginExceptionMapper())
                    .put(
                            NemIdException.class.getSimpleName(),
                            new AuthenticationExceptionMapper.NemIdExceptionMapper())
                    .put(
                            SupplementalInfoException.class.getSimpleName(),
                            new AuthenticationExceptionMapper.SupplementalInfoExceptionMapper())
                    .build();

    public static AgentBankApiError toError(AuthenticationException e) {
        log.error(e.getMessage());
        return Optional.ofNullable(EXCEPTIONS_MAPPERS.get(e.getClass().getSimpleName()))
                .map(mapper -> mapper.toAgentBankApiError(e))
                .orElse(new AuthorizationError());
    }

    public static AgentBankApiError toErrorFromBankServiceException(BankServiceException e) {
        return new ServerError(
                getErrorWithOriginalUserMessage(AgentError.HTTP_RESPONSE_ERROR.getCode(), e));
    }

    public interface AuthenticationExceptionMapper {

        AgentBankApiError toAgentBankApiError(AuthenticationException e);

        class LoginExceptionMapper implements AuthenticationExceptionMapper {

            @Override
            public AgentBankApiError toAgentBankApiError(AuthenticationException e) {
                LoginException loginException = (LoginException) e;
                switch (loginException.getError()) {
                    case CREDENTIALS_VERIFICATION_ERROR:
                        return new AuthorizationError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.GENERAL_AUTHORIZATION_ERROR.getCode(), e));
                    case INCORRECT_CREDENTIALS:
                        return new InvalidCredentialsError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.INVALID_CREDENTIALS.getCode(), e));
                    case NOT_CUSTOMER:
                        return new ThirdPartyAppNoClientError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.INVALID_CREDENTIALS.getCode(), e));
                    default:
                        return new ThirdPartyAppUnknownError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(), e));
                }
            }
        }

        class NemIdExceptionMapper implements AuthenticationExceptionMapper {

            @Override
            public AgentBankApiError toAgentBankApiError(AuthenticationException e) {
                NemIdException nemIdException = (NemIdException) e;
                switch (nemIdException.getError()) {
                    case REJECTED:
                        return new ThirdPartyAppCancelledError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.THIRD_PARTY_APP_CANCELLED.getCode(), e));
                    case INTERRUPTED:
                    case NEMID_LOCKED:
                    case NEMID_BLOCKED:
                        return new AuthenticationError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(), e));
                    case INVALID_CODE_CARD_CODE:
                    case USE_NEW_CODE_CARD:
                    case INVALID_CODE_TOKEN_CODE:
                        return new InvalidCredentialsError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.INVALID_CREDENTIALS.getCode(), e));
                    case TIMEOUT:
                        return new ThirdPartyAppTimedOutError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.THIRD_PARTY_APP_TIMEOUT.getCode(), e));
                    default:
                        return new ThirdPartyAppUnknownError(
                                getErrorWithOriginalUserMessage(
                                        AgentError.THIRD_PARTY_APP_UNKNOWN_ERROR.getCode(), e));
                }
            }
        }

        class SupplementalInfoExceptionMapper implements AuthenticationExceptionMapper {

            @Override
            public AgentBankApiError toAgentBankApiError(AuthenticationException e) {
                SupplementalInfoException supplementalInfoException = (SupplementalInfoException) e;
                if (supplementalInfoException.getError() == SupplementalInfoError.WAIT_TIMEOUT) {
                    return new NoUserInteractionResponseError();
                }
                throw e;
            }
        }
    }

    private static Error getErrorWithOriginalUserMessage(String errorCode, AgentException e) {
        return Error.builder()
                .uniqueId(UUID.randomUUID().toString())
                .errorCode(errorCode)
                .errorMessage(e.getUserMessage().get())
                .build();
    }
}
