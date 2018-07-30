package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.entities.ActiveUsersListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignInResponse extends DefaultResponse {
    @JsonProperty("emailPART")
    private String emailPart;
    private List<ActiveUsersListEntity> activeUsersList;

    public String getEmailPart() {
        return emailPart;
    }

    public List<ActiveUsersListEntity> getActiveUsersList() {
        return activeUsersList;
    }
}
