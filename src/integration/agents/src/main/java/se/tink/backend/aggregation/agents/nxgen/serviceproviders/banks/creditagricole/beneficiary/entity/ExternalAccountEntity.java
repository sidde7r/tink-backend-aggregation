package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExternalAccountEntity {
    private String iban;
    private String bic;
    private String country;
    private boolean isEditableBIC;

    public String getBic() {
        return bic;
    }
}
