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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;
import se.tink.backend.aggregation.eidas.Signer;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.register.nl.rabobank.RabobankRegisterConstants.Cli;
import se.tink.backend.aggregation.register.nl.rabobank.RabobankRegisterConstants.Header;
import se.tink.backend.aggregation.register.nl.rabobank.RabobankRegisterConstants.Jwt;
import se.tink.backend.aggregation.register.nl.rabobank.RabobankRegisterConstants.Url;
import se.tink.backend.aggregation.register.nl.rabobank.rpc.JwsRequest;

public final class RabobankRegisterCommand {

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

    private static Options createOptions() {

        final Option qsealcPathOption =
                Option.builder(Cli.CERTIFICATE_PATH)
                        .longOpt("qsealc-pem-path")
                        .required()
                        .hasArg()
                        .argName("Path")
                        .desc("Path to your QSeal certificate PEM file.")
                        .build();

        final Option emailOption =
                Option.builder(Cli.EMAIL)
                        .longOpt("email")
                        .required()
                        .hasArg()
                        .argName("Email")
                        .desc("Email address and username of the production account to be created.")
                        .build();

        final Options options = new Options();

        options.addOption(qsealcPathOption);
        options.addOption(emailOption);

        return options;
    }

    public static void main(final String[] args) {

        final Options options = createOptions();

        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(
                    "bazel run //src/commands/psd2-register/src/main/java/se/tink/backend/aggregation/register/nl/rabobank --",
                    options);
            System.exit(1);
        }

        final String qsealcCertificatePath = cmd.getOptionValue(Cli.CERTIFICATE_PATH);
        final String email = cmd.getOptionValue(Cli.EMAIL);

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        final TinkHttpClient client = new TinkHttpClient();

        client.setDebugOutput(true);

        final String qsealcB64 = readPemCertificateAsB64(qsealcCertificatePath);

        // Expire after 6 hours
        final int exp = (int) (System.currentTimeMillis() / 1000) + 6 * 60 * 60;

        final Signer jwsSigner = new QsealcEidasProxySigner(client, Url.EIDAS_PROXY_BASE_URL);

        final JwsRequest body =
                JwsRequest.create(qsealcB64, jwsSigner, exp, email, Jwt.ORGANIZATION);

        client.request(Url.REGISTER)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .header(Header.ENROLLMENT_CLIENT_ID_KEY, Header.ENROLLMENT_CLIENT_ID)
                .body(body)
                .post();
    }
}
