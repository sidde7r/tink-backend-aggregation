package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

@RunWith(MockitoJUnitRunner.class)
public class RequestedAccountsRestrictionWorkerCommandTest {

    private AgentWorkerCommandContext context;
    @Mock private ControllerWrapper controllerWrapper;
    @Mock private AgentsServiceConfiguration agentsServiceConfiguration;

    @Before
    public void init() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setCredentials(new Credentials());
        request.setRequestedAccountIds(ImmutableSet.of("s1", "cc2"));
        User user = getUser();
        request.setUser(user);
        Provider provider = getProvider();
        request.setProvider(provider);
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setClusterId("oxford-production");
        when(controllerWrapper.getHostConfiguration()).thenReturn(hostConfiguration);
        when(agentsServiceConfiguration.isFeatureEnabled("supportIndividualAccountsRefresh"))
                .thenReturn(true);

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

    @Test
    public void shouldFilterOutAccountsThatHaveNotBeenRequestedForRefresh() throws Exception {
        // given
        RequestedAccountsRestrictionWorkerCommand command =
                new RequestedAccountsRestrictionWorkerCommand(context);
        feedContextWithAccountData();

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        assertThat(
                        context.getAccountDataCache().getFilteredAccounts().stream()
                                .map(Account::getId)
                                .collect(Collectors.toList()))
                .containsExactlyInAnyOrder("s1", "cc2");
    }

    @Test
    public void shouldNotFilterOutIfGranularRefreshDisabled() throws Exception {
        // given
        when(agentsServiceConfiguration.isFeatureEnabled("supportIndividualAccountsRefresh"))
                .thenReturn(false);
        RequestedAccountsRestrictionWorkerCommand command =
                new RequestedAccountsRestrictionWorkerCommand(context);
        feedContextWithAccountData();

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        assertThat(
                        context.getAccountDataCache().getFilteredAccounts().stream()
                                .map(Account::getId)
                                .collect(Collectors.toList()))
                .containsExactlyInAnyOrder("s1", "s2", "c1", "c2", "cc1", "cc2");
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

    private void feedContextWithAccountData() {
        Stream.of(getCheckingAccounts(), getSavingAccounts(), getCreditCardAccounts())
                .flatMap(Collection::stream)
                .forEach(
                        accounts -> {
                            context.cacheAccount(accounts);
                        });
    }

    private List<Account> getCheckingAccounts() {
        Account a1 = new Account();
        a1.setId("c1");
        a1.setBankId("bid:c1");
        a1.setType(AccountTypes.CHECKING);

        Account a2 = new Account();
        a2.setId("c2");
        a2.setBankId("bid:c2");
        a2.setType(AccountTypes.CHECKING);

        return Arrays.asList(a1, a2);
    }

    private List<Account> getSavingAccounts() {
        Account a1 = new Account();
        a1.setId("s1");
        a1.setBankId("bid:s1");
        a1.setType(AccountTypes.SAVINGS);

        Account a2 = new Account();
        a2.setId("s2");
        a2.setBankId("bid:s2");
        a2.setType(AccountTypes.SAVINGS);

        return Arrays.asList(a1, a2);
    }

    private List<Account> getCreditCardAccounts() {
        Account a1 = new Account();
        a1.setId("cc1");
        a1.setBankId("bid:cc1");
        a1.setType(AccountTypes.CREDIT_CARD);

        Account a2 = new Account();
        a2.setId("cc2");
        a2.setBankId("bid:cc2");
        a2.setType(AccountTypes.CREDIT_CARD);

        return Arrays.asList(a1, a2);
    }
}
