package se.tink.backend.aggregation.register.nl.rabobank;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.rabobank.QsealcEidasProxySigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.rabobank.Signer;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.register.nl.rabobank.rpc.JwsRequest;

public final class RabobankRegisterCommand {

    // Client ID used specifically for enrollment:
    // https://developer.rabobank.nl/reference/third-party-providers/1-0-0
    private static final String ENROLLMENT_CLIENT_ID = "64f38624-718d-4732-b579-b8979071fcb0";

    private static PrivateKey readPemPrivateKey(final String path) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        final FileReader reader;
        try {
            reader = new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        final PEMParser pemParser = new PEMParser(reader);
        final PKCS8EncryptedPrivateKeyInfo encryptedKeyPair;
        try {
            encryptedKeyPair = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        final InputDecryptorProvider decryptorProvider;
        try {
            decryptorProvider =
                    new JceOpenSSLPKCS8DecryptorProviderBuilder().build("tink".toCharArray());
        } catch (OperatorCreationException e) {
            throw new IllegalStateException(e);
        }
        final PrivateKeyInfo privateKeyInfo;
        try {
            privateKeyInfo = encryptedKeyPair.decryptPrivateKeyInfo(decryptorProvider);
        } catch (PKCSException e) {
            throw new IllegalStateException(e);
        }
        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        try {
            return converter.getPrivateKey(privateKeyInfo);
        } catch (PEMException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String readPemCertificateAsB64(final String path) {
        final CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException(e);
        }
        final Certificate certificate;
        try {
            certificate = certificateFactory.generateCertificate(new FileInputStream(path));
        } catch (CertificateException | FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        final byte[] certificateBytes;
        try {
            certificateBytes = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException(e);
        }
        return Base64.getEncoder().encodeToString(certificateBytes);
    }

    public static void main(final String[] args) {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        final TinkHttpClient client = new TinkHttpClient();

        client.setDebugOutput(true);

        final URL url =
                new URL("https://api.rabobank.nl/openapi/open-banking/third-party-providers");

        final String qsealcCertificatePath =
                "src/commands/psd2-register/src/main/java/se/tink/backend/aggregation/register/nl/rabobank/resources/tink_qsealc.pem";

        final String qsealcB64 = readPemCertificateAsB64(qsealcCertificatePath);
        final int exp = 1559920641;
        final String email = "sebastian.olsson@tink.se";
        final String organization = "Tink AB";

        final Signer jwsSigner = new QsealcEidasProxySigner(client);

        final JwsRequest body = JwsRequest.create(qsealcB64, jwsSigner, exp, email, organization);

        client.request(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .header("x-ibm-client-id", ENROLLMENT_CLIENT_ID)
                .body(body)
                .post();
    }
}
