package se.tink.backend.aggregation.agents.banks.sbab.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OtherCondominium {

    // Unique id to be able to separate property shares on different borrowers. Use the same id (for example 1) if the
    // same property is owned by the applicants.
    @JsonProperty("bostadsrattsId")
    private Integer condominiumId;

    // The monthly fee in SEK (required).
    @JsonProperty("manadsavgift")
    private Integer monthlyFee;

    // If the property is going to be sold (required).
    @JsonProperty("skaSaljas")
    private Boolean willBeSold;

    // The percentage of the property which the borrower owns, max 100 and min 1 (required).
    @JsonProperty("agarandel")
    private Integer percentageOfPropertyOwned;

    public Integer getCondominiumId() {
        return condominiumId;
    }

    public void setCondominiumId(Integer condominiumId) {
        this.condominiumId = condominiumId;
    }

    public Integer getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(Integer monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public Boolean isWillBeSold() {
        return willBeSold;
    }

    public void setWillBeSold(Boolean willBeSold) {
        this.willBeSold = willBeSold;
    }

    public Integer getPercentageOfPropertyOwned() {
        return percentageOfPropertyOwned;
    }

    public void setPercentageOfPropertyOwned(Integer percentageOfPropertyOwned) {
        this.percentageOfPropertyOwned = percentageOfPropertyOwned;
    }
    
    public static OtherCondominium createFromApplication(GenericApplicationFieldGroup group, int id) {
        OtherCondominium condo = new OtherCondominium();
        
        condo.setCondominiumId(id);
        condo.setMonthlyFee(group.tryGetFieldAsInteger(ApplicationFieldName.MONTHLY_COST).orElse(0));
        condo.setPercentageOfPropertyOwned(100); // TODO: This should be supplied by the application.
        condo.setWillBeSold(false);
        
        return condo;
    }
}
