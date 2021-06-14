package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaFIConstants {
    public static final ImmutableMap<String, String> DEFAULT_FORM_PARAMS =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.AUTH_METHOD, "mta")
                    .put(FormParams.CLIENT_ID, "StNlrhoEJKZvCmfTSbiU")
                    .put(FormParams.COUNTRY, "FI")
                    .put(FormParams.SCOPE, "openid ndf agreement offline_access mpid")
                    .put(FormParams.REDIRECT_URI, "https://netbank.nordea.fi")
                    .build();

    public static final ImmutableMap<String, String> AUTH_FORM_PARAMS =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.CODE_CHALLENGE_METHOD, "S256")
                    .put(FormParams.GRANT_TYPE, "authorization_code")
                    .put(FormParams.RESPONSE_TYPE, "code")
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

    public static final TypeMapper<ThirdPartyAppStatus> AUTHENTICATION_RESPONSE =
            TypeMapper.<ThirdPartyAppStatus>builder()
                    .put(
                            ThirdPartyAppStatus.WAITING,
                            "external_authentication_required",
                            "external_authentication_pending")
                    .put(ThirdPartyAppStatus.CANCELLED, "authentication_cancelled")
                    .put(
                            ThirdPartyAppStatus.AUTHENTICATION_ERROR,
                            "invalid_request",
                            "authentication_failed")
                    .put(ThirdPartyAppStatus.ALREADY_IN_PROGRESS, "authentication_collision")
                    .build();

    public static final TypeMapper<ThirdPartyAppStatus> AUTHENTICATION_STATUS_RESPONSE =
            TypeMapper.<ThirdPartyAppStatus>builder()
                    .put(ThirdPartyAppStatus.DONE, "completed")
                    .put(ThirdPartyAppStatus.WAITING, "assignment_pending", "confirmation_pending")
                    .put(ThirdPartyAppStatus.CANCELLED, "cancelled")
                    .put(
                            ThirdPartyAppStatus.AUTHENTICATION_ERROR,
                            "invalid_request",
                            "authentication_failed")
                    .build();

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "transaction", "savings")
                    .put(AccountTypes.CREDIT_CARD, "credit", "combined")
                    .put(AccountTypes.LOAN, "mortgage")
                    .build();

    public static final TypeMapper<LoanDetails.Type> LOAN_TYPE_MAPPER =
            TypeMapper.<LoanDetails.Type>builder()
                    .put(LoanDetails.Type.MORTGAGE, "mortgage")
                    .put(LoanDetails.Type.CREDIT, "credit_loan")
                    .put(LoanDetails.Type.OTHER, "other") // consumer loan
                    .build();

    public static class Urls {
        private static final String BASE = "https://private.nordea.fi/api/dbf";

        public static final URL AUTHENTICATE = new URL(BASE + ApiService.AUTHENTICATE);
        public static final URL AUTHENTICATE_INIT = new URL(BASE + ApiService.AUTHENTICATE_INIT);
        public static final URL AUTHENTICATE_CODE = new URL(BASE + ApiService.AUTHENTICATE_CODE);
        public static final URL FETCH_ACCOUNTS = new URL(BASE + ApiService.FETCH_ACCOUNTS);
        public static final URL FETCH_CARDS = new URL(BASE + ApiService.FETCH_CARDS);
        public static final URL FETCH_INVESTMENTS = new URL(BASE + ApiService.FETCH_INVESTMENTS);
        public static final URL FETCH_LOANS = new URL(BASE + ApiService.FETCH_LOANS);
        public static final URL FETCH_CUSTOMER_INFO =
                new URL(BASE + ApiService.FETCH_CUSTOMER_INFO);
        public static final URL LOGOUT = new URL(BASE + ApiService.LOGOUT);
    }

    public static class ApiService {
        public static final String FETCH_TRANSACTIONS = "/transactions";
        private static final String FETCH_CARDS = "/ca/cards-v4/cards/";
        private static final String FETCH_INVESTMENTS = "/ca/savings-v1/savings/custodies";
        private static final String FETCH_LOANS = "/ca/loans-v1/loans/";
        private static final String AUTHENTICATE = "/ca/token-service-v3/oauth/token";
        private static final String AUTHENTICATE_INIT = "/ca/mta-v1/mta/authentications";
        private static final String AUTHENTICATE_CODE =
                "/ca/user-accounts-service-v1/user-accounts/primary/authorization";
        private static final String FETCH_ACCOUNTS = "/ca/accounts-v3/accounts/";
        private static final String FETCH_CUSTOMER_INFO = "/fi/customerinfo-v1/customers/info";
        private static final String LOGOUT = "/ca/token-revocation-v1/token/revoke";
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
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
    }

    public static class HeaderParams {
        public static final String LANGUAGE = "en-FI";
    }

    public static class QueryParams {
        public static final String LIMIT = "limit";
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "page_size";
        public static final String PAGE_SIZE_LIMIT = "30";
        public static final String CONTINUATION_KEY = "continuation_key";
    }

    public static class Fetcher {
        public static final int START_PAGE = 1;
    }

    public static class SessionStorage {
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String TOKEN_TYPE = "token_type";
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

        public static final String SERVICE_UNAVAILABLE = "service_unavailable";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 10;
        public static final int RETRY_SLEEP_MS = 3000;
        public static final int TIMEOUT_MS = 45000;
    }

    public static class LogTags {
        public static final LogTag CREDIT_TRANSACTIONS_ERROR =
                LogTag.from("NORDEA_FI_TRANSACTIONS_ERROR");
        public static final LogTag NORDEA_FI_AUTHENTICATE =
                LogTag.from("NORDEA_FI_AUTHENTICATION_ERROR");
        public static final LogTag NORDEA_FI_ACCOUNT_TYPE =
                LogTag.from("NORDEA_FI_UNKNOWN_ACCOUNT_TYPE");
    }
}
