package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class AbnAmroUtils {

    private AbnAmroUtils() {
        throw new AssertionError();
    }

    public static String getDate() {
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat(AbnAmroConstants.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static OAuth2Token getOauthToken(final PersistentStorage persistentStorage) {
        return persistentStorage
                .get(AbnAmroConstants.StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new NoSuchElementException("Missing Oauth token!"));
    }

    private static KeyStore getKeyStore(final byte[] pkcs12Bytes, final String password) {
        final ByteArrayInputStream pkcs12Stream = new ByteArrayInputStream(pkcs12Bytes);
        try {
            final KeyStore p12 = KeyStore.getInstance("PKCS12", "BC");
            p12.load(pkcs12Stream, password.toCharArray());
            return p12;
        } catch (final KeyStoreException
                | NoSuchProviderException
                | IOException
                | NoSuchAlgorithmException
                | CertificateException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PrivateKey getPrivateKeyFromKeyStore(
            final KeyStore keyStore, final String password) {
        try {
            final Enumeration<String> aliases = keyStore.aliases();
            if (!aliases.hasMoreElements()) {
                throw new IllegalStateException("No aliases in keystore!");
            }
            final String alias = aliases.nextElement();
            return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        } catch (final KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
