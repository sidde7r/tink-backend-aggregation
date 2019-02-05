package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MainLocalParticipantEntity {
    private String phone;
    private String localParticipantCode;
    private String name;
    private String uri;

    public String getPhone() {
        return phone;
    }

    public String getLocalParticipantCode() {
        return localParticipantCode;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }
}
