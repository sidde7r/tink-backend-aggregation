package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class NordeaFIConstants {
    public static final String CURRENCY = "EUR";

    public static final ImmutableMap<String, String> DEFAULT_FORM_PARAMS =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.AUTH_METHOD, "mta")
                    .put(FormParams.CLIENT_ID, "NDHMFI")
                    .put(FormParams.COUNTRY, "FI")
                    .put(FormParams.GRANT_TYPE, "password")
                    .put(FormParams.SCOPE, "ndf")
                    .build();

    public static final TypeMapper<Instrument.Type> INSTRUMENT_TYPE_MAP =
            TypeMapper.<Instrument.Type>builder()
                    .put(Instrument.Type.FUND, "FUND")
                    .put(Instrument.Type.STOCK, "EQUITY")
                    .build();

    public static final TypeMapper<Portfolio.Type> PORTFOLIO_TYPE_MAP =
            TypeMapper.<Portfolio.Type>builder()
                    .put(Portfolio.Type.DEPOT, "FONDA", "ASBS")
                    .put(Portfolio.Type.ISK, "ISK")
                    .put(Portfolio.Type.PENSION, "ISP", "NLPV2")
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

    public static TypeMapper<ThirdPartyAppStatus> AUTHENTICATION_RESPONSE =
            TypeMapper.<ThirdPartyAppStatus>builder()
                    .put(
                            ThirdPartyAppStatus.WAITING,
                            "external_authentication_required",
                            "external_authentication_pending")
                    .put(ThirdPartyAppStatus.CANCELLED, "authentication_cancelled")
                    .put(ThirdPartyAppStatus.TIMED_OUT, "authentication_failed", "invalid_request")
                    .put(ThirdPartyAppStatus.ALREADY_IN_PROGRESS, "authentication_collision")
                    .build();

    public static TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "transaction", "savings")
                    .put(AccountTypes.CREDIT_CARD, "credit", "combined")
                    .put(AccountTypes.LOAN, "mortgage")
                    .build();

    public static class Urls {
        private static final String BASE = "https://private.nordea.fi/api/dbf/ca";

        public static final URL AUTHENTICATE = new URL(BASE + ApiService.AUTHENTICATE);
        public static final URL FETCH_ACCOUNTS = new URL(BASE + ApiService.FETCH_ACCOUNTS);
        public static final URL FETCH_CARDS = new URL(BASE + ApiService.FETCH_CARDS);
        public static final URL FETCH_INVESTMENTS = new URL(BASE + ApiService.FETCH_INVESTMENTS);
        public static final URL FETCH_LOANS = new URL(BASE + ApiService.FETCH_LOANS);
        public static final URL LOGOUT = new URL(BASE + ApiService.LOGOUT);
    }

    public static class ApiService {
        public static final String FETCH_TRANSACTIONS = "/transactions";
        public static final String FETCH_CARDS = "/cards-v2/cards/";
        public static final String FETCH_INVESTMENTS = "/savings-v1/savings/custodies";
        public static final String FETCH_LOANS = "/loans-v1/loans/";
        private static final String AUTHENTICATE = "/authentication-mta-v1/security/oauth/token";
        private static final String FETCH_ACCOUNTS = "/accounts-v1/accounts/";
        private static final String LOGOUT = "/token-revocation-v1/token/revoke";
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
        public static final String CODE = "code";
    }

    public static class HeaderParams {
        public static final String LANGUAGE = "en-FI";
    }

    public static class QueryParams {
        public static final String OFFSET = "offset";
        public static final String LIMIT = "limit";
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "page_size";
        public static final String PAGE_SIZE_LIMIT = "30";
        public static final String STATUS = "status";
        public static final String STATUS_VALUES = "unconfirmed,confirmed,rejected,inprogress";
    }

    public static class Fetcher {
        public static final int START_PAGE = 1;
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
        public static final String USERNAME = "username";
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

    public static class LogTags {
        public static final LogTag CREDIT_TRANSACTIONS_ERROR =
                LogTag.from("NORDEA_FI_TRANSACTIONS_ERROR");
        public static final LogTag NORDEA_FI_AUTHENTICATE =
                LogTag.from("NORDEA_FI_AUTHENTICATION_ERROR");
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
}
