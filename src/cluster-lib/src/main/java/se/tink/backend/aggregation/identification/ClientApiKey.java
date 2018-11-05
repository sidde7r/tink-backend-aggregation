package se.tink.backend.aggregation.cluster.identification;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.cluster.exception.ClientNotValid;

public class ClientApiKey {
    public static final String CLIENT_API_KEY_HEADER = "x-tink-client-api-key";
    private final String clientApiKey;

    private ClientApiKey(String clientApiKey) {
        this.clientApiKey = clientApiKey;
    }

    public String getClientId() {
        return clientApiKey;
    }

    public static ClientApiKey of(String clientApiKey) throws ClientNotValid {
        if (isValidId(clientApiKey)) {
            return new ClientApiKey(clientApiKey);
        }

        throw new ClientNotValid();
    }

    /*
        TODO: We should not allow empty clientApiKeys
        TODO: We should add more validity tests
    */
    private static boolean isValidId(String clientApiKey) {
        return true;
    }
}
