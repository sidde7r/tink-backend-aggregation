package se.tink.backend.core;

import com.google.common.io.Files;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class UserSessionTest {

    private static final File testFile = new File("data/test/user-session-old-serialization.object");

    @Test
    @Ignore
    public void testSerializing() throws IOException {
        UserSession userSession = new UserSession();
        userSession.setCreated(new Date());
        userSession.setExpiry(new Date());
        userSession.setId("Hello!");
        userSession.setSessionType(SessionTypes.MOBILE); // TODO: Test if UUID depends on this.
        userSession.setUserId("my-userid");
        
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        ObjectOutputStream outStream = new ObjectOutputStream(byteBuffer);
        outStream.writeObject(userSession);
        outStream.flush();

        Files.write(byteBuffer.toByteArray(), testFile);
    }

    @Test
    public void testDeserializationAfterUpdate() throws IOException, ClassNotFoundException {
        FileInputStream fileInput = new FileInputStream(testFile);
        ObjectInputStream objInput = new ObjectInputStream(fileInput);
        try {
            UserSession userSession = (UserSession) objInput.readObject();
            Assert.assertNotNull(userSession);
            Assert.assertEquals("Hello!", userSession.getId());
            Assert.assertNull(userSession.getOAuthClientId());
        } finally {
            objInput.close();
        }
    }

}
