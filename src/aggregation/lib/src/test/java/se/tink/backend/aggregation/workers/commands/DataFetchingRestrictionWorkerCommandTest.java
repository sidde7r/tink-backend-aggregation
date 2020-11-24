package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

@RunWith(MockitoJUnitRunner.class)
public class DataFetchingRestrictionWorkerCommandTest {
    private AgentWorkerCommandContext context;
    private CredentialsRequest request;
    @Mock private ControllerWrapper controllerWrapper;
    @Mock private AgentsServiceConfiguration agentsServiceConfiguration;

    @Before
    public void init() {
        request = new RefreshInformationRequest();
        request.setCredentials(new Credentials());
        User user = getUser();
        request.setUser(user);
        Provider provider = getProvider();
        request.setProvider(provider);
        when(controllerWrapper.restrictAccounts(any())).thenReturn(Response.ok().build());
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setClusterId("oxford-production");
        when(controllerWrapper.getHostConfiguration()).thenReturn(hostConfiguration);
        when(agentsServiceConfiguration.isFeatureEnabled(anyString())).thenReturn(false);

        context =
                new AgentWorkerCommandContext(
                        request,
                        mock(MetricRegistry.class),
                        mock(CuratorFramework.class),
                        agentsServiceConfiguration,
                        mock(AggregatorInfo.class),
                        mock(SupplementalInformationController.class),
                        mock(ProviderSessionCacheController.class),
                        controllerWrapper,
                        "oxford-production",
                        "",
                        "",
                        mock(AccountInformationServiceEventsProducer.class));
    }

    private Provider getProvider() {
        Provider provider = new Provider();
        provider.setName("name");
        provider.setMarket("market");
        return provider;
    }

    private User getUser() {
        User user = new User();
        UserProfile userProfile = new UserProfile();
        userProfile.setLocale("SE");
        user.setProfile(userProfile);
        return user;
    }

    @Test
    public void shouldSendRestrictAccountsRequestOnce() throws Exception {
        // given
        request.setDataFetchingRestrictions(
                Arrays.asList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_ACCOUNTS,
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_TRANSACTIONS));
        DataFetchingRestrictionWorkerCommand command =
                new DataFetchingRestrictionWorkerCommand(context, controllerWrapper);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(controllerWrapper, times(1)).restrictAccounts(any());
    }

    @Test
    public void shouldNotSendAnyRestrictAccountsRequestWhenNoDataFetchingRestrictions()
            throws Exception {
        // given
        request.setDataFetchingRestrictions(Collections.emptyList());
        DataFetchingRestrictionWorkerCommand command =
                new DataFetchingRestrictionWorkerCommand(context, controllerWrapper);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(controllerWrapper, times(0)).restrictAccounts(any());
    }

    @Test
    public void shouldFilterOutRestrictedAccountsFromContextIfFeatureFlagEnabled()
            throws Exception {
        // given
        when(agentsServiceConfiguration.isFeatureEnabled(anyString())).thenReturn(true);
        request.setDataFetchingRestrictions(
                Arrays.asList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_ACCOUNTS,
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_TRANSACTIONS));
        DataFetchingRestrictionWorkerCommand command =
                new DataFetchingRestrictionWorkerCommand(context, controllerWrapper);
        feedContextWithAccountData(); // feed with accounts & transactions

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        verifyAccounts(getSavingAccountData());
        verifyTransactions(getSavingAccountData());
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(controllerWrapper, times(1)).restrictAccounts(any());
    }

    @Test
    public void shouldNotFilterOutRestrictedAccountsFromContextIfFeatureFlagDisabled()
            throws Exception {
        // given
        when(agentsServiceConfiguration.isFeatureEnabled(anyString())).thenReturn(false);
        request.setDataFetchingRestrictions(
                Arrays.asList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_ACCOUNTS,
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_TRANSACTIONS));
        DataFetchingRestrictionWorkerCommand command =
                new DataFetchingRestrictionWorkerCommand(context, controllerWrapper);
        feedContextWithAccountData(); // feed with accounts & transactions

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        List<Pair<Account, Transaction>> expectedAccountsAndTransactions =
                Stream.of(getSavingAccountData(), getCheckingAccountData())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        verifyAccounts(expectedAccountsAndTransactions);
        verifyTransactions(expectedAccountsAndTransactions);
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(controllerWrapper, times(1)).restrictAccounts(any());
    }

    private void verifyAccounts(List<Pair<Account, Transaction>> expectedAccountData) {
        assertThat(context.getAccountDataCache().getFilteredAccounts())
                .containsExactlyInAnyOrder(
                        expectedAccountData.stream().map(Pair::getLeft).toArray(Account[]::new));
    }

    private void verifyTransactions(List<Pair<Account, Transaction>> expectedAccountData) {
        List<String> transactionIds =
                context.getAccountDataCache().getFilteredAccountData().stream()
                        .map(AccountData::getTransactions)
                        .flatMap(Collection::stream)
                        .map(Transaction::getId)
                        .collect(Collectors.toList());
        assertThat(transactionIds)
                .containsExactlyInAnyOrder(
                        expectedAccountData.stream()
                                .map(pair -> pair.getRight().getId())
                                .toArray(String[]::new));
    }

    private void feedContextWithAccountData() {
        // add checking accounts
        getCheckingAccountData()
                .forEach(
                        pair -> {
                            Account a = pair.getLeft();
                            Transaction t = pair.getRight();
                            context.cacheAccount(a);
                            context.cacheTransactions(a.getBankId(), Collections.singletonList(t));
                        });
        // add saving accounts
        getSavingAccountData()
                .forEach(
                        pair -> {
                            Account a = pair.getLeft();
                            Transaction t = pair.getRight();
                            context.cacheAccount(a);
                            context.cacheTransactions(a.getBankId(), Collections.singletonList(t));
                        });
    }

    private List<Pair<Account, Transaction>> getCheckingAccountData() {
        Account a1 = new Account();
        a1.setBankId("c1");
        a1.setType(AccountTypes.CHECKING);
        Transaction t1 = new Transaction();
        t1.setId("t1");
        t1.setAccountId("c1");

        Account a2 = new Account();
        a2.setBankId("c2");
        a2.setType(AccountTypes.CHECKING);
        Transaction t2 = new Transaction();
        t2.setId("t2");
        t2.setAccountId("c2");

        return Arrays.asList(Pair.of(a1, t1), Pair.of(a2, t2));
    }

    private List<Pair<Account, Transaction>> getSavingAccountData() {
        Account a1 = new Account();
        a1.setBankId("s1");
        a1.setType(AccountTypes.SAVINGS);
        Transaction t1 = new Transaction();
        t1.setId("t3");
        t1.setAccountId("s1");

        Account a2 = new Account();
        a2.setBankId("s2");
        a2.setType(AccountTypes.SAVINGS);
        Transaction t2 = new Transaction();
        t2.setId("t4");
        t2.setAccountId("s2");

        return Arrays.asList(Pair.of(a1, t1), Pair.of(a2, t2));
    }
}
