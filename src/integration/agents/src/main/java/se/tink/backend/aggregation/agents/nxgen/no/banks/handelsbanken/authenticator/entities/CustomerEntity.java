package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CustomerEntity {
    private String profitCenter;
    private String segment;
    private String brand;
}
