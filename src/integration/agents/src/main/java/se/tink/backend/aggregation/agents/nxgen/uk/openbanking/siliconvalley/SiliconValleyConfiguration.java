package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.siliconvalley;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;

public class SiliconValleyConfiguration extends UkOpenBankingAisConfiguration {

    public SiliconValleyConfiguration(Builder builder) {
        super(builder);
    }

    @Override
    public ImmutableSet<UkObScope> getAvailablePermissions() {
        return ImmutableSet.<UkObScope>builder()
                .add(
                        UkObScope.READ_ACCOUNTS_DETAIL,
                        UkObScope.READ_BALANCES,
                        UkObScope.READ_TRANSACTIONS_CREDITS,
                        UkObScope.READ_TRANSACTIONS_DEBITS,
                        UkObScope.READ_TRANSACTIONS_DETAIL)
                .build();
    }
}
