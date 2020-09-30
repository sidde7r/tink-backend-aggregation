package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class StringSerializer {

    public static String serialize(final Serializable object) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (final IOException e) {
            throw new IllegalArgumentException("Can not serialize object to String");
        }
    }

    public static <T extends Serializable> T deserialize(final String objectAsString) {
        final byte[] data = Base64.getDecoder().decode(objectAsString);
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ((T) ois.readObject());
        } catch (final IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Can not deserialize from String");
        }
    }
}
