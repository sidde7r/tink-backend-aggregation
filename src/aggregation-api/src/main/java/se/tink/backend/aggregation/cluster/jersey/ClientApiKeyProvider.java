package se.tink.backend.aggregation.cluster.jersey;

import se.tink.backend.aggregation.cluster.exceptions.ClientNotValid;
import se.tink.backend.aggregation.cluster.identification.ClientApiKey;

public class ClientApiKeyProvider {
    public ClientApiKey getClientApiKey(String clientApiKeyValue) throws ClientNotValid {
        ClientApiKey clientApiKey = ClientApiKey.of(clientApiKeyValue);
        return clientApiKey;
    }
}
