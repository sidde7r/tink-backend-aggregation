package se.tink.backend.aggregation.register.nl.bunq;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

public final class BunqRegisterUtils {

    private BunqRegisterUtils() {
        throw new AssertionError();
    }

    public static String readFileContents(final String path) {
        try {
            return FileUtils.readFileToString(new File(path), "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String keyToPem(Key key) {
        final StringWriter writer = new StringWriter();
        final JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        try {
            pemWriter.writeObject(key);
            pemWriter.flush();
            pemWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }
}
