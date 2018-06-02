package se.tink.backend.common.providers.booli.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Residence {
    private Double additionalAndLivingArea;
    private Double additionalArea;
    private String apartmentNumber;
    private Boolean balcony;
    private Integer bathroomCondition;
    private Boolean buildingHasElevator;
    private Boolean canParkCar;
    private Double ceilingHeight;
    private String constructionEra;
    private Integer constructionYear;
    private String fireplace;
    private Integer floor;
    private Boolean hasBasement;
    private Integer kitchenCondition;
    private Double knowledge;
    private String lastGroundDrainage;
    private String lastRoofRenovation;
    private Double latitude;
    private Integer listPrice;
    private Double livingArea;
    private Double longitude;
    private String objectType;
    private Double operatingCost;
    private Double operatingCostPerSqm;
    private String patio;
    @JsonProperty("plot_area")
    private Double plotArea;
    private Double rent;
    private Double rentPerSqm;
    private Integer rooms;
    private String streetAddress;
    private List<Integer> valueScore;

    public Double getAdditionalAndLivingArea() {
        return additionalAndLivingArea;
    }

    public void setAdditionalAndLivingArea(Double additionalAndLivingArea) {
        this.additionalAndLivingArea = additionalAndLivingArea;
    }

    public Double getAdditionalArea() {
        return additionalArea;
    }

    public void setAdditionalArea(Double additionalArea) {
        this.additionalArea = additionalArea;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public Boolean getBalcony() {
        return balcony;
    }

    public void setBalcony(Boolean balcony) {
        this.balcony = balcony;
    }

    public Integer getBathroomCondition() {
        return bathroomCondition;
    }

    public void setBathroomCondition(Integer bathroomCondition) {
        this.bathroomCondition = bathroomCondition;
    }

    public Boolean getBuildingHasElevator() {
        return buildingHasElevator;
    }

    public void setBuildingHasElevator(Boolean buildingHasElevator) {
        this.buildingHasElevator = buildingHasElevator;
    }

    public Boolean getCanParkCar() {
        return canParkCar;
    }

    public void setCanParkCar(Boolean canParkCar) {
        this.canParkCar = canParkCar;
    }

    public Double getCeilingHeight() {
        return ceilingHeight;
    }

    public void setCeilingHeight(Double ceilingHeight) {
        this.ceilingHeight = ceilingHeight;
    }

    public String getConstructionEra() {
        return constructionEra;
    }

    public void setConstructionEra(String constructionEra) {
        this.constructionEra = constructionEra;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(Integer constructionYear) {
        this.constructionYear = constructionYear;
    }

    public String getFireplace() {
        return fireplace;
    }

    public void setFireplace(String fireplace) {
        this.fireplace = fireplace;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Boolean getHasBasement() {
        return hasBasement;
    }

    public void setHasBasement(Boolean hasBasement) {
        this.hasBasement = hasBasement;
    }

    public Integer getKitchenCondition() {
        return kitchenCondition;
    }

    public void setKitchenCondition(Integer kitchenCondition) {
        this.kitchenCondition = kitchenCondition;
    }

    public Double getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Double knowledge) {
        this.knowledge = knowledge;
    }

    public String getLastGroundDrainage() {
        return lastGroundDrainage;
    }

    public void setLastGroundDrainage(String lastGroundDrainage) {
        this.lastGroundDrainage = lastGroundDrainage;
    }

    public String getLastRoofRenovation() {
        return lastRoofRenovation;
    }

    public void setLastRoofRenovation(String lastRoofRenovation) {
        this.lastRoofRenovation = lastRoofRenovation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getListPrice() {
        return listPrice;
    }

    public void setListPrice(Integer listPrice) {
        this.listPrice = listPrice;
    }

    public Double getLivingArea() {
        return livingArea;
    }

    public void setLivingArea(Double livingArea) {
        this.livingArea = livingArea;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Double getOperatingCost() {
        return operatingCost;
    }

    public void setOperatingCost(Double operatingCost) {
        this.operatingCost = operatingCost;
    }

    public Double getOperatingCostPerSqm() {
        return operatingCostPerSqm;
    }

    public void setOperatingCostPerSqm(Double operatingCostPerSqm) {
        this.operatingCostPerSqm = operatingCostPerSqm;
    }

    public String getPatio() {
        return patio;
    }

    public void setPatio(String patio) {
        this.patio = patio;
    }

    public Double getPlotArea() {
        return plotArea;
    }

    public void setPlot_area(Double plot_area) {
        this.plotArea = plot_area;
    }

    public Double getRent() {
        return rent;
    }

    public void setRent(Double rent) {
        this.rent = rent;
    }

    public Double getRentPerSqm() {
        return rentPerSqm;
    }

    public void setRentPerSqm(Double rentPerSqm) {
        this.rentPerSqm = rentPerSqm;
    }

    public Integer getRooms() {
        return rooms;
    }

    public void setRooms(Integer rooms) {
        this.rooms = rooms;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public List<Integer> getValueScore() {
        return valueScore;
    }

    public void setValueScore(List<Integer> valueScore) {
        this.valueScore = valueScore;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("additionalAndLivingArea", additionalAndLivingArea)
                .add("additionalArea", additionalArea)
                .add("apartmentNumber", apartmentNumber)
                .add("balcony", balcony)
                .add("bathroomCondition", bathroomCondition)
                .add("buildingHasElevator", buildingHasElevator)
                .add("canParkCar", canParkCar)
                .add("ceilingHeight", ceilingHeight)
                .add("constructionEra", constructionEra)
                .add("constructionYear", constructionYear)
                .add("fireplace", fireplace)
                .add("floor", floor)
                .add("hasBasement", hasBasement)
                .add("kitchenCondition", kitchenCondition)
                .add("knowledge", knowledge)
                .add("lastGroundDrainage", lastGroundDrainage)
                .add("lastRoofRenovation", lastRoofRenovation)
                .add("latitude", latitude)
                .add("listPrice", listPrice)
                .add("livingArea", livingArea)
                .add("longitude", longitude)
                .add("objectType", objectType)
                .add("operatingCost", operatingCost)
                .add("operatingCostPerSqm", operatingCostPerSqm)
                .add("patio", patio)
                .add("plot_area", plotArea)
                .add("rent", rent)
                .add("rentPerSqm", rentPerSqm)
                .add("rooms", rooms)
                .add("streetAddress", streetAddress)
                .add("valueScore", valueScore)
                .toString();
    }
}
