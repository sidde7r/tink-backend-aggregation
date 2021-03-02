package se.tink.backend.aggregation.workers.commands.login.handler;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.BankApiErrorVisitor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.FetchDataError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthorizationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankIdErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankServiceEroroResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResultVisitor;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.libraries.i18n.Catalog;
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
        updateStatus(
                CredentialsStatus.AUTHENTICATION_ERROR, authenticationErrorResult.getException());
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

    private void updateStatus(
            final CredentialsStatus credentialsStatus, final Exception exception) {

        ConnectivityError error = ConnectivityErrorFactory.from(exception);
        String statusPayload = null;

        if (exception instanceof AgentException) {
            statusPayload = catalog.getString(((AgentException) exception).getUserMessage());
        }

        metricRegistry
                .meter(
                        EXCEPTION_TO_ERROR_MAPPING
                                .label("exception", exception.getClass().getSimpleName())
                                .label("error", error.getType().toString()))
                .inc();
        LOGGER.info(
                "[Login Result debugging]: Mapping exception {} to {}",
                exception.getClass().getSimpleName(),
                error.getType().toString(),
                exception);

        statusUpdater.updateStatusWithError(credentialsStatus, statusPayload, error);
    }
}
