package se.tink.backend.aggregation.workers.commands.login;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agentsplatform.framework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.framework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.framework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.framework.error.BankApiErrorVisitor;
import se.tink.backend.aggregation.agentsplatform.framework.error.FetchDataError;
import se.tink.backend.aggregation.agentsplatform.framework.error.IncorrectCardReaderResponseCodeError;
import se.tink.backend.aggregation.agentsplatform.framework.error.IncorrectOtpError;
import se.tink.backend.aggregation.agentsplatform.framework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.framework.error.InvalidRequestError;
import se.tink.backend.aggregation.agentsplatform.framework.error.InvalidScopeError;
import se.tink.backend.aggregation.agentsplatform.framework.error.RefreshTokenFailureError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ServerTemporaryUnavailableError;
import se.tink.backend.aggregation.agentsplatform.framework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ThirdPartyAppAlreadyInProgressError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ThirdPartyAppCancelledError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ThirdPartyAppNoClientError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ThirdPartyAppTimedOutError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ThirdPartyAppUnknownError;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;

@AllArgsConstructor
class AgentPlatformAuthenticationProcessExceptionBankApiErrorVisitor
        implements BankApiErrorVisitor<CredentialsStatus> {

    private static final Map<
                    Class<? extends ServerError>,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            SERVER_ERROR_TO_LOGIN_RESULT_MAPPING =
                    ImmutableMap
                            .<Class<? extends ServerError>,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    InvalidRequestError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_UNKNOWN)
                            .put(
                                    ServerTemporaryUnavailableError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_NO_BANK_SERVICE)
                            .build();

    private static final Map<
                    Class<? extends AuthenticationError>,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            AUTHENTICATION_ERROR_TO_LOGIN_RESULT_MAPPING =
                    ImmutableMap
                            .<Class<? extends AuthenticationError>,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    AccountBlockedError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                            .put(
                                    IncorrectCardReaderResponseCodeError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CHALLENGE_RESPONSE)
                            .put(
                                    IncorrectOtpError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CHALLENGE_RESPONSE)
                            .put(
                                    InvalidCredentialsError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    RefreshTokenFailureError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_SESSION_TERMINATED)
                            .put(
                                    SessionExpiredError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_SESSION_EXPIRED)
                            .put(
                                    NoUserInteractionResponseError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SUPPLEMENTAL_INFO_ERROR_NO_VALID_CODE)
                            .build();

    private static final Map<
                    Class<? extends AuthorizationError>,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            AUTHORIZATION_ERROR_TO_LOGIN_RESULT_MAPPING =
                    ImmutableMap
                            .<Class<? extends AuthorizationError>,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    InvalidScopeError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_UNAUTHORIZED)
                            .put(
                                    ThirdPartyAppAlreadyInProgressError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_ALREADY_IN_PROGRESS)
                            .put(
                                    ThirdPartyAppCancelledError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_CANCELLED)
                            .put(
                                    ThirdPartyAppNoClientError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_NO_CLIENT)
                            .put(
                                    ThirdPartyAppTimedOutError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_TIMED_OUT)
                            .put(
                                    ThirdPartyAppUnknownError.class,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_UNKNOWN)
                            .build();

    private final MetricActionIface metricAction;
    private final AgentLoginEventPublisherService agentLoginEventPublisherService;

    @Override
    public CredentialsStatus visit(AuthenticationError error) {
        metricAction.cancelled();
        agentLoginEventPublisherService.publishLoginResultEvent(
                AUTHENTICATION_ERROR_TO_LOGIN_RESULT_MAPPING.getOrDefault(
                        error.getClass(),
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .LOGIN_ERROR_UNKNOWN));
        return CredentialsStatus.AUTHENTICATION_ERROR;
    }

    @Override
    public CredentialsStatus visit(AuthorizationError error) {
        metricAction.cancelled();
        agentLoginEventPublisherService.publishLoginResultEvent(
                AUTHORIZATION_ERROR_TO_LOGIN_RESULT_MAPPING.getOrDefault(
                        error.getClass(),
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .AUTHORIZATION_ERROR_UNKNOWN));
        return CredentialsStatus.AUTHENTICATION_ERROR;
    }

    @Override
    public CredentialsStatus visit(ServerError error) {
        metricAction.unavailable();
        agentLoginEventPublisherService.publishLoginResultEvent(
                SERVER_ERROR_TO_LOGIN_RESULT_MAPPING.getOrDefault(
                        error.getClass(),
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .BANK_SERVICE_ERROR_UNKNOWN));
        return CredentialsStatus.TEMPORARY_ERROR;
    }

    @Override
    public CredentialsStatus visit(FetchDataError error) {
        throw new IllegalStateException("Fetching data error is not allowed during authentication");
    }
}
