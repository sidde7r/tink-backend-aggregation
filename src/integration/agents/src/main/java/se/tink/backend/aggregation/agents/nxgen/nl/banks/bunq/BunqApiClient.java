package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import java.security.PublicKey;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionUserResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionUserResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BunqApiClient {

    private final BunqBaseApiClient baseApiClient;
    private final TinkHttpClient client;

    public BunqApiClient(TinkHttpClient client, String baseApiEndpoint) {
        this.baseApiClient = new BunqBaseApiClient(client, baseApiEndpoint);
        this.client = client;
    }

    public CreateSessionUserResponse createSessionUser(String apiKey) {
        CreateSessionUserResponseWrapper response =
                client.request(baseApiClient.getUrl(BunqBaseConstants.Url.CREATE_SESSION))
                        .post(
                                CreateSessionUserResponseWrapper.class,
                                CreateSessionRequest.createFromApiKey(apiKey));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize CreateSessionResponse"));
    }

    public AccountsResponseWrapper listAccounts(String userId) {
        return baseApiClient.listAccounts(userId);
    }

    public InstallResponse installation(PublicKey publicKey) {
        return baseApiClient.installation(publicKey);
    }

    public RegisterDeviceResponse registerDevice(String apiKey, String aggregatorIdentifier) {
        return baseApiClient.registerDevice(apiKey, aggregatorIdentifier);
    }
}
