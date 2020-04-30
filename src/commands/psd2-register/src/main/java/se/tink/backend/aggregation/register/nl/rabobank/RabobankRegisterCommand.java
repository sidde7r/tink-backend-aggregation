package se.tink.backend.aggregation.register.nl.rabobank;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.register.nl.rabobank.RabobankRegisterConstants.Cli;
import se.tink.backend.aggregation.register.nl.rabobank.RabobankRegisterConstants.Header;
import se.tink.backend.aggregation.register.nl.rabobank.RabobankRegisterConstants.Url;
import se.tink.backend.aggregation.register.nl.rabobank.rpc.JwsRequest;

public final class RabobankRegisterCommand {

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

        final Option organizationOption =
                Option.builder(Cli.ORGANIZATION)
                        .longOpt("organization")
                        .required()
                        .hasArg()
                        .argName("Organization")
                        .desc(
                                "Name of the organization to which the production account belongs."
                                        + " Should match the organization stated in the QSeal"
                                        + " certificate.")
                        .build();

        final Option certificateIdOption =
                Option.builder(Cli.CERTIFICATE_ID)
                        .longOpt("certificate_id")
                        .required()
                        .hasArg()
                        .argName("Certificate ID")
                        .desc(
                                "An identifier for the QSealC key which the eIDAS proxy uses to"
                                        + " generate a signature.")
                        .build();

        final Option clusterIdOption =
                Option.builder(Cli.CLUSTER_ID)
                        .longOpt("cluster_id")
                        .required()
                        .hasArg()
                        .argName("Cluster ID")
                        .desc("An identifier for the cluster the proxy will use for signing")
                        .build();

        final Option appIdOption =
                Option.builder(Cli.APP_ID)
                        .longOpt("app_id")
                        .required()
                        .hasArg()
                        .argName("App ID")
                        .desc("An identifier for the app the proxy will use for signing")
                        .build();

        final Options options = new Options();

        options.addOption(qsealcPathOption);
        options.addOption(emailOption);
        options.addOption(organizationOption);
        options.addOption(certificateIdOption);
        options.addOption(clusterIdOption);
        options.addOption(appIdOption);

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
        final String organization = cmd.getOptionValue(Cli.ORGANIZATION);
        final String certificateId = cmd.getOptionValue(Cli.CERTIFICATE_ID);
        final String clusterId = cmd.getOptionValue(Cli.CLUSTER_ID);
        final String appId = cmd.getOptionValue(Cli.APP_ID);

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        final TinkHttpClient client = new LegacyTinkHttpClient();

        client.setDebugOutput(true);

        final String qsealcB64 = readPemCertificateAsB64(qsealcCertificatePath);

        // Expire after 6 hours
        final int exp = (int) (System.currentTimeMillis() / 1000) + 6 * 60 * 60;

        EidasIdentity eidasIdentity =
                new EidasIdentity(clusterId, appId, RabobankRegisterCommand.class);

        QsealcSigner jwsSigner =
                QsealcSignerImpl.build(
                        RabobankRegisterConstants.eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity,
                        certificateId);

        final JwsRequest body = JwsRequest.create(qsealcB64, jwsSigner, exp, email, organization);

        client.request(Url.REGISTER)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .header(Header.ENROLLMENT_CLIENT_ID_KEY, Header.ENROLLMENT_CLIENT_ID)
                .body(body)
                .post();
    }
}
