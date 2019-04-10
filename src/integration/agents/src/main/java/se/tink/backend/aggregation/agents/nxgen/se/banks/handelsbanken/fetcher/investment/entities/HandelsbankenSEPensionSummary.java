package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HandelsbankenSEPensionSummary {

    List<HandelsbankenSEProperty> items;

    public Optional<Double> toPaymentsMade() {
        return Optional.ofNullable(items)
                .flatMap(
                        properties ->
                                properties.stream()
                                        .filter(HandelsbankenSEProperty::isPayment)
                                        .findFirst())
                .map(HandelsbankenSEProperty::asDouble);
    }
}
