package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.siliconvalley;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants.ConsentPermission;

public class SiliconValleyConfiguration extends UkOpenBankingAisConfiguration {

    public SiliconValleyConfiguration(Builder builder) {
        super(builder);
    }

    @Override
    public ImmutableSet<String> getPermissions() {
        return ImmutableSet.<String>builder()
                .add(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_DIRECT_DEBITS.getValue(),
                        ConsentPermission.READ_STANDING_ORDERS_DETAIL.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_BALANCES.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue())
                .build();
    }
}
