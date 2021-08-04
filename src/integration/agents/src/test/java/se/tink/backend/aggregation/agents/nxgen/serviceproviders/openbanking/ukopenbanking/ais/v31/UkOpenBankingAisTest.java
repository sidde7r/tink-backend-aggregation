package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;

public class UkOpenBankingAisTest {

    @Test
    public void shouldGetAllRequiredPermissionsForChosenPartyEndpoints() {
        // given
        UkOpenBankingAisConfiguration configWithOnePartyPermission =
                UkOpenBankingAisConfiguration.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withWellKnownURL("wellKnown")
                        .withAllowedAccountOwnershipTypes(AccountOwnershipType.PERSONAL)
                        .withPartyEndpoints(PartyEndpoint.PARTY)
                        .build();
        UkOpenBankingAisConfiguration configWithTwoPartyPermissions =
                UkOpenBankingAisConfiguration.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withWellKnownURL("wellKnown")
                        .withAllowedAccountOwnershipTypes(AccountOwnershipType.PERSONAL)
                        .withPartyEndpoints(
                                PartyEndpoint.PARTY,
                                PartyEndpoint.ACCOUNT_ID_PARTIES,
                                PartyEndpoint.ACCOUNT_ID_PARTY)
                        .build();

        // when
        ImmutableSet<UkObScope> permissionSet1 =
                configWithOnePartyPermission.getAvailablePermissions();
        ImmutableSet<UkObScope> permissionSet2 =
                configWithTwoPartyPermissions.getAvailablePermissions();

        // then
        Assertions.assertThat(permissionSet1)
                .hasSize(8)
                .contains(
                        UkObScope.READ_ACCOUNTS_DETAIL,
                        UkObScope.READ_BALANCES,
                        UkObScope.READ_BENEFICIARIES_DETAIL,
                        UkObScope.READ_TRANSACTIONS_CREDITS,
                        UkObScope.READ_TRANSACTIONS_DEBITS,
                        UkObScope.READ_TRANSACTIONS_DETAIL,
                        UkObScope.READ_PARTY_PSU);
        Assertions.assertThat(permissionSet2)
                .hasSize(9)
                .contains(
                        UkObScope.READ_ACCOUNTS_DETAIL,
                        UkObScope.READ_BALANCES,
                        UkObScope.READ_BENEFICIARIES_DETAIL,
                        UkObScope.READ_TRANSACTIONS_CREDITS,
                        UkObScope.READ_TRANSACTIONS_DEBITS,
                        UkObScope.READ_TRANSACTIONS_DETAIL,
                        UkObScope.READ_PARTY_PSU,
                        UkObScope.READ_PARTY);
    }
}
