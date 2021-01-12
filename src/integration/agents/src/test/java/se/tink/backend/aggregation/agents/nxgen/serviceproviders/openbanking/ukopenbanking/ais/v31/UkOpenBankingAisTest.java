package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31;

import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;

public class UkOpenBankingAisTest {

    @Test
    public void shouldGetAllRequiredPermissionsForChosenPartyEndpoints() {
        // given
        UkOpenBankingAisConfiguration configWithOnePartyPermission =
                UkOpenBankingAisConfiguration.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withWellKnownURL("wellKnown")
                        .withAllowedAccountOwnershipType(AccountOwnershipType.PERSONAL)
                        .withPartyEndpoints(PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY)
                        .build();
        UkOpenBankingAisConfiguration configWithTwoPartyPermissions =
                UkOpenBankingAisConfiguration.builder()
                        .withApiBaseURL("apiBaseURL")
                        .withOrganisationId("orgId")
                        .withWellKnownURL("wellKnown")
                        .withAllowedAccountOwnershipType(AccountOwnershipType.PERSONAL)
                        .withPartyEndpoints(
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY,
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES,
                                PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY)
                        .build();

        // when
        Set<String> permissionSet1 = configWithOnePartyPermission.getPermissions();
        Set<String> permissionSet2 = configWithTwoPartyPermissions.getPermissions();

        // then
        Assertions.assertThat(permissionSet1)
                .hasSize(9)
                .contains(
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_ACCOUNTS_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_BALANCES.getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_BENEFICIARIES_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_DIRECT_DEBITS
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_STANDING_ORDERS_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_CREDITS
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DEBITS
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY_PSU.getValue());
        Assertions.assertThat(permissionSet2)
                .hasSize(10)
                .contains(
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_ACCOUNTS_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_BALANCES.getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_BENEFICIARIES_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_DIRECT_DEBITS
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_STANDING_ORDERS_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_CREDITS
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DEBITS
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY_PSU.getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY.getValue());
    }
}
