package se.tink.backend.connector.api;

import se.tink.backend.connector.exception.RequestException;

public interface CategorizationConnectorService {
    String categorize(String market, String transactionDescription) throws RequestException;
}
