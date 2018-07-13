package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication;

import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.authentication.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.SessionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinishAuthenticationResponse {
    private UserEntity user;
    private SessionEntity session;
    private ServerStatusResponse serverStatus;
    private String m2;

    public UserEntity getUser() {
        return user;
    }

    public SessionEntity getSession() {
        return session;
    }

    public ServerStatusResponse getServerStatus() {
        return serverStatus;
    }

    public String getM2() {
        return m2;
    }
}
