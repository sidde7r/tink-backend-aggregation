package se.tink.backend.aggregation.agents.nxgen.es.banks.popular;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class BancoPopularConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(BancoPopularConstants.class);

    public static class PersistentStorage {
        public static final String LOGIN_CONTRACTS = "LoginContracts";
        public static final String CLIENT_IP = "ClientIp";
    }

    public static class ApiService {
        public static final String LOGIN_PATH = "acl/login";
        public static final String KEEP_ALIVE_PATH = "acl/keepaliveJson";
        public static final String SET_CONTRACT_PATH = "acl/setContract";
        public static final String ACCOUNTS_PATH = "cuentas/BTAT";
        public static final String TRANSACTIONS_PATH = "cuentas/BTD6";
    }

    public static class Urls {
        private static final String BASE ="https://bm.bancopopular.es/ACBL/";

        public static final URL LOGIN_URL = new URL(BASE + ApiService.LOGIN_PATH);
        public static final URL KEEP_ALIVE_URL = new URL(BASE + ApiService.KEEP_ALIVE_PATH);

        public static final URL SET_CONTRACT_URL = new URL(BASE + ApiService.SET_CONTRACT_PATH);
        public static final URL FETCH_ACCOUNTS_URL = new URL(BASE + ApiService.ACCOUNTS_PATH);
        public static final URL FETCH_TRANSACTIONS_URL = new URL(BASE + ApiService.TRANSACTIONS_PATH);
    }

    public static class Fetcher {
        public static final String OK = "OK";

        public static final String MOVEMENT_TYPE = "T";
        public static final int CONCEP_ECRMVTO_2 = 98;

        public static final String CHECKING_ACCOUNT_IDENTIFIER = "A32";  // account
        public static final String CREDIT_CARD_ACCOUNT_IDENTIFIER = "A45";  // credit card account
        public static final String FUND_ACCOUNT_IDENTIFIER = "J01";  // funds
        public static final String LOAN_ACCOUNT_IDENTIFIER = "K11";   // loan
        public static final String INSURANCE_ACCOUNT_IDENTIFIER = "F99";   // securities (stocks?)

        // BTAT query constants
        public static final String LEVEL = "002";
        public static final String OCCURRENCES = "15";
        public static final String INDICATOR_3 = "1";
        public static final String INDICATOR_5 = "4";
        public static final String ACCOUNTS_PAGE = "full";

        // codServicio
        // ? transfer
        public static final int COD_SERVICIO_ACCOUNT_SETTLEMENT = 871;
        // payment (from)
        public static final int COD_SERVICIO_ORDEN_PAGO = 360;
        // transfer made
        public static final int COD_SERVICIO_TRANSFER_MADE = 305;
        // transfer received
        public static final int COD_SERVICIO_TRANSFER_RECEIVED = 350;
        // credit card payment
        public static final int COD_SERVICIO_PAGO_CON = 618;

        public static final String AMOUNT_SIGN_INDICATOR_1 = "D";
        public static final String AMOUNT_SIGN_INDICATOR_2 = "-";

        public static final String MERCHANT_NAME = "merchantName";
        public static final String MERCHANT_NAME_REGEX = String.format(
                ".*, en (?<%s>.+)$", MERCHANT_NAME);

        public static final LogTag INVESTMENT_LOGGING =
                LogTag.from("#investment-logging-popular-es");
        public static final LogTag LOAN_LOGGING =
                LogTag.from("#loan-logging-popular-es");
    }

    public static class Authentication {

        public static final String HEADER_TKN_CRC = "TKN-CRC";

        public static final String HEADER_X_CLIENT_IP = "X-Client-IP";

        public static final String HEADER_COD_PLATAFORMA = "cod_plataforma";
        public static final String PLATAFORMA = "20";

        public static final String CRYPTO_ALG = "HmacSHA512";
    }

    public static class StatusCodes {
        public static final int SESSION_EXPIRED = 454;
        public static final int INCORRECT_USERNAME_PASSWORD = 401;
        public static final int INCORRECT_TOKEN = 412;
    }

    public static final class Tags {
        public static final LogTag UNKNOWN_PRODUCT_CODE = LogTag.from("es_popular_unknown_product_code");
    }

    public static class ProductCode {

        /**
         * CUENTA CORRIENTE
         */
        private static final int CHECKING_ACCOUNT = 100;
        /**
         * CUENTA DE AHORRO
         */
        private static final int SAVINGS_ACCOUNT = 110;

        public static Optional<AccountTypes> translate(int productCode) {
            switch (productCode) {
            case CHECKING_ACCOUNT:
                return Optional.of(AccountTypes.CHECKING);
            case SAVINGS_ACCOUNT:
                return Optional.of(AccountTypes.SAVINGS);
            default:
                LOGGER.info("{} Unknown product code: {}", BancoPopularConstants.Tags.UNKNOWN_PRODUCT_CODE,
                        productCode);
                return Optional.empty();
            }
        }
    }

}
