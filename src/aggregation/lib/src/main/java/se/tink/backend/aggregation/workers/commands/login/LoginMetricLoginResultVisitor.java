package se.tink.backend.aggregation.workers.commands.login;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.BankApiErrorVisitor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.FetchDataError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.eidassigner.QsealcSignerException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.ConnectivityExceptionErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthorizationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankIdErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankServiceEroroResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResultVisitor;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;

public class LoginMetricLoginResultVisitor implements LoginResultVisitor {

    private static final Set<Pair<Class<? extends Exception>, String>>
            TINK_INFRASTRUCTURE_FAILURE_ERRORS = new HashSet<>();

    static {
        TINK_INFRASTRUCTURE_FAILURE_ERRORS.add(
                new ImmutablePair(
                        QsealcSignerException.class,
                        "IOException when requesting QSealC signature"));
        TINK_INFRASTRUCTURE_FAILURE_ERRORS.add(
                new ImmutablePair(
                        HttpClientException.class, "Remote host terminated the handshake"));
        TINK_INFRASTRUCTURE_FAILURE_ERRORS.add(
                new ImmutablePair(HttpClientException.class, "Connection reset"));
        TINK_INFRASTRUCTURE_FAILURE_ERRORS.add(
                new ImmutablePair(
                        HttpClientException.class, "Connect to tink-integration-eidas-proxy"));
        TINK_INFRASTRUCTURE_FAILURE_ERRORS.add(
                new ImmutablePair<>(HttpClientException.class, "readHandshakeRecord"));
    }

    private final MetricActionIface loginMetric;
    private Credentials credentials;

    public LoginMetricLoginResultVisitor(
            final MetricActionIface loginMetric, Credentials credentials) {
        this.loginMetric = loginMetric;
        this.credentials = credentials;
    }

    @Override
    public void visit(LoginSuccessResult successResult) {
        loginMetric.completed();
    }

    @Override
    public void visit(LoginAuthorizationErrorResult authorizationErrorResult) {
        loginMetric.cancelled();
    }

    @Override
    public void visit(LoginAuthenticationErrorResult authenticationErrorResult) {
        if (isThirdPartyAppTimeoutError(authenticationErrorResult)) {
            loginMetric.cancelledDueToThirdPartyAppTimeout();
        } else {
            loginMetric.cancelled();
        }
    }

    private boolean isThirdPartyAppTimeoutError(LoginAuthenticationErrorResult errorResult) {
        return credentials.getStatus() == CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION
                && errorResult.getException() instanceof ThirdPartyAppException
                && ((ThirdPartyAppException) errorResult.getException()).getError()
                        == ThirdPartyAppError.TIMED_OUT;
    }

    @Override
    public void visit(LoginBankIdErrorResult bankIdErrorResult) {
        loginMetric.cancelled();
    }

    @Override
    public void visit(LoginBankServiceEroroResult bankServiceErrorResult) {
        loginMetric.unavailable();
    }

    @Override
    public void visit(LoginUnknownErrorResult unknownErrorResult) {
        if (isTinkInfrastructureFailure(unknownErrorResult)) {
            loginMetric.failedDueToTinkInfrastructureFailure();
        } else {
            loginMetric.failed();
        }
    }

    @Override
    public void visit(AgentPlatformLoginErrorResult loginErrorResult) {
        loginErrorResult
                .getException()
                .getSourceAgentPlatformError()
                .accept(
                        new BankApiErrorVisitor<Void>() {
                            @Override
                            public Void visit(AuthenticationError error) {
                                if (isThirdPartyAppTimeoutError(error)) {
                                    loginMetric.cancelledDueToThirdPartyAppTimeout();
                                } else {
                                    loginMetric.cancelled();
                                }
                                return null;
                            }

                            @Override
                            public Void visit(AuthorizationError error) {
                                loginMetric.cancelled();
                                return null;
                            }

                            @Override
                            public Void visit(ServerError error) {
                                loginMetric.unavailable();
                                return null;
                            }

                            @Override
                            public Void visit(FetchDataError error) {
                                throw new IllegalStateException(
                                        "Fetching data error is not allowed during authentication");
                            }
                        });
    }

    private boolean isThirdPartyAppTimeoutError(AuthenticationError error) {
        return credentials.getStatus() == CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION
                && error instanceof NoUserInteractionResponseError;
    }

    private boolean isTinkInfrastructureFailure(LoginUnknownErrorResult loginUnknownErrorResult) {
        Exception exception = loginUnknownErrorResult.getException();
        return TINK_INFRASTRUCTURE_FAILURE_ERRORS.stream()
                .anyMatch(
                        (entry) ->
                                entry.getKey().isAssignableFrom(exception.getClass())
                                        && exception.getMessage().contains(entry.getValue()));
    }

    @Override
    public void visit(ConnectivityExceptionErrorResult connectivityExceptionErrorResult) {
        ConnectivityException connectivityException =
                connectivityExceptionErrorResult.getException();
        switch (connectivityException.getError().getType()) {
            case USER_LOGIN_ERROR:
            case AUTHORIZATION_ERROR:
                if (isThirdPartyAppTimeoutError(connectivityException)) {
                    loginMetric.cancelledDueToThirdPartyAppTimeout();
                } else {
                    loginMetric.cancelled();
                }
                break;
            case PROVIDER_ERROR:
                loginMetric.unavailable();
                break;
            default:
                loginMetric.failed();
        }
    }

    private boolean isThirdPartyAppTimeoutError(ConnectivityException connectivityException) {
        return credentials.getStatus() == CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION
                && connectivityException.getError().getType()
                        == ConnectivityErrorType.USER_LOGIN_ERROR
                && ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_FLOW_TIMEOUT
                        .name()
                        .equals(connectivityException.getError().getDetails().getReason());
    }
}
