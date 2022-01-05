package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.CaseFormat;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.strings.StringUtils;

@Slf4j
public class UkOpenBankingApiDefinitions {

    public static <E extends Enum<E>> E parseToEnumClass(Class<E> eClass, String key) {
        return Enums.getIfPresent(eClass, key)
                .toJavaUtil()
                .orElseGet(
                        () -> {
                            log.warn(
                                    "[UkOpenBankingApiDefinitions] Unmapped value for {} class is equal: {}",
                                    eClass.getName(),
                                    key);
                            return null;
                        });
    }

    public enum UkObBalanceType {
        CLEARED_BALANCE,
        CLOSING_AVAILABLE,
        CLOSING_BOOKED,
        CLOSING_CLEARED,
        EXPECTED,
        FORWARD_AVAILABLE,
        INFORMATION,
        INTERIM_AVAILABLE,
        INTERIM_BOOKED,
        INTERIM_CLEARED,
        OPENING_AVAILABLE,
        OPENING_BOOKED,
        OPENING_CLEARED,
        PREVIOUSLY_CLOSED_BOOKED;

        @JsonCreator
        public static UkObBalanceType fromString(String key) {
            return parseToEnumClass(
                    UkObBalanceType.class,
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key));
        }
    }

    public enum ExternalLimitType {
        AVAILABLE,
        CREDIT,
        EMERGENCY,
        PRE_AGREED,
        TEMPORARY;

        public static final ImmutableList<ExternalLimitType> CREDIT_LINE_PREFERRED_LIMIT_TYPES =
                ImmutableList.of(CREDIT, PRE_AGREED, TEMPORARY, EMERGENCY, AVAILABLE);

        @JsonCreator
        public static ExternalLimitType fromString(String key) {
            return parseToEnumClass(
                    ExternalLimitType.class,
                    CaseFormat.UPPER_CAMEL.to(
                            CaseFormat.UPPER_UNDERSCORE, StringUtils.removeNonAlphaNumeric(key)));
        }
    }

    public enum EntryStatusCode {
        BOOKED,
        PENDING,
        REJECTED;

        @JsonCreator
        public static EntryStatusCode fromString(String key) {
            return parseToEnumClass(EntryStatusCode.class, key.toUpperCase());
        }
    }

    /**
     * https://openbankinguk.github.io/read-write-api-site3/v3.1.6/resources-and-data-models/aisp/Transactions.html#mutability
     */
    public enum TransactionMutability {
        MUTABLE,
        IMMUTABLE,
        UNDEFINED;

        @JsonCreator
        public static TransactionMutability fromString(String key) {
            return parseToEnumClass(TransactionMutability.class, key.toUpperCase());
        }

        public boolean isMutable() {
            return this == MUTABLE;
        }
    }

    /**
     * https://openbanking.atlassian.net/wiki/spaces/DZ/pages/937623722/Namespaced+Enumerations+-+v3.1#NamespacedEnumerations-v3.1-OBExternalAccountIdentification4Code
     */
    public enum ExternalAccountIdentification4Code {
        BBAN,
        IBAN,
        PAYM,
        SORT_CODE_ACCOUNT_NUMBER,
        PAN,
        NWB_CURRENCY_ACCOUNT,
        RBS_CURRENCY_ACCOUNT,
        SAVINGS_ROLL_NUMBER,
        DANSKE_BANK_ACCOUNT_NUMBER;

        private static final GenericTypeMapper<ExternalAccountIdentification4Code, String>
                ACCOUNT_IDENTIFIER_TYPE_MAPPER =
                        GenericTypeMapper
                                .<ExternalAccountIdentification4Code, String>genericBuilder()
                                .put(NWB_CURRENCY_ACCOUNT, "UK.NWB.CurrencyAccount")
                                .put(RBS_CURRENCY_ACCOUNT, "UK.RBS.CurrencyAccount")
                                .put(BBAN, "UK.OBIE.BBAN")
                                .put(
                                        DANSKE_BANK_ACCOUNT_NUMBER,
                                        "DK.DanskeBank.AccountNumber",
                                        "SE.DanskeBank.AccountNumber")
                                .put(IBAN, "UK.OBIE.IBAN")
                                .put(PAYM, "UK.OBIE.Paym")
                                .put(SORT_CODE_ACCOUNT_NUMBER, "UK.OBIE.SortCodeAccountNumber")
                                .put(PAN, "UK.OBIE.PAN", "PAN")
                                .put(SAVINGS_ROLL_NUMBER, "UK.Santander.SavingsRollNumber")
                                .build();

        @JsonCreator
        public static ExternalAccountIdentification4Code fromString(String key) {
            return (!Strings.isNullOrEmpty(key))
                    ? ACCOUNT_IDENTIFIER_TYPE_MAPPER
                            .translate(key)
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    String.format(
                                                            "[UkOpenBankingApiDefinitions] %s value is unknown for ExternalAccountIdentification4Code class!",
                                                            key)))
                    : null;
        }

        @JsonValue
        public String toValue() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.toString());
        }
    }

    public enum CreditDebitIndicator {
        DEBIT,
        CREDIT;

        @JsonCreator
        public static CreditDebitIndicator fromString(String key) {
            return parseToEnumClass(CreditDebitIndicator.class, key.toUpperCase());
        }
    }

    public enum PartyType {
        DELEGATE,
        JOINT,
        SOLE;

        @JsonCreator
        public static PartyType fromString(String key) {
            return parseToEnumClass(PartyType.class, key.toUpperCase());
        }
    }

    public enum ConsentStatus {
        AUTHORISED,
        AWAITING_AUTHORISATION,
        REJECTED,
        REVOKED;

        @JsonCreator
        public static ConsentStatus fromString(String key) {
            return mapStringToEnum(key)
                    .orElseThrow(
                            () ->
                                    SessionError.CONSENT_INVALID.exception(
                                            "[UkOpenBankingApiDefinitions] Unknown consent status: "
                                                    + key));
        }

        private static Optional<ConsentStatus> mapStringToEnum(String key) {
            try {
                return Enums.getIfPresent(
                                ConsentStatus.class,
                                CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                        .toJavaUtil();
            } catch (Exception e) {
                log.warn("Incorrect mapping key {} to ConsentStatus enum", key);
                return Optional.empty();
            }
        }
    }

    public interface BalanceTypeMapper {
        GenericTypeMapper<AccountBalanceType, UkObBalanceType> ACCOUNT_BALANCE_TYPE_MAPPER =
                GenericTypeMapper.<AccountBalanceType, UkObBalanceType>genericBuilder()
                        .put(AccountBalanceType.CLEARED_BALANCE, UkObBalanceType.CLEARED_BALANCE)
                        .put(
                                AccountBalanceType.CLOSING_AVAILABLE,
                                UkObBalanceType.CLOSING_AVAILABLE)
                        .put(AccountBalanceType.CLOSING_BOOKED, UkObBalanceType.CLOSING_BOOKED)
                        .put(AccountBalanceType.CLOSING_CLEARED, UkObBalanceType.CLOSING_CLEARED)
                        .put(AccountBalanceType.EXPECTED, UkObBalanceType.EXPECTED)
                        .put(
                                AccountBalanceType.FORWARD_AVAILABLE,
                                UkObBalanceType.FORWARD_AVAILABLE)
                        .put(AccountBalanceType.INFORMATION, UkObBalanceType.INFORMATION)
                        .put(
                                AccountBalanceType.INTERIM_AVAILABLE,
                                UkObBalanceType.INTERIM_AVAILABLE)
                        .put(AccountBalanceType.INTERIM_BOOKED, UkObBalanceType.INTERIM_BOOKED)
                        .put(AccountBalanceType.INTERIM_CLEARED, UkObBalanceType.INTERIM_CLEARED)
                        .put(
                                AccountBalanceType.OPENING_AVAILABLE,
                                UkObBalanceType.OPENING_AVAILABLE)
                        .put(AccountBalanceType.OPENING_BOOKED, UkObBalanceType.OPENING_BOOKED)
                        .put(AccountBalanceType.OPENING_CLEARED, UkObBalanceType.OPENING_CLEARED)
                        .put(
                                AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED,
                                UkObBalanceType.PREVIOUSLY_CLOSED_BOOKED)
                        .build();

        static AccountBalanceType toTinkAccountBalanceType(UkObBalanceType type) {
            return ACCOUNT_BALANCE_TYPE_MAPPER
                    .translate(type)
                    .orElseThrow(
                            () ->
                                    new IllegalStateException(
                                            "[BalanceTypeMapper] Unknown balance type: " + type));
        }
    }
}
