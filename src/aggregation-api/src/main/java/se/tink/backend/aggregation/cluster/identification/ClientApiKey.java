package se.tink.backend.aggregation.cluster.identification;

public class ClientApiKey {
    public static final String CLIENT_API_KEY_HEADER = "X-Tink-Client-Api-Key";
    private final String clientApiKey;

    private ClientApiKey(String clientApiKey) {
        this.clientApiKey = clientApiKey;
    }

    public String getClientId() {
        return clientApiKey;
    }

    public static ClientApiKey of(String clientApiKey) {
        return new ClientApiKey(clientApiKey);
    }

    /*
        TODO: We should not allow empty clientApiKeys
        TODO: We should add more validity tests
    */
    private static boolean isValidId(String clientApiKey) {
        return true;
    }
}
