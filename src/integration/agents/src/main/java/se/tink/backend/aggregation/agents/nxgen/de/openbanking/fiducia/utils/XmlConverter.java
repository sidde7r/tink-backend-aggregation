package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.rpc.CreatePaymentXmlRequest;

public final class XmlConverter {

    private static final String XML_MARSHAL_EXCEPTION = "Object can't be serialized to XML";

    private XmlConverter() {
        throw new AssertionError();
    }

    public static String convertToXml(CreatePaymentXmlRequest document) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CreatePaymentXmlRequest.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter sw = new StringWriter();
            marshaller.marshal(document, sw);

            return sw.toString();
        } catch (JAXBException e) {
            throw new IllegalArgumentException(XML_MARSHAL_EXCEPTION);
        }
    }
}
