package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class MainLocalParticipantEntity {
    private String phone;
    private String localParticipantCode;
    private String name;
    private String uri;

    @JsonIgnore
    public boolean isHandelsbankenAccount() {
        return "HANDELSBANKEN".equalsIgnoreCase(name);
    }
}
