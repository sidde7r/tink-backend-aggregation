package se.tink.backend.aggregation.register.nl.bunq.environment.sandbox;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.libraries.uuid.UUIDUtils;

// Generate test certificates following instructions from here
// https://github.com/bunq/psd2_sample_csharp/blob/97ca777894e401ef85e43f9ae0e54a1e501290ac/Program.cs#L25
public final class BunqRegisterSandboxUtils {

    private static final KeyPair testCertificatesKeyPair = generateTestKeyPair();

    private BunqRegisterSandboxUtils() {}

    public static String getQSealCCertificateAsString() {
        X509Certificate qsealcCert = generateTestCertificate();
        return x509CertificateToPem(qsealcCert);
    }

    public static String readFileContents(final String path) {
        try {
            return FileUtils.readFileToString(new File(path), "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getPaymentServiceProviderCertificateChainAsString() {
        final String path =
                "src/commands/psd2-register/src/main/java/se/tink/backend/aggregation/register/nl/bunq/resources/rootcert.pem";
        final String rootCertString = readFileContents(path);

        X509Certificate rootCert = getCertificateFromPemBytes(rootCertString.getBytes());
        return x509CertificateToPem(rootCert);
    }

    public static String getClientPublicKeySignatureAsString(PublicKey publicKey, String token) {
        String clientPublicKeySignatureString = keyToPem(publicKey) + token;

        byte[] clientPublicKeySignature =
                clientPublicKeySignatureString.getBytes(StandardCharsets.UTF_8);
        byte[] signedClientPublicKeySignature =
                RSA.signSha256(testCertificatesKeyPair.getPrivate(), clientPublicKeySignature);

        return EncodingUtils.encodeAsBase64String(signedClientPublicKeySignature);
    }

    private static KeyPair generateTestKeyPair() {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        keyPairGenerator.initialize(4096, new SecureRandom());

        return keyPairGenerator.generateKeyPair();
    }

    private static X509Certificate generateTestCertificate() {
        // yesterday
        Date from = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        // in 1 year
        Date to = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365));

        SubjectPublicKeyInfo subjectPublicKeyInfo =
                SubjectPublicKeyInfo.getInstance(testCertificatesKeyPair.getPublic().getEncoded());

        X500Name x500Name =
                new X500Name("CN=TINK-" + UUIDUtils.generateUUID() + " PISP AISP, C=NL");
        BigInteger sn = new BigInteger(64, new SecureRandom());

        X509v1CertificateBuilder builder =
                new X509v1CertificateBuilder(
                        x500Name, sn, from, to, x500Name, subjectPublicKeyInfo);

        ContentSigner sigGen = null;
        try {
            sigGen =
                    new JcaContentSignerBuilder("SHA1withRSA")
                            .setProvider("BC")
                            .build(testCertificatesKeyPair.getPrivate());
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        }
        X509CertificateHolder certificateHolder = builder.build(sigGen);

        X509Certificate x509Certificate = null;
        try {
            x509Certificate =
                    new JcaX509CertificateConverter()
                            .setProvider("BC")
                            .getCertificate(certificateHolder);
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return x509Certificate;
    }

    private static String x509CertificateToPem(X509Certificate cert) {
        final StringWriter writer = new StringWriter();
        final JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        try {
            pemWriter.writeObject(cert);
            pemWriter.flush();
            pemWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    private static X509Certificate getCertificateFromPemBytes(byte[] byteCertificate) {
        try {
            ByteArrayInputStream isCert = new ByteArrayInputStream(byteCertificate);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(isCert);

            X509Certificate x509Certificate;

            if (cert instanceof X509Certificate) {
                x509Certificate = (X509Certificate) cert;
            } else {
                throw new IllegalStateException("Certificate not in expected format");
            }

            return x509Certificate;
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return null;
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
