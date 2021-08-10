package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.vanquis;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.UkObScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;

class VanquisAisConfiguration extends UkOpenBankingAisConfiguration {

    VanquisAisConfiguration(Builder builder) {
        super(builder);
    }

    @Override
    public ImmutableSet<UkObScope> getAvailablePermissions() {
        return ImmutableSet.<UkObScope>builder()
                .add(UkObScope.READ_ACCOUNTS_BASIC)
                .add(UkObScope.READ_ACCOUNTS_DETAIL)
                .add(UkObScope.READ_BALANCES)
                .add(UkObScope.READ_OFFERS)
                .add(UkObScope.READ_STATEMENTS_BASIC)
                .add(UkObScope.READ_STATEMENTS_DETAIL)
                .add(UkObScope.READ_TRANSACTIONS_BASIC)
                .add(UkObScope.READ_TRANSACTIONS_CREDITS)
                .add(UkObScope.READ_TRANSACTIONS_DEBITS)
                .add(UkObScope.READ_TRANSACTIONS_DETAIL)
                .build();
    }
}
