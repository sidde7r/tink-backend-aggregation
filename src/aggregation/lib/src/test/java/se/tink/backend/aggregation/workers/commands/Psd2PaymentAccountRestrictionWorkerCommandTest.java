package se.tink.backend.aggregation.workers.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.Psd2PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.regulatory_restrictions.RegulatoryRestrictions;
import se.tink.backend.aggregation.compliance.regulatory_restrictions.RegulatoryRestrictionsMetrics;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.user.rpc.User;

public class Psd2PaymentAccountRestrictionWorkerCommandTest {
    private AgentWorkerCommandContext context;
    private Psd2PaymentAccountRestrictionWorkerCommand command;
    private ControllerWrapper controllerWrapper;
    private static final RegulatoryRestrictionsMetrics regulatoryRestrictionsMetrics =
            new RegulatoryRestrictionsMetrics(new MetricRegistry());

    @Test
    public void shouldNotRestrictAccountsWhenNoDataFetchingRestrictions() throws Exception {
        // given
        setUp(Collections.emptyList());

        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(controllerWrapper, Mockito.times(0)).restrictAccounts(any());
    }

    @Test
    public void shouldRestrictAccountsWhenAccountsToRestrictAndDataFetchingRestrictionExist()
            throws Exception {
        // given
        setUp(
                Collections.singletonList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_PSD2_PAYMENT_ACCOUNTS));

        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(controllerWrapper, Mockito.times(1)).restrictAccounts(any());
    }

    private void setUp(List<DataFetchingRestrictions> dataFetchingRestrictions) {

        CredentialsRequest request = prepareCredentialsRequest(dataFetchingRestrictions);
        setUpContext(request);
        controllerWrapper = mock(ControllerWrapper.class);

        command =
                new Psd2PaymentAccountRestrictionWorkerCommand(
                        context,
                        request,
                        new RegulatoryRestrictions(regulatoryRestrictionsMetrics),
                        Psd2PaymentAccountClassifier.create(),
                        mock(AccountInformationServiceEventsProducer.class),
                        controllerWrapper);
    }

    private CredentialsRequest prepareCredentialsRequest(
            List<DataFetchingRestrictions> dataFetchingRestrictions) {
        Provider provider = new Provider();
        provider.setMarket("SE");
        CredentialsRequest request =
                RefreshInformationRequest.builder()
                        .provider(provider)
                        .user(new User())
                        .credentials(new Credentials())
                        .build();
        request.setDataFetchingRestrictions(dataFetchingRestrictions);
        return request;
    }

    private void setUpContext(CredentialsRequest request) {
        context = mock(AgentWorkerCommandContext.class);
        Provider provider = new Provider();
        provider.setMarket("SE");
        when(context.getRequest()).thenReturn(request);
        when(context.getAppId()).thenReturn("appId");
        AccountDataCache accountDataCache = getAccountDataCache();
        when(context.getAccountDataCache()).thenReturn(accountDataCache);
    }

    private AccountDataCache getAccountDataCache() {
        AccountDataCache accountDataCache = new AccountDataCache();
        Account account1 = new Account();
        account1.setBankId("id1");
        account1.setType(AccountTypes.CHECKING);
        accountDataCache.cacheAccount(account1);
        accountDataCache.setProcessedTinkAccountId("id1", "id1");
        Account account2 = new Account();
        account2.setBankId("id2");
        account2.setType(AccountTypes.CHECKING);
        accountDataCache.cacheAccount(account2);
        accountDataCache.setProcessedTinkAccountId("id2", "id2");
        return accountDataCache;
    }
}
