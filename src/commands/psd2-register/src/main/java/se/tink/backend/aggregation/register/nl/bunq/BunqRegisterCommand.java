package se.tink.backend.aggregation.register.nl.bunq;

import static se.tink.backend.aggregation.register.RegisterEnvironment.fromString;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.filter.BunqRequiredHeadersFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.filter.BunqSignatureHeaderFilter;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.backend.aggregation.register.RegisterEnvironment;
import se.tink.backend.aggregation.register.nl.bunq.BunqRegisterConstants.Mappers;
import se.tink.backend.aggregation.register.nl.bunq.environment.local.BunqRegisterLocalUtils;
import se.tink.backend.aggregation.register.nl.bunq.rpc.AddOAuthClientIdResponse;
import se.tink.backend.aggregation.register.nl.bunq.rpc.GetClientIdAndSecretResponse;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderRequest;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BunqRegisterCommand {

    private static final String OUTFILE_NAME = "bunq_registration_response_%d";

    private static BunqRegistrationResponse bunqRegistrationResponse =
            new BunqRegistrationResponse();
    private static TemporaryStorage temporaryStorage = new TemporaryStorage();

    public static void main(String args[]) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Options options = new Options();
        Option environmentOption =
                Option.builder("e")
                        .longOpt("environment")
                        .required()
                        .hasArg()
                        .desc(
                                "Environment in which you want to register. Currently supported: LOCAL, STAGING, PRODUCTION")
                        .build();
        options.addOption(environmentOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("bazel run //src/commands/psd2-register:bunq_register --", options);
            System.exit(1);
        }

        RegisterEnvironment selectedEnvironment = fromString(cmd.getOptionValue("e").toLowerCase());

        BunqRegisterCommandApiClient apiClient = createApiClient(selectedEnvironment);

        CreateSessionPSD2ProviderResponse psd2Session =
                registerAsPSD2ServiceProvider(apiClient, selectedEnvironment);
        String psd2UserId = String.valueOf(psd2Session.getUserPaymentServiceProvider().getId());
        registerOAuthCallback(apiClient, psd2UserId, selectedEnvironment);
        cleanupSession(apiClient, psd2Session);

        System.out.println(
                "\n### RESPONSE ###\n\n"
                        + bunqRegistrationResponse.toString()
                        + "\n\n################\n");
        String outFile = saveResponse(bunqRegistrationResponse.toString());
        System.out.println(String.format("Done! \nRegistration response saved to: %s", outFile));
    }

    private static BunqRegisterCommandApiClient createApiClient(
            RegisterEnvironment selectedEnvironment) {
        TinkHttpClient client = new TinkHttpClient();

        client.addFilter(new BunqRequiredHeadersFilter(temporaryStorage));
        client.addFilter(new BunqSignatureHeaderFilter(temporaryStorage, client.getUserAgent()));

        String baseApiEndpoint =
                BunqRegisterConstants.Mappers.environmentOptionToApiEndpointMapper
                        .translate(selectedEnvironment)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Selected environment is not valid, choose production, staging or local"));

        return new BunqRegisterCommandApiClient(client, baseApiEndpoint);
    }

    private static RegisterAsPSD2ProviderRequest getRegistrationPSD2RegisterRequestForEnvironment(
            RegisterEnvironment selectedEnvironment,
            PublicKey installationPublicKey,
            String psd2ClientAuthToken) {
        switch (selectedEnvironment) {
            case PRODUCTION:
                throw new NotImplementedException(
                        "Registration towards Bunq's for our production environment is not yet implemented.");

            case STAGING:
                throw new NotImplementedException(
                        "Registration towards Bunq's for our staging environment is not yet implemented.");

            case LOCAL:
                return new RegisterAsPSD2ProviderRequest(
                        BunqRegisterLocalUtils.getQSealCCertificateAsString(),
                        BunqRegisterLocalUtils.getPaymentServiceProviderCertificateChainAsString(),
                        BunqRegisterLocalUtils.getClientPublicKeySignatureAsString(
                                installationPublicKey, psd2ClientAuthToken));

            case UNKNOWN:
            default:
                throw new IllegalArgumentException(
                        "Selected environment is not valid, choose production, staging or local");
        }
    }

    // Bunq uses an endpoint to register a PSD2 provider, instead of the usual developer portal used
    // by other banks, this only needs to be run once per QSealC certificate used to obtain the PSD2
    // API key that can then be saved as a secret and used to create future session/register
    // devices.
    private static CreateSessionPSD2ProviderResponse registerAsPSD2ServiceProvider(
            BunqRegisterCommandApiClient apiClient, RegisterEnvironment selectedEnvironment) {
        KeyPair keyPair = RSA.generateKeyPair(2048);
        bunqRegistrationResponse.setPsd2InstallationKeyPair(
                SerializationUtils.serializeKeyPair(keyPair));

        // Execute POST v1/installation and get your installation Token with a unique random key
        // pair.
        InstallResponse installationResponse = apiClient.installation(keyPair.getPublic());

        // This token is used in one of the required headers. This must be set before the next
        // request is done.
        TokenEntity psd2ClientAuthToken = installationResponse.getToken();
        bunqRegistrationResponse.setPsd2ClientAuthToken(
                SerializationUtils.serializeToString(psd2ClientAuthToken));
        // Needed for the BunqSignatureHeaderFilter
        temporaryStorage.put(StorageKeys.CLIENT_AUTH_TOKEN, psd2ClientAuthToken);
        temporaryStorage.put(
                psd2ClientAuthToken.getToken(), SerializationUtils.serializeKeyPair(keyPair));

        // Use the installation Token and your unique PSD2 certificate to call POST
        // v1/payment-service-provider-credential. This will register your software.
        RegisterAsPSD2ProviderRequest registerAsPSD2ProviderRequest =
                getRegistrationPSD2RegisterRequestForEnvironment(
                        selectedEnvironment, keyPair.getPublic(), psd2ClientAuthToken.getToken());
        RegisterAsPSD2ProviderResponse registerSoftwareResponse =
                apiClient.registerAsPSD2Provider(registerAsPSD2ProviderRequest);

        String psd2ApiKey = registerSoftwareResponse.getToken();
        bunqRegistrationResponse.setPsd2ApiKey(psd2ApiKey);

        // Register a device by using POST v1/device-server using the API key for the secret and
        // passing the installation Token in the X-Bunq-Client-Authentication header.
        apiClient.registerDevice(psd2ApiKey, null);

        // Create your session by executing POST v1/session-server. Provide the installation
        // Token in the X-Bunq-Client-Authentication header. You will receive a session Token. Use
        // it in any following request in the X-Bunq-Client-Authentication header.
        CreateSessionPSD2ProviderResponse createSessionPSD2ProviderResponse =
                apiClient.createSessionPSD2Provider(psd2ApiKey);
        TokenEntity psd2ClientAuthTokenAfterSession = createSessionPSD2ProviderResponse.getToken();
        temporaryStorage.put(StorageKeys.CLIENT_AUTH_TOKEN, psd2ClientAuthTokenAfterSession);
        temporaryStorage.put(
                psd2ClientAuthTokenAfterSession.getToken(),
                SerializationUtils.serializeKeyPair(keyPair));

        return createSessionPSD2ProviderResponse;
    }

    private static void registerOAuthCallback(
            BunqRegisterCommandApiClient apiClient,
            String psd2UserId,
            RegisterEnvironment selectedEnvironment) {
        // Call POST /v1/user/{userID}/oauth-client
        AddOAuthClientIdResponse addOAuthClientIdResponse = apiClient.addOAuthClientId(psd2UserId);
        String oauthClientId = String.valueOf(addOAuthClientIdResponse.getId().getId());

        // Call GET /v1/user/{userID}/oauth-client/{oauth-clientID}. We will return your Client ID
        // and Client Secret.
        GetClientIdAndSecretResponse getClientIdAndSecretResponse =
                apiClient.getClientIdAndSecret(psd2UserId, oauthClientId);
        bunqRegistrationResponse.setClientId(
                getClientIdAndSecretResponse.getOauthClient().getClientId());
        bunqRegistrationResponse.setClientSecret(
                getClientIdAndSecretResponse.getOauthClient().getClientSecret());

        // Call POST /v1/user/{userID}/oauth-client/{oauth-clientID}/callback-url. Include the
        // OAuth callback URL of your application.
        String redirectUrl =
                Mappers.environmentToRedirectUrlMapper
                        .translate(selectedEnvironment)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Selected environment is not valid, choose production, staging or local"));
        apiClient.registerCallbackUrl(psd2UserId, oauthClientId, redirectUrl);
        bunqRegistrationResponse.setRedirectUrl(redirectUrl);
    }

    private static void cleanupSession(
            BunqRegisterCommandApiClient apiClient, CreateSessionPSD2ProviderResponse psd2Session) {

        apiClient.deleteSession(String.valueOf(psd2Session.getId().getId()));
    }

    private static String saveResponse(String response) throws IOException {
        File folder = new File(System.getProperty("user.home") + "/BunqRegistration");

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
}
