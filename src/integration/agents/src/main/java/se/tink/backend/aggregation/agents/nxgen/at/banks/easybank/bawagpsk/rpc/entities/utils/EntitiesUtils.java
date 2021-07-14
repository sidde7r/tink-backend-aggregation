package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

public final class EntitiesUtils {

    private EntitiesUtils() {
        throw new AssertionError();
    }

    public static <T> String entityToXml(final T entity) {
        final StringWriter stringWriter;

        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(entity.getClass());
            final Marshaller marshaller = jaxbContext.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            stringWriter = new StringWriter();

            marshaller.marshal(entity, stringWriter);
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to marshal JAXB, ", e);
        }

        return stringWriter.toString();
    }
}
