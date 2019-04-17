package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Optional;
import se.tink.libraries.strings.StringUtils;

public class UkOpenBankingApiDefinitions {
    /** Enums are specified as long form of ISO 20022 */
    public enum BankTransactionCode {
        ISSUED_CREDIT_TRANSFERS,
        ISSUED_CASH_CONCENTRATION,
        ISSUED_DIRECT_DEBITS,
        ISSUED_CHEQUES,
        MERCHANT_CARD_TRANSACTIONS,
        CUSTOMER_CARD_TRANSACTIONS,
        DRAFTS_OF_ORDERS,
        BILL_OF_ORDERS,
        ISSUED_REAL_TIME_CREDIT_TRANSFER,
        RECEIVED_CREDIT_TRANSFERS,
        RECEIVED_CASH_CONCENTRATION,
        RECEIVED_DIRECT_DEBITS,
        RECEIVED_CHEQUES,
        LOCK_BOX,
        COUNTER_TRANSACTIONS,
        RECEIVED_REAL_TIME_CREDIT_TRANSFER,
        NOT_AVAILABLE,
        OTHER,
        MISCELLANEOUS_CREDIT_OPERATIONS,
        MISCELLANEOUS_DEBIT_OPERATIONS;

        private static final ImmutableSet<BankTransactionCode> OUTGOING_TRANSACTION_CODES =
                ImmutableSet.<BankTransactionCode>builder()
                        .add(ISSUED_CREDIT_TRANSFERS)
                        .add(ISSUED_CASH_CONCENTRATION)
                        .add(ISSUED_DIRECT_DEBITS)
                        .add(ISSUED_CHEQUES)
                        .add(CUSTOMER_CARD_TRANSACTIONS)
                        .add(MERCHANT_CARD_TRANSACTIONS)
                        .add(DRAFTS_OF_ORDERS)
                        .add(BILL_OF_ORDERS)
                        .add(ISSUED_REAL_TIME_CREDIT_TRANSFER)
                        .build();

        @JsonCreator
        private static BankTransactionCode fromString(String key) {
            return (key != null)
                    ? BankTransactionCode.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }

        public boolean isOutGoing() {
            return OUTGOING_TRANSACTION_CODES.contains(this);
        }
    }

    public enum AccountBalanceType {
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

        private static final ImmutableList<AccountBalanceType> PREFERRED_TYPE_LIST =
                ImmutableList.<AccountBalanceType>builder()
                        .add(INTERIM_BOOKED)
                        .add(INTERIM_AVAILABLE)
                        .add(EXPECTED)
                        .add(OPENING_AVAILABLE)
                        .add(CLOSING_AVAILABLE)
                        .add(OPENING_BOOKED)
                        .add(CLOSING_BOOKED)
                        .build();

        public static <T> Optional<T> getPreferredBalanceEntity(
                Map<AccountBalanceType, T> typeMap) {
            for (AccountBalanceType id : PREFERRED_TYPE_LIST) {
                if (typeMap.containsKey(id)) {
                    return Optional.of(typeMap.get(id));
                }
            }
            return Optional.empty();
        }

        @JsonCreator
        private static AccountBalanceType fromString(String key) {
            return (key != null)
                    ? AccountBalanceType.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }
    }

    public enum ExternalLimitType {
        AVAILABLE,
        CREDIT,
        EMERGENCY,
        PRE_AGREED,
        TEMPORARY;

        @JsonCreator
        private static ExternalLimitType fromString(String key) {
            return (key != null)
                    ? ExternalLimitType.valueOf(
                            CaseFormat.UPPER_CAMEL.to(
                                    CaseFormat.UPPER_UNDERSCORE,
                                    StringUtils.removeNonAlphaNumeric(key)))
                    : null;
        }
    }

    public enum EntryStatusCode {
        BOOKED,
        PENDING;

        @JsonCreator
        private static EntryStatusCode fromString(String key) {
            return (key != null)
                    ? EntryStatusCode.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }
    }

    public enum ExternalAccountIdentification2Code {
        IBAN,
        SORT_CODE_ACCOUNT_NUMBER;

        @JsonCreator
        public static ExternalAccountIdentification2Code fromString(String key) {
            return (key != null)
                    ? ExternalAccountIdentification2Code.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }

        //        @JsonValue
        public String toValue() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.toString());
        }
    }

    public enum ExternalAccountIdentification3Code {
        IBAN,
        SORT_CODE_ACCOUNT_NUMBER,
        PAN;

        @JsonCreator
        private static ExternalAccountIdentification3Code fromString(String key) {
            return (key != null)
                    ? ExternalAccountIdentification3Code.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }

        //        @JsonValue
        public String toValue() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.toString());
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
        PAN;

        @JsonCreator
        private static ExternalAccountIdentification3Code fromString(String key) {
            return (key != null)
                    ? ExternalAccountIdentification3Code.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }

        //        @JsonValue
        public String toValue() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.toString());
        }
    }

    public enum OBExternalFinancialInstitutionIdentification4Code {
        BICFI;

        @JsonCreator
        private static ExternalAccountIdentification3Code fromString(String key) {
            return (key != null)
                    ? ExternalAccountIdentification3Code.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }

        //        @JsonValue
        public String toValue() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.toString());
        }
    }

    public enum ExternalPaymentContext1Code {
        BILL_PAYMENT,
        ECOMMERCE_GOODS,
        ECOMMERCE_SERVICES,
        PERSON_TO_PERSON,
        OTHER;

        @JsonCreator
        private static ExternalPaymentContext1Code fromString(String key) {
            return (key != null)
                    ? ExternalPaymentContext1Code.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }

        //        @JsonValue
        public String toValue() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.toString());
        }
    }

    public enum TransactionIndividualStatus1Code {
        ACCEPTED_CUSTOMER_PROFILE,
        ACCEPTED_SETTLEMENT_COMPLETED,
        ACCEPTED_SETTLEMENT_IN_PROCESS,
        ACCEPTED_TECHNICAL_VALIDATION,
        PENDING,
        REJECTED;

        @JsonCreator
        private static TransactionIndividualStatus1Code fromString(String key) {
            return (key != null)
                    ? TransactionIndividualStatus1Code.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }

        //        @JsonValue
        public String toValue() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.toString());
        }
    }

    public enum CreditDebitIndicator {
        DEBIT,
        CREDIT;

        @JsonCreator
        private static CreditDebitIndicator fromString(String key) {
            return (key != null)
                    ? CreditDebitIndicator.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }
    }
}
