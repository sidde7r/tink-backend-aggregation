package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ButtonsEntity {

    private ActionEntity action;
    private String formId;
    private String text;
    private String type;
}
