package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import java.util.UUID;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NovoBancoConstants {

    public static class URLs {
        public static final URL LOGIN = new URL(Endpoints.LOGIN0);
        public static final URL KEEP_ALIVE = new URL(Endpoints.GET_ACCOUNTS);
        public static final URL GET_ACCOUNTS = new URL(Endpoints.GET_ACCOUNTS);
        public static final URL GET_LOANS = new URL(Endpoints.GET_LOANS);
        public static final URL GET_LOAN_DETAILS = new URL(Endpoints.GET_LOAN_DETAILS);
        public static final URL GET_INVESTMENTS = new URL(Endpoints.GET_INVESTMENTS);
        public static final URL GET_AGGREGATED_SUMMARY = new URL(Endpoints.GET_AGGREGATED_SUMMARY);
        public static final URL GET_CARD_DETAILS = new URL(Endpoints.GET_CARD_DETAILS);
    }

    private static class Endpoints {
        public static final String API_BASE = "https://sec.novobanco.pt/";
        public static final String LOGIN0 = API_BASE + "mv2/api2/Security/Login0";
        public static final String GET_ACCOUNTS = API_BASE + "mv2/api2/Movimentos/ObterLista";
        public static final String GET_LOANS = API_BASE + "mv2/api2/PosicaoIntegrada/ObterSeccao";
        public static final String GET_LOAN_DETAILS =
                API_BASE + "mv2/api2/PosicaoIntegrada/ObterDetalheCreditoHabitacao";
        public static final String GET_INVESTMENTS =
                API_BASE + "mv2/api2/FundosInvestimento/ObterCarteiraFundos";
        public static final String GET_AGGREGATED_SUMMARY =
                API_BASE + "mv2/api2/PosicaoIntegrada/Obter";
        public static final String GET_CARD_DETAILS =
                API_BASE + "mv2/api2/ConsultaMovimentosCartoes/ObterLista";
    }

    public static class SecretKeys {
        public static final String API_KEY = "0102206970E0B44A5A960BD304A95E7FD3";
        // generated  key, encoded to B64 and converted to string (US-ASCII charset + trim)
        public static final String INSTANCE_KEY = "zT/upIt3YlRJSteHtdF3FaeMOzKea0+y5j0qiD0VMS0=";
    }

    public static class SessionKeys {
        public static final String AUTH_COOKIE_KEY = "AUTH_COOKIE";
        public static final String SESSION_COOKIE_KEY = "SESSION_COOKIE";
        public static final String DEVICE_ID_KEY = "DEVICE_ID";
        public static final String ACCOUNT_GENERAL_INFO_ID = "ACCOUNT_GENERAL_INFO";
    }

    public static class ServiceIds {
        public static final int MOVEMENTS_ID = 3396;
        public static final int LOANS_ID = 194;
        public static final int INVESTMENTS_ID = 158;
        public static final int SUMMARY_ID = 194;
        public static final int CARDS_ID = 2048;
    }

    public static class Header {
        public static final String CONNECTION_KEY = "Connection";
        public static final String CONNECTION_VALUE = "keep-alive";

        public static final String ENCODING_VALUE = "br, gzip, deflate";

        public static final String USER_AGENT_KEY = "User-Agent";
        public static final String USER_AGENT_VALUE =
                "NovoMobile/3.4.5 (iPhone; iOS 12.4; Scale/2.00)";

        public static final String NB_SIGNATURE_KEY = "X-NB-Signature";
    }

    public static class FieldValues {
        public static final String OS = "iOS";
        public static final String MODEL = "iPhone 8";
        public static final String OS_VERSION = "12.4";
        public static final double LATITUDE = 0.0;
        public static final double LONGITUDE = 0.0;
        public static final String DEVICE_NAME = "Tink";
        public static final String DEFAULT_DEVICE_ID = UUID.randomUUID().toString().toUpperCase();
        public static final String LANGUAGE = "PT";
        public static final int LOGIN_MODE = 1;
        public static final String CTX_ACCOUNTS = "contas";
        public static final int LOANS_SECTION_TYPE = 15;

        public static final String APP_VERSION = "3.4.5";
        public static final String APP_BUILD = "5466";
    }

    public static class DateFormats {
        public static final String DD_MM_YYYY = "dd-MM-yyyy";
    }

    public static class ResponseLabels {
        public static final String CONTRACT = "Contrato";
        public static final String INTEREST_RATE = "TAN";
        public static final String CURRENT_BALANCE = "Capital em divida";
        public static final String CREDIT_CARDS = "Cartões de Crédito";
        public static final Integer SECTION_TYPE = 30;
        public static final Integer COLLAPSIBLE_SECTION_TYPE = 32;

        public static final String INITIAL_DATE_PT = "Data Início do contrato";
        public static final String INITIAL_DATE_EN = "Initial Date";

        public static final String INITIAL_BALANCE_PT = "Capital utilizado";
        public static final String INITIAL_BALANCE_EN = "Initial Balance";

        public static final String PRODUCT_NAME_PT = "Designação";
        public static final String PRODUCT_NAME_EN = "Product Name";

        public static final String CURRENCY_PT = "Moeda";
        public static final String CURRENCY_EN = "Currency";
    }

    public static class ResponseCodes {
        public static final int SESSION_EXPIRED = 40;
        public static final int OK = 0;
    }

    public static class SecurityConfig {
        public static final String RSA = "RSA";
        public static final String RSA_TRANSFORMATION =
                String.format("%s/%s/%s", RSA, "NONE", "PKCS1Padding");
        public static final String HMAC_SHA256 = "HmacSHA256";
        public static final String SECRET_FORMAT = "%s|%s|%d|%s";
    }
}
