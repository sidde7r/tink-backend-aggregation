package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.vanquis;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants.ConsentPermission;

class VanquisAisConfiguration extends UkOpenBankingAisConfiguration {

    VanquisAisConfiguration(Builder builder) {
        super(builder);
    }

    @Override
    public ImmutableSet<String> getPermissions() {
        return ImmutableSet.<String>builder()
                .add(ConsentPermission.READ_ACCOUNTS_BASIC.getValue())
                .add(ConsentPermission.READ_ACCOUNTS_DETAIL.getValue())
                .add(ConsentPermission.READ_BALANCES.getValue())
                .add(ConsentPermission.READ_STATEMENTS_BASIC.getValue())
                .add(ConsentPermission.READ_STATEMENTS_DETAIL.getValue())
                .add(ConsentPermission.READ_TRANSACTIONS_BASIC.getValue())
                .add(ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue())
                .add(ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue())
                .add(ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue())
                .build();
    }
}
