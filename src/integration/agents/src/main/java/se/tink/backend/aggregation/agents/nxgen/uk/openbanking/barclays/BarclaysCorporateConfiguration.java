package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;

public class BarclaysCorporateConfiguration extends UkOpenBankingAisConfiguration {

    BarclaysCorporateConfiguration(Builder builder) {
        super(builder);
    }

    @Override
    public ImmutableSet<UkObScope> getAvailablePermissions() {
        return ImmutableSet.<UkObScope>builder()
                .add(UkObScope.READ_ACCOUNTS_DETAIL)
                .add(UkObScope.READ_BALANCES)
                .add(UkObScope.READ_TRANSACTIONS_CREDITS)
                .add(UkObScope.READ_TRANSACTIONS_DEBITS)
                .add(UkObScope.READ_TRANSACTIONS_DETAIL)
                .add(UkObScope.READ_BENEFICIARIES_DETAIL)
                .add(UkObScope.READ_SCHEDULED_PAYMENTS_DETAIL)
                .add(UkObScope.READ_PRODUCTS)
                .build();
    }
}
