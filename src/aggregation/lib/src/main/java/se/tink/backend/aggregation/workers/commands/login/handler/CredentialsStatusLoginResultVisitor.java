package se.tink.backend.aggregation.workers.commands.login.handler;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.BankApiErrorVisitor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.FetchDataError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.ConnectivityExceptionErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthorizationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankIdErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankServiceEroroResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResultVisitor;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import src.libraries.connectivity_errors.ConnectivityErrorFactory;

@AllArgsConstructor
public class CredentialsStatusLoginResultVisitor implements LoginResultVisitor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CredentialsStatusLoginResultVisitor.class);
    private static final MetricId EXCEPTION_TO_ERROR_MAPPING =
            MetricId.newId("aggregation_exception_to_error_mapping");

    private final MetricRegistry metricRegistry;
    private final StatusUpdater statusUpdater;
    private final Catalog catalog;

    @Override
    public void visit(LoginSuccessResult successResult) {
        // no update on credentials is needed in case of success login
    }

    @Override
    public void visit(LoginAuthorizationErrorResult authorizationErrorResult) {
        updateStatus(
                CredentialsStatus.AUTHENTICATION_ERROR, authorizationErrorResult.getException());
    }

    @Override
    public void visit(LoginAuthenticationErrorResult authenticationErrorResult) {
        if (isAborted(authenticationErrorResult)) {
            updateStatus(CredentialsStatus.UNCHANGED, authenticationErrorResult.getException());
        } else {
            updateStatus(
                    CredentialsStatus.AUTHENTICATION_ERROR,
                    authenticationErrorResult.getException());
        }
    }

    @Override
    public void visit(LoginBankIdErrorResult bankIdErrorResult) {
        updateStatus(CredentialsStatus.AUTHENTICATION_ERROR, bankIdErrorResult.getException());
    }

    @Override
    public void visit(LoginBankServiceEroroResult bankServiceErrorResult) {
        updateStatus(CredentialsStatus.TEMPORARY_ERROR, bankServiceErrorResult.getException());
    }

    @Override
    public void visit(LoginUnknownErrorResult unknownErrorResult) {
        updateStatus(CredentialsStatus.TEMPORARY_ERROR, unknownErrorResult.getException());
    }

    @Override
    public void visit(AgentPlatformLoginErrorResult loginErrorResult) {
        CredentialsStatus credentialsStatus =
                loginErrorResult
                        .getException()
                        .getSourceAgentPlatformError()
                        .accept(
                                new BankApiErrorVisitor<CredentialsStatus>() {
                                    @Override
                                    public CredentialsStatus visit(AuthenticationError error) {
                                        return CredentialsStatus.AUTHENTICATION_ERROR;
                                    }

                                    @Override
                                    public CredentialsStatus visit(AuthorizationError error) {
                                        return CredentialsStatus.AUTHENTICATION_ERROR;
                                    }

                                    @Override
                                    public CredentialsStatus visit(ServerError error) {
                                        return CredentialsStatus.TEMPORARY_ERROR;
                                    }

                                    @Override
                                    public CredentialsStatus visit(FetchDataError error) {
                                        return null;
                                    }
                                });
        updateStatus(credentialsStatus, loginErrorResult.getException());
    }

    @Override
    public void visit(ConnectivityExceptionErrorResult connectivityExceptionErrorResult) {
        switch (connectivityExceptionErrorResult.getException().getError().getType()) {
            case USER_LOGIN_ERROR:
            case AUTHORIZATION_ERROR:
                updateStatus(
                        CredentialsStatus.AUTHENTICATION_ERROR,
                        connectivityExceptionErrorResult.getException());
                break;
            default:
                updateStatus(
                        CredentialsStatus.TEMPORARY_ERROR,
                        connectivityExceptionErrorResult.getException());
        }
    }

    private void updateStatus(
            final CredentialsStatus credentialsStatus, final Exception exception) {
        ConnectivityError error =
                exception instanceof ConnectivityException
                        ? ((ConnectivityException) exception).getError()
                        : ConnectivityErrorFactory.fromLegacy(exception);
        String statusPayload = null;
        String exceptionError = "N/A";

        if (exception instanceof AgentException) {
            AgentException agentException = (AgentException) exception;
            statusPayload = catalog.getString(agentException.getUserMessage());
            exceptionError = agentException.getError().name();
        } else if (exception instanceof ConnectivityException) {
            ConnectivityException connectivityException = (ConnectivityException) exception;
            statusPayload = catalog.getString(connectivityException.getUserMessage());
            exceptionError =
                    connectivityException.getError().getType().name()
                            + ": "
                            + connectivityException.getError().getDetails().getReason();
        }

        metricRegistry
                .meter(
                        EXCEPTION_TO_ERROR_MAPPING
                                .label("exception", exception.getClass().getSimpleName())
                                .label("exception_error", exceptionError)
                                .label("type", error.getType().toString())
                                .label("reason", error.getDetails().getReason()))
                .inc();
        LOGGER.info(
                "[Login Result debugging]: Mapping exception {}: {} to {}: {}",
                exception.getClass().getSimpleName(),
                exceptionError,
                error.getType().toString(),
                error.getDetails().getReason(),
                exception);

        statusUpdater.updateStatusWithError(credentialsStatus, statusPayload, error);
    }

    private static boolean isAborted(LoginAuthenticationErrorResult authenticationErrorResult) {
        if (authenticationErrorResult.getException() instanceof SupplementalInfoException) {
            SupplementalInfoException exception =
                    (SupplementalInfoException) authenticationErrorResult.getException();
            return SupplementalInfoError.ABORTED.equals(exception.getError());
        }
        return false;
    }
}
