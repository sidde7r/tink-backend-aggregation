package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.utils.soap.SoapParser;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsXmlUtils {
    private final static XmlMapper MAPPER = new XmlMapper();

    public static String createAuthenticateCredentialMessage(String username, String password) {
        return String.format(
                "<v:Envelope xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"
                    + "<v:Header />"
                    + "<v:Body>"
                        + "<n0:authenticateCredential xmlns:n0=\"http://www.isban.es/webservices/TECHNICAL_FACADES/Security/F_facseg_security/internet/loginServicesNSegSAN/v1\" facade=\"loginServicesNSegSAN\">"
                            + "<CB_AuthenticationData i:type=\":CB_AuthenticationData\">"
                                + "<documento i:type=\":documento\">"
                                    + "<CODIGO_DOCUM_PERSONA_CORP i:type=\"d:string\">%s</CODIGO_DOCUM_PERSONA_CORP>"
                                    + "<TIPO_DOCUM_PERSONA_CORP i:type=\"d:string\">C</TIPO_DOCUM_PERSONA_CORP>"
                                + "</documento>"
                                + "<password i:type=\"d:string\">%s</password>"
                            + "</CB_AuthenticationData>"
                            + "<userAddress i:type=\"d:string\">127.0.0.1</userAddress>"
                        + "</n0:authenticateCredential>"
                    + "</v:Body>"
                + "</v:Envelope>", username, password);
    }

    public static String createLoginMessage(String tokenCredential) {
        return String.format(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://www.isban.es/webservices/BAMOBI/Posglobal/F_bamobi_posicionglobal_lip/internet/BAMOBIPGL/v1\">"
                    + "<soapenv:Header>"
                        + "<wsse:Security SOAP-ENV:actor=\"http://www.isban.es/soap/actor/wssecurityB64\" SOAP-ENV:mustUnderstand=\"1\" S12:role=\"wsssecurity\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                            + "<wsse:BinarySecurityToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SSOToken\" ValueType=\"esquema\" EncodingType=\"hwsse:Base64Binary\">%s</wsse:BinarySecurityToken>"
                        + "</wsse:Security>"
                    + "</soapenv:Header>"
                    + "<soapenv:Body>"
                        + "<v1:obtenerPosGlobal_LIP facade=\"BAMOBIPGL\">"
                            + "<entrada>"
                                + "<datosCabecera>"
                                    + "<version>%s</version>"
                                    + "<terminalID>%s</terminalID>"
                                    + "<idioma>%s</idioma>"
                                + "</datosCabecera>"
                            + "</entrada>"
                        + "</v1:obtenerPosGlobal_LIP>"
                    + "</soapenv:Body>"
                + "</soapenv:Envelope>",
                tokenCredential,
                SantanderEsConstants.DataHeader.VERSION,
                SantanderEsConstants.DataHeader.TERMINAL_ID,
                SantanderEsConstants.DataHeader.IDIOMA);
    }

    public static String createFirstPageTransactionsMessage(String tokenCredential, String userDataXmlString,
            String contractIdXmlString, String balancaXmlString, boolean isPagination) {
        return String.format(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://www.isban.es/webservices/BAMOBI/Cuentas/F_bamobi_cuentas_lip/internet/BAMOBICTA/v1\">"
                    + " <soapenv:Header>"
                        + "<wsse:Security SOAP-ENV:actor=\"http://www.isban.es/soap/actor/wssecurityB64\" SOAP-ENV:mustUnderstand=\"1\" S12:role=\"wsssecurity\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                            + "<wsse:BinarySecurityToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SSOToken\" ValueType=\"esquema\" EncodingType=\"hwsse:Base64Binary\">%s</wsse:BinarySecurityToken>"
                        + "</wsse:Security>"
                    + "</soapenv:Header>"
                    + "<soapenv:Body>"
                        + "<v1:listaMovCuentas_LIP facade=\"BAMOBICTA\">"
                            + "<entrada>"
                                + "<datosCabecera>"
                                    + "<version>%s</version>"
                                    + "<terminalID>%s</terminalID>"
                                    + "<idioma>%s</idioma>"
                                + "</datosCabecera>"
                                + "%s"
                                + "<esUnaPaginacion>%s</esUnaPaginacion>"
                            + "</entrada>"
                        + "</v1:listaMovCuentas_LIP>"
                    + "</soapenv:Body>"
                + "</soapenv:Envelope>",
                tokenCredential,
                SantanderEsConstants.DataHeader.VERSION,
                SantanderEsConstants.DataHeader.TERMINAL_ID,
                SantanderEsConstants.DataHeader.IDIOMA,
                formatRequestForTransactionFetching(userDataXmlString, contractIdXmlString, balancaXmlString),
                getIsPaginationString(isPagination));
    }

    public static String createTransactionPaginationRequest(String tokenCredential, String userDataXmlString,
            String contractIdXmlString, String balancaXmlString, boolean isPagination, String repositionXmlString) {
        return String.format(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://www.isban.es/webservices/BAMOBI/Cuentas/F_bamobi_cuentas_lip/internet/BAMOBICTA/v1\">"
                    + " <soapenv:Header>"
                        + "<wsse:Security SOAP-ENV:actor=\"http://www.isban.es/soap/actor/wssecurityB64\" SOAP-ENV:mustUnderstand=\"1\" S12:role=\"wsssecurity\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                            + "<wsse:BinarySecurityToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SSOToken\" ValueType=\"esquema\" EncodingType=\"hwsse:Base64Binary\">%s</wsse:BinarySecurityToken>"
                        + "</wsse:Security>"
                    + "</soapenv:Header>"
                    + "<soapenv:Body>"
                        + "<v1:listaMovCuentas_LIP facade=\"BAMOBICTA\">"
                            + "<entrada>"
                                + "<datosCabecera>"
                                    + "<version>%s</version>"
                                    + "<terminalID>%s</terminalID>"
                                    + "<idioma>%s</idioma>"
                                + "</datosCabecera>"
                                + "%s"
                                + "<esUnaPaginacion>%s</esUnaPaginacion>"
                                + "<repo>%s</repo>"
                            + "</entrada>"
                        + "</v1:listaMovCuentas_LIP>"
                    + "</soapenv:Body>"
                + "</soapenv:Envelope>",
                tokenCredential,
                SantanderEsConstants.DataHeader.VERSION,
                SantanderEsConstants.DataHeader.TERMINAL_ID,
                SantanderEsConstants.DataHeader.IDIOMA,
                formatRequestForTransactionFetching(userDataXmlString, contractIdXmlString, balancaXmlString),
                getIsPaginationString(isPagination), repositionXmlString);
    }

    private static String getIsPaginationString(boolean isPagination) {
        return isPagination ? "S" : "N";
    }

    private static String formatRequestForTransactionFetching(String userDataXmlString, String contractIdXmlString,
            String balancaXmlString) {

        return String.format("<datosConexion>%s</datosConexion>"
                + "<contratoID>%s</contratoID>"
                + "<importeCta>%s</importeCta>",
                userDataXmlString, contractIdXmlString, balancaXmlString);
    }

    public static Node getTagNodeFromSoapString(String responseString, String tagName) {
        Node node = SoapParser.getSoapBody(responseString);
        Preconditions.checkState(node instanceof Element,
                "Could not parse SOAP body from server response.");

        Element element = (Element) node;
        return element.getElementsByTagName(tagName).item(0);
    }

    public static String parseJsonToXmlString(Object jsonObject) {
        try {
            JSONObject json = new JSONObject(SerializationUtils.serializeToString(jsonObject));
            return XML.toString(json);
        } catch (JSONException e) {
            throw new IllegalStateException("Could not parse JSON object to XML string.");
        }

    }

    public static <T> T parseXmlStringToJson(String xmlString, Class<T> returnType) {
        try {
            return MAPPER.readValue(xmlString, returnType);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse XML string into JSON object.");
        }

    }
}
