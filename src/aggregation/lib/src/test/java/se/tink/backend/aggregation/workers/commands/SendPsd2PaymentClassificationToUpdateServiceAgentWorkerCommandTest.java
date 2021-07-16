package se.tink.backend.aggregation.workers.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.Psd2PaymentAccountClassifier;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.user.rpc.User;

public class SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommandTest {
    private AgentWorkerCommandContext context;
    private MetricAction metricAction;
    private SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand command;
    private ControllerWrapper controllerWrapper;

    @Before
    public void setUp() {
        setUpContext();

        AgentWorkerCommandMetricState metrics = Mockito.mock(AgentWorkerCommandMetricState.class);
        when(metrics.init(Mockito.any())).thenReturn(metrics);

        metricAction = Mockito.mock(MetricAction.class);
        when(metrics.buildAction(
                        Mockito.eq(
                                new MetricId.MetricLabels()
                                        .add("action", "send_psd2_account_classification"))))
                .thenReturn(metricAction);

        controllerWrapper = Mockito.mock(ControllerWrapper.class);

        command =
                new SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand(
                        context,
                        metrics,
                        Psd2PaymentAccountClassifier.create(),
                        controllerWrapper,
                        true);
    }

    private void setUpContext() {
        context = Mockito.mock(AgentWorkerCommandContext.class);
        Provider provider = new Provider();
        provider.setMarket("SE");
        CredentialsRequest request =
                RefreshInformationRequest.builder()
                        .provider(provider)
                        .user(new User())
                        .credentials(new Credentials())
                        .build();
        when(context.getRequest()).thenReturn(request);
        when(context.getAppId()).thenReturn("appId");
        AccountDataCache accountDataCache = getAccountDataCache();
        when(context.getAccountDataCache()).thenReturn(accountDataCache);
    }

    private AccountDataCache getAccountDataCache() {
        AccountDataCache accountDataCache = new AccountDataCache();
        Account account1 = new Account();
        account1.setBankId("id1");
        accountDataCache.cacheAccount(account1);
        accountDataCache.setProcessedTinkAccountId("id1", "id1");
        Account account2 = new Account();
        account2.setBankId("id2");
        accountDataCache.cacheAccount(account2);
        accountDataCache.setProcessedTinkAccountId("id2", "id2");
        return accountDataCache;
    }

    @Test
    public void commandReturnContinueWhenContextThrowException() throws Exception {
        // given
        when(context.getAccountDataCache()).thenThrow(new RuntimeException("booom"));
        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(metricAction, Mockito.times(1)).failed();
    }

    @Test
    public void commandCompletedWhenContextSuccessful() throws Exception {
        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(metricAction, Mockito.times(1)).completed();
    }

    @Test
    public void shouldSendUpsertForEachAccount() throws Exception {
        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(controllerWrapper, Mockito.times(2)).upsertRegulatoryClassification(any());
    }
}
