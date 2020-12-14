package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31;

import java.util.Set;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint.PartyPermission;

public class UkOpenBankingAisTest {

    @Test
    public void shouldGetAllRequiredPermissionsForChosenPartyEndpoints() {
        // given
        UkOpenBankingAisConfiguration withOnePermission =
                UkOpenBankingAisConfiguration.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withPartyEndpoints(PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY)
                        .build();
        UkOpenBankingAisConfiguration withTwoPermissions =
                UkOpenBankingAisConfiguration.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withPartyEndpoints(
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY,
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES,
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY)
                        .build();

        // when
        Set<String> onePermission = withOnePermission.getAdditionalPermissions();
        Set<String> twoPermissions = withTwoPermissions.getAdditionalPermissions();

        // then
        Assert.assertThat(onePermission.size(), Is.is(1));
        Assert.assertThat(
                onePermission.contains(
                        PartyPermission.ACCOUNT_PERMISSION_READ_PARTY_PSU.getPermissionValue()),
                Is.is(true));
        Assert.assertThat(twoPermissions.size(), Is.is(2));
        Assert.assertThat(
                twoPermissions.contains(
                        PartyPermission.ACCOUNT_PERMISSION_READ_PARTY.getPermissionValue()),
                Is.is(true));
        Assert.assertThat(
                twoPermissions.contains(
                        PartyPermission.ACCOUNT_PERMISSION_READ_PARTY_PSU.getPermissionValue()),
                Is.is(true));
    }
}
