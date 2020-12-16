package se.tink.backend.aggregation.workers.commands.login;

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
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;

public class LoginMetricLoginResultVisitor implements LoginResultVisitor {

    private final MetricActionIface loginMetric;

    public LoginMetricLoginResultVisitor(final MetricActionIface loginMetric) {
        this.loginMetric = loginMetric;
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
        loginMetric.cancelled();
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
        loginMetric.failed();
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
                                loginMetric.cancelled();
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
}
