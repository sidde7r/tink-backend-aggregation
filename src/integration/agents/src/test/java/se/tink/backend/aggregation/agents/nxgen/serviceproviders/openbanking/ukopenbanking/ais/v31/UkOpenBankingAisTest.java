package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint.PartyPermission;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;

public class UkOpenBankingAisTest {

    @Test
    public void shouldGetAllDefaultRequiredPermissions() {
        // given
        UKOpenBankingAis aisConfig =
                UKOpenBankingAis.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .build();

        // when
        Set<String> permissions = aisConfig.getPermissions();
        Set<String> constantPermissions =
                new HashSet<>(OpenIdAuthenticatorConstants.ACCOUNT_PERMISSIONS);

        // then
        Assert.assertThat(permissions.containsAll(constantPermissions), Is.is(true));
    }

    @Test
    public void shouldGetAllCustomRequiredPermissions() {
        // given
        ImmutableSet<String> customPermissions =
                ImmutableSet.<String>builder().add("Permission1", "Permission2").build();

        UKOpenBankingAis aisConfig =
                UKOpenBankingAis.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withPermissions(customPermissions)
                        .build();

        // when
        Set<String> permissions = aisConfig.getPermissions();

        // then
        Assert.assertThat(permissions.size(), Is.is(2));
        Assert.assertThat(permissions.contains("Permission1"), Is.is(true));
        Assert.assertThat(permissions.contains("Permission2"), Is.is(true));
    }

    @Test
    public void shouldGetAllRequiredPermissionsForChosenPartyEndpoints() {
        // given
        UKOpenBankingAis withOnePermission =
                UKOpenBankingAis.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withPartyEndpoints(PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY)
                        .build();
        UKOpenBankingAis withTwoPermissions =
                UKOpenBankingAis.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withPartyEndpoints(
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY,
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES,
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY)
                        .build();

        // when
        Set<String> onePartyPermission = withOnePermission.getPermissions();
        Set<String> twoPartyPermissions = withTwoPermissions.getPermissions();

        // then
        Assert.assertThat(onePartyPermission.size(), Is.is(9));
        Assert.assertThat(
                onePartyPermission.contains(
                        PartyPermission.ACCOUNT_PERMISSION_READ_PARTY_PSU.getPermissionValue()),
                Is.is(true));
        Assert.assertThat(twoPartyPermissions.size(), Is.is(10));
        Assert.assertThat(
                twoPartyPermissions.contains(
                        PartyPermission.ACCOUNT_PERMISSION_READ_PARTY.getPermissionValue()),
                Is.is(true));
        Assert.assertThat(
                twoPartyPermissions.contains(
                        PartyPermission.ACCOUNT_PERMISSION_READ_PARTY_PSU.getPermissionValue()),
                Is.is(true));
    }
}
