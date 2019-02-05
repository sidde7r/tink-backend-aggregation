package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;

public class AuthenticationRequest {

    private String grant_type = "password";
    private String password;
    private String username;

    public AuthenticationRequest(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getBody() throws UnsupportedEncodingException {
        return new StringBuilder("grant_type=")
                .append(grant_type)
                .append(N26Constants.Body.PASSWORD)
                .append(URLEncoder.encode(password, "UTF-8"))
                .append(N26Constants.Body.USERNAME)
                .append(URLEncoder.encode(username, "UTF-8")).toString();
    }
}
