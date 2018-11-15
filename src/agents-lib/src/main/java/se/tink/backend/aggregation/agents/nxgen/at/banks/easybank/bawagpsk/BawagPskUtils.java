package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class BawagPskUtils {
    private BawagPskUtils() {
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

    public static <T> T xmlToEntity(final String xml, Class<T> entityClass) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(entityClass);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final Object o = unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            return (T) o;
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to unmarshal JAXB, ", e);
        }
    }
}
