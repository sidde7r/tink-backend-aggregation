package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class EBankingDetailedMessage {

    private String ind;
    private String code;
    private String text;
}
