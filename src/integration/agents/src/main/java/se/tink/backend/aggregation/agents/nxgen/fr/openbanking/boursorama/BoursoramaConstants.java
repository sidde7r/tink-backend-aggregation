package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama;

import java.time.ZoneId;
import java.util.Locale;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BoursoramaConstants {

    public static final String USER_HASH = "USER_HASH";
    public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
    public static final String PISP_OAUTH_TOKEN = "PISP_TOKEN";

    public static final ZoneId ZONE_ID = ZoneId.of("CET");
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Paris");
    public static final Locale DEFAULT_LOCALE = new Locale("fr_FR", "FR");

    public static class Urls {
        private static final String BASE_URL = "https://api-dsp2.boursorama.com";
        public static final URL CONSUME_AUTH_CODE =
                new URL(
                        BASE_URL
                                + "/services/api/v1.7/_public_/authentication/oauth/consumeauthorizationcode");
        public static final URL REFRESH_TOKEN =
                new URL(BASE_URL + "/services/api/v1.7/_public_/authentication/oauth/refreshtoken");

        public static final URL IDENTITY_TEMPLATE =
                new URL(BASE_URL + "/services/api/v1.7/_user_/_{userHash}_/dsp2/users/identity");
        public static final URL ACCOUNTS_TEMPLATE =
                new URL(BASE_URL + "/services/api/v1.7/_user_/_{userHash}_/dsp2/accounts");
        public static final URL BALANCES_TEMPLATE =
                new URL(
                        BASE_URL
                                + "/services/api/v1.7/_user_/_{userHash}_/dsp2/accounts/balances/{resourceId}");
        public static final URL TRANSACTIONS_TEMPLATE =
                new URL(
                        BASE_URL
                                + "/services/api/v1.7/_user_/_{userHash}_/dsp2/accounts/transactions/{resourceId}");

        public static final URL TRUSTED_BENEFICIARIES_TEMPLATE =
                new URL(
                        BASE_URL
                                + "/services/api/v1.7/_user_/_{userHash}_/dsp2/trusted-beneficiaries");

        public static final URL PISP_TOKEN =
                URL.of(BASE_URL + "/services/api/v1.7/_public_/authentication/oauth/token");
        public static final URL CREATE_PAYMENT =
                new URL(BASE_URL + "/services/api/v1.7/_public_/dsp2/payment-requests");
        public static final URL GET_PAYMENT =
                new URL(BASE_URL + "/services/api/v1.7/_public_/dsp2/payment-requests/{paymentId}");
    }
}
