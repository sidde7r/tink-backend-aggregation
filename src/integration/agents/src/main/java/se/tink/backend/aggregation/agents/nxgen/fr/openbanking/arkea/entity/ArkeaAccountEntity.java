package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.agents.common.types.CashAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaAccountEntity {

    private String resourceId;
    private String bicFi;
    private ArkeaAccountIdEntity accountId;
    private String name;
    private String usage;
    private CashAccountType cashAccountType;
    private String psuStatus;

    @JsonProperty("_links")
    private ArkeaAccountLinksEntity links;
}
