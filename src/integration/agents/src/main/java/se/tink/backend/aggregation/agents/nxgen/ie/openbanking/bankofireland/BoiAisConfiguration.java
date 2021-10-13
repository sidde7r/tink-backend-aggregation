package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.bankofireland;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;

public class BoiAisConfiguration extends UkOpenBankingAisConfiguration {

    public BoiAisConfiguration(Builder builder) {
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
                .add(UkObScope.READ_SCHEDULED_PAYMENTS_DETAIL)
                .build();
    }
}
