package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.CreatePaymentXmlRequest;

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
            marshaller.setProperty(
                    Marshaller.JAXB_SCHEMA_LOCATION,
                    "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03 schema.xsd");

            StringWriter sw = new StringWriter();
            marshaller.marshal(document, sw);

            return sw.toString();
        } catch (JAXBException e) {
            throw new IllegalArgumentException(XML_MARSHAL_EXCEPTION);
        }
    }
}
