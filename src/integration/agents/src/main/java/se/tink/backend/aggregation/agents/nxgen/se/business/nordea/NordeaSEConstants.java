package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import static se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.StorageKeys.SENSITIVE_PAYLOAD_PASSWORD;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaSEConstants {

    private NordeaSEConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static class Urls {
        private Urls() {}

        public static final String BASE_URL = "https://corporate.nordea.se/api/dbf/";

        public static final String INIT_BANKID = BASE_URL + Endpoints.INIT_BANKID;
        public static final String POLL_BANKID = BASE_URL + Endpoints.POLL_BANKID;
        public static final String FETCH_TOKEN = BASE_URL + Endpoints.FETCH_TOKEN;
        public static final String FETCH_ACCOUNT = BASE_URL + Endpoints.FETCH_ACCOUNTS;
        public static final String FETCH_ACCOUNT_DETAILS =
                BASE_URL + Endpoints.FETCH_ACCOUNT_DETAILS;
        public static final String FETCH_TRANSACTIONS = BASE_URL + Endpoints.FETCH_TRANSACTIONS;
        public static final URL LOGIN_BANKID_AUTOSTART =
                new URL(BASE_URL + ApiService.LOGIN_BANKID_AUTOSTART);
        public static final URL FETCH_LOGIN_CODE = new URL(BASE_URL + ApiService.FETCH_LOGIN_CODE);
        public static final URL FETCH_ACCESS_TOKEN =
                new URL(BASE_URL + ApiService.FETCH_ACCESS_TOKEN);
        public static final URL LOGIN_BANKID = new URL(BASE_URL + ApiService.LOGIN_BANKID);
        public static final URL FETCH_ACCOUNTS = new URL(BASE_URL + ApiService.FETCH_ACCOUNTS);
        public static final URL FETCH_ACCOUNT_TRANSACTIONS =
                new URL(BASE_URL + ApiService.FETCH_TRANSACTIONS);
        public static final URL FETCH_CARDS = new URL(BASE_URL + ApiService.FETCH_CARDS);
        public static final URL FETCH_CARD_TRANSACTIONS =
                new URL(BASE_URL + ApiService.FETCH_CARD_TRANSACTIONS);
        public static final URL FETCH_INVESTMENTS =
                new URL(BASE_URL + ApiService.FETCH_INVESTMENTS);
        public static final URL FETCH_LOANS = new URL(BASE_URL + ApiService.FETCH_LOANS);
        public static final URL FETCH_LOAN_DETAILS =
                new URL(BASE_URL + ApiService.FETCH_LOAN_DETAILS);
        public static final URL FETCH_IDENTITY_DATA =
                new URL(BASE_URL + ApiService.FETCH_IDENTITY_DATA);
    }

    public static class IdTags {
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String CARD_ID = "cardId";
        public static final String PAYMENT_ID = "paymentId";
        public static final String APPLICATION_ID = "applicationId";
    }

    public static class StorageKeys {
        public static final String ACCESS_TOKEN = "auth_token";
        public static final String TOKEN_TYPE = "token_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ID_TOKEN = "id_token";
        public static final String UH = "uh";
        public static final String SSN = "ssn";
        public static final String TOKEN_AUTH_METHOD = "auth_type";
        public static final String SENSITIVE_PAYLOAD_PASSWORD = "password";
        public static final String HOLDER_NAME = "holder_name";
    }

    public static class FormParams {
        public static final String AUTH_METHOD = "auth_method";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String COUNTRY = "country";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
        public static final String USERNAME = "username";
        public static final String TOKEN = "token";
        public static final String TOKEN_TYPE = "access_token";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String COUNTRY_VALUE = "SE";
        public static final String CLIENT_ID_VALUE = "NDHMSE";
    }

    public static class Endpoints {
        private Endpoints() {}

        public static final String INIT_BANKID =
                "/SE/MobileBankIdServiceV1.1/MobileBankIdInitialAuthentication";
        public static final String POLL_BANKID =
                "/SE/MobileBankIdServiceV1.1/MobileBankIdAuthenticationResult/";
        public static final String FETCH_TOKEN = "/SE/AuthenticationServiceV1.1/SecurityToken";
        public static final String FETCH_ACCOUNTS = "ca/accounts-v2/accounts/";
        public static final String FETCH_ACCOUNT_DETAILS = "SE/BankingServiceV1.1/Accounts/";
        public static final String FETCH_TRANSACTIONS = "SE/BankingServiceV1.1/Transactions";
    }

    public static class ApiService {
        public static final String LOGIN_BANKID_AUTOSTART =
                "ca/bankidse-v1/bankidse/authentications/";
        public static final String FETCH_LOGIN_CODE =
                "ca/user-accounts-service-v1/user-accounts/primary/authorization";
        public static final String FETCH_ACCESS_TOKEN = "ca/token-service-v3/oauth/token";
        public static final String LOGIN_BANKID =
                "se/authentication-bankid-v1/security/oauth/token";
        public static final String FETCH_ACCOUNTS = "ca/accounts-v3/accounts/";
        public static final String FETCH_TRANSACTIONS =
                "ca/accounts-v3/accounts/{accountNumber}/transactions";
        public static final String FETCH_CARDS = "ca/cards-v2/cards/";
        public static final String FETCH_CARD_TRANSACTIONS =
                "ca/cards-v3/cards/{cardId}/transactions";
        public static final String FETCH_INVESTMENTS = "ca/savings-v1/savings/custodies";
        public static final String FETCH_LOANS = "ca/loans-v1/loans/";
        public static final String FETCH_LOAN_DETAILS = "ca/loans-v1/loans/{loanId}";
        public static final String FETCH_IDENTITY_DATA = "se/customerinfo-v2/customers/info";
    }

    public static class LogMessages {
        public static final String BANKSIDE_ERROR_WHEN_SEARCHING_OUTBOX =
                "Error from bank when trying to fetch details about payment outbox";
        public static final String WRONG_TO_ACCOUNT_LENGTH =
                "Invalid destination account number, it is too long.";
        public static final String WRONG_OCR_MESSAGE = "Error in reference number (OCR)";
        public static final String USER_UNAUTHORIZED_MESSAGE = "User not authorised to operation";
    }

    public static final class HeaderParams {
        public static final String LANGUAGE = "en-SE";
        public static final String REFERER_VALUE =
                "https://corporate.nordea.se/inapp?app_channel=NDCM_SE_IOS&consent_insight=true&consent_marketing=true";
    }

    public static class QueryKeys {
        private QueryKeys() {}

        public static final String ACCOUNT_ID = "productId";
        public static final String CONTINUE_KEY = "continueKey";
    }

    public static class Headers {
        private Headers() {}

        public static final String REQUEST_ID = "x-Request-Id";
        public static final String REFERER = "Referer";
    }

    public static final ImmutableMap<String, Object> NORDEA_CUSTOM_HEADERS =
            ImmutableMap.<String, Object>builder()
                    .put("x-App-Country", "SE")
                    .put("x-App-Language", "en_SE")
                    .put("x-App-Version", "3.14.0.148")
                    .put("x-Device-Make", "Apple")
                    .put("x-Device-Model", "iPhone9,4")
                    .put("x-Platform-Type", "iOS")
                    .put("x-Platform-Version", "13.3.1")
                    .put("x-app-segment", "corporate")
                    .put("x-device-ec", 1)
                    .put("x-Device-Id", "934B5E23-119E-4E8F-BE66-D7D3B285F744")
                    .put(
                            "User-Agent",
                            "com.nordea.SMEMobileBank.se/3.14.0.148 (Apple iPhone9,4; iOS 13.3.1)")
                    .build();
    public static final ImmutableMap<String, String> REQUEST_TOKEN_FORM =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.AUTH_METHOD, AuthMethod.BANKID_SE)
                    .put(FormParams.CLIENT_ID, TagValues.APPLICATION_ID)
                    .put(FormParams.COUNTRY, FormParams.COUNTRY_VALUE)
                    .put(FormParams.GRANT_TYPE, "authorization_code")
                    .put(FormParams.REDIRECT_URI, TagValues.REDIRECT_URI)
                    .put(FormParams.SCOPE, TagValues.SCOPE_VALUE)
                    .build();
    public static final ImmutableMap<String, String> DEFAULT_FORM_PARAMS =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.AUTH_METHOD, AuthMethod.BANKID_SE)
                    .put(FormParams.CLIENT_ID, FormParams.CLIENT_ID_VALUE)
                    .put(FormParams.COUNTRY, FormParams.COUNTRY_VALUE)
                    .put(FormParams.GRANT_TYPE, SENSITIVE_PAYLOAD_PASSWORD)
                    .put(FormParams.SCOPE, TagValues.SCOPE_VALUE)
                    .build();
    public static final ImmutableMap<String, String> REFRESH_TOKEN_FORM =
            ImmutableMap.<String, String>builder()
                    .put(FormParams.CLIENT_ID, FormParams.CLIENT_ID_VALUE)
                    .build();
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "transaction")
                    .put(AccountTypes.SAVINGS, "savings")
                    .put(AccountTypes.CREDIT_CARD, "credit", "combined")
                    .put(AccountTypes.LOAN, "mortgage")
                    .build();

    public static class BankIdStatus {
        private BankIdStatus() {}

        public static final String COMPLETE = "COMPLETE";
        public static final String WAITING = "OUTSTANDING_TRANSACTION";
        public static final String NO_CLIENT = "NO_CLIENT";
    }

    public static class NordeaBankIdStatus {
        private NordeaBankIdStatus() {}

        public static final String BANKID_AUTOSTART_PENDING = "assignment_pending";
        public static final String BANKID_AUTOSTART_SIGN_PENDING = "confirmation_pending";
        public static final String BANKID_AUTOSTART_COMPLETED = "completed";
        public static final String BANKID_AUTOSTART_CANCELLED = "cancelled";
        public static final String AGREEMENTS_UNAVAILABLE = "agreements_unavailable";
        public static final String EXTERNAL_AUTHENTICATION_REQUIRED =
                "external_authentication_required";
        public static final String AUTHENTICATION_CANCELLED = "authentication_cancelled";
        public static final String EXTERNAL_AUTHENTICATION_PENDING =
                "external_authentication_pending";
        public static final String PENDING = "PENDING";
        public static final String CANCELLED = "CANCELLED";
        public static final String OK = "OK";
    }

    public static class AccountType {
        private AccountType() {}

        private static final String OMBUDSKONTO = "Ombudskonto";
        private static final String DEPOSIT = "Deposit";
        private static final String FASTRANTEPLACERING = "Fastränteplacering";
        private static final String CENTRALKONTO = "Centralkonto";

        private static final Map<String, String> ACCOUNT_NAMES_BY_CODE = Maps.newHashMap();
        private static final Map<String, TransactionalAccountType> ACCOUNT_TYPES_BY_CODE =
                Maps.newHashMap();

        static {
            addType("SE0000", "Personkonto", TransactionalAccountType.CHECKING);
            addType("SE0001", "Fn-konto", TransactionalAccountType.CHECKING);
            addType("SE0002", OMBUDSKONTO, TransactionalAccountType.CHECKING);
            addType("SE0004", "Synskadekonto", TransactionalAccountType.CHECKING);
            addType("SE0005", "Utlandslönekonto", TransactionalAccountType.CHECKING);
            addType("SE1101", "Private Bankingkonto", TransactionalAccountType.CHECKING);
            addType("SE0100", "Personkonto Solo", TransactionalAccountType.CHECKING);
            addType("SE0101", "Fn-konto Solo", TransactionalAccountType.CHECKING);
            addType("SE0104", "Synskadekonto Solo", TransactionalAccountType.CHECKING);
            addType("SE0105", "Utlandslönekto Solo", TransactionalAccountType.CHECKING);
            addType("SE0200", "Personkonto-student", TransactionalAccountType.CHECKING);
            addType("SE0300", "Personkonto-ungdom", TransactionalAccountType.CHECKING);
            addType("SE0302", OMBUDSKONTO, TransactionalAccountType.CHECKING);
            addType("SE0304", "Synskadekonto", TransactionalAccountType.CHECKING);
            addType("SE0402", "IPS cash account", TransactionalAccountType.CHECKING);
            addType("SE0500", "Depålikvidkonto", TransactionalAccountType.CHECKING);
            addType("SE0501", "ISK Trader likvidkonto", TransactionalAccountType.CHECKING);
            addType("SE0502", OMBUDSKONTO, TransactionalAccountType.CHECKING);
            addType("SE0600", "Pgkonto Privat", TransactionalAccountType.CHECKING);
            addType("SE0606", "Eplusgiro Privat", TransactionalAccountType.CHECKING);
            addType("SE0700", "Pgkonto Privat Ftg", TransactionalAccountType.CHECKING);
            addType("SE0706", "Eplusgiro Privat Ftg", TransactionalAccountType.CHECKING);
            addType("SE0900", "Flexkonto", TransactionalAccountType.CHECKING);

            addType("SE1000", "Checkkonto", TransactionalAccountType.CHECKING);
            addType("SE1001", "Pensionskredit", TransactionalAccountType.CHECKING);
            addType("SE1002", "Boflex", TransactionalAccountType.CHECKING);
            addType("SE1003", "Boflex pension", TransactionalAccountType.CHECKING);
            addType("SE1004", "Buffertkonto kredit", TransactionalAccountType.CHECKING);
            addType("SE1100", "Aktielikvidkonto", TransactionalAccountType.CHECKING);
            addType("SE1200", "Externt Konto", TransactionalAccountType.CHECKING);
            addType("SE1211", "Zb Ext Top", TransactionalAccountType.CHECKING);
            addType("SE1212", "Zb Externt Sub Gr Lvl", TransactionalAccountType.CHECKING);
            addType("SE1213", "Zb Externt Sub Acc", TransactionalAccountType.CHECKING);
            addType("SE1214", "Zb Externt Adj Acc", TransactionalAccountType.CHECKING);
            addType("SE1300", "Cross-Border Account", TransactionalAccountType.CHECKING);
            addType("SE1311", "Zb Cross-Border Top", TransactionalAccountType.CHECKING);
            addType("SE1312", "Zb C-B Sub Gr Level", TransactionalAccountType.CHECKING);
            addType("SE1313", "Zb C-B Sub Account", TransactionalAccountType.CHECKING);
            addType("SE1314", "Zb Cross-Border Adj", TransactionalAccountType.CHECKING);
            addType("SE1400", "Föreningskonto", TransactionalAccountType.CHECKING);
            addType("SE1411", "Zb Föreningskonto Top", TransactionalAccountType.CHECKING);
            addType("SE1412", "Zb Före.Kto Sub Gr Level", TransactionalAccountType.CHECKING);
            addType("SE1413", "Zb Föreningskonto Sub Acc", TransactionalAccountType.CHECKING);
            addType("SE1414", "Zb Föreningskonto Adj Acc", TransactionalAccountType.CHECKING);
            addType("SE1500", "Församlingskonto", TransactionalAccountType.CHECKING);
            addType("SE1600", "Baskonto", TransactionalAccountType.CHECKING);
            addType("SE1700", "Direktkonto", TransactionalAccountType.CHECKING);
            addType("SE1701", "Affärskonto", TransactionalAccountType.CHECKING);
            addType("SE1711", "Zb Direktkonto Top", TransactionalAccountType.CHECKING);
            addType("SE1712", "Zb Dir.Konto Sub Gr Level", TransactionalAccountType.CHECKING);
            addType("SE1713", "Zb Dir.Konto Sub Account", TransactionalAccountType.CHECKING);
            addType("SE1714", "Zb Dir.Konto Adj Account", TransactionalAccountType.CHECKING);
            addType("SE1800", "Företagskonto", TransactionalAccountType.CHECKING);
            addType("SE1801", "Top Account", TransactionalAccountType.CHECKING);
            addType("SE1802", "Sub Group Level", TransactionalAccountType.CHECKING);
            addType("SE1803", "Sub Account", TransactionalAccountType.CHECKING);
            addType("SE1804", "Adjustement Account", TransactionalAccountType.CHECKING);
            addType("SE1811", "Zb Företagskonto Top", TransactionalAccountType.CHECKING);
            addType("SE1812", "Zb Ftgskonto Sub Gr Level", TransactionalAccountType.CHECKING);
            addType("SE1813", "Zb Företagskonto Sub Acc", TransactionalAccountType.CHECKING);
            addType("SE1814", "Zb Företagskonto Adj Acc", TransactionalAccountType.CHECKING);
            addType("SE1900", "Arbetsgivarkonto", TransactionalAccountType.CHECKING);
            addType("SE1901", "Avdragsmottagarkonto", TransactionalAccountType.CHECKING);
            addType("SE1902", "Floatkonto", TransactionalAccountType.CHECKING);

            addType("SE2000", "Girokapital Kfm", TransactionalAccountType.CHECKING);
            addType("SE2100", "Specialkonto", TransactionalAccountType.CHECKING);
            addType("SE2200", "Sparkonto Företag", TransactionalAccountType.SAVINGS);
            addType("SE2300", "Koncern Plusgirokonto", TransactionalAccountType.CHECKING);
            addType("SE2311", "Koncern Toppkonto", TransactionalAccountType.CHECKING);
            addType("SE2312", "Koncern Samlingskto", TransactionalAccountType.CHECKING);
            addType("SE2313", "Koncern Transkonto", TransactionalAccountType.CHECKING);
            addType("SE2400", "Myndighetskonto", TransactionalAccountType.CHECKING);
            addType("SE2405", "Myndighetskonto", TransactionalAccountType.CHECKING);
            addType("SE2411", "Myndighet Toppkonto", TransactionalAccountType.CHECKING);
            addType("SE2412", "Myndighet Samlingskto", TransactionalAccountType.CHECKING);
            addType("SE2413", "Myndighet Transkonto", TransactionalAccountType.CHECKING);
            addType("SE2499", "Avsl Myndighetskonto", TransactionalAccountType.CHECKING);
            addType("SE2500", "Plusgirokonto Nordea", TransactionalAccountType.CHECKING);
            addType("SE2700", "Internt Arbetskonto", TransactionalAccountType.CHECKING);
            addType("SE2900", "Plusgirokonto", TransactionalAccountType.CHECKING);

            addType("SE4000", "Spara Kapital", TransactionalAccountType.SAVINGS);
            addType("SE4300", "ISK Classic likvidkonto", TransactionalAccountType.SAVINGS);
            addType("SE4309", "ISK Classic likvidkonto", TransactionalAccountType.SAVINGS);
            addType("SE4400", "Skatteutjämningskonto", TransactionalAccountType.SAVINGS);
            addType("SE4401", "Skogskonto", TransactionalAccountType.SAVINGS);
            addType("SE4402", "Skogsskadekonto", TransactionalAccountType.SAVINGS);
            addType("SE4403", "Upphovsmannakonto", TransactionalAccountType.SAVINGS);
            addType("SE4404", "Allm Investeringskonto", TransactionalAccountType.SAVINGS);
            addType("SE4405", "Uppfinnarkonto", TransactionalAccountType.SAVINGS);
            addType("SE4500", "Sparkonto", TransactionalAccountType.SAVINGS);
            addType("SE4600", DEPOSIT, TransactionalAccountType.SAVINGS);
            addType("SE4601", DEPOSIT, TransactionalAccountType.SAVINGS);
            addType("SE4602", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4603", "Dagsinlåning", TransactionalAccountType.SAVINGS);
            addType("SE4604", DEPOSIT, TransactionalAccountType.SAVINGS);
            addType("SE4605", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4606", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4607", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4608", "Placering 4 År", TransactionalAccountType.SAVINGS);
            addType("SE4609", FASTRANTEPLACERING, TransactionalAccountType.SAVINGS);
            addType("SE4610", "Bonuskonto, utgåva", TransactionalAccountType.SAVINGS);
            addType("SE4611", "Tillväxtkonto", TransactionalAccountType.SAVINGS);
            addType("SE4700", "Planeringskonto 2 År", TransactionalAccountType.SAVINGS);
            addType("SE4800", "Planeringskonto 3 År", TransactionalAccountType.SAVINGS);
            addType("SE4900", "Planeringskonto 4 År", TransactionalAccountType.SAVINGS);

            addType("SE5000", "Planeringskonto 5 År", TransactionalAccountType.SAVINGS);
            addType("SE5100", "Kapitalkonto", TransactionalAccountType.SAVINGS);
            addType("SE5200", "Skogslikvidkonto", TransactionalAccountType.SAVINGS);
            addType("SE5400", "Banksparkonto Ips", TransactionalAccountType.SAVINGS);
            addType("SE5500", "Ungbonus", TransactionalAccountType.SAVINGS);
            addType("SE5700", "Förmånskonto", TransactionalAccountType.SAVINGS);
            addType("SE5900", "Privatcertifikat", TransactionalAccountType.SAVINGS);

            addType("SE6000", "Affärsgiro", TransactionalAccountType.SAVINGS);
            addType("SE6013", "Zb Företagskonto Sub Acc", TransactionalAccountType.SAVINGS);
            addType("SE6100", "Plusgirokonto Företag", TransactionalAccountType.CHECKING);
            addType("SE6200", "Föreningsgiro", TransactionalAccountType.SAVINGS);
            addType("SE6300", "Pgkonto Förening", TransactionalAccountType.SAVINGS);
            addType("SE6600", "Plusgirokonto", TransactionalAccountType.SAVINGS);
            addType("SE6700", "Pensionssparkonto", TransactionalAccountType.SAVINGS);

            addType("SE7000", "Nostro", TransactionalAccountType.SAVINGS);
            addType("SE7100", "Loro 1", TransactionalAccountType.SAVINGS);
            addType("SE7200", "Kvk Valutatoppkonto", TransactionalAccountType.SAVINGS);
            addType("SE7300", "Kvk Toppkonto", TransactionalAccountType.SAVINGS);
            addType("SE7320", "Kvk Summeringskonto", TransactionalAccountType.SAVINGS);
            addType("SE7321", "Kvk Valutasumm Kto", TransactionalAccountType.SAVINGS);
            addType("SE7330", "Kvk Transaktionskto", TransactionalAccountType.SAVINGS);
            addType("SE7350", "Kvk Preliminäröppnat", TransactionalAccountType.SAVINGS);
            addType("SE7400", "Överföringskonto", TransactionalAccountType.SAVINGS);
            addType("SE7401", "Arbetskonto", TransactionalAccountType.SAVINGS);
            addType("SE7402", "Internkonto", TransactionalAccountType.SAVINGS);
            addType("SE7500", "Internt Kassakonto", TransactionalAccountType.SAVINGS);
            addType("SE7600", "Kontoöversikt", TransactionalAccountType.SAVINGS);
            addType("SE7601", "Zb Top Account", TransactionalAccountType.SAVINGS);
            addType("SE7602", "Zb Sub Group Level", TransactionalAccountType.SAVINGS);
            addType("SE7603", "Zb Sub Account", TransactionalAccountType.SAVINGS);
            addType("SE7604", "Zb Adjustement Account", TransactionalAccountType.SAVINGS);
            addType("SE7605", "Z Hdjustement Account", TransactionalAccountType.SAVINGS);
            addType("SE7700", CENTRALKONTO, TransactionalAccountType.SAVINGS);
            addType("SE7701", CENTRALKONTO, TransactionalAccountType.SAVINGS);
            addType("SE7702", CENTRALKONTO, TransactionalAccountType.SAVINGS);
            addType("SE7703", CENTRALKONTO, TransactionalAccountType.SAVINGS);
            addType("SE7800", "Samlingsnummer", TransactionalAccountType.SAVINGS);
            addType("SE7900", "Dispositionsnummer", TransactionalAccountType.SAVINGS);
            addType("SE7901", "Belastningsnummer", TransactionalAccountType.SAVINGS);

            addType("SE46100601", "Bonuskonto 2006 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46100602", "Bonuskonto 2006 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46100603", "Bonuskonto 2006 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46100604", "Bonuskonto 2006 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46100605", "Bonuskonto 2006 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46100606", "Bonuskonto 2006 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46100607", "Bonuskonto 2006 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46100608", "Bonuskonto 2006 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46100609", "Bonuskonto 2006 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46100610", "Bonuskonto 2006 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46100611", "Bonuskonto 2006 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46100612", "Bonuskonto 2006 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46100701", "Bonuskonto 2007 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46100702", "Bonuskonto 2007 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46100703", "Bonuskonto 2007 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46100704", "Bonuskonto 2007 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46100705", "Bonuskonto 2007 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46100706", "Bonuskonto 2007 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46100707", "Bonuskonto 2007 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46100708", "Bonuskonto 2007 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46100709", "Bonuskonto 2007 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46100710", "Bonuskonto 2007 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46100711", "Bonuskonto 2007 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46100712", "Bonuskonto 2007 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46100719", "Bonuskonto 2007 utgåva 19", TransactionalAccountType.CHECKING);
            addType("SE46100801", "Bonuskonto 2008 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46100802", "Bonuskonto 2008 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46100803", "Bonuskonto 2008 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46100804", "Bonuskonto 2008 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46100805", "Bonuskonto 2008 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46100806", "Bonuskonto 2008 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46100807", "Bonuskonto 2008 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46100808", "Bonuskonto 2008 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46100809", "Bonuskonto 2008 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46100810", "Bonuskonto 2008 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46100811", "Bonuskonto 2008 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46100812", "Bonuskonto 2008 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46100901", "Bonuskonto 2009 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46100902", "Bonuskonto 2009 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46100903", "Bonuskonto 2009 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46100904", "Bonuskonto 2009 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46100905", "Bonuskonto 2009 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46100906", "Bonuskonto 2009 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46100907", "Bonuskonto 2009 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46100908", "Bonuskonto 2009 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46100909", "Bonuskonto 2009 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46100910", "Bonuskonto 2009 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46100911", "Bonuskonto 2009 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46100912", "Bonuskonto 2009 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46101001", "Bonuskonto 2010 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46101002", "Bonuskonto 2010 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46101003", "Bonuskonto 2010 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46101004", "Bonuskonto 2010 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46101005", "Bonuskonto 2010 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46101006", "Bonuskonto 2010 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46101007", "Bonuskonto 2010 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46101008", "Bonuskonto 2010 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46101009", "Bonuskonto 2010 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46101010", "Bonuskonto 2010 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46101011", "Bonuskonto 2010 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46101012", "Bonuskonto 2010 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46101101", "Bonuskonto 2011 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46101102", "Bonuskonto 2011 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46101103", "Bonuskonto 2011 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46101104", "Bonuskonto 2011 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46101105", "Bonuskonto 2011 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46101106", "Bonuskonto 2011 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46101107", "Bonuskonto 2011 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46101108", "Bonuskonto 2011 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46101109", "Bonuskonto 2011 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46101110", "Bonuskonto 2011 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46101111", "Bonuskonto 2011 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46101112", "Bonuskonto 2011 utgåva 12", TransactionalAccountType.CHECKING);
            addType("SE46101201", "Bonuskonto 2012 utgåva 1", TransactionalAccountType.CHECKING);
            addType("SE46101202", "Bonuskonto 2012 utgåva 2", TransactionalAccountType.CHECKING);
            addType("SE46101203", "Bonuskonto 2012 utgåva 3", TransactionalAccountType.CHECKING);
            addType("SE46101204", "Bonuskonto 2012 utgåva 4", TransactionalAccountType.CHECKING);
            addType("SE46101205", "Bonuskonto 2012 utgåva 5", TransactionalAccountType.CHECKING);
            addType("SE46101206", "Bonuskonto 2012 utgåva 6", TransactionalAccountType.CHECKING);
            addType("SE46101207", "Bonuskonto 2012 utgåva 7", TransactionalAccountType.CHECKING);
            addType("SE46101208", "Bonuskonto 2012 utgåva 8", TransactionalAccountType.CHECKING);
            addType("SE46101209", "Bonuskonto 2012 utgåva 9", TransactionalAccountType.CHECKING);
            addType("SE46101210", "Bonuskonto 2012 utgåva 10", TransactionalAccountType.CHECKING);
            addType("SE46101211", "Bonuskonto 2012 utgåva 11", TransactionalAccountType.CHECKING);
            addType("SE46101212", "Bonuskonto 2012 utgåva 12", TransactionalAccountType.CHECKING);

            addType(
                    "Företagslån Nordea Bank",
                    "Företagslån Nordea Bank",
                    TransactionalAccountType.SAVINGS);
        }

        private static void addType(String code, String name, TransactionalAccountType type) {
            ACCOUNT_TYPES_BY_CODE.put(code, type);
            ACCOUNT_NAMES_BY_CODE.put(code, name);
        }
    }

    public static class ErrorCodes {
        public static final String TOKEN_REQUIRED = "token_required";
        public static final String NOT_CUSTOMER = "MBS8636";

        public static final String AGREEMENT_NOT_CONFIRMED =
                "RBO_ACCESS_DENIED_AGREEMENT_NOT_CONFIRMED";
        public static final String CLASSIFICATION_NOT_CONFIRMED =
                "RBO_ACCESS_DENIED_CLASSIFICATION_NOT_CONFIRMED";

        public static final String UNABLE_TO_LOAD_CUSTOMER = "ERROR_OSIA_UNABLE_TO_LOAD_CUSTOMER";
        public static final String INVALID_TOKEN = "invalid_token";
        public static final String INVALID_GRANT = "invalid_grant";
        public static final String RESOURCE_NOT_FOUND = "resource_not_found";
        public static final String AUTHENTICATION_COLLISION = "authentication_collision";
        public static final String AUTHENTICATION_FAILED = "authentication_failed";
        public static final String UNABLE_TO_FETCH_ACCOUNTS = "Could not retrieve accounts.";
        public static final String DUPLICATE_PAYMENT =
                "Duplicate payment. Technical code. Please try again.";
        public static final String PAYMENT_ERROR = "Something went wrong with the payment.";
        public static final String UNREGISTERED_RECIPIENT =
                "Recipient accounts missing from accounts ledger";
        public static final String NOT_ENOUGH_FUNDS = "Not enough funds";
        public static final String EXTERNAL_SERVICE_CALL_FAILED = "External service call failed";
        public static final String INTERNAL_SERVER_ERROR = "internal_server_error";

        public static final String SIGNING_COLLISION = "signing_collision";
        public static final String SIGNING_COLLISION_MESSAGE = "A signing collision has occurred.";
        public static final String WRONG_TO_ACCOUNT_LENGTH = "BESE1125";
        public static final String WRONG_TO_ACCOUNT_LENGHT_MESSAGE =
                "Wrong To account length for the chosen bank";
        public static final String HYSTRIX_CIRCUIT_SHORT_CIRCUITED =
                "Hystrix circuit short".toLowerCase();
        public static final String TIMEOUT_AFTER_MESSAGE = "Timeout after".toLowerCase();
        public static final String ERROR_CORE_UNKNOWN = "error_core_unknown";
        public static final String INVALID_PARAMETERS_FOR_PAYMENT =
                "Invalid parameter(s) for payment";
        public static final String BESE1076 = "BESE1076".toLowerCase();
        public static final String INVALID_OCR_ERROR_CODE = "BESE1009";
        public static final String OWN_MESSAGE_CONSTRAINTS =
                "Own message must be between".toLowerCase();
        public static final String UNEXPECTED_EXECUTION_ERROR_CODE = "unexpected_execution_error";
        public static final String UNEXPECTED_EXECUTION_ERROR =
                "An unexpected execution error has occurred".toLowerCase();
        public static final String USER_UNAUTHORIZED = "error_core_unauthorized";
        public static final String USER_UNAUTHORIZED_MESSAGE = "User not authorised to operation";
    }

    public class AuthMethod {
        public static final String BANKID_SE = "bankid_se";
    }

    public static class TagValues {
        public static final String APPLICATION_ID = "Hjh7wsmPVojMkPioAvky";
        public static final String REDIRECT_URI = "com.nordea.SMEMobileBank.se://auth-callback";
        public static final String SCOPE_VALUE = "ndf";
    }

    public static class QueryParams {
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
        public static final String LIMIT = "limit";
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "page_size";
        public static final String STATUS = "status";
    }
}
