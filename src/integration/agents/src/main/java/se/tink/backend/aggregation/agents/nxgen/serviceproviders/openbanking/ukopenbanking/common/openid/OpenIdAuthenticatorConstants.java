package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class OpenIdAuthenticatorConstants {

    /** According to examples the max age is 24h */
    public static final long MAX_AGE = TimeUnit.DAYS.toSeconds(90);

    /**
     * "To indicate that secure customer authentication must be carried out as mandated by the PSD2
     * RTS"
     */
    public static final String ACR_SECURE_AUTHENTICATION_RTS = "urn:openbanking:psd2:sca";

    public enum ConsentPermission {
        READ_ACCOUNTS_DETAIL("ReadAccountsDetail"),
        READ_BALANCES("ReadBalances"),
        READ_BENEFICIARIES_DETAIL("ReadBeneficiariesDetail"),
        READ_DIRECT_DEBITS("ReadDirectDebits"),
        READ_PRODUCTS("ReadProducts"),
        READ_STANDING_ORDERS_DETAIL("ReadStandingOrdersDetail"),
        READ_TRANSACTIONS_CREDITS("ReadTransactionsCredits"),
        READ_TRANSACTIONS_DEBITS("ReadTransactionsDebits"),
        READ_TRANSACTIONS_DETAIL("ReadTransactionsDetail"),
        READ_OFFERS("ReadOffers"),
        READ_SCHEDULED_PAYMENTS_DETAIL("ReadScheduledPaymentsDetail"),
        READ_STATEMENTS_DETAIL("ReadStatementsDetail"),
        READ_PARTY("ReadParty"),
        READ_PARTY_PSU("ReadPartyPSU");

        private final String permissionValue;

        ConsentPermission(String permissionValue) {
            this.permissionValue = permissionValue;
        }

        public String getValue() {
            return permissionValue;
        }

        public static List<String> listAll() {
            return Stream.of(ConsentPermission.values())
                    .map(ConsentPermission::name)
                    .collect(Collectors.toList());
        }
    }

    public static final ImmutableSet<String> ALL_PERMISSIONS =
            ImmutableSet.<String>builder().addAll(ConsentPermission.listAll()).build();

    public static final ImmutableSet<String> ACCOUNT_FULL_PERMISSIONS =
            ImmutableSet.<String>builder()
                    .add(
                            ConsentPermission.READ_ACCOUNTS_DETAIL.getValue(),
                            ConsentPermission.READ_BALANCES.getValue(),
                            ConsentPermission.READ_BENEFICIARIES_DETAIL.getValue(),
                            ConsentPermission.READ_DIRECT_DEBITS.getValue(),
                            ConsentPermission.READ_STANDING_ORDERS_DETAIL.getValue(),
                            ConsentPermission.READ_TRANSACTIONS_CREDITS.getValue(),
                            ConsentPermission.READ_TRANSACTIONS_DEBITS.getValue(),
                            ConsentPermission.READ_TRANSACTIONS_DETAIL.getValue())
                    .build();

    public static class Params {
        public static final String REQUEST = "request";
        public static final String MAX_AGE = "max_age";
        public static final String CLAIMS = "claims";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
    }
}
