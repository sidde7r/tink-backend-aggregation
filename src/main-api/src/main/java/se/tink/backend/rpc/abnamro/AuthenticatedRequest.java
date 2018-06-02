package se.tink.backend.rpc.abnamro;

public interface AuthenticatedRequest {

    String getBcNumber();

    String getSessionToken();
    
    void setBcNumber(String bcNumber);
    
    void setSessionToken(String sessionToken);
}
