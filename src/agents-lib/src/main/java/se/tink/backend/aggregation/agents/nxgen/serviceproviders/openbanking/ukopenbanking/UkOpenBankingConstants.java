package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.strings.StringUtils;

public abstract class UkOpenBankingConstants {

    public static class ApiServices {

        private static final String ACCOUNT_BULK_REQUEST = "/accounts";
        private static final String ACCOUNT_BALANCE_REQUEST = "/accounts/%s/balances";
        private static final String ACCOUNT_TRANSACTIONS_REQUEST = "/accounts/%s/transactions";
        private static final String ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST = "/accounts/%s/scheduled-payments";

        public static URL getBulkAccountRequestURL(URL apiBaseUrl) {
            return apiBaseUrl.concat(ACCOUNT_BULK_REQUEST);
        }

        public static URL getAccountBalanceRequestURL(URL apiBaseUrl, String accountId) {
            return apiBaseUrl.concat(String.format(ACCOUNT_BALANCE_REQUEST, accountId));
        }

        public static String getInitialTransactionsPaginationKey(String accountId) {
            return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
        }

        public static URL getUpcomingTransactionRequestURL(URL apiBaseUrl, String accountId) {
            return apiBaseUrl.concat(String.format(ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
        }
    }

    public enum CreditDebitIndicator {

        DEBIT,
        CREDIT;

        @JsonCreator
        private static CreditDebitIndicator fromString(String key) {
            return (key != null) ?
                    CreditDebitIndicator.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key)) : null;
        }
    }

    public enum EntryStatusCode {

        BOOKED,
        PENDING;

        @JsonCreator
        private static EntryStatusCode fromString(String key) {
            return (key != null) ?
                    EntryStatusCode.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key)) : null;
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
            return (key != null) ?
                    ExternalLimitType.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE,
                                    StringUtils.removeNonAlphaNumeric(key))) : null;
        }
    }

    /**
     * Enums are specified as long form of ISO 20022
     */
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

        public boolean isOutGoing() {
            return OUTGOING_TRANSACTION_CODES.contains(this);
        }

        @JsonCreator
        private static BankTransactionCode fromString(String key) {
            return (key != null) ?
                    BankTransactionCode.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key)) : null;
        }
    }

}
