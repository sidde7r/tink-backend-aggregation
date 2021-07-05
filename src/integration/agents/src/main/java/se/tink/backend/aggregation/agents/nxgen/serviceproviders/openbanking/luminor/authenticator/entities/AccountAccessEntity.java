package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities;

import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountAccessEntity {

    @Setter String iban;
}
