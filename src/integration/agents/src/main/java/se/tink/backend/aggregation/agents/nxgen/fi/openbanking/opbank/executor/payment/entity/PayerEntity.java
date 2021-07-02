package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PayerEntity {
    private String iban;

    public PayerEntity(String iban) {
        this.iban = iban;
    }
}
