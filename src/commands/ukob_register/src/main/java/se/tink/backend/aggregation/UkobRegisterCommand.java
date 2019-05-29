package se.tink.backend.aggregation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Security;
import java.util.NoSuchElementException;
import java.util.Scanner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UkobRegisterCommand {

    private static final String OUTFILE_NAME = "registration_response_%d";
    private static final String DEFAULT_SS = "tink";

    public static void main(String args[]) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        if (!isValidInput(args)) {
            printUsageInstructions();
            return;
        }

        final UkOpenBankingConfiguration config =
                SerializationUtils.deserializeFromString(
                        readCredentialFile(args[0]), UkOpenBankingConfiguration.class);
        config.validate();
        final String wellKnown = args[1];

        final String ssName = args.length == 3 ? args[2] : DEFAULT_SS;
        final SoftwareStatement softwareStatement =
                config.getSoftwareStatement(ssName)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                String.format(
                                                        "Software Statement \"%s\" was not found in config.",
                                                        ssName)));

        TinkHttpClient httpClient = createHttpClient(config, softwareStatement);
        String res =
                OpenIdApiClient.registerClient(softwareStatement, new URL(wellKnown), httpClient);

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
        System.out.println("config      - Path to UkOpenBankingConfiguration in json format.");
        System.out.println("well-known  - URL to banks well-known configuration.");
        System.out.println("ss          - Name of software statement. Default = tink.\n");
    }

    private static TinkHttpClient createHttpClient(
            UkOpenBankingConfiguration config, SoftwareStatement softwareStatement) {

        TinkHttpClient httpClient = new TinkHttpClient();
        httpClient.disableSignatureRequestHeader();
        httpClient.trustRootCaCertificate(config.getRootCAData(), config.getRootCAPassword());

        // Softw. Transp. key
        httpClient.setSslClientCertificate(
                softwareStatement.getTransportKeyP12(),
                softwareStatement.getTransportKeyPassword());

        return httpClient;
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
        try {
            return new Scanner(file).useDelimiter("\\Z").next();
        } catch (NoSuchElementException e) {
            throw new IOException(String.format("File %s is empty.", file.getName()));
        }
    }
}
