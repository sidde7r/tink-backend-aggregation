package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import com.fasterxml.jackson.annotation.JsonCreator;
import se.tink.libraries.strings.StringUtils;

public abstract class UkOpenBankingConstants {

    public static class ApiServices {

        public static final String ACCOUNT_BULK_REQUEST = "/accounts";
        public static final String ACCOUNT_BALANCE_BULK_REQUEST = "/balances";
        private static final String ACCOUNT_TRANSACTIONS_REQUEST = "/accounts/%s/transactions";

        public static String getTransactionsRequest(String accountId) {
            return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
        }
    }

    public enum CreditDebitIndicator {

        DEBIT,
        CREDIT;

        @JsonCreator
        public static CreditDebitIndicator fromString(String key) {
            return (key != null) ?
                    CreditDebitIndicator.valueOf(
                            StringUtils.removeNonAlphaNumeric(key).toUpperCase()) : null;
        }
    }

    public enum EntryStatusCode {

        BOOKED,
        PENDING;

        @JsonCreator
        public static EntryStatusCode fromString(String key) {
            return (key != null) ?
                    EntryStatusCode.valueOf(
                            StringUtils.removeNonAlphaNumeric(key).toUpperCase()) : null;
        }
    }

    public enum ExternalLimitType {

        AVAILABLE,
        CREDIT,
        EMERGENCY,
        PREAGREED,
        TEMPORARY;

        @JsonCreator
        public static ExternalLimitType fromString(String key) {
            return (key != null) ?
                    ExternalLimitType.valueOf(
                            StringUtils.removeNonAlphaNumeric(key).toUpperCase()) : null;
        }
    }

}
