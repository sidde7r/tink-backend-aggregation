package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WhereEntity {

    private Double latitude;
    private Double longitude;
    private String date;
    private UserEntity user;

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getDate() {
        return date;
    }

    public UserEntity getUser() {
        return user;
    }
}
