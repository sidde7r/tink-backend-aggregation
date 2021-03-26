package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.SAVINGS_ROLL_NUMBER;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.strings.StringUtils;

public class UkOpenBankingApiDefinitions {

    public static final List<ExternalAccountIdentification4Code>
            ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS =
                    ImmutableList.of(SORT_CODE_ACCOUNT_NUMBER, IBAN, BBAN, SAVINGS_ROLL_NUMBER);

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

        private static final ImmutableList<ExternalLimitType> PREFERRED_LIMIT_TYPE_LIST =
                ImmutableList.<ExternalLimitType>builder()
                        .add(AVAILABLE)
                        .add(CREDIT)
                        .add(PRE_AGREED)
                        .add(EMERGENCY)
                        .add(TEMPORARY)
                        .build();

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

    /**
     * https://openbankinguk.github.io/read-write-api-site3/v3.1.6/resources-and-data-models/aisp/Transactions.html#mutability
     */
    public enum TransactionMutability {
        MUTABLE,
        IMMUTABLE,
        UNDEFINED;

        @JsonCreator
        public static TransactionMutability fromString(String key) {
            return Optional.ofNullable(key)
                    .map(k -> valueOf(key.toUpperCase()))
                    .orElse(TransactionMutability.UNDEFINED);
        }

        public boolean isMutable() {
            return this == MUTABLE;
        }
    }

    /**
     * https://openbanking.atlassian.net/wiki/spaces/DZ/pages/937623722/Namespaced+Enumerations+-+v3.1#NamespacedEnumerations-v3.1-OBExternalAccountIdentification4Code
     */
    public enum ExternalAccountIdentification4Code {
        // todo remove unnecessary mapping
        BBAN,
        IBAN,
        PAYM,
        SORT_CODE_ACCOUNT_NUMBER,
        PAN,
        SAVINGS_ROLL_NUMBER;

        private static final GenericTypeMapper<ExternalAccountIdentification4Code, String>
                ACCOUNT_IDENTIFIER_TYPE_MAPPER =
                        GenericTypeMapper
                                .<ExternalAccountIdentification4Code, String>genericBuilder()
                                .put(
                                        ExternalAccountIdentification4Code.BBAN,
                                        "UK.OBIE.BBAN",
                                        "DK.DanskeBank.AccountNumber",
                                        "UK.NWB.CurrencyAccount")
                                .put(ExternalAccountIdentification4Code.IBAN, "UK.OBIE.IBAN")
                                .put(ExternalAccountIdentification4Code.PAYM, "UK.OBIE.Paym")
                                .put(
                                        ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER,
                                        "UK.OBIE.SortCodeAccountNumber")
                                .put(ExternalAccountIdentification4Code.PAN, "UK.OBIE.PAN", "PAN")
                                .put(
                                        ExternalAccountIdentification4Code.SAVINGS_ROLL_NUMBER,
                                        "UK.Santander.SavingsRollNumber")
                                .build();

        @JsonCreator
        private static ExternalAccountIdentification4Code fromString(String key) {

            return (key != null)
                    ? ACCOUNT_IDENTIFIER_TYPE_MAPPER
                            .translate(key)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    String.format(
                                                            "%s unknown ExternalAccountIdentification4Code!",
                                                            key)))
                    : null;
        }

        @JsonValue
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

        @JsonValue
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

        @JsonValue
        public String toValue() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.toString());
        }
    }

    public enum CreditDebitIndicator {
        DEBIT,
        CREDIT;

        @JsonCreator
        private static CreditDebitIndicator fromString(String key) {
            return key != null ? CreditDebitIndicator.valueOf(key.toUpperCase()) : null;
        }
    }

    public enum PartyType {
        DELEGATE,
        JOINT,
        SOLE;

        @JsonCreator
        private static PartyType fromString(String key) {
            return (key != null)
                    ? PartyType.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }
    }
}
