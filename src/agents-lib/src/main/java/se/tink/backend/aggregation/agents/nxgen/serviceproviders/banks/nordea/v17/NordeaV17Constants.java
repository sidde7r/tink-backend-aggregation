package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.UrlEnum;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class NordeaV17Constants {
    private static final String VERSION = "V1.8";

    public static class Service {
        private static final String SERVICE_VERSION = "Service" + VERSION;
        public static final String BANKING = "Banking" + SERVICE_VERSION;
        public static final String SAVINGS = "Savings" + SERVICE_VERSION;
        public static final String AUTHENTICATION = "Authentication" + SERVICE_VERSION;
    }

    public static final Map<String, String> GENERAL_ERROR_MESSAGES_BY_CODE = ImmutableMap.<String, String>builder()
            .put("MAS9001", "Unknown error (found occurrence--and frequently occurring--during LightLoginRequest)")
            .put("MAS9098", "You are using an outdated version of the application. Please update your application in order to login")
            .put("MAS9099", "Technical error, please try again")
            .put("MBS9099", "A temporary error occurred")
            .put("MBS0110", "Your holdings can't be displayed at the moment, please try again later")
            .build();

    public static final Map<String, AuthorizationException> AUTHORIZATION_EXCEPTIONS_BY_CODE = ImmutableMap.<String, AuthorizationException>builder()
            .put("MAS0010", AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.CODE_BLOCKED.getKey()))
            .put("MAS0002", AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.CODE_BLOCKED.getKey()))
            .put("MBS0908", AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.NO_VALID_AGREEMENT.getKey()))
            .build();

    public static final Map<String, AuthenticationException> AUTHENTICATION_EXCEPTIONS_BY_CODE = ImmutableMap.<String, AuthenticationException>builder()
            .put("MAS0031", LoginError.INCORRECT_CREDENTIALS.exception())
            .put("MAS0030", LoginError.INCORRECT_CREDENTIALS.exception())
            .put("MAS0004", LoginError.INCORRECT_CREDENTIALS.exception())
            .build();

    public static final LogTag HTTP_REQUEST_LOG_TAG = LogTag.from("#nordea_v17_http_request");
    public static final LogTag CREDITCARD_LOG_TAG = LogTag.from("#nordea_v17_creditcard");

    public enum UserMessage implements LocalizableEnum {
        CODE_BLOCKED(new LocalizableKey("Your personal code has been locked. Contact Nordea customer services (0771-42 15 16) to order a new code, or contact your local Nordea office.")),
        NO_VALID_BANKID(new LocalizableKey("You're missing a valid Mobilt BankID. Download the BankID app and login to Internetbanken to order and connect to Mobil BankID.")),
        NO_VALID_AGREEMENT(new LocalizableKey("We could not find a valid internet banking agreement. If you login to Nordea's internetbank with e-code (card reader) you may sign an agreement for internet and telephone banking"));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    public static class UrlParameter {
        public static final String MARKET_CODE = "marketCode";
        public static final String CARD_BALANCES_PARAM = "cardNumbers";
        public static final String CARD_NUMBER = "cardNumber";
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymendId";
        public static final String CONTINUE_KEY = "continueKey";
        public static final String CREDIT_CONTINUE_KEY = "continuationKey";
        public static final String INVOICE_PERIOD = "invoicePeriod";
    }

    public enum Url implements UrlEnum {
        INITIAL_CONTEXT(getBankingEndpoint("/initialContext")),
        CARDS_BALANCES(getBankingEndpoint("/Cards/Balances")),
        LOANS(getBankingEndpoint("/Loans/Details/{" + UrlParameter.ACCOUNT_ID + "}")),
        TRANSACTIONS(getBankingEndpoint("/Transactions")),
        PAYMENTS(getBankingEndpoint("/Payments")),
        PAYMENT_DETAILS(getBankingEndpoint("/Payments/{" + UrlParameter.PAYMENT_ID + "}")),
        LIGHT_LOGIN(getAuthenticationEndpoint("/SecurityToken")),
        REGISTER_DEVICE(getAuthenticationEndpoint("/RegisterDevice")),
        CUSTODY_ACCOUNTS(getSavingsEndpoint("/CustodyAccounts"));

        public static final String BASE_URL = "https://no.mobilebank.prod.nordea.com/";
        private final URL url;

        Url(String url) {
            this.url = new URL(url);
        }

        public static String constructServiceEndpoint(String service, String path) {
            return BASE_URL + "{" + UrlParameter.MARKET_CODE + "}/" + service + path;
        }

        public static String getBankingEndpoint(String path) {
            return constructServiceEndpoint(Service.BANKING, path);
        }

        public static String getAuthenticationEndpoint(String path) {
            return constructServiceEndpoint(Service.AUTHENTICATION, path);
        }

        public static String getSavingsEndpoint(String path) {
            return constructServiceEndpoint(Service.SAVINGS, path);
        }

        @Override
        public URL get() {
            return url;
        }

        @Override
        public URL parameter(String key, String value) {
            return url.parameter(key, value);
        }

        @Override
        public URL queryParam(String key, String value) {
            return url.queryParam(key, value);
        }
    }


    public static class HeaderKey {
        public static final String REQUEST_ID = "x-Request-Id";
        public static final String APP_COUNTRY = "x-App-Country";
        public static final String APP_VERSION = "x-App-Version";
    }

    public enum Header {
        PLATFORM_VERSION("x-Platform-Version", "10.2"),
        APP_LANGUAGE("x-App-Language", "en"),
        APP_NAME("x-App-Name", "MBA"),
        APP_VERSION("x-App-Version", "2.4.0-256"),
        DEVICE_MAKE("x-Device-Make", "Tink"),
        DEVICE_MODEL("x-Device-Model", "Tink"),
        PLATFORM_TYPE("x-Platform-Type", "iOS");

        private final String key;
        private final String value;

        Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static class ProductType {
        public static final String ACCOUNT = "Account";
        public static final String CARD = "Card";
        public static final String LOAN = "Loan";
    }

    public static class Payment {
        public enum StatusCode {
            UNCONFIRMED("Unconfirmed"), CONFIRMED("Confirmed");

            private String serializedValue;

            StatusCode(String serializedValue) {
                this.serializedValue = serializedValue;
            }

            public String getSerializedValue() {
                return serializedValue;
            }

            public Predicate<PaymentEntity> predicateForType() {
                final String serializedValue = getSerializedValue();
                return paymentEntity -> paymentEntity != null &&
                        serializedValue.equalsIgnoreCase(paymentEntity.getStatusCode());
            }
        }
    }

    public static class Investments {
        public static class PortfolioTypes {
            public static final String FOND = "fonda";
            public static final String ISK = "isk";
            public static final String IPS = "ips";
            public static final String ASBS = "asbs";
            public static final String AKTIV = "aktiv";
            public static final String NLP = "nlp";
        }

        public static class InstrumentTypes {
            public static final String EQUITY = "equity";
            public static final String FUND = "fund";
            public static final String DERIVATIVE = "derivative";
        }
    }

    public static class CardType {
        public static final String CREDIT_CARD = "creditcard";
    }
}
