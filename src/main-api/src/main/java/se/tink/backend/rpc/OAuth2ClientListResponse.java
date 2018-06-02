package se.tink.backend.rpc;

import java.util.List;
import se.tink.backend.core.oauth2.OAuth2Client;

public class OAuth2ClientListResponse {
    private List<OAuth2Client> clients;

    public List<OAuth2Client> getClients() {
        return clients;
    }

    public void setClients(List<OAuth2Client> clients) {
        this.clients = clients;
    }
}
