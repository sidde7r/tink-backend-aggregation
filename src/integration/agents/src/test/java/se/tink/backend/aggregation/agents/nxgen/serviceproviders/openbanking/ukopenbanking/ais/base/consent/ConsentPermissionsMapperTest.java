package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.libraries.credentials.service.RefreshableItem;

public class ConsentPermissionsMapperTest {

    private ConsentPermissionsMapper mapper;
    private UkOpenBankingAisConfig aisConfigMock;

    @Before
    public void setUp() throws Exception {
        this.aisConfigMock = mock(UkOpenBankingAisConfig.class);
        this.mapper = new ConsentPermissionsMapper(aisConfigMock);
    }

    @Test
    public void shouldMapAccountItemsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.SAVING_ACCOUNTS);
        given(aisConfigMock.getPermissions()).willReturn(getMaxPermissions());

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_BALANCES.getValue());
    }

    @Test
    public void shouldMapAllAccountItemsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.LOAN_ACCOUNTS,
                        RefreshableItem.INVESTMENT_ACCOUNTS);
        given(aisConfigMock.getPermissions()).willReturn(getMaxPermissions());

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_BALANCES.getValue());
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS, RefreshableItem.SAVING_TRANSACTIONS);
        given(aisConfigMock.getPermissions()).willReturn(getMaxPermissions());

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue());
    }

    @Test
    public void shouldMapAllTransactionsItemsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS,
                        RefreshableItem.CREDITCARD_TRANSACTIONS,
                        RefreshableItem.INVESTMENT_TRANSACTIONS,
                        RefreshableItem.LOAN_TRANSACTIONS);
        given(aisConfigMock.getPermissions()).willReturn(getMaxPermissions());

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue());
    }

    @Test
    public void shouldMapAccountAndTransactionsItemsCorrectly1() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS);
        given(aisConfigMock.getPermissions()).willReturn(getMaxPermissions());

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_BALANCES.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue());
    }

    @Test
    public void shouldTrimPermissionsCorrectly() {
        // given
        Set<RefreshableItem> items =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS);
        given(aisConfigMock.getPermissions())
                .willReturn(ImmutableSet.of(ConsentPermission.READ_ACCOUNTS_DETAIL.getValue()));

        // when
        ImmutableSet<String> permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions).containsExactly(ConsentPermission.READ_ACCOUNTS_DETAIL.getValue());
    }

    public ImmutableSet<String> getMaxPermissions() {
        Set<String> set = new HashSet<>();
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_ACCOUNTS_DETAIL.getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_BALANCES.getValue());
        set.add(
                OpenIdAuthenticatorConstants.ConsentPermission.READ_BENEFICIARIES_DETAIL
                        .getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_DIRECT_DEBITS.getValue());
        set.add(
                OpenIdAuthenticatorConstants.ConsentPermission.READ_STANDING_ORDERS_DETAIL
                        .getValue());
        set.add(
                OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_CREDITS
                        .getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY_PSU.getValue());
        set.add(OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY.getValue());

        return ImmutableSet.<String>builder().addAll(set).build();
    }
}
