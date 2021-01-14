package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SantanderEsConstants {

    public static final class Urls {
        public static final String HOST = "https://www.bsan.mobi";

        public static final URL AUTHENTICATION_ENDPOINT =
                new URL(HOST + "/SANMOV_IPAD_NSeg_ENS/ws/SANMOV_Def_Listener");
        public static final URL WEB_SERVICE_ENDPOINT =
                new URL(HOST + "/SCH_BAMOBI_WS_ENS/ws/BAMOBI_WS_Def_Listener");
        public static final URL FUNDS_ENDPOINT =
                new URL(HOST + "/SCH_BAMOBI_FONDOS_ENS/ws/BAMOBI_WS_Def_Listener");
        public static final URL STOCKS_ENDPOINT =
                new URL(HOST + "/SCH_BAMOBI_VALORES_ENS/ws/BAMOBI_WS_Def_Listener");
        public static final URL LOANS_ENDPOINT =
                new URL(HOST + "/SCH_BAMOBI_PRESTAMOS_ENS/ws/BAMOBI_WS_Def_Listener");
    }

    public static final class Headers {
        public static final String SOAP_ACTION = "SOAPAction";
        public static final String TEXT_XML_UTF8 = "text/xml; charset=utf-8";
    }

    public static final class NodeTags {
        public static final String METHOD_RESULT = "methodResult";
        public static final String TOKEN_CREDENTIAL = "tokenCredential";
        public static final String CODIGO_ERROR = "codigoError";
        public static final String FAULT_ERROR = "error";
    }

    public static final class ErrorCodes {
        public static final List<String> INCORRECT_CREDENTIALS =
                Arrays.asList("SBAMOV_00002", "SBAMOV_00003");
        public static final List<String> BLOCKED_CREDENTIALS = Arrays.asList("SBAMOV_00004");
    }

    public static final class Storage {
        public static final String LOGIN_RESPONSE = "loginResponse";
        public static final String USER_DATA_XML = "userDataXml";
        public static final String CONTRACT_ID_XML = "contractIdXml";
        public static final String BALANCE_XML = "balanceXml";
        public static final String CARD_ENTITY = "cardEntity";
        public static final String ID_NUMBER = "idNumber";
        public static final String ACCESS_TOKEN = "accessToken";
    }

    public static final class DataHeader {
        public static final String VERSION = "5.1.2";
        public static final String TERMINAL_ID = "iPhone";
        public static final String IDIOMA = "es-ES";
    }

    public static final class AccountTypes {
        public static final String PROD_NR_300 = "300";
        public static final String PROD_NR_301 = "301";
        public static final String DEBIT_CARD_TYPE = "débito";
    }

    public static final class PersonType {
        public static final String HOLDER = "F";
        public static final String INSURANCE_HOLDER = "J";
    }

    public static final ImmutableMap<String, Type> LOAN_TYPES =
            ImmutableMap.<String, LoanDetails.Type>builder()
                    .put("143", Type.MORTGAGE)
                    .put("103", Type.OTHER)
                    .build();

    public static final class Tags {
        public static final LogTag LOAN_ACCOUNT = LogTag.from("es_santander_loan");
        public static final LogTag INVESTMENT_ACCOUNT = LogTag.from("es_santander_investment");
    }

    public static final class LogMessages {
        public static final String LOGIN_RESPONSE_NOT_FOUND =
                "Login response not found in session storage";
    }

    public static final class Indicators {
        public static final String YES = "S";
        public static final String NO = "N";
    }

    public static final class SoapErrorMessages {
        public static final String NO_MORE_TRANSACTIONS =
                "no existen movimientos asociados a esta consulta";
        public static final String NOT_CUSTOMER =
                "NO EXISTEN CONTRATOS VISIBLES ASOCIADOS A LA PERSONA INDICADA";
    }

    public static final String DEFAULT_CURRENCY = "EUR";
    public static final String DEFAULT_LOAN_AMOUNT = "0";
}
