package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class PayeesEntity {
    private String accountNo;
    private String name;
    private String payeeKey;
    private String regNo;
    private String textToReceiver;
    private String textToSender;
}
