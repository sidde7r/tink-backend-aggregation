package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TaxPartyEntity {
    @JsonProperty("taxIdentification")
    private String taxIdentification = null;

    @JsonProperty("registrationIdentification")
    private String registrationIdentification = null;

    @JsonProperty("taxType")
    private String taxType = null;

    @JsonProperty("authorisation")
    private TitleAndNameEntity authorisation = null;

    public String getTaxIdentification() {
        return taxIdentification;
    }

    public void setTaxIdentification(String taxIdentification) {
        this.taxIdentification = taxIdentification;
    }

    public String getRegistrationIdentification() {
        return registrationIdentification;
    }

    public void setRegistrationIdentification(String registrationIdentification) {
        this.registrationIdentification = registrationIdentification;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public TitleAndNameEntity getAuthorisation() {
        return authorisation;
    }

    public void setAuthorisation(TitleAndNameEntity authorisation) {
        this.authorisation = authorisation;
    }
}
