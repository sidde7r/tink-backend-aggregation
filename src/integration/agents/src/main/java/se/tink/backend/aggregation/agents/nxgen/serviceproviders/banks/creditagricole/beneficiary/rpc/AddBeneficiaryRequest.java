package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddBeneficiaryRequest {
    private String label;
    private String bic;
    private String iban;
    private String isNew;

    public AddBeneficiaryRequest(String label, String iban, String bic) {
        this.label = label;
        this.bic = bic;
        this.iban = iban;
        this.isNew = "true";
    }
}

