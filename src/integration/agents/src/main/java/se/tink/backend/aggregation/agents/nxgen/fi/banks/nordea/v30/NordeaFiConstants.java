package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class NordeaFiConstants {

    private static class ApiServices {
        private static final String AUTHENTICATE_PATH =
                "authentication-mta-v1/security/oauth/token";
        private static final String PRODUCT_PATH = "xfm-product-v1/xfm/products?";
        private static final String TRANSACTIONS_PATH = "xfm-transaction-v1/xfm/transactions?";
        private static final String CARDS_DETAILED_PATH = "cards-v1/cards/";
        private static final String SAVINGS_PATH = "savings-v1/savings/custodies?";
        private static final String LOGOUT_PATH = "token-revocation-v1/token/revoke";
    }

    public static class Urls {
        private static final String URL_BASE = "https://private.nordea.fi/api/dbf/ca/";

        public static final URL AUTHENTICATE = new URL(URL_BASE + ApiServices.AUTHENTICATE_PATH);
        public static final URL FETCH_PRODUCT = new URL(URL_BASE + ApiServices.PRODUCT_PATH);
        public static final URL FETCH_TRANSACTIONS =
                new URL(URL_BASE + ApiServices.TRANSACTIONS_PATH);
        public static final URL FETCH_CARDS_DETAILED =
                new URL(URL_BASE + ApiServices.CARDS_DETAILED_PATH);
        public static final URL FETCH_SAVINGS = new URL(URL_BASE + ApiServices.SAVINGS_PATH);
        public static final URL LOGOUT = new URL(URL_BASE + ApiServices.LOGOUT_PATH);
    }

    public static class QueryParams {

        public static final String PRODUCT_CATEGORY = "product_category";
        public static final String PRODUCT_ID = "product_id";
        public static final String OFFSET = "offset";
        public static final String LIMIT = "limit";
        public static final String TYPE = "type";
    }

    public static class Products {

        public static final String ACCOUNT = "account";
        public static final String CARD = "card";
        public static final String LOAN = "loan";
        public static final String SAVINGS = "FREE_CUSTODY";
    }

    public static class SessionStorage {
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String REFRESH_EXPIRES_IN = "refresh_expires_in";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String SCOPE = "scope";
        public static final String EXPIRES_IN = "expires_in";
        public static final String ISSUED_TOKEN_TYPE = "issued_token_type";
        public static final String TOKEN_TYPE = "token_type";
        public static final String USER_ID = "user_id";
        public static final String AGREEMENT_ID = "agreement_id";
    }

    public static class FormParams {
        public static final String AUTH_METHOD = "auth_method";
        public static final String CLIENT_ID = "client_id";
        public static final String COUNTRY = "country";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
        public static final String TOKEN = "token";
        public static final String TOKEN_TYPE_HINT = "token_type_hint";
        public static final String USERNAME = "username";
        public static final String AUTH_REF = "code";
    }

    public static class LogTags {
        public static final LogTag NORDEA_FI_LOAN = LogTag.from("nordea_fi_loans");
    }

    public static class ErrorCodes {
        // user has no agreement (for investments)
        public static final String AGREEMENT_NOT_CONFIRMED =
                "RBO_ACCESS_DENIED_AGREEMENT_NOT_CONFIRMED";
        public static final String CLASSIFICATION_NOT_CONFIRMED =
                "RBO_ACCESS_DENIED_CLASSIFICATION_NOT_CONFIRMED";

        // user has no account connected to depot, cannot fetch investments
        public static final String UNABLE_TO_LOAD_CUSTOMER = "ERROR_OSIA_UNABLE_TO_LOAD_CUSTOMER";
        // access token has expired
        public static final String INVALID_TOKEN = "invalid_token";
        // refresh token has expired
        public static final String INVALID_GRANT = "invalid_grant";
    }

    public static class NordeaCodesPayload {

        private static final LocalizableKey DOWNLOAD_TITLE =
                new LocalizableKey("Download Nordea Codes");
        private static final LocalizableKey DOWNLOAD_MESSAGE =
                new LocalizableKey(
                        "You need to download the Nordea Codes app in order to continue.");
        private static final LocalizableKey UPGRADE_TITLE =
                new LocalizableKey("Upgrade Nordea Codes");
        private static final LocalizableKey UPGRADE_MESSAGE =
                new LocalizableKey(
                        "You need to upgrade the Nordea Codes app in order to continue.");

        private static final String CODES_APP_STORE_URL =
                "https://itunes.apple" + ".com/se/app/nordea-codes/id995971128";
        private static final String CODES_APP_SCHEME = "nordeamta://";
        private static final String CODES_RETURN_LINK = "confirm?returnUrl=tink://";
        private static final String CODES_ANDROID_PACKAGE_NAME = "com.nordea.mobiletoken";
        private static final int CODES_REQUIRED_ANDROID_VERSION = 1050200; // 1.5.2.0

        public static ThirdPartyAppAuthenticationPayload build() {

            ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

            // Generic things about download and upgrade
            payload.setDownloadTitle(DOWNLOAD_TITLE.get());
            payload.setDownloadMessage(DOWNLOAD_MESSAGE.get());

            payload.setUpgradeTitle(UPGRADE_TITLE.get());
            payload.setUpgradeMessage(UPGRADE_MESSAGE.get());

            // iOS details
            ThirdPartyAppAuthenticationPayload.Ios iosPayload =
                    new ThirdPartyAppAuthenticationPayload.Ios();
            iosPayload.setAppScheme(CODES_APP_SCHEME);
            iosPayload.setDeepLinkUrl(CODES_APP_SCHEME + CODES_RETURN_LINK);
            iosPayload.setAppStoreUrl(CODES_APP_STORE_URL);
            payload.setIos(iosPayload);

            // Android details
            ThirdPartyAppAuthenticationPayload.Android androidPayload =
                    new ThirdPartyAppAuthenticationPayload.Android();
            androidPayload.setPackageName(CODES_ANDROID_PACKAGE_NAME);
            androidPayload.setRequiredVersion(CODES_REQUIRED_ANDROID_VERSION);
            androidPayload.setIntent(CODES_APP_SCHEME + CODES_RETURN_LINK);
            payload.setAndroid(androidPayload);

            return payload;
        }
    }

    public static final ImmutableMap<String, ThirdPartyAppStatus> AUTHENTICATION_RESPONSE =
            ImmutableMap.<String, ThirdPartyAppStatus>builder()
                    .put("external_authentication_required", ThirdPartyAppStatus.WAITING)
                    .put("external_authentication_pending", ThirdPartyAppStatus.WAITING)
                    .put("authentication_cancelled", ThirdPartyAppStatus.CANCELLED)
                    .put("authentication_failed", ThirdPartyAppStatus.TIMED_OUT)
                    .put("invalid_request", ThirdPartyAppStatus.TIMED_OUT)
                    .put("authentication_collision", ThirdPartyAppStatus.ALREADY_IN_PROGRESS)
                    .build();

    public static final ImmutableMap<ThirdPartyAppStatus, LocalizableKey>
            AUTHENTICATION_ERROR_MESSAGE =
                    ImmutableMap.<ThirdPartyAppStatus, LocalizableKey>builder()
                            .put(
                                    ThirdPartyAppStatus.CANCELLED,
                                    new LocalizableKey(
                                            "Authentication cancelled by the Codes app. Please try again."))
                            .put(
                                    ThirdPartyAppStatus.TIMED_OUT,
                                    new LocalizableKey("Authentication timed out."))
                            .put(
                                    ThirdPartyAppStatus.ALREADY_IN_PROGRESS,
                                    new LocalizableKey(
                                            "Another client is already trying to sign in. \nPlease close the Codes app and try again."))
                            .build();

    public static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES =
            ImmutableMap.<String, AccountTypes>builder()
                    .put("savings", AccountTypes.SAVINGS)
                    .put("transaction", AccountTypes.CHECKING)
                    .build();

    public static final ImmutableMap<String, String> DEFAULT_FORM_PARAMS =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.AUTH_METHOD, "mta")
                    .put(FormParams.CLIENT_ID, "NDHMFI")
                    .put(FormParams.COUNTRY, "FI")
                    .put(FormParams.GRANT_TYPE, "password")
                    .put(FormParams.SCOPE, "ndf")
                    .build();

    private static final ImmutableMap<String, Instrument.Type> INSTRUMENT_TYPE_MAP =
            ImmutableMap.<String, Instrument.Type>builder()
                    .put("FUND", Instrument.Type.FUND)
                    .put("EQUITY", Instrument.Type.STOCK)
                    .build();

    public static Instrument.Type GET_INSTRUMENT_TYPE(String rawType) {
        return INSTRUMENT_TYPE_MAP.getOrDefault(rawType.toUpperCase(), Instrument.Type.OTHER);
    }

    private static final ImmutableMap<String, Portfolio.Type> PORTFOLIO_TYPE_MAP =
            ImmutableMap.<String, Portfolio.Type>builder()
                    .put("FONDA", Portfolio.Type.DEPOT)
                    .put("ISK", Portfolio.Type.ISK)
                    .put("ISP", Portfolio.Type.PENSION)
                    .put("ASBS", Portfolio.Type.DEPOT)
                    .build();

    public static Portfolio.Type GET_PORTFOLIO_TYPE(String rawType) {
        return PORTFOLIO_TYPE_MAP.getOrDefault(rawType.toUpperCase(), Portfolio.Type.OTHER);
    }
}
