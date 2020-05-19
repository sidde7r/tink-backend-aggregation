package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@Getter
@JsonObject
public class DebtorAccountEntity {

    private String iban;
}
