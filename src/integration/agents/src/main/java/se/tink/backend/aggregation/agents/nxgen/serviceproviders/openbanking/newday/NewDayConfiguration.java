package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.newday;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants.ConsentPermission;

public class NewDayConfiguration extends UkOpenBankingAisConfiguration {

    private NewDayConfiguration(Builder builder) {
        super(builder);
    }

    public static NewDayConfiguration getAisConfigWithUrlsByBrand(String brand) {
        return new NewDayConfiguration(
                UkOpenBankingAisConfiguration.builder()
                        .withAllowedAccountOwnershipTypes(AccountOwnershipType.PERSONAL)
                        .withOrganisationId(NewDayConstants.ORGANISATION_ID)
                        .withApiBaseURL(NewDayConstants.AIS_API_URL)
                        .withWellKnownURL(NewDayConstants.getWellKnownUrlByBrand(brand)));
    }

    @Override
    public ImmutableSet<String> getPermissions() {
        return ImmutableSet.<String>builder()
                .add(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue(),
                        ConsentPermission.READ_BALANCES.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue())
                .build();
    }

    @Override
    public ImmutableSet<UkObScope> getAvailablePermissions() {
        return ImmutableSet.<UkObScope>builder()
                .add(UkObScope.READ_ACCOUNTS_DETAIL)
                .add(UkObScope.READ_BALANCES)
                .add(UkObScope.READ_TRANSACTIONS_CREDITS)
                .add(UkObScope.READ_TRANSACTIONS_DEBITS)
                .add(UkObScope.READ_TRANSACTIONS_DETAIL)
                .build();
    }
}
