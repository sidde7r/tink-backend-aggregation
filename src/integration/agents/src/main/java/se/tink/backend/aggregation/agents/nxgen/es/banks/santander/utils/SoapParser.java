package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SoapParser {
    public static Node getSoapBody(String xml) {
        SOAPMessage message = getSoapMessage(xml);
        return getFirstElement(message);
    }

    private static SOAPMessage getSoapMessage(String xml) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            final ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            return factory.createMessage(new MimeHeaders(), byteArrayInputStream);
        } catch (SOAPException | IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static Node getFirstElement(SOAPMessage message) {
        try {
            final NodeList childNodes = message.getSOAPBody().getChildNodes();
            Node firstElement = null;
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i) instanceof Element) {
                    firstElement = childNodes.item(i);
                    break;
                }
            }
            return firstElement;
        } catch (SOAPException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
