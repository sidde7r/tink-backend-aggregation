package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.UserInfoBusinessMessageBulk;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.UserInfoValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserInfoResponse {
    private UserInfoBusinessMessageBulk businessMessageBulk;
    private UserInfoValue value;

    public UserInfoBusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }

    public UserInfoValue getValue() {
        return value;
    }
}
