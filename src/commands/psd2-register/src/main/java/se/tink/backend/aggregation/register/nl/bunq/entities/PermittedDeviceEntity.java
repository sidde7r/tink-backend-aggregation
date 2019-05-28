package se.tink.backend.aggregation.register.nl.bunq.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PermittedDeviceEntity {
    private String description;
    private String ip;
}
