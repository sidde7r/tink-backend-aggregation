package se.tink.backend.aggregation.agents.nxgen.es.banks.ing;

import java.time.ZoneId;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Product;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class IngConstants {

    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String DATE_OF_BIRTH = "date-of-birth";
    public static final String ORIGINAL_ENTITY = "originalEntity";
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Madrid");
    public static final String PROVIDER_NAME = "es-ing-password";
    public static final String MARKET = "es";
    public static final String CURRENCY = "EUR";
    private static final String URL_BASE = "https://ing.ingdirect.es";
    private static final AggregationLogger LOGGER = new AggregationLogger(IngConstants.class);

    public final class Url {
        public static final String GENOMA_LOGIN_REST_SESSION = URL_BASE + "/genoma_login/rest/session";
        public static final String GENOMA_API_REST_COMMUNICATION = URL_BASE + "/genoma_api/rest/communication/";
        public static final String GENOMA_API_REST_SESSION = URL_BASE + "/genoma_api/rest/session";
        public static final String GENOMA_API_REST_CLIENT = URL_BASE + "/genoma_api/rest/client";
        public static final String GENOMA_API_REST_PRODUCTS = URL_BASE + "/genoma_api/rest/products";
        public static final String GENOMA_API_REST_PRODUCTS_MOVEMENTS = URL_BASE + "/genoma_api/rest/products/{product}/movements";
        public static final String GENOMA_API_LOGIN_AUTH_RESPONSE = URL_BASE + "/genoma_api/login/auth/response";
    }

    public static class Default {
        public static final String MOBILE_PHONE = "mobilePhone";
        public static final String SESSION_NAME_ALL = "all";
        public static final String ACTION_NAME_LOGOUT = "logout";
        public static final String OPERATION_NAME_EMPTY = "";
    }

    public static class UsernameType {
        public static final int NIF = 0;
        public static final int NIE = 1;
        public static final int PASSPORT = 2;
    }

    public static class ErrorCode {
        public static final String INVALID_LOGIN_DOCUMENT_TYPE = "ESValidLoginDocument.loginDocument";
    }

    public static class ProductType {

        /**
         * Tarjeta Débito
         */
        private static final int DEBIT_CARD = 1;
        /**
         * Tarjeta Crédito
         */
        private static final int CREDIT_CARD = 3;
        /**
         * Cuenta de efectivo
         */
        private static final int CASH_ACCOUNT = 10;
        /**
         * Cuenta sin nomina
         */
        private static final int ACCOUNT_WITHOUT_PAYROLL = 17;
        /**
         * Cuenta NARANJA
         */
        private static final int ORANGE_ACCOUNT = 20;
        /**
         * Fondo Dinámico, Fondo Renta Fija, Fondo S&P 500, Fondo Ibex 35...
         */
        private static final int INVESTMENT_FUND = 40;
        /**
         * Plan 2050
         */
        private static final int PENSION_PLAN_2050 = 41;
        /**
         * Cuenta de valores
         */
        private static final int VALUE_ACCOUNT = 42;
        /**
         * Préstamo NARANJA
         */
        private static final int ORANGE_LOAN = 77;
        /**
         * MINI Cuota
         */
        private static final int LIFE_INSURANCE_MINI_CUOTA = 100;

        public static Optional<AccountTypes> translate(Product product) {

            switch (product.getType()) {
            case DEBIT_CARD:
            case VALUE_ACCOUNT:
            case LIFE_INSURANCE_MINI_CUOTA:
                return Optional.empty();
            case CREDIT_CARD:
                return Optional.of(AccountTypes.CREDIT_CARD);
            case CASH_ACCOUNT:
            case ACCOUNT_WITHOUT_PAYROLL:
                return Optional.of(AccountTypes.CHECKING);
            case ORANGE_ACCOUNT:
                return Optional.of(AccountTypes.SAVINGS);
            case INVESTMENT_FUND:
                return Optional.of(AccountTypes.INVESTMENT);
            case PENSION_PLAN_2050:
                return Optional.of(AccountTypes.PENSION);
            case ORANGE_LOAN:
                return Optional.of(AccountTypes.LOAN);
            default:
                LOGGER.infoExtraLong(SerializationUtils.serializeToString(product),
                        IngConstants.Logging.UNKNOWN_ACCOUNT_TYPE);
                return Optional.empty();
            }
        }
    }

    public static class Query {
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
        public static final String LIMIT = "limit";
        public static final String OFFSET = "offset";
        public static final String TICKET = "ticket";
        public static final String DEVICE = "device";
        public static final String SESSION_NAME = "sessionName";
        public static final String ACTION_NAME = "actionName";
        public static final String OPERATION_NAME = "operationName";
    }

    public final class FetchControl {
        /** max allowed is 100 */
        public static final int PAGE_SIZE = 100;
    }

    public static class Logging {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from(PROVIDER_NAME + "-unknown-account-type");
    }
}
