package se.tink.backend.aggregation.agents.contexts;

public interface CorrelationIdentifierContext {

    String getCorrelationId();

    void setCorrelationId(String correlationId);
}
