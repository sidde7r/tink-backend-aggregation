package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionUserResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionUserResponseWrapper;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BunqApiClient extends BunqBaseApiClient {

    public BunqApiClient(TinkHttpClient client, String baseApiEndpoint) {
        super(client, baseApiEndpoint);
    }

    public CreateSessionUserResponse createSessionUser(String apiKey) {
        CreateSessionUserResponseWrapper response =
                client.request(getUrl(BunqBaseConstants.Url.CREATE_SESSION))
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
}
