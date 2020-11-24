package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import se.tink.libraries.account_data_cache.FilterReason;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

@RunWith(MockitoJUnitRunner.class)
public class SendAccountRestrictionEventsWorkerCommandTest {
    private static final String CLUSTER_ID = "oxford-production";
    private static final String APP_ID = "appId";
    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID = "userId";
    private static final String CREDENTIALS_ID = "credentialsId";
    private final Provider PROVIDER = getProvider();
    private AgentWorkerCommandContext context;
    private CredentialsRequest request;
    @Mock private ControllerWrapper controllerWrapper;
    @Mock private AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;

    @Before
    public void init() {
        request = new RefreshInformationRequest();
        request.setCredentials(getCredentials());
        User user = getUser();
        request.setUser(user);
        request.setProvider(PROVIDER);
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setClusterId("oxford-production");
        when(controllerWrapper.getHostConfiguration()).thenReturn(hostConfiguration);

        context =
                new AgentWorkerCommandContext(
                        request,
                        mock(MetricRegistry.class),
                        mock(CuratorFramework.class),
                        mock(AgentsServiceConfiguration.class),
                        mock(AggregatorInfo.class),
                        mock(SupplementalInformationController.class),
                        mock(ProviderSessionCacheController.class),
                        controllerWrapper,
                        CLUSTER_ID,
                        APP_ID,
                        CORRELATION_ID,
                        accountInformationServiceEventsProducer);
    }

    @Test
    public void shouldSendEventsAccordinglyToAllRegisteredFilters() throws Exception {
        // given
        feedContextWithAccountData();
        context.getAccountDataCache()
                .addFilter(
                        a -> a.getType() != AccountTypes.CHECKING,
                        FilterReason.DATA_FETCHING_RESTRICTIONS);
        context.getAccountDataCache()
                .addFilter(a -> !a.getBankId().equals("s1"), FilterReason.OPT_IN);
        SendAccountRestrictionEventsWorkerCommand command =
                new SendAccountRestrictionEventsWorkerCommand(
                        context, accountInformationServiceEventsProducer);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(accountInformationServiceEventsProducer, times(3))
                .sendAccountAggregationRestrictedEvent(
                        eq("oxford-production"),
                        eq("appId"),
                        eq("userId"),
                        eq(PROVIDER),
                        eq("correlationId"),
                        eq("credentialsId"),
                        anyString(),
                        anyString(),
                        argument.capture());
        List<String> values = argument.getAllValues();
        assertThat(values)
                .containsExactlyInAnyOrder(
                        FilterReason.DATA_FETCHING_RESTRICTIONS.name(),
                        FilterReason.DATA_FETCHING_RESTRICTIONS.name(),
                        FilterReason.OPT_IN.name());
    }

    @Test
    public void shouldSendNoEventsIfNoAccountFilters() throws Exception {
        // given
        feedContextWithAccountData();
        SendAccountRestrictionEventsWorkerCommand command =
                new SendAccountRestrictionEventsWorkerCommand(
                        context, accountInformationServiceEventsProducer);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(accountInformationServiceEventsProducer, times(0))
                .sendAccountAggregationRestrictedEvent(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString());
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
        a1.setBankId("c1");
        a1.setType(AccountTypes.CHECKING);

        Account a2 = new Account();
        a2.setBankId("c2");
        a2.setType(AccountTypes.CHECKING);

        return Arrays.asList(a1, a2);
    }

    private List<Account> getSavingAccounts() {
        Account a1 = new Account();
        a1.setBankId("s1");
        a1.setType(AccountTypes.SAVINGS);

        Account a2 = new Account();
        a2.setBankId("s2");
        a2.setType(AccountTypes.SAVINGS);

        return Arrays.asList(a1, a2);
    }

    private List<Account> getCreditCardAccounts() {
        Account a1 = new Account();
        a1.setBankId("cc1");
        a1.setType(AccountTypes.CREDIT_CARD);

        Account a2 = new Account();
        a2.setBankId("cc2");
        a2.setType(AccountTypes.CREDIT_CARD);

        return Arrays.asList(a1, a2);
    }

    private Credentials getCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(CREDENTIALS_ID);
        return credentials;
    }

    private Provider getProvider() {
        Provider provider = new Provider();
        provider.setName("providerName");
        provider.setMarket("market");
        return provider;
    }

    private User getUser() {
        User user = new User();
        user.setId(USER_ID);
        UserProfile userProfile = new UserProfile();
        userProfile.setLocale("SE");
        user.setProfile(userProfile);
        return user;
    }
}
