package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbanValidationRequest {
    private String iban;

    public IbanValidationRequest(String iban) {
        this.iban = iban;
    }
}
