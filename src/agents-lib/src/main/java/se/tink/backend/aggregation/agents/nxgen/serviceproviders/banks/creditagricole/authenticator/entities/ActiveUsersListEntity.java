package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActiveUsersListEntity {
    private String userId;
    private String partnerId;
    private String userEmail;
    private String lastAccessDate;

    public String getUserId() {
        return userId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getLastAccessDate() {
        return lastAccessDate;
    }
}
