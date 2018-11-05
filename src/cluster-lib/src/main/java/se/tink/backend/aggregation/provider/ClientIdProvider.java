package se.tink.backend.aggregation.cluster.provider;

import se.tink.backend.aggregation.cluster.exception.ClientNotValid;
import se.tink.backend.aggregation.cluster.identification.ClientApiKey;

public class ClientIdProvider {
    public ClientApiKey getClientApiKey(String clientApiKeyValue) throws ClientNotValid {
        ClientApiKey clientApiKey = ClientApiKey.of(clientApiKeyValue);
        return clientApiKey;
    }
}
