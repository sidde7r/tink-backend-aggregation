package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class CreditorAccountDto {

    private String iban;

    private String other;
}
