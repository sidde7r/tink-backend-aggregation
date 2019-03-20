package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.FundEntity;

public class FundDetailsRequest {

    public static String create(String tokenCredential, String userDataXml, FundEntity fundEntity) {

        ContractEntity contractEntity = fundEntity.getGeneralInfo().getContractId();

        return String.format(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://www.isban.es/webservices/BAMOBI/Fondos/F_bamobi_fondos_lip/internet/ACBAMOBIFON/v1\">"
                        + "<soapenv:Header>"
                        + "<wsse:Security SOAP-ENV:actor=\"http://www.isban.es/soap/actor/wssecurityB64\" SOAP-ENV:mustUnderstand=\"1\" S12:role=\"wsssecurity\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                        + "<wsse:BinarySecurityToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SSOToken\" ValueType=\"esquema\" EncodingType=\"hwsse:Base64Binary\">%s</wsse:BinarySecurityToken>"
                        + "</wsse:Security>"
                        + "</soapenv:Header>"
                        + "<soapenv:Body>"
                        + "<v1:detalleFondo_LA facade=\"ACBAMOBIFON\">"
                        + "<entrada>"
                        + "<contrato>"
                        + "<CENTRO><CENTRO>%s</CENTRO><EMPRESA>%s</EMPRESA></CENTRO>"
                        + "<PRODUCTO>%s</PRODUCTO>"
                        + "<NUMERO_DE_CONTRATO>%s</NUMERO_DE_CONTRATO>"
                        + "</contrato>"
                        + "</entrada>"
                        + "<datosConexion>%s</datosConexion>"
                        + "<datosCabecera>"
                        + "<version>%s</version>"
                        + "<terminalID>%s</terminalID>"
                        + "<idioma>%s</idioma>"
                        + "</datosCabecera>"
                        + "</v1:detalleFondo_LA>"
                        + "</soapenv:Body>"
                        + "</soapenv:Envelope>",
                tokenCredential,
                contractEntity.getBankOffice().getOffice(),
                contractEntity.getBankOffice().getCompany(),
                contractEntity.getProduct(),
                contractEntity.getContractNumber(),
                userDataXml,
                SantanderEsConstants.DataHeader.VERSION,
                SantanderEsConstants.DataHeader.TERMINAL_ID,
                SantanderEsConstants.DataHeader.IDIOMA);
    }
}
