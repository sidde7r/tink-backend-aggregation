package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;

public class SebKortConstants {
    public static final String AUTHORIZATION_UUID =
            "ca4d47c0-e61f-30ad-b0c0-86d20f26d5eb"; // Required (and the same) for all SEB Kort

    // This has to be kept in sync with the OB providers configuration.
    // As per SEB docs "chevrolet" and "djurgards" brands are not available in the OB agent, so
    // should not be flagged
    public static final AccountTypeMapper PROVIDER_PSD2_FLAG_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.CREDIT_CARD,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "stse",
                            "ecse",
                            "fase",
                            "jese",
                            "nkse",
                            "cose",
                            "opse",
                            "sbse",
                            "sase",
                            "wase",
                            "sjse")
                    .put(AccountTypes.CREDIT_CARD, "chse", "djse")
                    .build();

    public static final TypeMapper<AccountCapabilities> ACCOUNT_CAPABILITIES_MAPPER =
            TypeMapper.<AccountCapabilities>builder()
                    .put(
                            new AccountCapabilities(
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.YES),
                            "EC",
                            "EC_CORPORATE",
                            "EC_GOLD",
                            "EC_PLATINUM",
                            "EC_CORPORATE_LIMIT",
                            "EC_CORPORATE_PLATINUM",
                            "SAS_EB_MC_BASIC",
                            "SAS_EB_PREMIUM_MC_BASIC",
                            "SAS_EB_POINTEARNER")
                    .build();

    public static class Urls {
        private static final String SEBKORT_HOST = "https://secure.eurocard.se";
        public static final URL SEBKORT_LOGIN = new URL(SEBKORT_HOST + "/sea/external/EidLogin");
        public static final URL SEBKORT_LOGOUT = new URL(SEBKORT_HOST + "/pkmslogout");
        public static final URL SEBKORT_AUTH =
                new URL(SEBKORT_HOST + "/nauth4/Authentication/Auth");
        public static final URL SEBKORT_CARDS =
                new URL(SEBKORT_HOST + "/api/common/cardcontracts/v1/");
        public static final URL SEBKORT_RESERVATIONS =
                new URL(SEBKORT_HOST + "/api/common/reservations/v1/");
        public static final URL SEBKORT_TRANSACTIONS =
                new URL(SEBKORT_HOST + "/api/common/transactions/v1/");

        private static final String BANKID_HOST = "https://id.signicat.com";
        public static final URL BANK_ID_INIT = new URL(BANKID_HOST + "/std/method/seb");
    }

    static class Headers {
        static final String USER_AGENT = "User-Agent";
        static final String USER_AGENT_VALUE =
                "SEBKortClient/1.0 (os=iOS/13.3.1; app=se.eurocard.Eurocard/5.6.1)";
        static final String REFERER = "Referer";
    }

    public static class StorageKey {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CARD_ACCOUNT_ID = "cardAccountId";
        public static final String IS_ACCOUNT_OWNER = "isAccountOwner";
    }

    public static class StorageValue {
        public static final String NULL = "null";
    }

    public static class QueryKey {
        public static final String LANGUAGE_CODE = "languageCode";
        public static final String CARD_ACCOUNT_ID = "cardAccountId";
        public static final String CARD_CONTRACT_ID = "cardContractId";
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
        public static final String METHOD = "method";
        public static final String PROFILE = "profile";
        public static final String LANGUAGE = "language";
        public static final String PREFILLED_SUBJECT = "prefilled.subject";
        public static final String TARGET = "target";
        public static final String REDIRECT = "redirect";
    }

    public static class QueryValue {
        public static final String LANGUAGE_CODE = "EN";
        public static final String PROFILE = "app";
        public static final String LANGUAGE = "en";
        public static final String TARGET = Urls.SEBKORT_HOST + "/mbidcomplete";
        public static final String REDIRECT = "/nis/m/%s/external/logout";
    }

    public static class FormKey {
        public static final String PRODGROUP = "prodgroup";
        public static final String SAML_RESPONSE = "SAMLResponse";
        public static final String SEB_REFERER = "SEB_Referer";
        public static final String TARGET_URL = "targetUrl";
        public static final String COUNTRY_CODE = "countryCode";
        public static final String UID = "UID";
        public static final String TARGET = "target";
        public static final String TYPE = "TYPE";
        public static final String SECRET = "scrt";
        public static final String SEB_AUTH_MECHANISM = "SEB_Auth_Mechanism";
    }

    public static class FormValue {
        public static final String COUNTRY_CODE = "SE";
        public static final String TARGET_URL = Urls.SEBKORT_HOST + "/mbidcomplete";
        public static final String SEB_REFERER = "/nis";
        public static final String TARGET = "/nis/m/%s/login/loginSuccess";
        public static final String TYPE = "EID";
        public static final String SEB_AUTH_MECHANISM = "5";
    }

    public static class TransactionType {
        public static final String PAYMENT = "PAYMENT";
        public static final String FEE = "FEE";
    }

    public static class Error {
        public static final String BANKID_ALREADY_IN_PROGRESS = "ALREADY_IN_PROGRESS";
        public static final String GENERIC_TECHNICAL_ERROR = "GENERIC_TECHNICAL_ERROR";
        public static final String BANK_SIDE_FAILURE = "tillfälligt tekniskt fel";
    }
}
