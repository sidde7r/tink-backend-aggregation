package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.rpc;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.GeneralInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.DateEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.LocalDateToXml;

public class LoanMovementsRequest {

    public static String create(String tokenCredential, String userDataXml, LoanEntity loanEntity) {

        GeneralInfoEntity generalInfo = loanEntity.getGeneralInfo();
        ContractEntity contractEntity = generalInfo.getContractId();
        AmountEntity balance = loanEntity.getBalance();
        DateEntity now = LocalDateToXml.serializeLocalDateToXml(LocalDate.now());
        DateEntity yearAgo = LocalDateToXml.serializeLocalDateToXml(LocalDate.now().minusYears(1));

        return String.format(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                        + "xmlns:v1=\"http://www.isban.es/webservices/BAMOBI/Prestamos/F_bamobi_prestamos_lip/internet/ACBAMOBIPRE/v1\">"
                        + "<soapenv:Header>"
                        + "<wsse:Security SOAP-ENV:actor=\"http://www.isban.es/soap/actor/wssecurityB64\" SOAP-ENV:mustUnderstand=\"1\" S12:role=\"wsssecurity\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                        + "<wsse:BinarySecurityToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SSOToken\" ValueType=\"esquema\" EncodingType=\"hwsse:Base64Binary\">%s</wsse:BinarySecurityToken>"
                        + "</wsse:Security>"
                        + "</soapenv:Header>"
                        + "<soapenv:Body>"
                        + "<v1:listaMovimientosPrestamo_LA facade=\"ACBAMOBIPRE\">"
                        + "<entrada>"
                        + "<contrato>"
                        + "<CENTRO><EMPRESA>%s</EMPRESA><CENTRO>%s</CENTRO></CENTRO>"
                        + "<PRODUCTO>%s</PRODUCTO>"
                        + "<NUMERO_DE_CONTRATO>%s</NUMERO_DE_CONTRATO>"
                        + "</contrato>"
                        + "<fechaDesde>%s</fechaDesde>"
                        + "<fechaHasta>%s</fechaHasta>"
                        + "<divisa>%s</divisa>"
                        + "</entrada>"
                        + "<datosConexion>%s</datosConexion>"
                        + "<datosCabecera>"
                        + "<version>%s</version>"
                        + "<terminalID>%s</terminalID>"
                        + "<idioma>%s</idioma>"
                        + "</datosCabecera>"
                        + "</v1:listaMovimientosPrestamo_LA>"
                        + "</soapenv:Body>"
                        + "</soapenv:Envelope>",
                tokenCredential,
                contractEntity.getBankOffice().getCompany(),
                contractEntity.getBankOffice().getOffice(),
                contractEntity.getProduct(),
                contractEntity.getContractNumber(),
                SantanderEsXmlUtils.parseJsonToXmlString(yearAgo),
                SantanderEsXmlUtils.parseJsonToXmlString(now),
                balance.getCurrency(),
                userDataXml,
                SantanderEsConstants.DataHeader.VERSION,
                SantanderEsConstants.DataHeader.TERMINAL_ID,
                SantanderEsConstants.DataHeader.IDIOMA);
    }
}
