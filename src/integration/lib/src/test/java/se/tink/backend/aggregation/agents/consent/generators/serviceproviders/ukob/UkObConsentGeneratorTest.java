package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

public class UkObConsentGeneratorTest {

    private UkObConsentGenerator generator;
    private AgentComponentProvider componentProviderMock;
    private ManualAuthenticateRequest requestMock;
    private RefreshScope scope = new RefreshScope();

    @Before
    public void setUp() throws Exception {
        this.componentProviderMock = mock(AgentComponentProvider.class);
        this.requestMock = mock(ManualAuthenticateRequest.class);
        this.scope = new RefreshScope();
        this.requestMock.setRefreshScope(scope);
    }

    @Test
    public void shouldMapAccountItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.SAVING_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = UkObConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        AccountPermissionRequest permissionRequest = generator.generate();

        // then
        assertThat(permissionRequest.getData().getPermissions())
                .containsExactlyInAnyOrder(
                        UkObScope.READ_ACCOUNTS_DETAIL.toString(),
                        UkObScope.READ_BALANCES.toString(),
                        UkObScope.READ_PARTY.toString());
    }

    @Test
    public void shouldMapAllAccountItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.LOAN_ACCOUNTS,
                        RefreshableItem.INVESTMENT_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = UkObConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        AccountPermissionRequest permissionRequest = generator.generate();

        // then
        assertThat(permissionRequest.getData().getPermissions())
                .containsExactlyInAnyOrder(
                        UkObScope.READ_ACCOUNTS_DETAIL.toString(),
                        UkObScope.READ_BALANCES.toString(),
                        UkObScope.READ_PARTY.toString());
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = UkObConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        AccountPermissionRequest permissionRequest = generator.generate();

        // then
        assertThat(permissionRequest.getData().getPermissions())
                .containsExactlyInAnyOrder(
                        UkObScope.READ_TRANSACTIONS_DETAIL.toString(),
                        UkObScope.READ_TRANSACTIONS_CREDITS.toString(),
                        UkObScope.READ_TRANSACTIONS_DEBITS.toString());
    }

    @Test
    public void shouldMapAllTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS,
                        RefreshableItem.CREDITCARD_TRANSACTIONS,
                        RefreshableItem.INVESTMENT_TRANSACTIONS,
                        RefreshableItem.LOAN_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = UkObConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        AccountPermissionRequest permissionRequest = generator.generate();

        // then
        assertThat(permissionRequest.getData().getPermissions())
                .containsExactlyInAnyOrder(
                        UkObScope.READ_TRANSACTIONS_DETAIL.toString(),
                        UkObScope.READ_TRANSACTIONS_CREDITS.toString(),
                        UkObScope.READ_TRANSACTIONS_DEBITS.toString());
    }

    @Test
    public void shouldMapAccountAndTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = UkObConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        AccountPermissionRequest permissionRequest = generator.generate();

        // then
        assertThat(permissionRequest.getData().getPermissions())
                .containsExactlyInAnyOrder(
                        UkObScope.READ_ACCOUNTS_DETAIL.toString(),
                        UkObScope.READ_BALANCES.toString(),
                        UkObScope.READ_TRANSACTIONS_DETAIL.toString(),
                        UkObScope.READ_TRANSACTIONS_CREDITS.toString(),
                        UkObScope.READ_TRANSACTIONS_DEBITS.toString(),
                        UkObScope.READ_PARTY.toString());
    }

    @Test
    public void shouldTrimPermissionsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator =
                UkObConsentGenerator.of(
                        componentProviderMock, Sets.newHashSet(UkObScope.READ_ACCOUNTS_DETAIL));

        // when
        AccountPermissionRequest permissionRequest = generator.generate();

        // then
        assertThat(permissionRequest.getData().getPermissions())
                .containsExactlyInAnyOrder(UkObScope.READ_ACCOUNTS_DETAIL.toString());
    }

    @Test
    public void shouldTrimPartyPermission() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.IDENTITY_DATA));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator =
                UkObConsentGenerator.of(
                        componentProviderMock,
                        Sets.newHashSet(
                                UkObScope.READ_ACCOUNTS_DETAIL,
                                UkObScope.READ_BALANCES,
                                UkObScope.READ_BENEFICIARIES_DETAIL,
                                UkObScope.READ_TRANSACTIONS_CREDITS,
                                UkObScope.READ_TRANSACTIONS_DEBITS,
                                UkObScope.READ_TRANSACTIONS_DETAIL));

        // when
        AccountPermissionRequest permissionRequest = generator.generate();

        // then
        assertThat(permissionRequest.getData().getPermissions())
                .containsExactlyInAnyOrder(
                        UkObScope.READ_ACCOUNTS_DETAIL.toString(),
                        UkObScope.READ_BALANCES.toString());
    }

    public ImmutableSet<UkObScope> getAllScopes() {
        Set<UkObScope> set = new HashSet<>();
        set.add(UkObScope.READ_ACCOUNTS_DETAIL);
        set.add(UkObScope.READ_BALANCES);
        set.add(UkObScope.READ_BENEFICIARIES_DETAIL);
        set.add(UkObScope.READ_TRANSACTIONS_CREDITS);
        set.add(UkObScope.READ_TRANSACTIONS_DEBITS);
        set.add(UkObScope.READ_TRANSACTIONS_DETAIL);
        set.add(UkObScope.READ_PARTY_PSU);
        set.add(UkObScope.READ_PARTY);

        return ImmutableSet.<UkObScope>builder().addAll(set).build();
    }
}
