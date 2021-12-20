package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.newday;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;

public class NewDayConfiguration extends UkOpenBankingAisConfiguration {

    private NewDayConfiguration(Builder builder) {
        super(builder);
    }

    public static NewDayConfiguration getAisConfigWithUrlsByBrand(String brand) {
        return new NewDayConfiguration(
                UkOpenBankingAisConfiguration.builder()
                        .withOrganisationId(NewDayConstants.ORGANISATION_ID)
                        .withApiBaseURL(NewDayConstants.AIS_API_URL)
                        .withWellKnownURL(NewDayConstants.getWellKnownUrlByBrand(brand)));
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
