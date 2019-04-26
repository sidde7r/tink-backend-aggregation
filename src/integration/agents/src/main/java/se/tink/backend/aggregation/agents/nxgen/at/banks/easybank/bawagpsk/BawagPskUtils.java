package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public final class BawagPskUtils {
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

    /**
     * Tells the HTTP client to trust the intermediate Digicert certificate which is placed between
     * the Digicert root CA and Bawag PSK. The Bawag PSK server is misconfigured as it sends only
     * the server certificate even though it is supposed to send the intermediate certificate as
     * well. To get around this, we have to trust the intermediate certificate explicitly.
     */
    public static void trustIntermediateCertificate(final TinkHttpClient client) {
        byte[] jksBytes;
        try {
            jksBytes = Files.readAllBytes(Paths.get(BawagPskConstants.Tls.INTERMEDIATE_CERT_PATH));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        client.trustRootCaCertificate(jksBytes, BawagPskConstants.Tls.INTERMEDIATE_CERT_PASSWORD);
    }
}
