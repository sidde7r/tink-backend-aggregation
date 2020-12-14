package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.enums.UnicreditPaymentProduct;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payments.common.model.PaymentScheme;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UnicreditConstants {

    public static final GenericTypeMapper<PaymentType, Pair<Type, Type>> PAYMENT_TYPE_MAPPER =
            GenericTypeMapper.<PaymentType, Pair<Type, Type>>genericBuilder()
                    .put(PaymentType.SEPA, new Pair<>(Type.IBAN, Type.IBAN))
                    .build();

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_CREDENTIALS = "Client credentials missing.";
        public static final String MISSING_SCA_URL = "scaRedirect url not present in response";
        public static final String ACCOUNT_BALANCE_NOT_FOUND = "Account balance not found";
        public static final String UNDEFINED_BALANCE_TYPE =
                "There is balanceType of value '%s' defined in Enum %s";
        public static final String MISSING_LINKS_ENTITY = "Response missing links entity";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Endpoints {

        public static final String CONSENTS = "/hydrogen/v1/consents";
        public static final String CONSENT_STATUS = "/hydrogen/v1/consents/{consent-id}/status";
        public static final String CONSENT_DETAILS = "/hydrogen/v1/consents/{consent-id}";
        public static final String ACCOUNTS = "/hydrogen/v1/accounts";
        public static final String ACCOUNT_DETAILS = "/hydrogen/v1/accounts/{account-id}";
        public static final String BALANCES = "/hydrogen/v1/accounts/{account-id}/balances";
        public static final String TRANSACTIONS = "/hydrogen/v1/accounts/{account-id}/transactions";
        public static final String PAYMENT_INITIATION = "/hydrogen/v1/payments/{payment-product}";
        public static final String FETCH_PAYMENT =
                "/hydrogen/v1/payments/{payment-product}/{paymentId}";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PathParameters {

        public static final String CONSENT_ID = "consent-id";
        public static final String ACCOUNT_ID = "account-id";
        public static final String PAYMENT_PRODUCT = "payment-product";
        public static final String PAYMENT_ID = "paymentId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String STATE = "STATE";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryKeys {

        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String ENTRY_REFERENCE_FROM = "entryReferenceFrom";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String TRANSACTION_FROM_DATE = "1970-01-01";
        public static final int MAX_PERIOD_IN_DAYS = 89;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String PSU_ID_TYPE = "PSU-ID-Type";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TPP_REDIRECT_PREFERED = "TPP-Redirect-Preferred";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderValues {

        public static final String CODE = "code";
        public static final String PSU_IP_ADDRESS = "0.0.0.0";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormValues {

        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final int FREQUENCY_PER_DAY = 4;
        public static final int CONSENT_VALIDATION_PERIOD_IN_DAYS = 90;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Formats {
        public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ConsentStatusStates {
        public static final String VALID = "valid";
        public static final String VALID_PIS = "ACCP";
    }

    public static final int MAX_NO_OF_TRANSACTIONS_IN_ONE_PAGE = 500;

    public static final GenericTypeMapper<String, PaymentScheme> PAYMENT_PRODUCT_MAPPER =
            GenericTypeMapper.<String, PaymentScheme>genericBuilder()
                    .put(
                            UnicreditPaymentProduct.SEPA_CREDIT_TRANSFERS.toString(),
                            PaymentScheme.SEPA_CREDIT_TRANSFER)
                    .put(
                            UnicreditPaymentProduct.INSTANT_SEPA_CREDIT_TRANSFERS.toString(),
                            PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                    .build();
}
