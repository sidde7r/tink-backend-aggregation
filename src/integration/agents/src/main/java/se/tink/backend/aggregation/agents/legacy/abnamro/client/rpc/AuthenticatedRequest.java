package se.tink.backend.aggregation.agents.abnamro.client.rpc;

public interface AuthenticatedRequest {

    String getBcNumber();

    String getSessionToken();

    void setBcNumber(String bcNumber);

    void setSessionToken(String sessionToken);
}
