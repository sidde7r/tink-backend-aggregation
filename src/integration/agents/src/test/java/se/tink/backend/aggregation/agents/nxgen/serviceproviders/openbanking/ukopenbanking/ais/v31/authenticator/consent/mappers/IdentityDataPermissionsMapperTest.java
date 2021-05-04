package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentPermission;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IdentityDataPermissionsMapperTest {

    private IdentityDataPermissionsMapper mapper;
    private UkOpenBankingAisConfig mockedAisConfig;
    private Set<RefreshableItem> items;
    private Set<ConsentPermission> permissions;

    @Before
    public void setUp() throws Exception {
        this.mockedAisConfig = mock(UkOpenBankingAisConfig.class);
        this.mapper = new IdentityDataPermissionsMapper(mockedAisConfig);
    }

    @Test
    public void shouldReturnEmptySetIfWrongItem() {
        // given
        items = Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions).isEmpty();
    }

    @Test
    public void shouldReturnEmptySetIfPartyEndpointsDisabled() {
        // given
        items = Sets.newHashSet(RefreshableItem.IDENTITY_DATA);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions).isEmpty();
    }

    @Test
    public void shouldReturnPartyEndpointPermission() {
        // given
        given(mockedAisConfig.isPartyEndpointEnabled()).willReturn(Boolean.TRUE);
        mapper = new IdentityDataPermissionsMapper(mockedAisConfig);
        items = Sets.newHashSet(RefreshableItem.IDENTITY_DATA);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions).containsExactlyInAnyOrder(ConsentPermission.READ_PARTY_PSU);
    }

    @Test
    public void shouldReturnPartyAccountEndpointPermissions() {
        // given
        given(mockedAisConfig.isAccountPartyEndpointEnabled()).willReturn(Boolean.TRUE);
        mapper = new IdentityDataPermissionsMapper(mockedAisConfig);
        items = Sets.newHashSet(RefreshableItem.IDENTITY_DATA);

        // when
        permissions = mapper.mapFrom(items);

        // then
        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_PARTY, ConsentPermission.READ_ACCOUNTS_DETAIL);
    }

    @Test
    public void shouldReturnPartyAccountsEndpointPermissions() {
        // given
        given(mockedAisConfig.isAccountPartiesEndpointEnabled()).willReturn(Boolean.TRUE);
        mapper = new IdentityDataPermissionsMapper(mockedAisConfig);
        items = Sets.newHashSet(RefreshableItem.IDENTITY_DATA);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_PARTY, ConsentPermission.READ_ACCOUNTS_DETAIL);
    }

    @Test
    public void shouldReturnAllPartyAccountEndpointsPermissions() {
        // given
        given(mockedAisConfig.isAccountPartyEndpointEnabled()).willReturn(Boolean.TRUE);
        given(mockedAisConfig.isAccountPartiesEndpointEnabled()).willReturn(Boolean.TRUE);
        mapper = new IdentityDataPermissionsMapper(mockedAisConfig);
        items = Sets.newHashSet(RefreshableItem.IDENTITY_DATA);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_PARTY, ConsentPermission.READ_ACCOUNTS_DETAIL);
    }

    @Test
    public void shouldReturnAllPartyEndpointPermissions() {
        // given
        given(mockedAisConfig.isPartyEndpointEnabled()).willReturn(Boolean.TRUE);
        given(mockedAisConfig.isAccountPartyEndpointEnabled()).willReturn(Boolean.TRUE);
        given(mockedAisConfig.isAccountPartiesEndpointEnabled()).willReturn(Boolean.TRUE);
        mapper = new IdentityDataPermissionsMapper(mockedAisConfig);
        items = Sets.newHashSet(RefreshableItem.IDENTITY_DATA);

        // when
        permissions = mapper.mapFrom(items);

        // then
        assertThat(permissions)
                .containsExactlyInAnyOrder(
                        ConsentPermission.READ_PARTY_PSU,
                        ConsentPermission.READ_PARTY,
                        ConsentPermission.READ_ACCOUNTS_DETAIL);
    }
}
