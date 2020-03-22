package se.tink.backend.aggregation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Security;
import java.util.NoSuchElementException;
import java.util.Scanner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UkobRegisterCommand {

    private static final String OUTFILE_NAME = "registration_response_%d";
    private static final String DEFAULT_SS = "tink";

    /**
     * Some banks are using self-signed certificates in their /register endpoints. To be able to
     * work with such endpoints, currently we disabled SSL verification. After those banks will fix
     * this problem, we will enable it again
     */
    private static final boolean DISABLE_SSL_VERIFICATION = true;

    public static void main(String args[]) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        if (!isValidInput(args)) {
            printUsageInstructions();
            return;
        }

        final UkobRegisterConfiguration config =
                SerializationUtils.deserializeFromString(
                        readCredentialFile(args[0]), UkobRegisterConfiguration.class);

        final String wellKnown = args[1];

        TinkHttpClient httpClient = createHttpClient(config);
        String res =
                OpenIdApiClient.registerClient(
                        config.getSoftwareStatement(),
                        new URL(wellKnown),
                        httpClient,
                        config.getSignerOverride()
                                .orElseThrow(
                                        () ->
                                                new UnsupportedOperationException(
                                                        "Default signer not implemented for ukob register.")));

        System.out.println("\n### RESPONSE ###\n\n" + res + "\n\n################\n");
        String outFile = saveResponse(res);
        System.out.println(String.format("Done! \nResponse saved to: %s", outFile));
    }

    private static boolean isValidInput(String args[]) {

        if (args.length < 2) {
            return false;
        }

        try {
            URL url = new URL(args[1]);
            url.toUri();
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    private static void printUsageInstructions() {
        System.out.println(
                "usage: bazel run :ukob-register [config] [well-known] [ss (optional)]\n");
        System.out.println("config      - Path to configuration in JSON format.");
        System.out.println("well-known  - URL to banks well-known configuration.");
    }

    private static TinkHttpClient createHttpClient(UkobRegisterConfiguration configuration) {

        TinkHttpClient httpClient = new LegacyTinkHttpClient();
        httpClient.disableSignatureRequestHeader();

        if (DISABLE_SSL_VERIFICATION) {
            httpClient.disableSslVerification();
        } else {
            httpClient.trustRootCaCertificate(
                    configuration.getRootCAData(), configuration.getRootCAPassword());
        }

        configuration
                .getTlsConfigurationOverride()
                .orElse(UkobRegisterCommand::useEidasProxy)
                .applyConfiguration(httpClient);

        return httpClient;
    }

    private static void useEidasProxy(final TinkHttpClient client) {
        throw new UnsupportedOperationException(
                "Ukob registration command does not support default tls configuration yet.");
    }

    private static String saveResponse(String response) throws IOException {
        File folder = new File(System.getProperty("user.home") + "/UkobRegistrations");

        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new RuntimeException(
                        String.format(
                                "No output folder %s exists and none could be created.",
                                folder.getPath()));
            }
        }

        // Find free filename to prevent deleting old registration response
        final String fullPath = folder.getPath() + String.format("/%s.json", OUTFILE_NAME);
        int fileNum = 0;
        File file;
        do {
            file = new File(String.format(fullPath, fileNum));
            fileNum++;
        } while (file.exists());

        if (!file.createNewFile()) {
            throw new RuntimeException(
                    String.format(
                            "No output file %s exists and none could be created.", file.getPath()));
        }

        PrintWriter out = new PrintWriter(file);
        out.write(response);
        out.close();
        return file.getPath();
    }

    private static String readCredentialFile(String path) throws IOException {
        File file = new File(path);

        if (file.canRead()) {
            return readWholeFile(file);
        }
        throw new IOException(String.format("Credential file path invalid: %s", path));
    }

    private static String readWholeFile(File file) throws IOException {
        try(Scanner scanner = new Scanner(file)) {
            return scanner.useDelimiter("\\Z").next();
        } catch (NoSuchElementException e) {
            throw new IOException(String.format("File %s is empty.", file.getName()));
        }
    }
}
