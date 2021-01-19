package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class IdDetailsEntity {
    private String identification;
    private String issuer;
    private String schemeName;
    private String cardNumber;
}
