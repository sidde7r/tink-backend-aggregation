package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class RequestBusinessDataEntity {
    private String accountNumber;
    private String countryCode;
    private MessageInformationEntity messageInformation;
}
