package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
public class PayeeEntity {
    private String iban;
    private String name;

    public PayeeEntity(String iban, String name) {
        this.iban = iban;
        this.name = name;
    }
}
