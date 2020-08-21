package se.tink.backend.aggregation.workers.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
        setUp(Collections.emptyList(), getPaymentAccounts());

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
                        DataFetchingRestrictions.RESTRICT_FETCHING_PSD2_PAYMENT_ACCOUNTS),
                getPaymentAccounts());

        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(controllerWrapper, Mockito.times(1)).restrictAccounts(any());
    }

    @Test
    public void
            shouldNotRestrictAccountsWhenAccountsAreUndeterminedAndFetchingRestrictionsDoNotRestrictUndetermined()
                    throws Exception {
        // given
        setUp(
                Collections.singletonList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_PSD2_PAYMENT_ACCOUNTS),
                getUndeterminedAccounts());

        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(controllerWrapper, Mockito.times(0)).restrictAccounts(any());
    }

    @Test
    public void
            shouldRestrictAccountsWhenAccountsAreUndeterminedAndFetchingRestrictionsRequireToRestrictUndetermined()
                    throws Exception {
        // given
        setUp(
                Arrays.asList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_PSD2_PAYMENT_ACCOUNTS,
                        DataFetchingRestrictions
                                .RESTRICT_FETCHING_PSD2_UNDETERMINED_PAYMENT_ACCOUNTS),
                getUndeterminedAccounts());

        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(controllerWrapper, Mockito.times(1)).restrictAccounts(any());
    }

    private void setUp(
            List<DataFetchingRestrictions> dataFetchingRestrictions, List<Account> accounts) {

        CredentialsRequest request = prepareCredentialsRequest(dataFetchingRestrictions);
        setUpContext(request, accounts);
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

    private void setUpContext(CredentialsRequest request, List<Account> accounts) {
        context = mock(AgentWorkerCommandContext.class);
        Provider provider = new Provider();
        provider.setMarket("SE");
        when(context.getRequest()).thenReturn(request);
        when(context.getAppId()).thenReturn("appId");
        AccountDataCache accountDataCache = prepareAccountDataCache(accounts);
        when(context.getAccountDataCache()).thenReturn(accountDataCache);
    }

    private AccountDataCache prepareAccountDataCache(List<Account> accounts) {
        AccountDataCache accountDataCache = new AccountDataCache();
        accounts.forEach(
                account -> {
                    accountDataCache.cacheAccount(account);
                    accountDataCache.setProcessedTinkAccountId(
                            account.getBankId(), account.getBankId());
                });
        return accountDataCache;
    }

    private List<Account> getUndeterminedAccounts() {
        // it's safe to assume that accounts of type OTHER will be categorized as Undetermined
        // Payment Accounts
        Account account1 = new Account();
        account1.setBankId("id1");
        account1.setType(AccountTypes.OTHER);
        Account account2 = new Account();
        account2.setBankId("id2");
        account2.setType(AccountTypes.OTHER);
        return Arrays.asList(account1, account2);
    }

    private List<Account> getPaymentAccounts() {
        Account account1 = new Account();
        account1.setBankId("id1");
        account1.setType(AccountTypes.CHECKING);
        Account account2 = new Account();
        account2.setBankId("id2");
        account2.setType(AccountTypes.CHECKING);
        return Arrays.asList(account1, account2);
    }
}
