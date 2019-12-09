package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TagEntity {

    private String id;
    private String value;
    private String date;
    private UserEntity user;

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }

    public UserEntity getUser() {
        return user;
    }
}
