package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.TokenEntity;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.uuid.UUIDUtils;

@JsonObject
public class RegisterAsPSD2ProviderRequest {
    @JsonProperty("client_payment_service_provider_certificate")
    private String clientPaymentServiceProviderCertificate;

    @JsonProperty("client_payment_service_provider_certificate_chain")
    private String clientPaymentServiceProviderCertificateChain;

    @JsonProperty("client_public_key_signature")
    private String clientPublicKeySignature;

    public RegisterAsPSD2ProviderRequest(
            String clientPaymentServiceProviderCertificate,
            String clientPaymentServiceProviderCertificateChain,
            String clientPublicKeySignature) {
        this.clientPaymentServiceProviderCertificate = clientPaymentServiceProviderCertificate;
        this.clientPaymentServiceProviderCertificateChain =
                clientPaymentServiceProviderCertificateChain;
        this.clientPublicKeySignature = clientPublicKeySignature;
    }

    public static RegisterAsPSD2ProviderRequest of(PublicKey publicKey, TokenEntity tokenEntity) {
        // Generate test certificates following instructions from here
        // https://github.com/bunq/psd2_sample_csharp/blob/97ca777894e401ef85e43f9ae0e54a1e501290ac/Program.cs#L25
        KeyPair keyPair = generateKeyPair();
        PrivateKey qsealPrivateKey = keyPair.getPrivate();

        X509Certificate qsealCert = generateCertificate(keyPair);

        String rootCertString =
                "-----BEGIN CERTIFICATE-----\n"
                        + "MIID1zCCAr+gAwIBAgIBATANBgkqhkiG9w0BAQsFADB1MQswCQYDVQQGEwJOTDEW\n"
                        + "MBQGA1UECBMNTm9vcmQtSG9sbGFuZDESMBAGA1UEBxMJQW1zdGVyZGFtMRIwEAYD\n"
                        + "VQQKEwlidW5xIGIudi4xDzANBgNVBAsTBkRldk9wczEVMBMGA1UEAxMMUFNEMiBU\n"
                        + "ZXN0IENBMB4XDTE5MDIxODEzNDkwMFoXDTI5MDIxODEzNDkwMFowdTELMAkGA1UE\n"
                        + "BhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQxEjAQBgNVBAcTCUFtc3RlcmRh\n"
                        + "bTESMBAGA1UEChMJYnVucSBiLnYuMQ8wDQYDVQQLEwZEZXZPcHMxFTATBgNVBAMT\n"
                        + "DFBTRDIgVGVzdCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALOv\n"
                        + "zPl+uegBpnFhXsjuKs0ws00e+232wR9tvDqYBjGdOlYorw8CyrT+mr0HKO9lx7vg\n"
                        + "xhJ3f+oonkZvBb+IehDmEsBbZ+vRtdjEWw3RTWVBT69jPcRQGE2e5qUuTJYVCONY\n"
                        + "JsOQP8CoCHXa6+oUSmUyMZX/zNJhTvbLV9e/qpIWwWVrKzK0EEB5c71gITNgzOXG\n"
                        + "+lIKJmOnvvJyWPCx02hIgQI3nVphDj8ydMEKuwTgBrFV5Lqkar3L6ngF7LgzjXPC\n"
                        + "Nbf3JL/2Ccp0hYPb2MLVEpYba8/38eN6izjorJiwu+uGehOpj/RNcfv27iGyvXRY\n"
                        + "FC2PfRP8ZP5CpoijJR8CAwEAAaNyMHAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4E\n"
                        + "FgQU38gzLVi6UQYiNLXKIhwoklPnSYMwCwYDVR0PBAQDAgEGMBEGCWCGSAGG+EIB\n"
                        + "AQQEAwIABzAeBglghkgBhvhCAQ0EERYPeGNhIGNlcnRpZmljYXRlMA0GCSqGSIb3\n"
                        + "DQEBCwUAA4IBAQBt6HBrnSvEbUX514ab3Zepdct0QWVLzzEKFC8y9ARLWttyaRJ5\n"
                        + "AhzCa4t8LJnyoYuEPHOKDIsqLzmJlwqBnsXPuMdWEd3vnFRgj1oL3vVqoJwrfqDp\n"
                        + "S3jHshWopqMKtmzAO9Q3BWpk/lrqJTP1y/6057LtMGhwA6m0fDmvA+VuTrh9mgzw\n"
                        + "FgWwmahVa08h1Cm5+vc1Phi8wVXi3R1NzmVUQFYOixSwifs8P0MstBfCFlBFQ47C\n"
                        + "EvGEYvOBLlEiiaoMUT6aoYj+L8zHWXakSQFAzIzQFJn668q2ds6zx67P7wKFZ887\n"
                        + "VJSv7sTqspxON1s1oFlkRXu5JihaVJcHmFAY\n"
                        + "-----END CERTIFICATE-----\n";
        X509Certificate rootCert = getCertificateFromPEMBytes(rootCertString.getBytes());

        String clientPublicKeySignatureString = keyToPem(publicKey) + tokenEntity.getToken();

        byte[] clientPublicKeySignature =
                clientPublicKeySignatureString.getBytes(StandardCharsets.UTF_8);
        byte[] signedClientPublicKeySignature =
                RSA.signSha256(qsealPrivateKey, clientPublicKeySignature);

        return new RegisterAsPSD2ProviderRequest(
                x509CertificateToPem(qsealCert),
                x509CertificateToPem(rootCert),
                EncodingUtils.encodeAsBase64String(signedClientPublicKeySignature));
    }

    private static X509Certificate generateCertificate(KeyPair keyPair) {
        // yesterday
        Date from = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        // in 1 year
        Date to = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365));

        SubjectPublicKeyInfo subjectPublicKeyInfo =
                SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        X500Name x500Name = new X500Name("CN=TINK" + UUIDUtils.generateUUID() + " PISP AISP, C=NL");
        BigInteger sn = new BigInteger(64, new SecureRandom());

        X509v1CertificateBuilder builder =
                new X509v1CertificateBuilder(
                        x500Name, sn, from, to, x500Name, subjectPublicKeyInfo);

        ContentSigner sigGen = null;
        try {
            sigGen =
                    new JcaContentSignerBuilder("SHA1withRSA")
                            .setProvider("BC")
                            .build(keyPair.getPrivate());
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

        StringBuffer s = new StringBuffer();

        return x509Certificate;
    }

    private static KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGenerator.initialize(4096, new SecureRandom());

        return keyPairGenerator.generateKeyPair();
    }

    private static X509Certificate getCertificateFromPEMBytes(byte[] byteCertificate) {
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

    private static String keyToPem(Key key) {
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
}
