package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CreditCardRepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.LocalDateToXml;

public class SantanderEsCreditCardTransactionsRequest {

    public static String create(
            String tokenCredential,
            String userDataXmlString,
            CardEntity card,
            LocalDate fromDate,
            LocalDate toDate,
            CreditCardRepositionEntity repositionEntity) {

        ContractEntity contractEntity = card.getGeneralInfo().getContractId();
        String creditCardNumber = card.getCardNumber();
        String contractBankOffice =
                SantanderEsXmlUtils.parseJsonToXmlString(contractEntity.getBankOffice());

        return String.format(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://www.isban.es/webservices/BAMOBI/Tarjetas/F_bamobi_tarjetas_lip/internet/BAMOBITAJ/v1\">"
                        + "<soapenv:Header>"
                        + "<wsse:Security SOAP-ENV:actor=\"http://www.isban.es/soap/actor/wssecurityB64\" SOAP-ENV:mustUnderstand=\"1\" S12:role=\"wsssecurity\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                        + "<wsse:BinarySecurityToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SSOToken\" ValueType=\"esquema\" EncodingType=\"hwsse:Base64Binary\">%s</wsse:BinarySecurityToken>"
                        + "</wsse:Security>"
                        + "</soapenv:Header>"
                        + "<soapenv:Body>"
                        + "<v1:listaMovTarjetasFechas_LIP facade=\"BAMOBITAJ\">"
                        + "<entrada>"
                        + "<datosConexion>%s</datosConexion>"
                        + "<datosCabecera>"
                        + "<version>%s</version>"
                        + "<terminalID>%s</terminalID>"
                        + "<idioma>%s</idioma>"
                        + "</datosCabecera>"
                        + "<contratoTarjeta>"
                        + "<PRODUCTO>%s</PRODUCTO>"
                        + "<NUMERO_DE_CONTRATO>%s</NUMERO_DE_CONTRATO>"
                        + "<CENTRO>%s</CENTRO>"
                        + "</contratoTarjeta>"
                        + "<numeroTarj>%s</numeroTarj>"
                        + "%s"
                        + "</entrada>"
                        + "</v1:listaMovTarjetasFechas_LIP>"
                        + "</soapenv:Body>"
                        + "</soapenv:Envelope>",
                tokenCredential,
                userDataXmlString,
                SantanderEsConstants.DataHeader.VERSION,
                SantanderEsConstants.DataHeader.TERMINAL_ID,
                SantanderEsConstants.DataHeader.IDIOMA,
                contractEntity.getProduct(),
                contractEntity.getContractNumber(),
                contractBankOffice,
                creditCardNumber,
                formatPaginationData(fromDate, toDate, repositionEntity));
    }

    private static String formatPaginationData(
            LocalDate fromDate, LocalDate toDate, CreditCardRepositionEntity repositionEntity) {
        boolean isPaginationRequest = repositionEntity != null;
        String repositionData = "";
        if (isPaginationRequest) {
            repositionData =
                    String.format(
                            "<repos>%s</repos>",
                            SantanderEsXmlUtils.parseJsonToXmlString(repositionEntity));
        }

        return String.format(
                "<fechaDesde>%s</fechaDesde>"
                        + "<fechaHasta>%s</fechaHasta>"
                        + "<esUnaPaginacion>%s</esUnaPaginacion>"
                        + "%s",
                LocalDateToXml.seralizeLocalDateToXml(fromDate),
                LocalDateToXml.seralizeLocalDateToXml(toDate),
                (isPaginationRequest ? "S" : "N"),
                repositionData);
    }
}
