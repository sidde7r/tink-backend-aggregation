package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.ResponseKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.SoapKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.SoapXmlFragment;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.utils.soap.SoapParser;
import se.tink.libraries.identitydata.IdentityData;

public class SoapHelper {
    private static final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
    private static final XmlMapper mapper = new XmlMapper();

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
                if (eventType == XMLStreamConstants.START_ELEMENT
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
