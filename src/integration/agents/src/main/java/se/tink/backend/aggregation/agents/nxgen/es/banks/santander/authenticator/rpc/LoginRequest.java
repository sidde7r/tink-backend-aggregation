package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.rpc;

import java.util.function.UnaryOperator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants.Urls;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public enum LoginRequest {
    NEW_LOGIN(
            Urls.NEW_WEB_SERVICE_ENDPOINT,
            token ->
                    String.format(
                            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://www.isban.es/webservices/BAMOBI/Posglobal/F_bamobi_posicionglobal_lip/internet/BAMOBIPGL/v1\">"
                                    + "<soapenv:Header>"
                                    + "<wsse:Security SOAP-ENV:actor=\"http://www.isban.es/soap/actor/wssecurityB64\" SOAP-ENV:mustUnderstand=\"1\" S12:role=\"wsssecurity\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                                    + "<wsse:BinarySecurityToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SSOToken\" ValueType=\"esquema\" EncodingType=\"hwsse:Base64Binary\">%s</wsse:BinarySecurityToken>"
                                    + "</wsse:Security>"
                                    + "</soapenv:Header>"
                                    + "<soapenv:Body>"
                                    + "<v1:obtenerPosGlobalConCestasInvers_LIP facade=\"BAMOBIPGL\">"
                                    + "<entrada>"
                                    + "<datosCabecera>"
                                    + "<version>%s</version>"
                                    + "<terminalID>%s</terminalID>"
                                    + "<idioma>%s</idioma>"
                                    + "</datosCabecera>"
                                    + "</entrada>"
                                    + "</v1:obtenerPosGlobalConCestasInvers_LIP>"
                                    + "</soapenv:Body>"
                                    + "</soapenv:Envelope>",
                            token,
                            SantanderEsConstants.DataHeader.VERSION,
                            SantanderEsConstants.DataHeader.TERMINAL_ID,
                            SantanderEsConstants.DataHeader.IDIOMA)),
    LEGACY_LOGIN(
            Urls.WEB_SERVICE_ENDPOINT,
            token ->
                    String.format(
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
                            token,
                            SantanderEsConstants.DataHeader.VERSION,
                            SantanderEsConstants.DataHeader.TERMINAL_ID,
                            SantanderEsConstants.DataHeader.IDIOMA));

    private final URL url;

    private final UnaryOperator<String> request;

    LoginRequest(URL url, UnaryOperator<String> request) {
        this.url = url;
        this.request = request;
    }

    public URL getUrl() {
        return url;
    }

    public String getBody(String token) {
        return request.apply(token);
    }
}
