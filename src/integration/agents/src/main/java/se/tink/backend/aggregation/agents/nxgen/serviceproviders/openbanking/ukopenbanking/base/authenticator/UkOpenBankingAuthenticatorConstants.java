package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.concurrent.TimeUnit;

public abstract class UkOpenBankingAuthenticatorConstants {

    /** According to examples the max age is 24h */
    public static final long MAX_AGE = TimeUnit.DAYS.toSeconds(90);

    /**
     * "To indiciate that secure customer authentication must be carried out as mandated by the PSD2
     * RTS"
     */
    public static final String ACR_SECURE_AUTHENTICATION_RTS = "urn:openbanking:psd2:sca";

    public static final ImmutableList<String> ACCOUNT_PERMISSIONS =
            ImmutableList.<String>builder()
                    .add(
                            "ReadAccountsDetail",
                            "ReadBalances",
                            "ReadBeneficiariesDetail",
                            "ReadDirectDebits",
                            "ReadProducts",
                            "ReadStandingOrdersDetail",
                            "ReadTransactionsCredits",
                            "ReadTransactionsDebits",
                            "ReadTransactionsDetail")
                    .build();

    public static class Params {
        public static final String REQUEST = "request";
        public static final String MAX_AGE = "max_age";
        public static final String CLAIMS = "claims";
    }
}
