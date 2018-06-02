package se.tink.backend.system.product.mortgage;

import java.util.Optional;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.property.PropertyType;

public class MortgageParameters {
    private Integer marketValue;
    private Integer mortgageAmount;
    private Integer numberOfApplicants;
    private PropertyType propertyType;

    public MortgageParameters() {

    }

    public MortgageParameters(
            PropertyType propertyType,
            Integer marketValue,
            Integer mortgageAmount,
            Integer numberOfApplicants) {
        this.marketValue = marketValue;
        this.mortgageAmount = mortgageAmount;
        this.numberOfApplicants = numberOfApplicants;
        this.propertyType = propertyType;
    }

    public Optional<Integer> getMarketValue() {
        return Optional.ofNullable(marketValue);
    }

    public Optional<Integer> getMortgageAmount() {
        return Optional.ofNullable(mortgageAmount);
    }

    public Optional<Integer> getNumberOfApplicants() {
        return Optional.ofNullable(numberOfApplicants);
    }

    public Optional<PropertyType> getPropertyType() {
        return Optional.ofNullable(propertyType);
    }

    public Optional<String> getPropertyTypeFieldValue() {
        Optional<PropertyType> propertyType = getPropertyType();

        if (!propertyType.isPresent()) {
            return Optional.empty();
        }

        switch (propertyType.get()) {
        case HOUSE:
            return Optional.of(ApplicationFieldOptionValues.HOUSE);
        case APARTMENT:
            return Optional.of(ApplicationFieldOptionValues.APARTMENT);
        case VACATION_HOUSE:
            return Optional.of(ApplicationFieldOptionValues.VACATION_HOUSE);
        }

        return Optional.empty();
    }

    public void setMarketValue(Integer marketValue) {
        this.marketValue = marketValue;
    }

    public void setMortgageAmount(Integer mortgageAmount) {
        this.mortgageAmount = mortgageAmount;
    }

    public void setNumberOfApplicants(Integer numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public boolean hasAllParameters() {
        return marketValue != null &&
                mortgageAmount != null &&
                numberOfApplicants != null &&
                propertyType != null;
    }
}
