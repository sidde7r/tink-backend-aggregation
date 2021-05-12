package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofireland;

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
                        ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue(),
                        ConsentPermission.READ_BENEFICIARIES_DETAIL.getValue(),
                        ConsentPermission.READ_PRODUCTS.getValue(),
                        ConsentPermission.READ_SCHEDULED_PAYMENTS_DETAIL.getValue(),
                        ConsentPermission.READ_STANDING_ORDERS_DETAIL.getValue(),
                        ConsentPermission.READ_STATEMENTS_DETAIL.getValue())
                .build();
    }
}
