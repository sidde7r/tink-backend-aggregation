package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppUnknownError;
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
        return Optional.ofNullable(EXCEPTIONS_MAPPERS.get(e.getClass().getSimpleName()))
                .map(mapper -> mapper.toAgentBankApiError(e))
                .orElse(new AuthorizationError());
    }

    public interface AuthenticationExceptionMapper {

        AgentBankApiError toAgentBankApiError(AuthenticationException e);

        class LoginExceptionMapper implements AuthenticationExceptionMapper {

            @Override
            public AgentBankApiError toAgentBankApiError(AuthenticationException e) {
                LoginException loginException = (LoginException) e;
                switch (loginException.getError()) {
                    case CREDENTIALS_VERIFICATION_ERROR:
                        log.error(e.getMessage());
                        return new AuthorizationError();
                    case INCORRECT_CREDENTIALS:
                        log.error(e.getMessage());
                        return new InvalidCredentialsError();
                    case DEFAULT_MESSAGE:
                        log.error(e.getMessage());
                        return new ThirdPartyAppUnknownError();
                    default:
                        return new ThirdPartyAppUnknownError();
                }
            }
        }

        class NemIdExceptionMapper implements AuthenticationExceptionMapper {

            @Override
            public AgentBankApiError toAgentBankApiError(AuthenticationException e) {
                NemIdException nemIdException = (NemIdException) e;
                switch (nemIdException.getError()) {
                    case REJECTED:
                        log.error(e.getError().name());
                        return new ThirdPartyAppCancelledError();
                    case TIMEOUT:
                        log.error(e.getError().name());
                        return new ThirdPartyAppTimedOutError();
                    case CODE_TOKEN_NOT_SUPPORTED:
                        log.error(e.getMessage());
                        return new AuthorizationError();
                    default:
                        return new ThirdPartyAppUnknownError();
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
}
