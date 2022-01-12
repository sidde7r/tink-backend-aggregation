package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaAccountIdEntity {

    private String iban;
}
