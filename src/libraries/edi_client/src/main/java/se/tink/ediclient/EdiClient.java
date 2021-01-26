package se.tink.ediclient;

import static se.tink.ediclient.EdiCryptoUtils.generateCSR;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdiClient {
    private static final Logger LOG = LoggerFactory.getLogger(EdiClient.class);

    private static final String DEVCERT_P12 = "devcert.p12";

    private static final String CERT_ALIAS = "clientcert";
    private static final char[] DEFAULT_KEYSTORE_PWD = "changeme".toCharArray();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static KeyStore issue()
            throws GeneralSecurityException, URISyntaxException, IOException,
                    OperatorCreationException {
        KeyPair pair = EdiCryptoUtils.generateKeyPair();
        String csrString = generateCSR(pair);

        String url = EdiApiClient.urlForCsr(csrString);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
            LOG.info("Opening url: {}", url);
            Desktop.getDesktop().browse(new URI(url));
        } else {
            LOG.info(
                    "Could not automatically open browser. URL to issue development certificate: {}",
                    url);
        }

        RSAPublicKey publicKey = (RSAPublicKey) pair.getPublic();
        BigInteger modulus = publicKey.getModulus();

        byte[] certPemBytes = EdiApiClient.pollForModulus(modulus);
        X509Certificate certificate = EdiCryptoUtils.parseCertificate(certPemBytes);
        Certificate[] certChain = new Certificate[1];
        certChain[0] = certificate;
        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(null, DEFAULT_KEYSTORE_PWD);
        keyStore.setKeyEntry(CERT_ALIAS, pair.getPrivate(), null, certChain);
        return keyStore;
    }

    public static KeyStore requestOrGetDevCert(File workDir)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException,
                    NoSuchProviderException {
        if (!workDir.exists() && !workDir.mkdirs()) {
            LOG.warn("Could not make working directory: {}", workDir.getAbsolutePath());
        }
        File devCertKeystore = new File(workDir, DEVCERT_P12);
        if (devCertKeystore.exists()) {
            // load keystore
            LOG.info("loading existing keystore");
            KeyStore keyStore = loadExistingKeystore(devCertKeystore);
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(CERT_ALIAS);
            if (certificate != null) {
                LOG.info("Certificate exists, checking expiry");
                if (certificateAboutToExpire(certificate.getNotAfter(), ZonedDateTime.now())) {
                    LOG.info("Certificate is about to expire, should be renewed");
                    Files.delete(devCertKeystore.toPath());
                } else {
                    LOG.info("Certificate has plenty of validity");
                    return keyStore;
                }
            }
        }
        try {

            KeyStore newKeyStore = issue();
            LOG.info("saving key store");
            trySaveKeystore(devCertKeystore, newKeyStore);
            return newKeyStore;
        } catch (Exception ex) {
            throw new IllegalStateException("Exception thrown issuing dev cert", ex);
        }
    }

    @SuppressWarnings("java:S106")
    private static void trySaveKeystore(File saveAs, KeyStore newKeyStore)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException {
        try (FileOutputStream keystoreOutputStream = new FileOutputStream(saveAs)) {
            newKeyStore.store(keystoreOutputStream, DEFAULT_KEYSTORE_PWD);
        } catch (IOException ex) {
            LOG.info("IOException saving keystore", ex);
            printCouldNotSaveKeystoreMessage(LOG::error);

            // Use println here to make message more visible in IDE
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(
                                    () -> printCouldNotSaveKeystoreMessage(System.out::println)));
        }
    }

    private static final String ARROW_PREFIX = "====>";

    private static void printCouldNotSaveKeystoreMessage(Consumer<String> print) {
        print.accept(ARROW_PREFIX);
        print.accept(
                ARROW_PREFIX
                        + " Could not save keystore. The test will proceed, but you will have");
        print.accept(ARROW_PREFIX + " to issue a new one in the next test run.");
        print.accept(ARROW_PREFIX);
        print.accept(ARROW_PREFIX + " You can issue one manually with:");
        print.accept(ARROW_PREFIX + "     bazel run //src/libraries/edi_client:issue");
        print.accept(ARROW_PREFIX);
    }

    private static KeyStore loadExistingKeystore(File devCertKeystore)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
                    NoSuchProviderException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        try (FileInputStream keyStoreStream = new FileInputStream(devCertKeystore)) {
            keyStore.load(keyStoreStream, DEFAULT_KEYSTORE_PWD);
        }
        return keyStore;
    }

    public static KeyStore requestOrGetDevCert()
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException,
                    NoSuchProviderException {
        return requestOrGetDevCert(getDefaultWorkDir());
    }

    private static File getDefaultWorkDir() {
        String operatingSystem =
                System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (operatingSystem.contains("mac") || operatingSystem.contains("darwin")) {
            // On macOS, this directory is excluded from sandbox protection,
            // so we can write to it
            return new File(System.getProperty("user.home"), "Library/Developer/edi-client");
        }

        return new File(System.getProperty("user.home"), ".edi-client");
    }

    static boolean certificateAboutToExpire(Date notAfter, ZonedDateTime now) {
        ZonedDateTime aboutToExpire =
                ZonedDateTime.ofInstant(notAfter.toInstant(), ZoneId.systemDefault()).minusHours(4);
        return now.isAfter(aboutToExpire);
    }
}
