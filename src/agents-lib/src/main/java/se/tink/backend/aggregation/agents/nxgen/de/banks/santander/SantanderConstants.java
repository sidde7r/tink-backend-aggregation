package se.tink.backend.aggregation.agents.nxgen.de.banks.santander;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class SantanderConstants {

  public static final String WHITESPACE = " ";
  public static final String INDMPX_L = "L";

  public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
      AccountTypeMapper.builder()
          .put(AccountTypes.CHECKING, "GIROKONTO")
          .put(AccountTypes.CREDIT_CARD, "1PLUS CARD")
          .build();

  public static class URL {
    public static final String BASEURL = "https://w4m.santander.de";
    public static final String LOGIN = "/LGMBSE_SCB_ENS/ws/LGMBSE_Def_Listener";
    public static final String ACCOUNT = "/MBANDE_ACC_SCB_ENS/json/F_MBANDE_GlobalPosition/1.0";
    public static final String TRANSACTIONS =
        "/MBANDE_ACC_SCB_ENS/json/F_MBANDE_BookedTransactions/1.0";
    public static final String CARDINFO =
        "/CARDSB_SERVICEKARTEN_ENS_SCB/json/F_CARDSB_ServiceKarten_LA/1.0";
    public static final String CREDIT_TRANSACTIONS =
        "/CARDSB_SERVICEKARTEN_ENS_SCB/json/F_CARDSB_ServiceKarten_LA/1.0/CARDSB_SERVICEKARTEN_ENS_SCB/json/F_CARDSB_ServiceKarten_LA/1.0";
  }

  public static class SOAP {
    public static final String LOGIN_REQUEST =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://www.isban.es/webservices/LOGONMOBILESEB/Service_la/F_lgmbse_service_la/internet/validateUserLA/v1\""
            + "><soapenv:Header/><soapenv:Body><v1:validateUserLA facade=\"validateUserLA\"><cbValidateUserLAE><userId>%s</userId><pin><![CDATA[%s]]>"
            + "</pin><language><IDIOMA_ISO>de</IDIOMA_ISO><DIALECTO_ISO>DE</DIALECTO_ISO></language><entity></entity><channel>INT</channel></cbValidateUserLAE></v1:validateUserLA></soapenv:Body></soapenv:Envelope>";
  }

  public static class ERROR {
    public static final String WRONG_PASSWORD_CODE = "LGMBSE_1000";
  }

  public static class REGEX {
    private static final String TOKEN_REGEX = "<token>(.*)<\\/token>";
    public static final int FULL_MATCH_ONLY = 1;
    public static final Pattern pattern = Pattern.compile(TOKEN_REGEX);
  }

  public static class STORAGE {
    public static final String TOKEN = "TOKEN";
    public static final String LOCAL_CONTRACT_TYPE = "LOCAL_CONTRACT_TYPE";
    public static final String LOCAL_CONTRACT_DETAIL = "LOCAL_CONTRACT_DETAIL";
    public static final String COMPANY_ID = "COMPANY_ID";
  }

  public static class QUERYPARAMS {
    public static final String AUTHENTICATION_TYPE = "authenticationType";
    public static final String AUTHENTICATION_TYPE_TOKEN = "token";
    public static final String TOKEN = "token";
    public static final String REQUEST_DATA = "requestData";
    public static final String COMPANY_ID = "3293";
    public static final String CHANNEL = "INT";
    public static final String LANGUAGE_DE = "de";
    public static final String DIALECT_DE = "DE";
  }

  public static class LOGTAG {
    public static final LogTag SANTANDER_LOGIN_ERROR = LogTag.from("SANTANDER_LOGIN_ERROR");
    public static final LogTag SANTANDER_REGEX_PARSE_ERROR =
        LogTag.from("SANTANDER_REGEX_PARSE_ERROR");
    public static final LogTag SANTANDER_UNKNOWN_ACCOUNTTYPE =
        LogTag.from("SANTANDER_UNKNOWN_ACCOUNTTYPE");
    public static final LogTag SANTANDER_ACCOUNT_LOGGING = LogTag.from("SANTANDER_ACCOUNT_LOGGING");
    public static final LogTag SANTANDER_ACCOUNT_PARSING_ERROR =
        LogTag.from("SANTANDER_ACCOUNT_PARSING_ERROR");
    public static final LogTag SANTANDER_TRANSACTION_LOGGING =
        LogTag.from("SANTANDER_TRANSACTION_LOGGING");
  }

  public static class DATE {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
  }
}
