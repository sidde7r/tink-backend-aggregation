package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessageInformationEntity {
    private String correlationId;
    private String created;
    private int retryCount;
}
