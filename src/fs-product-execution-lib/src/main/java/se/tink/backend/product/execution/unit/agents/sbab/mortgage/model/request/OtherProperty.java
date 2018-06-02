package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.GenericApplicationFieldGroup;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OtherProperty {

    // Unique id to be able to separate property shares on different borrowers. Use the same id (for example 1) if the
    // same property is owned by the applicants.
    @JsonProperty("fastighetsId")
    private Integer propertyId;

    // The operating cost of the property in SEK (not required).
    @JsonProperty("driftskostnad")
    private Integer operatingCost;

    // The assessed value of the property in SEK (not required).
    @JsonProperty("taxeringsvarde")
    private Integer assessedValue;

    // The percentage of the property which the borrower owns, max 100 and min 1 (required).
    @JsonProperty("agarandel")
    private Integer percentageOfPropertyOwned;

    // The code for the municipality to which the property belongs (required).
    @JsonProperty("kommun")
    private String municipalityCode;

    // The label unique to the property on the format <area>[<block>:]<unit> (required).
    @JsonProperty("beteckning")
    private String label;

    // If the property is going to be sold (required).
    @JsonProperty("skaSaljas")
    private Boolean willBeSold;

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public Integer getOperatingCost() {
        return operatingCost;
    }

    public void setOperatingCost(Integer operatingCost) {
        this.operatingCost = operatingCost;
    }

    public Integer getAssessedValue() {
        return assessedValue;
    }

    public void setAssessedValue(Integer assessedValue) {
        this.assessedValue = assessedValue;
    }

    public Integer getPercentageOfPropertyOwned() {
        return percentageOfPropertyOwned;
    }

    public void setPercentageOfPropertyOwned(Integer percentageOfPropertyOwned) {
        this.percentageOfPropertyOwned = percentageOfPropertyOwned;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean isWillBeSold() {
        return willBeSold;
    }

    public void setWillBeSold(Boolean willBeSold) {
        this.willBeSold = willBeSold;
    }

    public static OtherProperty createFromApplication(GenericApplicationFieldGroup group, int id) {
        OtherProperty property = new OtherProperty();

        property.setAssessedValue(group.tryGetFieldAsInteger(ApplicationFieldName.ASSESSED_VALUE).orElse(0));
        property.setLabel(group.tryGetField(ApplicationFieldName.HOUSE_LABEL).orElse("Unknown"));
        property.setMunicipalityCode(group.tryGetField(ApplicationFieldName.MUNICIPALITY).orElse("0"));
        property.setOperatingCost(group.tryGetFieldAsInteger(ApplicationFieldName.OPERATING_COST).orElse(0));
        property.setPercentageOfPropertyOwned(100); // TODO: (New feature) This should be supplied by the application.
        property.setPropertyId(id);
        property.setWillBeSold(false);

        return property;
    }
}
