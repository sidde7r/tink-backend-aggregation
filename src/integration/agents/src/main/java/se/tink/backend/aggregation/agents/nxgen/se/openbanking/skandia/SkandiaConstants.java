package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SkandiaConstants {
    public static final String PROVIDER_MARKET = "SE";

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "Allt-i-Ett konto")
                    .put(TransactionalAccountType.SAVINGS, "Sparkonto")
                    .build();

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder()
                    .put(PaymentStatus.PENDING, "PDNG", "RCVD")
                    .put(
                            PaymentStatus.SIGNED,
                            "ACSC",
                            "ACSP",
                            "ACTC",
                            "ACCC",
                            "ACCP",
                            "ACWC",
                            "ACWP")
                    .put(PaymentStatus.REJECTED, "RJCT")
                    .put(PaymentStatus.CANCELLED, "CANC")
                    .build();

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_CREDENTIALS = "Client Credentials missing.";
        public static final String UNSUPPORTED_PAYMENT_TYPE = "Payment type is not supported.";
        public static final String UNSUPPORTED_REMITTANCE_INFORMATION =
                "Remittance information not supported.";
        public static final String MISSING_TOKEN = "Failed to retrieve access token.";
        public static final String SERVICE_BLOCKED = "Service_blocked";
        public static final String EXPIRED_AUTHORIZATION_CODE =
                "Authorization code is invalid or expired.";
        public static final String INVALID_INFO_STRUCTURED =
                "Invalid remittance information structured";
        public static final String INVALID_INFO_UNSTRUCTURED =
                "Invalid remittance information unstructured";
        public static final String REMITTANCE_INFO_NOT_SET_FOR_GIROS =
                "Only one of remittance information unstructured or structured can be set";
        public static final String INVALID_CREDITOR_ACCOUNT = "Invalid creditor account";
        public static final String INVALID_REQUESTED_EXECUTION_DATE =
                "Invalid requested execution date";
        public static final String NOT_ENOUGH_FUNDS =
                "Not enough funds on account to make payments";
        public static final String SCA_REDIRECT_MISSING = "SCA Redirect missing";
        public static final String INVALID_SCOPE =
                "%s contain invalid scope(s), only support scopes AIS and PIS";
        public static final String MISSING_SCOPE = "Scope not provided, support scopes AIS and PIS";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorCodes {
        public static final String INVALID_GRANT = "invalid_grant";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final String BASE_OAUTH = "https://fsts.skandia.se/as";
        public static final String BASE_URL = "https://apis.skandia.se/open-banking/core-bank";
        public static final URL AUTHORIZE = new URL(BASE_OAUTH + ApiServices.AUTHORIZE);
        public static final URL TOKEN = new URL(BASE_OAUTH + ApiServices.TOKEN);
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiServices.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiServices.GET_TRANSACTIONS);
        public static final URL GET_BALANCES = new URL(BASE_URL + ApiServices.GET_BALANCES);
        public static final URL CREATE_PAYMENT = new URL(BASE_URL + ApiServices.CREATE_PAYMENT);
        public static final URL GET_PAYMENT = new URL(BASE_URL + ApiServices.GET_PAYMENT);
        public static final URL GET_PAYMENT_STATUS =
                new URL(BASE_URL + ApiServices.GET_PAYMENT_STATUS);
        public static final URL DELETE_PAYMENT = new URL(BASE_URL + ApiServices.DELETE_PAYMENT);
        public static final URL POST_SIGN_PAYMENT =
                new URL(BASE_URL + ApiServices.POST_SIGN_PAYMENT);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ApiServices {
        public static final String AUTHORIZE = "/authorization.oauth2";
        public static final String TOKEN = "/token.oauth2";
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_TRANSACTIONS = "/v1/accounts/{accountId}/transactions";
        public static final String GET_BALANCES = "/v1/accounts/{accountId}/balances";
        public static final String CREATE_PAYMENT = "/pis/v2/payments/{paymentProduct}";
        public static final String GET_PAYMENT = "/pis/v2/payments/{paymentProduct}/{paymentId}";
        public static final String GET_PAYMENT_STATUS =
                "/pis/v2/payments/paymentProduct/{paymentId}/status";
        public static final String DELETE_PAYMENT = "/pis/v2/payments/{paymentProduct}/{paymentId}";
        public static final String POST_SIGN_PAYMENT =
                "/pis/v2/payments/{paymentProduct}/{paymentId}/authorisations";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryValues {
        public static final String PENDING = "pending";
        public static final String BOOKED = "booked";
        public static final String CODE = "code";
        public static final String SCOPE = "psd2.aisp%20psd2.pisp";
        public static final String SCOPE_WITHOUT_PAYMENT = "psd2.aisp";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormKeys {
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String X_CLIENT_CERTIFICATE = "X-Client-Certificate";
        public static final String CLIENT_ID = "Client-Id";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_TYPE = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    @Getter
    @RequiredArgsConstructor
    public enum PaymentProduct {
        DOMESTIC_CREDIT_TRANSFERS("domestic-credit-transfer", ReferenceType.PDTX),
        DOMESTIC_GIROS("giro-domestic-credit-transfer", ReferenceType.SCOR),
        CROSS_BORDER_CREDIT_TRANSFERS("cross-border-credit-transfers", ReferenceType.SCOR);

        private final String product;
        private final ReferenceType referenceType;

        private static final GenericTypeMapper<PaymentProduct, AccountIdentifierType> MAPPER =
                GenericTypeMapper.<PaymentProduct, AccountIdentifierType>genericBuilder()
                        .put(PaymentProduct.DOMESTIC_CREDIT_TRANSFERS, AccountIdentifierType.SE)
                        .put(
                                PaymentProduct.DOMESTIC_GIROS,
                                AccountIdentifierType.SE_BG,
                                AccountIdentifierType.SE_PG)
                        .build();

        public static PaymentProduct from(Payment payment) {
            return MAPPER.translate(payment.getCreditor().getAccountIdentifierType())
                    .orElseThrow(
                            () ->
                                    new IllegalStateException(
                                            ErrorMessages.UNSUPPORTED_PAYMENT_TYPE));
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PaymentTypes {
        public static final String DOMESTIC_CREDIT_TRANSFERS_RESPONSE = "DOMESTIC_CREDIT_TRANSFERS";
        public static final String DOMESTIC_GIROS_RESPONSE = "DOMESTIC_GIROS";
        public static final String CROSS_BORDER_CREDIT_TRANSFERS_RESPONSE =
                "CROSS_BORDER_CREDIT_TRANSFERS";
    }

    public enum ReferenceType {
        SCOR,
        PDTX
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BodyValues {
        public static final String EMPTY_BODY = "{}";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AccountIdentifier {

        public static final String BANK_GIRO_TYPE = "BANKGIRO";
        public static final String PLUS_GIRO_TYPE = "PLUSGIRO";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Date {
        static final int GIROS_DOMESTIC_CUT_OFF_HOURS = 9;
        static final int GIROS_DOMESTIC_CUT_OFF_MINUTES = 0;

        static final int DOMESTIC_CUT_OFF_HOURS = 13;
        static final int DOMESTIC_CUT_OFF_MINUTES = 45;

        static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Stockholm");
        static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MinimumValues {
        public static final BigDecimal MINIMUM_AMOUNT = BigDecimal.ONE;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class Scopes {
        public static final String AIS = "AIS";
        public static final String PIS = "PIS";
    }
}
