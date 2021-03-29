package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.bankofireland;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants.ConsentPermission;

public class BankOfIrelandAisConfiguration extends UkOpenBankingAisConfiguration {

    public BankOfIrelandAisConfiguration(Builder builder) {
        super(builder);
    }

    @Override
    public ImmutableSet<String> getPermissions() {
        return ImmutableSet.<String>builder()
                .add(
                        ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                        ConsentPermission.READ_BALANCES.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue(),
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue())
                .build();
    }
}
