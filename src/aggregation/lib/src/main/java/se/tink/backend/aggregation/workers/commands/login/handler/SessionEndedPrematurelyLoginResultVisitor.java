package se.tink.backend.aggregation.workers.commands.login.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.BankApiErrorVisitor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.FetchDataError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthorizationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankIdErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankServiceEroroResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResultVisitor;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

@Slf4j
@RequiredArgsConstructor
public class SessionEndedPrematurelyLoginResultVisitor implements LoginResultVisitor {

    private final MetricRegistry metricRegistry;
    private final CredentialsRequest credentialsRequest;
    private final LocalDateTimeSource dateTimeSource;

    static final MetricId metric = MetricId.newId("session_expired_before_expected_date");
    private static final List<Integer> BUCKETS =
            Arrays.asList(0, 1, 5, 10, 30, 50, 70, 80, 85, 89, 90);

    @Override
    public void visit(LoginAuthenticationErrorResult authenticationErrorResult) {
        if (authenticationErrorResult.getException().getError() instanceof SessionError) {
            writeToMetric();
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
                                if (error instanceof SessionExpiredError) {
                                    writeToMetric();
                                }
                                return null;
                            }

                            @Override
                            public Void visit(AuthorizationError error) {
                                return null;
                            }

                            @Override
                            public Void visit(ServerError error) {
                                return null;
                            }

                            @Override
                            public Void visit(FetchDataError error) {
                                return null;
                            }
                        });
    }

    private void writeToMetric() {
        // Only when sessionExpiryDate exists
        Credentials credentials = credentialsRequest.getCredentials();
        Date sessionExpiryDate = credentialsRequest.getCredentials().getSessionExpiryDate();
        if (sessionExpiryDate == null) {
            return;
        }

        LocalDate sessionExpInLocalDate =
                sessionExpiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = dateTimeSource.now().toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(today, sessionExpInLocalDate);

        Provider provider = credentialsRequest.getProvider();

        metricRegistry
                .histogram(
                        metric.label("provider_name", provider.getName())
                                .label("provider_type", provider.getMetricTypeName())
                                .label("provider_access_type", provider.getAccessType().name())
                                .label("market", provider.getMarket())
                                .label("className", provider.getClassName())
                                .label("credential", credentials.getMetricTypeName())
                                .label("request_type", credentialsRequest.getType().name()),
                        BUCKETS)
                .update(daysBetween);
    }

    @Override
    public void visit(LoginSuccessResult successResult) {
        // We only care about auth, session excepion.
    }

    @Override
    public void visit(LoginAuthorizationErrorResult authorizationErrorResult) {
        // We only care about auth, session excepion.
    }

    @Override
    public void visit(LoginBankIdErrorResult bankIdErrorResult) {
        // We only care about auth, session excepion.
    }

    @Override
    public void visit(LoginBankServiceEroroResult bankServiceErrorResult) {
        // We only care about auth, session excepion.
    }

    @Override
    public void visit(LoginUnknownErrorResult unknownErrorResult) {
        // We only care about auth, session excepion.
    }
}
