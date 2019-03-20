package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioRepositionEntity;

public class PortfolioDetailsRequest {

    public static String create(
            String tokenCredential,
            String userDataXml,
            PortfolioEntity depositEntity,
            boolean firstFetch,
            PortfolioRepositionEntity reposition) {

        String pagination = SantanderEsConstants.Indicators.NO;
        String paginationData = "";
        if (!firstFetch) {
            pagination = SantanderEsConstants.Indicators.YES;
            paginationData = createPagination(reposition);
        }
        ContractEntity contractEntity = depositEntity.getGeneralInfo().getContractId();

        return String.format(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://www.isban.es/webservices/BAMOBI/Valores/F_bamobi_valores_lip/internet/ACBAMOBIVAL/v1\">"
                        + "<soapenv:Header>"
                        + "<wsse:Security SOAP-ENV:actor=\"http://www.isban.es/soap/actor/wssecurityB64\" SOAP-ENV:mustUnderstand=\"1\" S12:role=\"wsssecurity\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                        + "<wsse:BinarySecurityToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SSOToken\" ValueType=\"esquema\" EncodingType=\"hwsse:Base64Binary\">%s</wsse:BinarySecurityToken>"
                        + "</wsse:Security>"
                        + "</soapenv:Header>"
                        + "<soapenv:Body>"
                        + "<v1:listaCarteraValoresSaldoPos_LIP facade=\"ACBAMOBIVAL\">"
                        + "<entrada>"
                        + "<datosCabecera>"
                        + "<version>%s</version>"
                        + "<terminalID>%s</terminalID>"
                        + "<idioma>%s</idioma>"
                        + "</datosCabecera>"
                        + "<datosConexion>%s</datosConexion>"
                        + "<contratoID>"
                        + "<CENTRO>"
                        + "<EMPRESA>%s</EMPRESA>"
                        + "<CENTRO>%s</CENTRO>"
                        + "</CENTRO>"
                        + "<PRODUCTO>%s</PRODUCTO>"
                        + "<NUMERO_DE_CONTRATO>%s</NUMERO_DE_CONTRATO>"
                        + "</contratoID>"
                        + "</entrada>"
                        + "<indicadores>"
                        + "<esUnaPaginacion>%s</esUnaPaginacion>"
                        + "</indicadores>"
                        + "%s"
                        + "</v1:listaCarteraValoresSaldoPos_LIP>"
                        + "</soapenv:Body>"
                        + "</soapenv:Envelope>",
                tokenCredential,
                SantanderEsConstants.DataHeader.VERSION,
                SantanderEsConstants.DataHeader.TERMINAL_ID,
                SantanderEsConstants.DataHeader.IDIOMA,
                userDataXml,
                contractEntity.getBankOffice().getCompany(),
                contractEntity.getBankOffice().getOffice(),
                contractEntity.getProduct(),
                contractEntity.getContractNumber(),
                pagination,
                paginationData);
    }

    private static String createPagination(PortfolioRepositionEntity reposition) {
        return String.format(
                "<repos>"
                        + "<codigoEmisionValores>"
                        + "<CODIGO_DE_VALOR>%s</CODIGO_DE_VALOR>"
                        + "<CODIGO_DE_EMISION>%s</CODIGO_DE_EMISION>"
                        + "</codigoEmisionValores>"
                        + "</repos>",
                reposition.getEmissionCode().getStockCode(),
                reposition.getEmissionCode().getEmissionCode());
    }
}
