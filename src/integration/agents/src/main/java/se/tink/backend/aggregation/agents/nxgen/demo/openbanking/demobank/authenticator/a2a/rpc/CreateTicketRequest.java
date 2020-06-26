package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateTicketRequest {

    private String username;
    private String password;
    private String returnUri;

    public CreateTicketRequest(String username, String password, String returnUri) {
        this.username = username;
        this.password = password;
        this.returnUri = returnUri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getReturnUri() {
        return returnUri;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setReturnUri(String returnUri) {
        this.returnUri = returnUri;
    }
}
