package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.PaymentDocument;

public final class XmlUtils {
    private XmlUtils() {
        throw new AssertionError();
    }

    public static String convertToXml(PaymentDocument document) {
        String result = "";
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PaymentDocument.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter sw = new StringWriter();
            marshaller.marshal(document, sw);

            result = sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
