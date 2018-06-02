package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.authentication.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.SessionEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FinishAuthenticationResponse {
    private UserEntity user;
    private SessionEntity session;
    private ServerStatusResponse serverStatus;
    private String m2;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public SessionEntity getSession() {
        return session;
    }

    public void setSession(SessionEntity session) {
        this.session = session;
    }

    public ServerStatusResponse getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(
            ServerStatusResponse serverStatus) {
        this.serverStatus = serverStatus;
    }

    public String getM2() {
        return m2;
    }

    public void setM2(String m2) {
        this.m2 = m2;
    }
}
