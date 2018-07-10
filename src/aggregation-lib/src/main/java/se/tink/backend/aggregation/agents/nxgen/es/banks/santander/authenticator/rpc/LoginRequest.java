package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;

public class LoginRequest {

    public static String create(String tokenCredential) {
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
}
