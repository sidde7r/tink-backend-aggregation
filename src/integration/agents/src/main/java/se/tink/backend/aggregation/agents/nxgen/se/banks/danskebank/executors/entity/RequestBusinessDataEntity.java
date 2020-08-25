package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestBusinessDataEntity {
    private String accountNumber;
    private String countryCode;
    private MessageInformationEntity messageInformation;
}
