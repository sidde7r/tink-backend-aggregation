package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateTicketRequest {

    private String username;
    private String password;
    private String returnUri;
    private String state;

    public CreateTicketRequest(String username, String password, String returnUri, String state) {
        this.username = username;
        this.password = password;
        this.returnUri = returnUri;
        this.state = state;
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

    public String getState() {
        return state;
    }
}
