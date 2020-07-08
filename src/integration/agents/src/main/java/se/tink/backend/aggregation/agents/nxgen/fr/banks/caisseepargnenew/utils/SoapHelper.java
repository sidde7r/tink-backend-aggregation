package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.ResponseKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.SoapKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.SoapXmlFragment;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.utils.soap.SoapParser;
import se.tink.libraries.identitydata.IdentityData;

public class SoapHelper {
    private static final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
    private static final XmlMapper mapper = new XmlMapper();

    public static String createSsoBapiRequest(String acessToken, String termId) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                + "<soap:Body>"
                + "<sso_BAPI xmlns=\"http://caisse-epargne.fr/webservices/\">"
                + "<modeALD>0</modeALD>"
                + "<idTerminal>"
                + termId
                + "</idTerminal>"
                + "<etablissement>CAISSE_EPARGNE</etablissement>"
                + "<at>"
                + acessToken
                + "</at>"
                + "</sso_BAPI>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    public static String createGetAccountsRequest() {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "<soap:Body>\n"
                + "<GetSyntheseCpteAbonnement xmlns=\"http://caisse-epargne.fr/webservices/\"/>\n"
                + "</soap:Body>\n"
                + "</soap:Envelope>";
    }

    public static String createGetAccountDetailsRequest(String fullAccountNumber) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "<soap:Body>\n"
                + "<GetRice xmlns=\"http://caisse-epargne.fr/webservices/\">\n"
                + "<cpt>"
                + fullAccountNumber
                + "</cpt>\n"
                + "</GetRice>\n"
                + "</soap:Body>\n"
                + "</soap:Envelope>";
    }

    private static Element parseSoapBody(String xml) {
        Node soapBody = SoapParser.getSoapBody(xml);
        Preconditions.checkState(
                soapBody instanceof Element, "Could not parse SOAP body from server response.");

        return (Element) soapBody;
    }

    public static IdentityData getIdentityData(String xml) {
        xml = xml.replace("\n", "").replace("\r", "");
        Element soapBody = parseSoapBody(xml);
        NodeList identityDataList = soapBody.getElementsByTagName(SoapKeys.VALID_SUBSCRIPTION);
        Preconditions.checkState(
                identityDataList.getLength() == 1, "Could not parse identity data: " + xml);
        NodeList identityData = identityDataList.item(0).getChildNodes();
        String name = "";
        String surname = "";
        for (int i = 0; i < identityData.getLength(); i++) {
            Node node = identityData.item(i);
            String nodeName = node.getNodeName();
            if (Objects.equals(nodeName, SoapKeys.SURNAME)) {
                surname = node.getFirstChild().getNodeValue();
            } else if (Objects.equals(nodeName, SoapKeys.NAME)) {
                name = node.getFirstChild().getNodeValue();
            }
        }
        return IdentityData.builder()
                .addFirstNameElement(name)
                .addSurnameElement(surname)
                .setDateOfBirth(null)
                .build();
    }

    public static AccountsResponse getAccounts(String accountsResponse) {
        return parseXmlToResponse(
                ResponseKeys.ACCOUNTS_RESPONSE, accountsResponse, AccountsResponse.class);
    }

    public static AccountDetailsResponse getAccountDetails(String accountDetailsResponse) {
        return parseXmlToResponse(
                ResponseKeys.ACCOUNT_DETAILS_RESULT,
                accountDetailsResponse,
                AccountDetailsResponse.class);
    }

    private static <T> T parseXmlToResponse(
            String xmlElementKey, String xmlResponse, Class<T> clazz) {
        xmlResponse = xmlResponse.replace("\n", "").replace("\r", "");
        try (StringReader stringReader = new StringReader(xmlResponse)) {
            XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(stringReader);
            while (xmlReader.hasNext()) {
                int eventType = xmlReader.next();
                if (eventType == XMLStreamReader.START_ELEMENT
                        && xmlReader.getLocalName().equals(xmlElementKey)) {
                    break;
                }
            }
            T response = mapper.readValue(xmlReader, clazz);
            xmlReader.close();
            return response;
        } catch (XMLStreamException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> String formRequest(T request) {
        String requestAsString;
        try {
            requestAsString = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return SoapXmlFragment.PREFIX + requestAsString + SoapXmlFragment.SUFFIX;
    }

    public static TransactionsResponse getTransactions(String transactionsResponse) {
        return parseXmlToResponse(
                ResponseKeys.TRANSACTIONS_RESULT, transactionsResponse, TransactionsResponse.class);
    }
}
