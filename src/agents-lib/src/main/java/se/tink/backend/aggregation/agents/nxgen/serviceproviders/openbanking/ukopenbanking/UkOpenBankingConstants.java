package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import com.fasterxml.jackson.annotation.JsonCreator;
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
