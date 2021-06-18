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
}
