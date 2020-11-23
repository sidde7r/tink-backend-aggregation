package se.tink.ediclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.openssl.MiscPEMGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;

class EdiCryptoUtils {
    static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        return keyGen.genKeyPair();
    }

    static String generateCSR(KeyPair pair) throws IOException, OperatorCreationException {
        // CN on CSR is ignored, service will populate CN based on user's GSuite account
        PKCS10CertificationRequestBuilder p10Builder =
                new JcaPKCS10CertificationRequestBuilder(
                        new X500Principal("CN=tink-backend-aggregation"), pair.getPublic());
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer = csBuilder.build(pair.getPrivate());
        PKCS10CertificationRequest csr = p10Builder.build(signer);
        StringWriter string = new StringWriter();
        PemWriter pemWriter = new PemWriter(string);
        PemObjectGenerator objGen = new MiscPEMGenerator(csr);
        pemWriter.writeObject(objGen);
        pemWriter.close();
        return string.toString();
    }

    static String randomString(int byteCount) {
        byte[] b = new byte[byteCount];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().encodeToString(b);
    }

    static String sha256(String in) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(in.getBytes(StandardCharsets.UTF_8));
        return new String(Hex.encode(hash), StandardCharsets.US_ASCII);
    }

    public static X509Certificate parseCertificate(byte[] pemData) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate)
                certificateFactory.generateCertificate(new ByteArrayInputStream(pemData));
    }
}
