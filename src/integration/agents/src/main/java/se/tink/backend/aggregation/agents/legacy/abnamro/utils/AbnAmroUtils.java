package se.tink.backend.aggregation.agents.abnamro.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.account.enums.AccountTypes;

public class AbnAmroUtils {

    private static final Logger log = LoggerFactory.getLogger(AbnAmroUtils.class);

    // These are the abstract providers that where used in Grip 2.0 and earlier
    public static final int ABN_AMRO_ICS_SUFFIX_LENGTH = 5;
    public static final int ABN_AMRO_ICS_HAS_SUFFIX_MIN_LENGTH = 15;

    public static final String ABN_AMRO_ICS_ACCOUNT_CONTRACT_PAYLOAD = "contract_number";

    // Payload keys for the internal payload. Supplement to `Transaction.InternalPayloadKeys`.
    public static class InternalPayloadKeys {
        public static final String MERCHANT_DESCRIPTION = "MERCHANT_DESCRIPTION";
    }

    public static final Map<String, AccountTypes> ACCOUNT_TYPE_BY_PRODUCT_GROUP =
            ImmutableMap.<String, AccountTypes>builder()
                    .put("PAYMENT_ACCOUNTS", AccountTypes.CHECKING)
                    .put("SAVINGS_ACCOUNTS", AccountTypes.SAVINGS)
                    .put("VARIABLE_SAVINGS_TOR_FLAT", AccountTypes.SAVINGS)
                    .put("VARIABLE_SAVINGS_TOR_BONUS", AccountTypes.SAVINGS)
                    .put("CREDIT_CARDS_PRIVATE_AND_RETAIL", AccountTypes.CREDIT_CARD)
                    .put("PAYMENT_SERVICES", AccountTypes.CREDIT_CARD)
                    .build();

    public static class DescriptionKeys {
        public static String CARD_NUMBER = "kaartnummer";
        public static String DESCRIPTION = "omschrijving";
        public static String DIRECT_DEBIT = "machtiging";
        public static String FULL = "full";
        public static String NAME = "naam";
        public static String IBAN = "iban";
    }

    public static AccountTypes getAccountType(String productGroup) {
        if (ACCOUNT_TYPE_BY_PRODUCT_GROUP.containsKey(productGroup)) {
            return ACCOUNT_TYPE_BY_PRODUCT_GROUP.get(productGroup);
        } else {
            return AccountTypes.CHECKING;
        }
    }

    /**
     * This isn't the real credit card number but a contract number that is constructed by ABN AMRO.
     * The 4 last digits are the same as on the actual credit card.
     */
    public static String maskCreditCardContractNumber(String accountNumber) {
        return maskCreditCardContractNumber(accountNumber, false);
    }

    public static String maskCreditCardContractNumber(String accountNumber, boolean compactFormat) {

        if (accountNumber.length() <= 4) {
            return accountNumber;
        }

        if (accountNumber.startsWith("****")) {
            log.warn("Account number is already masked");
            return accountNumber;
        }

        String lastFourDigits = accountNumber.substring(accountNumber.length() - 4);

        if (compactFormat) {
            return String.format("**** %s", lastFourDigits);
        } else {
            return String.format("**** **** **** %s", lastFourDigits);
        }
    }

    public static String prettyFormatIban(String iban) {
        if (iban == null) {
            return null;
        }

        // Insert space after every 4th character
        return iban.replaceAll("(.{4})(?!$)", "$1 ");
    }

    public static String creditCardIdToAccountId(String ccId) {
        if (ccId.length() < ABN_AMRO_ICS_HAS_SUFFIX_MIN_LENGTH) {
            return ccId;
        }

        return ccId.substring(0, ccId.length() - ABN_AMRO_ICS_SUFFIX_LENGTH);
    }

    public static boolean isValidBcNumberFormat(String input) {
        return !Strings.isNullOrEmpty(input) && StringUtils.isNumeric(input);
    }
}
