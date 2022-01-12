package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaBalanceEntity {

    private String name;

    @JsonProperty("balanceAmount")
    private ArkeaBalanceAmountEntity balanceAmountEntity;

    private String balanceType;
    private String lastCommittedTransaction;
}
