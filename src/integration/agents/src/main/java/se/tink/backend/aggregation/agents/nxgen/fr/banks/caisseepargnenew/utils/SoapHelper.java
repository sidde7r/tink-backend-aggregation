package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.utils;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.SoapKeys;
import se.tink.backend.aggregation.agents.utils.soap.SoapParser;
import se.tink.libraries.identitydata.IdentityData;

public class SoapHelper {
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
}
