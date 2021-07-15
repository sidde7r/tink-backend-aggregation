package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.FinancialService;
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
import se.tink.backend.aggregation.workers.operation.OperationStatusManager;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

@RunWith(MockitoJUnitRunner.class)
public class AccountSegmentRestrictionWorkerCommandTest {
    @Mock private ControllerWrapper controllerWrapper;
    @Mock private AgentsServiceConfiguration agentsServiceConfiguration;
    @Mock private UnleashClient unleashClient;

    private AgentWorkerCommandContext context;
    private RefreshInformationRequest request;

    @Before
    public void init() {
        request = new RefreshInformationRequest();
        request.setCredentials(new Credentials());
        User user = getUser();
        request.setUser(user);
        Provider provider = getProvider();
        request.setProvider(provider);

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setClusterId("oxford-production");
        when(controllerWrapper.getHostConfiguration()).thenReturn(hostConfiguration);

        when(agentsServiceConfiguration.isFeatureEnabled(
                        AccountSegmentRestrictionWorkerCommand
                                .USE_ACCOUNT_SEGMENT_RESTRICTION_FEATURE_NAME))
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
                        mock(AccountInformationServiceEventsProducer.class),
                        unleashClient,
                        mock(OperationStatusManager.class),
                        true);
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
    public void nullSegmentsShouldNotFilter() throws Exception {
        // given
        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setFinancialServiceSegmentsIn(null);
        request.setRefreshScope(refreshScope);
        AccountSegmentRestrictionWorkerCommand command =
                new AccountSegmentRestrictionWorkerCommand(context);

        feedContextWithAccountData(); // feed with accounts & transactions

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        List<Pair<Account, Transaction>> expectedAccountsAndTransactions =
                Stream.of(
                                getPersonalAccountData(),
                                getBusinessAccountData(),
                                getUndeterminedAccountData())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        verifyAccounts(expectedAccountsAndTransactions);
        verifyTransactions(expectedAccountsAndTransactions);
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void emptySegmentsShouldNotFilter() throws Exception {
        // given
        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setFinancialServiceSegmentsIn(Collections.emptySet());
        request.setRefreshScope(refreshScope);
        AccountSegmentRestrictionWorkerCommand command =
                new AccountSegmentRestrictionWorkerCommand(context);

        feedContextWithAccountData(); // feed with accounts & transactions

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        List<Pair<Account, Transaction>> expectedAccountsAndTransactions =
                Stream.of(
                                getPersonalAccountData(),
                                getBusinessAccountData(),
                                getUndeterminedAccountData())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        verifyAccounts(expectedAccountsAndTransactions);
        verifyTransactions(expectedAccountsAndTransactions);
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void shouldFilterOutNonPersonalSegments() throws Exception {
        // given
        Set<FinancialService.FinancialServiceSegment> refreshableSegments =
                ImmutableSet.<FinancialService.FinancialServiceSegment>builder()
                        .add(FinancialService.FinancialServiceSegment.PERSONAL)
                        .build();

        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setFinancialServiceSegmentsIn(refreshableSegments);
        request.setRefreshScope(refreshScope);

        AccountSegmentRestrictionWorkerCommand command =
                new AccountSegmentRestrictionWorkerCommand(context);

        feedContextWithAccountData(); // feed with accounts & transactions

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        List<Pair<Account, Transaction>> expectedAccountsAndTransactions =
                Stream.of(getPersonalAccountData())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        verifyAccounts(expectedAccountsAndTransactions);
        verifyTransactions(expectedAccountsAndTransactions);
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void shouldFilterOutNonBusinessSegments() throws Exception {
        // given
        Set<FinancialService.FinancialServiceSegment> refreshableSegments =
                ImmutableSet.<FinancialService.FinancialServiceSegment>builder()
                        .add(FinancialService.FinancialServiceSegment.BUSINESS)
                        .build();

        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setFinancialServiceSegmentsIn(refreshableSegments);
        request.setRefreshScope(refreshScope);

        AccountSegmentRestrictionWorkerCommand command =
                new AccountSegmentRestrictionWorkerCommand(context);

        feedContextWithAccountData(); // feed with accounts & transactions

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        List<Pair<Account, Transaction>> expectedAccountsAndTransactions =
                Stream.of(getBusinessAccountData())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        verifyAccounts(expectedAccountsAndTransactions);
        verifyTransactions(expectedAccountsAndTransactions);
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void shouldFilterOutNonUndeterminedSegments() throws Exception {
        // given
        Set<FinancialService.FinancialServiceSegment> refreshableSegments =
                ImmutableSet.<FinancialService.FinancialServiceSegment>builder()
                        .add(FinancialService.FinancialServiceSegment.UNDETERMINED)
                        .build();

        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setFinancialServiceSegmentsIn(refreshableSegments);
        request.setRefreshScope(refreshScope);

        AccountSegmentRestrictionWorkerCommand command =
                new AccountSegmentRestrictionWorkerCommand(context);

        feedContextWithAccountData(); // feed with accounts & transactions

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        List<Pair<Account, Transaction>> expectedAccountsAndTransactions =
                Stream.of(getUndeterminedAccountData())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        verifyAccounts(expectedAccountsAndTransactions);
        verifyTransactions(expectedAccountsAndTransactions);
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void shouldNotFilterSegmentsIfFeatureIsDisabled() throws Exception {
        // given
        when(agentsServiceConfiguration.isFeatureEnabled(
                        AccountSegmentRestrictionWorkerCommand
                                .USE_ACCOUNT_SEGMENT_RESTRICTION_FEATURE_NAME))
                .thenReturn(false);

        Set<FinancialService.FinancialServiceSegment> refreshableSegments =
                ImmutableSet.<FinancialService.FinancialServiceSegment>builder()
                        .add(FinancialService.FinancialServiceSegment.UNDETERMINED)
                        .build();

        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setFinancialServiceSegmentsIn(refreshableSegments);
        request.setRefreshScope(refreshScope);

        AccountSegmentRestrictionWorkerCommand command =
                new AccountSegmentRestrictionWorkerCommand(context);

        feedContextWithAccountData(); // feed with accounts & transactions

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        List<Pair<Account, Transaction>> expectedAccountsAndTransactions =
                Stream.of(
                                getPersonalAccountData(),
                                getBusinessAccountData(),
                                getUndeterminedAccountData())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        verifyAccounts(expectedAccountsAndTransactions);
        verifyTransactions(expectedAccountsAndTransactions);
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
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
        getPersonalAccountData()
                .forEach(
                        pair -> {
                            Account a = pair.getLeft();
                            Transaction t = pair.getRight();
                            context.cacheAccount(a);
                            context.cacheTransactions(a.getBankId(), Collections.singletonList(t));
                        });
        getBusinessAccountData()
                .forEach(
                        pair -> {
                            Account a = pair.getLeft();
                            Transaction t = pair.getRight();
                            context.cacheAccount(a);
                            context.cacheTransactions(a.getBankId(), Collections.singletonList(t));
                        });
        getUndeterminedAccountData()
                .forEach(
                        pair -> {
                            Account a = pair.getLeft();
                            Transaction t = pair.getRight();
                            context.cacheAccount(a);
                            context.cacheTransactions(a.getBankId(), Collections.singletonList(t));
                        });
    }

    private List<Pair<Account, Transaction>> getPersonalAccountData() {
        Account a1 = new Account();
        a1.setBankId("c1");
        a1.setType(AccountTypes.CHECKING);
        AccountHolder a1AccountHolder = new AccountHolder();
        a1AccountHolder.setType(AccountHolderType.PERSONAL);
        a1.setAccountHolder(a1AccountHolder);
        Transaction t1 = new Transaction();
        t1.setId("t1");
        t1.setAccountId("c1");

        Account a2 = new Account();
        a2.setBankId("c2");
        a2.setType(AccountTypes.SAVINGS);
        AccountHolder a2AccountHolder = new AccountHolder();
        a2AccountHolder.setType(AccountHolderType.PERSONAL);
        a2.setAccountHolder(a2AccountHolder);
        Transaction t2 = new Transaction();
        t2.setId("t2");
        t2.setAccountId("c2");

        return Arrays.asList(Pair.of(a1, t1), Pair.of(a2, t2));
    }

    private List<Pair<Account, Transaction>> getBusinessAccountData() {
        Account a1 = new Account();
        a1.setBankId("s1");
        a1.setType(AccountTypes.CHECKING);
        AccountHolder a1AccountHolder = new AccountHolder();
        a1AccountHolder.setType(AccountHolderType.BUSINESS);
        a1.setAccountHolder(a1AccountHolder);
        Transaction t1 = new Transaction();
        t1.setId("t3");
        t1.setAccountId("s1");

        Account a2 = new Account();
        a2.setBankId("s2");
        a2.setType(AccountTypes.SAVINGS);
        AccountHolder a2AccountHolder = new AccountHolder();
        a2AccountHolder.setType(AccountHolderType.CORPORATE);
        a2.setAccountHolder(a2AccountHolder);
        Transaction t2 = new Transaction();
        t2.setId("t4");
        t2.setAccountId("s2");

        return Arrays.asList(Pair.of(a1, t1), Pair.of(a2, t2));
    }

    private List<Pair<Account, Transaction>> getUndeterminedAccountData() {
        Account a1 = new Account();
        a1.setBankId("u1");
        a1.setType(AccountTypes.CHECKING);
        // No `accountHolder` --> UNDETERMINED.
        Transaction t1 = new Transaction();
        t1.setId("t5");
        t1.setAccountId("u1");

        Account a2 = new Account();
        a2.setBankId("u2");
        a2.setType(AccountTypes.SAVINGS);
        AccountHolder a2AccountHolder = new AccountHolder();
        // No `accountHolder::type` --> UNDETERMINED.
        a2.setAccountHolder(a2AccountHolder);
        Transaction t2 = new Transaction();
        t2.setId("t6");
        t2.setAccountId("u2");

        Account a3 = new Account();
        a3.setBankId("u3");
        a3.setType(AccountTypes.SAVINGS);
        AccountHolder a3AccountHolder = new AccountHolder();
        a3AccountHolder.setType(AccountHolderType.UNKNOWN);
        a3.setAccountHolder(a3AccountHolder);
        Transaction t3 = new Transaction();
        t3.setId("t7");
        t3.setAccountId("u3");

        return Arrays.asList(Pair.of(a1, t1), Pair.of(a2, t2), Pair.of(a3, t3));
    }
}
