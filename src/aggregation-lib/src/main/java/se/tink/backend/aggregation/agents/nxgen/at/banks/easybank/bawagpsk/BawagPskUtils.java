package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;

public class BawagPskUtils {

    public static String envelopeToXml(final Envelope envelope) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(envelope.getClass());
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        final StringWriter stringWriter = new StringWriter();

        marshaller.marshal(envelope, stringWriter);

        return stringWriter.toString();
    }
}
