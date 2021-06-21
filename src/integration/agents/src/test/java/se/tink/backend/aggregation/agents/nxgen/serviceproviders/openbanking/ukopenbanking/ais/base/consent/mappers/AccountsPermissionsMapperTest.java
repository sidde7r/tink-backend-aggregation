package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.ConsentPermission;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.libraries.credentials.service.RefreshableItem;

public class AccountsPermissionsMapperTest {

    private AccountsPermissionsMapper mapper;
    private UkOpenBankingAisConfig mockedAisConfig;
    private Set<RefreshableItem> items;
    private Set<ConsentPermission> permissions;

    @Before
    public void setUp() throws Exception {
        this.mockedAisConfig = mock(UkOpenBankingAisConfig.class);
        this.mapper = new AccountsPermissionsMapper(mockedAisConfig);
    }

    @Test
    public void shouldReturnEmptySetIfWrongItem() {
        // given
        items = Sets.newHashSet(RefreshableItem.LIST_BENEFICIARIES);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions).isEmpty();
    }

    @Test
    public void shouldReturnAccountPermissionsIfPartyEndpointsDisabled() {
        // given
        items = Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL, ConsentPermission.READ_BALANCES);
    }

    @Test
    public void shouldReturnPartyAccountEndpointPermissions() {
        // given
        given(mockedAisConfig.isAccountPartyEndpointEnabled()).willReturn(Boolean.TRUE);
        mapper = new AccountsPermissionsMapper(mockedAisConfig);
        items = Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL,
                        ConsentPermission.READ_BALANCES,
                        ConsentPermission.READ_PARTY);
    }

    @Test
    public void shouldReturnPartyAccountsEndpointPermissions() {
        // given
        given(mockedAisConfig.isAccountPartiesEndpointEnabled()).willReturn(Boolean.TRUE);
        mapper = new AccountsPermissionsMapper(mockedAisConfig);
        items = Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL,
                        ConsentPermission.READ_BALANCES,
                        ConsentPermission.READ_PARTY);
    }

    @Test
    public void shouldReturnAllPartyAccountEndpointsPermissions() {
        // given
        given(mockedAisConfig.isAccountPartyEndpointEnabled()).willReturn(Boolean.TRUE);
        given(mockedAisConfig.isAccountPartiesEndpointEnabled()).willReturn(Boolean.TRUE);
        mapper = new AccountsPermissionsMapper(mockedAisConfig);
        items = Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_ACCOUNTS_DETAIL,
                        ConsentPermission.READ_BALANCES,
                        ConsentPermission.READ_PARTY);
    }
}
