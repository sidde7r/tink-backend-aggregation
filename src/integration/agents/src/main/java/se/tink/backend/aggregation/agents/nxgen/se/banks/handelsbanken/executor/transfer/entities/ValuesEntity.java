package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValuesEntity {
    private String accessibility;
    private String text;
    private String type;
    private AmountEntity amount;
    private boolean isPrimary;
}
