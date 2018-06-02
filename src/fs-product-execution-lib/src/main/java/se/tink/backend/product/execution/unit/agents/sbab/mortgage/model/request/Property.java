package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.GenericApplicationFieldGroup;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {
    // The label unique to the property on the format <area>[<block>:]<unit> (required).
    @JsonProperty("beteckning")
    private String label;

    // The living space in m2 (not required).
    @JsonProperty("boyta")
    private Integer livingSpace;

    // The operating cost of the property in SEK (not required).
    @JsonProperty("driftskostnad")
    private Integer operatingCost;

    // The assessed value of the property in SEK/month (not required).
    @JsonProperty("taxeringsvarde")
    private Integer assessedValue;

    // The code for the municipality to which the property belongs (required).
    @JsonProperty("kommun")
    private Integer municipalityCode;

    // The type of the property (required).
    @JsonProperty("typ")
    private PropertyType type;

    // The purchase price in SEK (required).
    @JsonProperty("kopeskilling")
    private Integer purchasePrice;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(Integer livingSpace) {
        this.livingSpace = livingSpace;
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

    public Integer getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(Integer municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getType() {
        return type.toString();
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public Integer getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Integer purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public static Property createFromApplication(GenericApplicationFieldGroup group) {
        Property property = new Property();

        // Cadastral required only for houses
        // FIXME: When SBAB allows sending null in cadastral field in their API, remove default "Abc 1:2".
        Optional<String> cadastral = group.tryGetField(ApplicationFieldName.CADASTRAL);
        property.setLabel(cadastral.orElse("Abc 1:2"));

        // Required properties.
        Optional<Integer> municipalityCode = group.tryGetFieldAsInteger(ApplicationFieldName.MUNICIPALITY);
        Preconditions.checkState(municipalityCode.isPresent(), "Missing municipality.");
        property.setMunicipalityCode(municipalityCode.get());

        Optional<PropertyType> propertyType = MortgageApplicationMapper
                .getPropertyType(group.getField(ApplicationFieldName.PROPERTY_TYPE));
        Preconditions.checkState(propertyType.isPresent(), "Missing property type.");
        property.setType(propertyType.get());

        // SBAB calculates the debt ratio based on the purchase price field, which causes issues if the residence
        // has accrued a lot of value since it was bought. To mitigate that, we submit the estimated market value in
        // the purchase price field, and attach the purchase price as a comment instead.
        Optional<Integer> estimatedMarketValue = group
                .tryGetFieldAsInteger(ApplicationFieldName.ESTIMATED_MARKET_VALUE);
        Preconditions.checkState(estimatedMarketValue.isPresent(), "Missing estimated market value.");
        property.setPurchasePrice(estimatedMarketValue.get());

        // Optional properties.
        property.setAssessedValue(group.getFieldAsInteger(ApplicationFieldName.ASSESSED_VALUE));
        property.setLivingSpace(group.getFieldAsInteger(ApplicationFieldName.LIVING_AREA));
        property.setOperatingCost(group.getFieldAsInteger(ApplicationFieldName.OPERATING_COST));

        return property;
    }
}
