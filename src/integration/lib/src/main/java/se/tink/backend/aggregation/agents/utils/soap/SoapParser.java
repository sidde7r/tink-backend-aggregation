package se.tink.backend.aggregation.agents.utils.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
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
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
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
