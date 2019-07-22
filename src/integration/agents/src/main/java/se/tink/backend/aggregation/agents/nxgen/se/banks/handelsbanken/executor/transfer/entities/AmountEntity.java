package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
    private double amount;
    private String amountFormatted;
}
