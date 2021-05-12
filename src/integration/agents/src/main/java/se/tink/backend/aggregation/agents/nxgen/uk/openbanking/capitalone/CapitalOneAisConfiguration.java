package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.capitalone;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;

public class CapitalOneAisConfiguration extends UkOpenBankingAisConfiguration {

    public CapitalOneAisConfiguration(Builder builder) {
        super(builder);
    }

    @Override
    public ImmutableSet<String> getPermissions() {
        return ImmutableSet.<String>builder()
                .add(
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_ACCOUNTS_DETAIL
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_BALANCES.getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_CREDITS
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DEBITS
                                .getValue(),
                        OpenIdAuthenticatorConstants.ConsentPermission.READ_TRANSACTIONS_DETAIL
                                .getValue())
                .build();
    }
}
