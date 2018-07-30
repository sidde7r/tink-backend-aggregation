package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.http.HeaderEnum;
import se.tink.backend.aggregation.nxgen.http.URL;

public class CreditAgricoleConstants {

    public static final class Url {
        private static final String HOST = "https://ibudget.iphone.credit-agricole.fr";

        public static final URL SELECT_REGION = new URL(HOST + String.format(
                "/budget5/iphoneservice/vitrine/{%s}/isResetPwdBamActive",
                StorageKey.REGION_ID));
        public static final URL NUMBER_PAD = new URL(HOST + "/budget5/iphoneservice/authentication/grid");
        public static final URL SIGN_IN = new URL(HOST + String.format(
                "/budget5/iphoneservice/user/{%s}/search",
                StorageKey.REGION_ID));
        public static final URL USER_AGREEMENT = new URL(HOST + String.format(
                "/budget5/iphoneservice/about/{%s}?lastUpdate=0",
                StorageKey.REGION_ID));
        public static final URL APP_CODE = new URL(HOST + String.format(
                "/budget5/iphoneservice/user/{%s}/{%s}/{%s}/restore",
                StorageKey.USER_ID,
                StorageKey.REGION_ID,
                StorageKey.PARTNER_ID));
        public static final URL STRONG_AUTHENTICATION = new URL(HOST + String.format(
                "/budget5/iphoneservice/authentication/{%s}/strong",
                StorageKey.REGION_ID));
        public static final URL KEEP_ALIVE = new URL(HOST + String.format(
                "/budget5/iphoneservice/authentication/{%s}/{%s}/longSessionProfile",
                StorageKey.USER_ID,
                StorageKey.REGION_ID));
        public static final URL CONTRACTS = new URL(HOST + String.format(
                "/budget5/iphoneservice/portfolio/{%s}/{%s}/{%s}/contracts",
                StorageKey.USER_ID,
                StorageKey.REGION_ID,
                StorageKey.PARTNER_ID));
        public static final URL PERIMETER_ACCOUNTS = new URL(HOST + String.format(
                "/budget5/iphoneservice/portfolio/{%s}/perimeters/{%s}/{%s}/accounts",
                StorageKey.USER_ID,
                StorageKey.REGION_ID,
                StorageKey.PARTNER_ID));
        public static final URL OPERATIONS = new URL(HOST + String.format(
                "/budget5/iphoneservice/portfolio/{%s}/{%s}/{%s}/accounts/{%s}/operations",
                StorageKey.USER_ID,
                StorageKey.REGION_ID,
                StorageKey.PARTNER_ID,
                StorageKey.ACCOUNT_NUMBER)); // takes query param startDate={BASIC_ISO_DATE}
    }

    // This data is used in query params, and may not be changed
    // without affecting communication with the bank
    public static class QueryParam {
        public static final String USER_ACCOUNT_CODE = "accountCode";
        public static final String USER_ACCOUNT_NUMBER = "accountNumber";
        public static final String START_DATE = "startDate";
    }

    public enum ConstantHeader implements HeaderEnum {
        USER_AGENT("User-Agent", "MonBudget_iOS/14.0.1.2 iOS/10.1.1 Apple/iPhone9,3 750x1334/2.00"
                + " Tink(+https://www.tink.se/;noc@tink.se)");

        private String key;
        private String value;

        private ConstantHeader(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public static class Authorization {
        public static String HEADER = "Authorization";
        public static String BASIC_PREFIX = "Basic ";
    }

    // This data should only be used internally by us, and changing it
    // should thus not affect communication with the bank
    public static class StorageKey {
        public static final String REGION_ID = "regionId";
        public static final String USER_ACCOUNT_NUMBER = "userAccountNumber";
        public static final String USER_ACCOUNT_CODE = "userAccountCode";
        public static final String SHUFFLED_USER_ACCOUNT_CODE = "shuffledAccountCode";
        public static final String APP_CODE = "appCode";
        public static final String PARTNER_ID = "partnerId";
        public static final String USER_ID = "userId";
        public static final String LOGIN_EMAIL = "login";
        public static final String LL_TOKEN = "llToken";
        public static final String ACCOUNT_NUMBER = "accountNumber";
    }

    // This data is used in request body forms, and may not be changed
    // without affecting communication with the bank
    public static class Form {
        public static final String USER_ACCOUNT_CODE = "accountCode";
        public static final String USER_ACCOUNT_NUMBER = "accountNumber";
        public static final String APP_CODE = "password";
        public static final String LOGIN_EMAIL = "login";
        public static final String LL_TOKEN = "llToken";
    }

    public static final class AccountType {
        public static final String CHECKING = "Compte courant";
    }

    public static final class Currency {
        public static final String EUR = "EUR";
    }

    public static class ErrorCode {
        public static String INCORRECT_CREDENTIALS = "fr.mabanque.auth.wrongpassword";
    }

    public static final String DATE_FORMAT = "yyyyMMdd";
}
