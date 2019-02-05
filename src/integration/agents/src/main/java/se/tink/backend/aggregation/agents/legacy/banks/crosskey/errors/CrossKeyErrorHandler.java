package se.tink.backend.aggregation.agents.banks.crosskey.errors;

public interface CrossKeyErrorHandler {
    void handleError(String code) throws Exception;
}
