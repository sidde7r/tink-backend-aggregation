package se.tink.backend.aggregation.agents.creditcards.coop.v2;

import java.util.Locale;
import java.util.Objects;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.log.AggregationLogger;

public enum AccountType {
    MEDMERA_VISA(-1, "Visa", AccountTypes.CREDIT_CARD, "Coop MedMera Visa"),
    MEDMERA_KONTO(0, "Konto", AccountTypes.OTHER, "Coop MedMera Konto"),
    MEDMERA_FAKTURA(1, "Faktura", AccountTypes.CREDIT_CARD, "Coop MedMera Faktura"),
    MEDMERA_FORE(6, "Före", AccountTypes.CREDIT_CARD, "Coop MedMera Före"),
    MEDMERA_EFTER_1(
            7,
            "Efter",
            AccountTypes.CREDIT_CARD,
            "Coop MedMera Efter 1"), // For legacy reason we add a number on this hash so that it's
    // separated from below EFTER
    MEDMERA_MER(8, "Mer", AccountTypes.CREDIT_CARD, "Coop MedMera Mer"),
    MEDMERA_EFTER_2(9, "Efter", AccountTypes.CREDIT_CARD, "Coop MedMera Efter");

    private static final AggregationLogger log = new AggregationLogger(AccountType.class);
    private static final Locale SWEDISH_LOCALE = new Locale("sv", "SE");

    private final int accountTypeOrdinal;
    private final String accountNameSuffix;
    private final AccountTypes accountType;
    private final String legacyBankIdPart;

    AccountType(
            int accountTypeOrdinal,
            String accountNameSuffix,
            AccountTypes accountType,
            String legacyBankIdPart) {
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
            if (Objects.equals(
                    accountType.accountNameSuffix.toLowerCase(SWEDISH_LOCALE), suffixLowerCase)) {
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
