package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.utils;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.UUID;

public class AbnAmroUtils {
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

    public static String createSignatureString(
            final String date, final String digest, final String requestId) {
        String result = AbnAmroConstants.Signature.SIGNING_STRING_DATE + date + "\n";
        result +=
                AbnAmroConstants.Signature.SIGNING_STRING_DIGEST + AbnAmroConstants.Signature.SIGNING_STRING_SHA_512 + digest + "\n";
        result += AbnAmroConstants.Signature.SIGNING_STRING_REQUEST_ID + requestId;
        return result;
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

    private static X509Certificate getX509CertificateFromKeystore(final KeyStore keyStore) {
        try {
            final Enumeration<String> aliases = keyStore.aliases();
            if (!aliases.hasMoreElements()) {
                throw new IllegalStateException("No aliases in keystore!");
            }

            final String alias = aliases.nextElement();
            final Certificate cert = keyStore.getCertificate(alias);
            if (!(cert instanceof X509Certificate)) {
                throw new IllegalStateException("Certificate is not x509!");
            }
            return (X509Certificate) cert;
        } catch (final KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    public static X509Certificate getX509Certificate(
            final byte[] pkcs12Bytes, final String password) {
        final KeyStore keyStore = getKeyStore(pkcs12Bytes, password);
        return getX509CertificateFromKeystore(keyStore);
    }

    public static String getB64EncodedX509Certificate(
            final byte[] pkcs12Bytes, final String password) {
        try {
            return Base64.getEncoder()
                    .encodeToString(getX509Certificate(pkcs12Bytes, password).getEncoded());
        } catch (final CertificateEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static PrivateKey getPrivateKey(final byte[] pkcs12Bytes, final String password) {
        return getPrivateKeyFromKeyStore(getKeyStore(pkcs12Bytes, password), password);
    }

    public static String getCertificateSerialNumber(
            final byte[] pkcs12Bytes, final String password) {
        final X509Certificate x509 =
                getX509CertificateFromKeystore(getKeyStore(pkcs12Bytes, password));
        return x509.getSerialNumber().toString();
    }

    public static String createSignatureHeader(
            final String keyId,
            final String algorithm,
            final String b64Signature,
            final String headersValue) {
        return new AbnAmroSignatureHeader(keyId, algorithm, b64Signature, headersValue).toString();
    }
}
