package se.tink.backend.aggregation.register.fi.opbank;

import com.google.common.net.HttpHeaders;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import java.security.Security;
import javax.ws.rs.core.MediaType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterConstants.Option;
import se.tink.backend.aggregation.register.fi.opbank.utils.PSD2Utils;

public class OPBankRegisterCommand {

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
                    "bazel run //src/commands/psd2-register/src/main/java/se/tink/backend/aggregation/register/fi/opbank --",
                    options);
            System.exit(1);
        }

        final String clusterId = cmd.getOptionValue(Option.CLUSTER_ID);
        final String appId = cmd.getOptionValue(Option.APP_ID);

        final TinkHttpClient client = new LegacyTinkHttpClient();
        Security.addProvider(BouncyCastleProviderSingleton.getInstance());

        client.setDebugOutput(true);

        client.setEidasProxy(PSD2Utils.eidasProxyConf);

        final String signJwt = PSD2Utils.generateSignedSSAJwt(clusterId, appId);

        client.request(OPBankRegisterConstants.Url.TPP_REGISTER)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, "application/jwt")
                .body(signJwt)
                .post();
    }

    private static Options createOptions() {
        final org.apache.commons.cli.Option certificateIdOption =
                org.apache.commons.cli.Option.builder(Option.CERTIFICATE_ID)
                        .longOpt("certificate_id")
                        .required()
                        .hasArg()
                        .argName("Certificate ID")
                        .desc(
                                "An identifier for the QSealC key which the eIDAS proxy uses to"
                                        + " generate a signature.")
                        .build();

        final org.apache.commons.cli.Option clusterIdOption =
                org.apache.commons.cli.Option.builder(Option.CLUSTER_ID)
                        .longOpt("cluster_id")
                        .required()
                        .hasArg()
                        .argName("Cluster ID")
                        .desc("An identifier for the cluster the proxy will use for signing")
                        .build();

        final org.apache.commons.cli.Option appIdOption =
                org.apache.commons.cli.Option.builder(Option.APP_ID)
                        .longOpt("app_id")
                        .required()
                        .hasArg()
                        .argName("App ID")
                        .desc("An identifier for the app the proxy will use for signing")
                        .build();

        final Options options = new Options();

        options.addOption(certificateIdOption);
        options.addOption(clusterIdOption);
        options.addOption(appIdOption);

        return options;
    }
}
