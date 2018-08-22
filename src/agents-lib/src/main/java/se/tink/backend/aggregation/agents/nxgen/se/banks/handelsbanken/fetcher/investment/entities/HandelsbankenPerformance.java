package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HandelsbankenPerformance {

    private HandelsbankenAmount changeAmount;

    public Optional<Double> asDouble() {
        return Optional.ofNullable(changeAmount).map(HandelsbankenAmount::asDouble);
    }
}
