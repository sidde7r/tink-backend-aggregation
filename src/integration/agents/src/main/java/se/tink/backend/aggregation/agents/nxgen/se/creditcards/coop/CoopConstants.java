package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.agents.rpc.AccountTypes;

public class CoopConstants {

    public static class Url {
        private static String BASE_URL = "https://www.coop.se/ExternalServices/V5";
        public static final URL AUTHENTICATE = new URL(BASE_URL + "/UserServiceV5.svc/Authenticate");
        public static final URL USER_SUMMARY = new URL(BASE_URL + "/UserServiceV5.svc/GetUserSummary");
        public static final URL TRANSACTIONS = new URL(BASE_URL + "/FinancialServiceV5.svc/GetTransactions");
    }

    public static class Account {

        public static final String OWNER_NAME = "ownername";
        public static final String ACCOUNT_NUMBER = "accountnumber";
        public static final String PG_NUMBER = "plusgironumber";
        public static final String OCR_NUMBER = "ocrnumber";

        public static final int YEAR_TO_START_FETCH = 2000;
        public static final List<Integer> TRANSACTION_BATCH_SIZE = ImmutableList.of(200, 1000, 10000);
    }

    public static class Storage {
        public static final String USER_ID = "UserId";
        public static final String TOKEN = "Token";
        public static final String USER_SUMMARY = "UserSummary";
        public static final String CREDENTIALS_ID = "CredentialsId";
    }

    public static class Header {
        public final static Map<String, String> DEFAULT_HEADERS = ImmutableMap.of(
                "ApplicationId", "687D17CB-85C3-4547-9F8D-A346C7008EB1",
                "Content-Type", "application/json"
        );

        public static final String TOKEN_TYPE = "user";
    }

    public enum AccountType {
        MEDMERA_VISA(-1, "Visa", AccountTypes.CREDIT_CARD, "Coop MedMera Visa"),
        MEDMERA_KONTO(0, "Konto", AccountTypes.OTHER, "Coop MedMera Konto"),
        MEDMERA_FAKTURA(1, "Faktura", AccountTypes.CREDIT_CARD, "Coop MedMera Faktura"),
        MEDMERA_FORE(6, "Före", AccountTypes.CREDIT_CARD, "Coop MedMera Före"),
        MEDMERA_EFTER_1(7, "Efter", AccountTypes.CREDIT_CARD, "Coop MedMera Efter 1"), // For legacy reason we add a number on this hash so that it's separated from below EFTER
        MEDMERA_MER(8, "Mer", AccountTypes.CREDIT_CARD, "Coop MedMera Mer"),
        MEDMERA_EFTER_2(9, "Efter", AccountTypes.CREDIT_CARD, "Coop MedMera Efter");

        private static final AggregationLogger log = new AggregationLogger(
                AccountType.class);
        private static final Locale SWEDISH_LOCALE = new Locale("sv", "SE");

        private final int accountTypeOrdinal;
        private final String accountNameSuffix;
        private final AccountTypes accountType;
        private final String legacyBankIdPart;

        AccountType(int accountTypeOrdinal, String accountNameSuffix, AccountTypes accountType, String legacyBankIdPart) {
            this.accountTypeOrdinal = accountTypeOrdinal;
            this.accountNameSuffix = accountNameSuffix;
            this.accountType = accountType;
            this.legacyBankIdPart = legacyBankIdPart;
        }

        public static AccountType valueOf(int accountTypeOrdinal) {
            for (AccountType accountType : values()) {
                if (accountType.accountTypeOrdinal == accountTypeOrdinal) {
                    return accountType;
                }
            }

            log.warn("valueOf(" + accountTypeOrdinal + ") --> null");
            return null;
        }

        public static AccountType guessFromName(String accountName) {
            String[] nameWords = accountName.trim().split(" ");

            if (nameWords.length == 0) {
                return null;
            }

            String suffixLowerCase = nameWords[nameWords.length - 1].toLowerCase(SWEDISH_LOCALE);

            for (AccountType accountType : values()) {
                if (Objects.equals(accountType.accountNameSuffix.toLowerCase(SWEDISH_LOCALE), suffixLowerCase)) {
                    log.info("guessFromName(" + accountName + ") --> " + accountType.name());
                    return accountType;
                }
            }

            log.warn("valueOf(" + accountName + ") --> null");
            return null;
        }

        public String getLegacyBankIdPart() {
            return legacyBankIdPart;
        }

        public AccountTypes getAccountType() {
            return accountType;
        }
    }
}
