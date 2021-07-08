package se.tink.backend.aggregation.agents.consent.ukob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

public class UkObConsentGeneratorTest {

    private UkObConsentGenerator generator;
    private ManualAuthenticateRequest requestMock;
    private RefreshScope scope = new RefreshScope();

    @Before
    public void setUp() throws Exception {
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
        generator = new UkObConsentGenerator(requestMock, getMaxPermissions());

        // when
        Set<String> permissions = generator.generate();

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        UkObPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        UkObPermission.READ_BALANCES.getValue(),
                        UkObPermission.READ_PARTY.getValue());
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
        generator = new UkObConsentGenerator(requestMock, getMaxPermissions());

        // when
        Set<String> permissions = generator.generate();

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        UkObPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        UkObPermission.READ_BALANCES.getValue(),
                        UkObPermission.READ_PARTY.getValue());
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        generator = new UkObConsentGenerator(requestMock, getMaxPermissions());

        // when
        Set<String> permissions = generator.generate();

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        UkObPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        UkObPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        UkObPermission.READ_TRANSACTIONS_DEBITS.getValue());
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
        generator = new UkObConsentGenerator(requestMock, getMaxPermissions());

        // when
        Set<String> permissions = generator.generate();

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        UkObPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        UkObPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        UkObPermission.READ_TRANSACTIONS_DEBITS.getValue());
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
        generator = new UkObConsentGenerator(requestMock, getMaxPermissions());

        // when
        Set<String> permissions = generator.generate();

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        UkObPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        UkObPermission.READ_BALANCES.getValue(),
                        UkObPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        UkObPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        UkObPermission.READ_TRANSACTIONS_DEBITS.getValue(),
                        UkObPermission.READ_PARTY.getValue());
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
        generator =
                new UkObConsentGenerator(
                        requestMock,
                        Sets.newHashSet(UkObPermission.READ_ACCOUNTS_DETAIL.getValue()));

        // when
        Set<String> permissions = generator.generate();

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(UkObPermission.READ_ACCOUNTS_DETAIL.getValue());
    }

    @Test
    public void shouldTrimPartyPermissionf() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.IDENTITY_DATA));
        given(requestMock.getRefreshScope()).willReturn(scope);
        generator =
                new UkObConsentGenerator(
                        requestMock,
                        Sets.newHashSet(
                                UkObPermission.READ_ACCOUNTS_DETAIL.getValue(),
                                UkObPermission.READ_BALANCES.getValue(),
                                UkObPermission.READ_BENEFICIARIES_DETAIL.getValue(),
                                UkObPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                                UkObPermission.READ_TRANSACTIONS_DEBITS.getValue(),
                                UkObPermission.READ_TRANSACTIONS_DETAIL.getValue()));

        // when
        Set<String> permissions = generator.generate();

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        UkObPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        UkObPermission.READ_BALANCES.getValue());
    }

    public ImmutableSet<String> getMaxPermissions() {
        Set<String> set = new HashSet<>();
        set.add(UkObPermission.READ_ACCOUNTS_DETAIL.getValue());
        set.add(UkObPermission.READ_BALANCES.getValue());
        set.add(UkObPermission.READ_BENEFICIARIES_DETAIL.getValue());
        set.add(UkObPermission.READ_TRANSACTIONS_CREDITS.getValue());
        set.add(UkObPermission.READ_TRANSACTIONS_DEBITS.getValue());
        set.add(UkObPermission.READ_TRANSACTIONS_DETAIL.getValue());
        set.add(UkObPermission.READ_PARTY_PSU.getValue());
        set.add(UkObPermission.READ_PARTY.getValue());

        return ImmutableSet.<String>builder().addAll(set).build();
    }
}
