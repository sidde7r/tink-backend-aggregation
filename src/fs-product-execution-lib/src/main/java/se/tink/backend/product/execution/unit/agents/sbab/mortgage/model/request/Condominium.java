package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.GenericApplicationFieldGroup;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Condominium {
    // The name of the homeowners association (required).
    @JsonProperty("foreningsnamn")
    private String associationName;

    // The number of the condominium in the property (required).
    @JsonProperty("lagenhetsnr")
    private String condominiumNumber;

    // The number of rooms (required).
    @JsonProperty("antalRum")
    private Integer numberOfRooms;

    // The living space in m2 (required).
    @JsonProperty("boyta")
    private Integer livingSpace;

    // The corporate identity number of the homeowners association on the format 'nnnnnn-nnnn' (not required).
    @JsonProperty("foreningsOrgnr")
    private String associationIdentityNumber;

    // The address (required).
    @JsonProperty("adress")
    private Address address;

    // The code for the municipality to which the property belongs (required).
    @JsonProperty("kommun")
    private Integer municipalityCode;

    // The purchase price in SEK (required).
    @JsonProperty("kopeskilling")
    private Integer purchasePrice;

    // The monthly fee in SEK (required).
    @JsonProperty("manadsavgift")
    private Integer monthlyFee;

    public String getAssociationName() {
        return associationName;
    }

    public void setAssociationName(String associationName) {
        this.associationName = associationName;
    }

    public String getCondominiumNumber() {
        return condominiumNumber;
    }

    public void setCondominiumNumber(String condominiumNumber) {
        this.condominiumNumber = condominiumNumber;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public Integer getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(Integer livingSpace) {
        this.livingSpace = livingSpace;
    }

    public String getAssociationIdentityNumber() {
        return associationIdentityNumber;
    }

    public void setAssociationIdentityNumber(String associationIdentityNumber) {
        this.associationIdentityNumber = associationIdentityNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Integer getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(Integer municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public Integer getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Integer purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(Integer monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public static Condominium createFromApplication(GenericApplicationFieldGroup group) {
        Address address = new Address();

        Optional<String> postalCode = group.tryGetField(ApplicationFieldName.POSTAL_CODE);
        Preconditions.checkState(postalCode.isPresent(), "Missing postal code.");
        address.setPostalCode(postalCode.get());

        Optional<String> streetAddress = group.tryGetField(ApplicationFieldName.STREET_ADDRESS);
        Preconditions.checkState(streetAddress.isPresent(), "Missing town.");
        address.setStreetAddress(streetAddress.get());

        Optional<String> town = group.tryGetField(ApplicationFieldName.TOWN);
        Preconditions.checkState(town.isPresent(), "Missing town.");
        address.setPostTown(town.get());

        Condominium condo = new Condominium();
        condo.setAddress(address);

        Optional<String> housingCommunityName = group.tryGetField(ApplicationFieldName.HOUSING_COMMUNITY_NAME);
        Preconditions.checkState(housingCommunityName.isPresent(), "Missing housing community name.");
        condo.setAssociationName(housingCommunityName.get());

        Optional<Integer> livingArea = group.tryGetFieldAsInteger(ApplicationFieldName.LIVING_AREA);
        Preconditions.checkState(livingArea.isPresent(), "Missing living area.");
        condo.setLivingSpace(livingArea.get());

        Optional<Integer> monthlyHousingCommunityFee = group.tryGetFieldAsInteger(ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE);
        Preconditions.checkState(monthlyHousingCommunityFee.isPresent(), "Missing monthly housing community fee.");
        condo.setMonthlyFee(monthlyHousingCommunityFee.get());

        Optional<Integer> numberOfRooms = group.tryGetFieldAsInteger(ApplicationFieldName.NUMBER_OF_ROOMS);
        Preconditions.checkState(numberOfRooms.isPresent(), "Missing number of rooms.");
        condo.setNumberOfRooms(numberOfRooms.get());

        Optional<Integer> municipalityCode = group.tryGetFieldAsInteger(ApplicationFieldName.MUNICIPALITY);
        Preconditions.checkState(municipalityCode.isPresent(), "Missing municipality.");
        condo.setMunicipalityCode(municipalityCode.get());

        Optional<Integer> estimatedMarketValue = group.tryGetFieldAsInteger(ApplicationFieldName.ESTIMATED_MARKET_VALUE);
        Preconditions.checkState(estimatedMarketValue.isPresent(), "Missing estimated market value.");
        condo.setPurchasePrice(estimatedMarketValue.get());

        // These fields are mandatory, but we don't have the values. Setting placeholder values to validate.
        condo.setAssociationIdentityNumber("111111-1111");
        condo.setCondominiumNumber("00");

        return condo;
    }
}
