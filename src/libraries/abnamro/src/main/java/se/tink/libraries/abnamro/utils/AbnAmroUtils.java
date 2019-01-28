package se.tink.libraries.abnamro.utils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.log.legacy.LogUtils;
import se.tink.libraries.abnamro.client.model.ContractEntity;
import se.tink.libraries.abnamro.client.model.RejectedContractEntity;
import se.tink.libraries.strings.StringUtils;

public class AbnAmroUtils {

    private static final LogUtils log = new LogUtils(AbnAmroUtils.class);
    private static final ImmutableSet VALID_LOCALES = ImmutableSet.of("en_US".toLowerCase(), "nl_NL".toLowerCase());

    // This is the name of the ABN AMRO provider for Grip 3.0 and later
    public static final String ABN_AMRO_PROVIDER_NAME_V2 = "nl-abnamro";

    // These are the abstract providers that where used in Grip 2.0 and earlier
    public static final String ABN_AMRO_PROVIDER_NAME = "nl-abnamro-abstract";
    public static final String ABN_AMRO_ICS_PROVIDER_NAME = "nl-abnamro-ics-abstract";
    public static final String BC_NUMBER_FIELD_NAME = "bcNumber";
    public static final String CREDENTIALS_BLOCKED_PAYLOAD = "BLOCKED";
    public static final int ABN_AMRO_ICS_SUFFIX_LENGTH = 5;
    public static final int ABN_AMRO_ICS_HAS_SUFFIX_MIN_LENGTH = 15;

    public static final String ABN_AMRO_ICS_ACCOUNT_CONTRACT_PAYLOAD = "contract_number";

    public static boolean isAggregationCredentials(Credentials credentials) {
        return Objects.equals(credentials.getProviderName(), ABN_AMRO_ICS_PROVIDER_NAME) ||
                Objects.equals(credentials.getProviderName(), ABN_AMRO_PROVIDER_NAME_V2);
    }

    // Payload keys for the external payload (in the `TransactionEntity`).
    public static class ExternalPayloadKeys {
        public static final String TRANSACTION_ID = "TRANSACTION_ID";
        public static final String ORIGIN_TYPE = "ORIGIN_TYPE";
    }

    // Payload keys for the internal payload. Supplement to `Transaction.InternalPayloadKeys`.
    public static class InternalPayloadKeys {
        public static final String ABNAMRO_PAYLOAD = "ABNAMRO_PAYLOAD";
        public static final String DESCRIPTION_LINES = "DESCRIPTION_LINES";
        public static final String MERCHANT_DESCRIPTION = "MERCHANT_DESCRIPTION";
    }

    public static class InternalAccountPayloadKeys {
        public static final String IBAN = "iban";
        public static final String CURRENCY = "currency";
        public static final String REJECTED_DATE = "rejected-date";
        public static final String REJECTED_REASON_CODE = "rejected-reason-code";
        public static final String FAILED_DATE = "failed-date";
        public static final String LOCKED = "locked";
        public static final String SUBSCRIBED = "subscribed";
    }

    public static final Map<String, AccountTypes> ACCOUNT_TYPE_BY_PRODUCT_GROUP = ImmutableMap
            .<String, AccountTypes>builder()
            .put("PAYMENT_ACCOUNTS", AccountTypes.CHECKING)
            .put("SAVINGS_ACCOUNTS", AccountTypes.SAVINGS)
            .put("VARIABLE_SAVINGS_TOR_FLAT", AccountTypes.SAVINGS)
            .put("VARIABLE_SAVINGS_TOR_BONUS", AccountTypes.SAVINGS)
            .put("CREDIT_CARDS_PRIVATE_AND_RETAIL", AccountTypes.CREDIT_CARD)
            .put("PAYMENT_SERVICES", AccountTypes.CREDIT_CARD)
            .build();

    public static final Set<String> CREDIT_CARD_PRODUCT_GROUPS = ImmutableSet
            .<String>builder()
            .add("CREDIT_CARDS_PRIVATE_AND_RETAIL")
            .add("PAYMENT_SERVICES")
            .build();

    public static class DescriptionKeys {
        public static String CARD_NUMBER = "kaartnummer";
        public static String DESCRIPTION = "omschrijving";
        public static String DIRECT_DEBIT = "machtiging";
        public static String FULL = "full";
        public static String NAME = "naam";
        public static String POS = "pos";
        public static String RECIPIENT = "incassant";
        public static String IBAN = "iban";
    }

    private static Pattern DESCRIPTION_PARTS_PATTERN = Pattern
            .compile("(?<key>[^ \\d]+): ?(?<value>.*?)(?:(?= [^ \\d]+:)|$)");

    private static Pattern DESCRIPTION_POS_TRANSACTION_PATTERN = Pattern.compile(
            "^(?:[gb]ea( nr:\\w+)? \\d{2}.\\d{2}.\\d{2}\\/\\d{2}.\\d{2} )(?<value>.+)", Pattern.CASE_INSENSITIVE);

    // Complement to se.tink.backend.utils.guavaimpl.Functions
    public static class Functions {
        public static final Function<RejectedContractEntity, Long> REJECTED_CONTRACT_TO_CONTRACT_NUMBER = RejectedContractEntity::getContractNumber;
    }

    // Complement to se.tink.backend.aggregation.utils.Predicates
    public static class Predicates {
        public static final Predicate<ContractEntity> IS_VALID_CONTRACT_ENTITY = contractEntity -> AbnAmroAccountValidator
                .validate(contractEntity).isValid();

        public static final Predicate<Credentials> IS_BLOCKED = credentials -> Objects
                .equals(AbnAmroUtils.CREDENTIALS_BLOCKED_PAYLOAD, credentials.getPayload());
    }

    public static boolean isValidLocale(String locale) {
        return locale != null && VALID_LOCALES.contains(locale.toLowerCase());
    }

    public static Long getAccountNumber(String bankId) {
        return Long.valueOf(bankId);
    }

    public static AccountTypes getAccountType(String productGroup) {
        if (ACCOUNT_TYPE_BY_PRODUCT_GROUP.containsKey(productGroup)) {
            return ACCOUNT_TYPE_BY_PRODUCT_GROUP.get(productGroup);
        } else {
            return AccountTypes.CHECKING;
        }
    }

    public static String getBankId(Long accountNumber) {
        return Long.toString(accountNumber);
    }

    public static String getDescription(Map<String, String> descriptionParts) {
        return getDescription(descriptionParts, true);
    }

    public static String getDescription(Map<String, String> descriptionParts, boolean usePaymentProvider) {

        String description;

        description = descriptionParts.get(DescriptionKeys.POS);
        if (description != null) {
            return description;
        }

        if (usePaymentProvider) {
            description = AbnAmroPaymentProviderUtils.getPaymentProviderDescription(descriptionParts);
            if (description != null) {
                return description;
            }
        }

        description = descriptionParts.get(DescriptionKeys.NAME);
        if (description != null) {
            return description;
        }

        description = descriptionParts.get(DescriptionKeys.DESCRIPTION);
        if (description != null) {
            return description;
        }

        return descriptionParts.get(DescriptionKeys.FULL);
    }

    public static Map<String, String> getDescriptionParts(List<String> descriptionLines) {
        Map<String, String> parts = Maps.newHashMap();

        if (descriptionLines == null) {
            return parts;
        }

        String rawDescription = Joiner
                .on(" ").join(descriptionLines)
                .replaceAll("\\s+", " ")
                .trim();

        Matcher posMatcher = DESCRIPTION_POS_TRANSACTION_PATTERN.matcher(rawDescription);

        parts.put(DescriptionKeys.FULL, rawDescription);

        if (posMatcher.find()) {
            // Point of sale (POS) transaction (card purchase).
            String value = StringUtils.trimToNull(posMatcher.group("value"));
            if (value != null) {
                parts.put(DescriptionKeys.POS, value);
            }
        } else {
            // Online card purchase, bill payment or bank transfer.

            Matcher m = DESCRIPTION_PARTS_PATTERN.matcher(rawDescription);

            while (m.find()) {
                String value = StringUtils.trimToNull(m.group("value"));
                if (value != null) {
                    parts.put(m.group("key").toLowerCase(), value);
                }
            }
        }

        return parts;
    }

    /**
     * This isn't the real credit card number but a contract number that is constructed by ABN AMRO. The 4 last digits
     * are the same as on the actual credit card.
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

    /**
     * Return the difference between two lists of accounts based on bank id.
     */
    public static List<Account> getAccountDifference(List<Account> left, List<Account> right) {

        if (CollectionUtils.isEmpty(left)) {
            return Collections.emptyList();
        }

        final Set<String> rightBankIds = right.stream().map(AbnAmroUtils::getIcsShortBankId)
                .collect(Collectors.toSet());

        return left.stream().filter(account -> !rightBankIds.contains(getIcsShortBankId(account)))
                .collect(Collectors.toList());

    }

    public static boolean isAbnAmroProvider(String providerName) {
        return Objects.equals(ABN_AMRO_PROVIDER_NAME, providerName) ||
                Objects.equals(ABN_AMRO_ICS_PROVIDER_NAME, providerName) ||
                Objects.equals(ABN_AMRO_PROVIDER_NAME_V2, providerName);
    }

    public static boolean isAccountRejected(Account account) {
        return !Strings.isNullOrEmpty(account.getPayload(InternalAccountPayloadKeys.REJECTED_REASON_CODE));
    }

    public static void markAccountAsRejected(Account account, Integer rejectedReasonCode) {
        Preconditions.checkNotNull(account);
        account.putPayload(InternalAccountPayloadKeys.REJECTED_DATE, new DateTime().toString());
        account.putPayload(InternalAccountPayloadKeys.REJECTED_REASON_CODE, String.valueOf(rejectedReasonCode));
    }

    public static void markAccountAsFailed(Account account) {
        Preconditions.checkNotNull(account);
        account.putPayload(InternalAccountPayloadKeys.FAILED_DATE, new DateTime().toString());
    }

    public static void markAccountAsActive(Account account) {
        Preconditions.checkNotNull(account);
        account.removePayload(InternalAccountPayloadKeys.REJECTED_DATE);
        account.removePayload(InternalAccountPayloadKeys.REJECTED_REASON_CODE);
    }

    public static String getIcsShortBankId(Account account) {
        if (account.getType() == AccountTypes.CREDIT_CARD) {
            return creditCardIdToAccountId(account.getBankId());
        }

        return account.getBankId();
    }

    public static String creditCardIdToAccountId(String ccId) {
        if (ccId.length() < ABN_AMRO_ICS_HAS_SUFFIX_MIN_LENGTH) {
            return ccId;
        }

        return ccId.substring(0, ccId.length()-ABN_AMRO_ICS_SUFFIX_LENGTH);
    }

    public static boolean isValidBcNumberFormat(String input) {
        return !Strings.isNullOrEmpty(input) && StringUtils.isNumeric(input);
    }

    public static Optional<String> getBcNumber(Credentials credentials) {
        if (credentials == null || !Objects.equals(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME_V2,
                credentials.getProviderName()) || !isValidBcNumberFormat(credentials.getPayload())) {
            return Optional.empty();
        }

        return Optional.of(credentials.getPayload());
    }

    public static boolean shouldUseNewIcsAccountFormat(User user) {
        return shouldUseNewIcsAccountFormat(user.getFlags());
    }

    public static boolean shouldUseNewIcsAccountFormat(List<String> featureFlags) {
        return featureFlags.contains(FeatureFlags.ABN_AMRO_ICS_NEW_ACCOUNT_FORMAT);
    }
}
