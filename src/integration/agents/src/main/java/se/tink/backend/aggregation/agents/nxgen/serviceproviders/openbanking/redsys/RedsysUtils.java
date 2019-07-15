package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;

public class RedsysUtils {
    private static String getKeyID(X509Certificate cert) {
        return String.format(
                Locale.ENGLISH,
                Signature.KEY_ID_FORMAT,
                cert.getSerialNumber(),
                cert.getIssuerX500Principal().getName());
    }

    private static String getOrganizationIdentifier(X509Certificate certificate) {
        return new X500Name(certificate.getSubjectX500Principal().getName())
                .getRDNs(BCStyle.ORGANIZATION_IDENTIFIER)[0]
                .getFirst()
                .getValue()
                .toString();
    }

    private static PrivateKeyInfo decryptPrivateKey(
            RedsysConfiguration configuration, PKCS8EncryptedPrivateKeyInfo encryptedKeyInfo)
            throws OperatorCreationException, PKCSException {
        final String password =
                configuration
                        .getClientSigningKeyPassword()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "clientsigningKeyPassword not set"));
        final InputDecryptorProvider decryptorProvider =
                new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password.toCharArray());
        return encryptedKeyInfo.decryptPrivateKeyInfo(decryptorProvider);
    }

    private static PrivateKey readPrivateKey(RedsysConfiguration configuration) throws IOException {
        final String keyPath =
                configuration
                        .getClientSigningKeyPath()
                        .orElseThrow(
                                () -> new IllegalStateException("clientSigningKeyPath not set"));
        final PEMParser parser = new PEMParser(new InputStreamReader(new FileInputStream(keyPath)));
        final Object readKeyInfo = parser.readObject();
        final PrivateKeyInfo keyInfo;
        if (readKeyInfo instanceof PKCS8EncryptedPrivateKeyInfo) {
            try {
                keyInfo =
                        decryptPrivateKey(
                                configuration, (PKCS8EncryptedPrivateKeyInfo) readKeyInfo);
            } catch (OperatorCreationException | PKCSException e) {
                throw new IllegalStateException("Unable to decrypt private key", e);
            }
        } else if (readKeyInfo instanceof PEMKeyPair) {
            keyInfo = ((PEMKeyPair) readKeyInfo).getPrivateKeyInfo();
        } else {
            throw new IllegalStateException(
                    "Unexpected key class: " + readKeyInfo.getClass().getCanonicalName());
        }

        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        return converter.getPrivateKey(keyInfo);
    }

    private static String signPayloadWithProxy(
            EidasProxyConfiguration proxyConfiguration, String payload, String certificateId) {
        return new QsealcEidasProxySigner(proxyConfiguration, certificateId)
                .getSignatureBase64(payload.getBytes());
    }

    private static String signPayload(
            RedsysConfiguration configuration,
            EidasProxyConfiguration eidasProxyConfiguration,
            String payload) {
        if (configuration.getClientSigningKeyPath().isPresent()) {
            // sign with key
            final PrivateKey privateKey;
            try {
                privateKey = readPrivateKey(configuration);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return Base64.getEncoder()
                    .encodeToString(RSA.signSha256(privateKey, payload.getBytes()));
        } else {
            // sign with EIDAS proxy
            return signPayloadWithProxy(
                    eidasProxyConfiguration,
                    payload,
                    configuration.getClientSigningCertificateId());
        }
    }

    public static String getAuthClientId(X509Certificate cert) {
        return getOrganizationIdentifier(cert);
    }

    static final String[] SIGN_HEADERS = {
        HeaderKeys.DIGEST, HeaderKeys.REQUEST_ID, HeaderKeys.TPP_REDIRECT_URI
    };

    public static String generateRequestSignature(
            RedsysConfiguration configuration,
            EidasProxyConfiguration eidasProxyConfiguration,
            X509Certificate certificate,
            Map<String, Object> headers) {
        ArrayList<String> signedHeaders = Lists.newArrayList();
        ArrayList<String> payloadElements = Lists.newArrayList();
        for (String header : SIGN_HEADERS) {
            if (headers.containsKey(header)) {
                signedHeaders.add(header);
                payloadElements.add(
                        String.format(
                                "%s: %s",
                                header.toLowerCase(Locale.ENGLISH),
                                headers.get(header).toString()));
            }
        }

        final String payloadToSign = Joiner.on("\n").join(payloadElements);
        final String headerList = Joiner.on(" ").join(signedHeaders).toLowerCase(Locale.ENGLISH);
        final String signature = signPayload(configuration, eidasProxyConfiguration, payloadToSign);
        final String keyID = getKeyID(certificate);
        return String.format(Signature.FORMAT, keyID, headerList, signature);
    }

    public static X509Certificate parseCertificate(String encodedCertificate) {
        try {
            final String certificatePem =
                    "-----BEGIN CERTIFICATE-----\n"
                            + encodedCertificate
                            + "\n-----END CERTIFICATE-----";
            InputStream in = new ByteArrayInputStream(certificatePem.getBytes(Charsets.US_ASCII));
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(in);
            return cert;
        } catch (CertificateException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String getEncodedSigningCertificate(X509Certificate certificate) {
        try {
            return Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException("Could not encode signing certificate", e);
        }
    }
}
