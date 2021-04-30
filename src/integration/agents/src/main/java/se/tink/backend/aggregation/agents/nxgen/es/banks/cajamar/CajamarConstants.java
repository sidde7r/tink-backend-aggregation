package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.libraries.account.enums.AccountFlag;

public class CajamarConstants {
    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "3")
                    .build();

    public static final class LoginParameter {
        public static final String APP_NAME = "WEFFERENT";
        public static final String APP_VERSION = "1.67.28";
        public static final String OS_NAME = "IOS";
        public static final String OS_VERSION = "14.4.1";
        public static final String DEVICE_ID = "1101031615543534457519899";
        public static final String DEVICE_NAME = "iPhone 8";
        public static final String PUSH_TOKEN =
                "D3812F47DB01F91A8AB0A3C4F89CC534E83239312D3D6D28AE4D04DE3CD64BF6";
        public static final String LANGUAGE = "eng";
        public static final Integer SCREEN_HEIGHT = 667;
        public static final Integer SCREEN_WIDTH = 375;
    }

    public static final class AuthenticationKeys {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String BEARER_TOKEN = "bearer";
    }

    public static final class URLs {
        public static final String PARAM_ID = "ID";
        public static final String BASE_URL = "https://api.cajamar.es/amea-web/abh";
        public static final String VERSION = "/v19.39.0";
        public static final String ACCOUNT_ENDPOINT = "/account";
        public static final String CARD_ENDPOINT = "/card";
        public static final String TRANSACTIONS_ENDPOINT = "/transactions";

        public static final String REFRESH_TOKEN = BASE_URL + "/refreshToken";
        public static final String ENROLLMENT = BASE_URL + "/enrollment";
        public static final String LOGIN = BASE_URL + "/login";
        public static final String LOGOUT = BASE_URL + "/logout";
        public static final String POSITIONS = BASE_URL + VERSION + "/position";
        public static final String UPDATE_PUSH_TOKEN = BASE_URL + VERSION + "/updatePushToken";
        public static final String ACCOUNT =
                BASE_URL + VERSION + ACCOUNT_ENDPOINT + "/{" + PARAM_ID + "}";
        public static final String ACCOUNT_TRANSACTIONS =
                BASE_URL
                        + VERSION
                        + ACCOUNT_ENDPOINT
                        + "/{"
                        + PARAM_ID
                        + "}"
                        + TRANSACTIONS_ENDPOINT;
        public static final String CREDIT_CARD =
                BASE_URL + VERSION + CARD_ENDPOINT + "/{" + PARAM_ID + "}";
        public static final String CARD_TRANSACTIONS =
                BASE_URL + VERSION + CARD_ENDPOINT + "/{" + PARAM_ID + "}" + TRANSACTIONS_ENDPOINT;
        public static final String IDENTITY_DATA =
                BASE_URL + VERSION + ACCOUNT_ENDPOINT + "/{" + PARAM_ID + "}/certificate";
    }

    public static final class HeaderValues {
        public static final String USER_AGENT_VALUE =
                "webankProduction/18 CFNetwork/1120 Darwin/19.0.0";
    }

    public static final class HeaderKeys {
        public static final String USER_AGENT = "User-Agent";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static final class Fetchers {
        public static final int MAX_TRY_ATTEMPTS = 3;
        public static final long BACKOFF = 1000;
    }

    public static final class ErrorMessages {
        public static final String MAX_TRY_ATTEMPTS =
                String.format("Reached max retry attempts of %d", Fetchers.MAX_TRY_ATTEMPTS);
    }

    public static final class LogTags {
        public static final LogTag TRANSACTIONS_RETRYING =
                LogTag.from("Cajamar retrying transactions");
    }

    public static final class HolderNames {
        public static final String OWNER = "FIRST HOLDER";
        public static final String AUTHORIZED = "AUTHORISED";
    }

    public static class QueryParams {
        public static final String PAGE_NUMBER = "pageNumber";
        public static final String CERTIFICATE_TYPE = "type";
    }

    public static class QueryValues {
        public static final String FIRST_PAGE = "1";
    }

    public static final class SplitValues {
        public static final String NIF = "con N.I.F. ";
        public static final String PASSPORT = "con PASAPORTE ";
        public static final String END_OF_DOCUMENT_ID = ", ";
        public static final String ADDITIONAL_PARSER = "Que según consta en nuestros archivos,";
        public static final String ADDITIONAL_END_PARSER = "desde el ";
    }

    public static class CardTypes {
        public static final String CREDIT = "CR";
    }

    public static class SessionKeys {
        public static final String PUSH_TOKEN = "PUSH_TOKEN";
        public static final String ACCOUNT_HOLDER_NAME = "HOLDER";
    }

    public static class Proxy {
        public static final String COUNTRY = "es";
        public static final String ES_PROXY = "esProxy";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
