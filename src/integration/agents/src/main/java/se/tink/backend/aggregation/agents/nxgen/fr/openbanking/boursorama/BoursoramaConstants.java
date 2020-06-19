package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama;

import java.time.ZoneId;

public class BoursoramaConstants {

    public static final String USER_HASH = "USER_HASH";
    public static final String OAUTH_TOKEN = "OAUTH_TOKEN";

    public static final ZoneId ZONE_ID = ZoneId.of("CET");

    public static class Urls {

        public static final String CONSUME_AUTH_CODE =
                "/services/api/v1.7/_public_/authentication/oauth/consumeauthorizationcode";
        public static final String REFRESH_TOKEN =
                "/services/api/v1.7/_public_/authentication/oauth/refreshtoken";

        public static final String IDENTITY_TEMPLATE =
                "/services/api/v1.7/_user_/_%s_/dsp2/users/identity";
        public static final String ACCOUNTS_TEMPLATE =
                "/services/api/v1.7/_user_/_%s_/dsp2/accounts";
        public static final String BALANCES_TEMPLATE =
                "/services/api/v1.7/_user_/_%s_/dsp2/accounts/balances/";
        public static final String TRANSACTIONS_TEMPLATE =
                "/services/api/v1.7/_user_/_%s_/dsp2/accounts/transactions/";

        public static final String CREATE_PAYMENT =
                "/services/api/v1.7/_public_/dsp2/payment-requests";
        public static final String GET_PAYMENT =
                "/services/api/v1.7/_public_/dsp2/payment-requests/{paymentId}";
    }
}
