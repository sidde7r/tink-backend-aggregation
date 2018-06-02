package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Entity
@Table(name = "booli_estimates")
public class BooliEstimate {
    @Id
    private String id;
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
    private String residenceType;
    private Double operatingCost;
    private Double operatingCostPerSqm;
    private String patio;
    private Double plotArea;
    private Double rent;
    private Double rentPerSqm;
    private Integer rooms;
    private String streetAddress;
    private Double biddingAveragePrediction;
    private Double biddingAverageWeight;
    private Double differenceAverage;
    private Double differenceCv;
    private Double knnPrediction;
    private Double knnWeight;
    private Date predictionDate;
    private String predictor;
    private Double previousSalePrediction;
    private Double previousSaleWeight;
    private Double priceCv;
    private String recommendation;
    private Double accuracy;
    private Integer price;
    private Integer priceRangeHigh;
    private Integer priceRangeLow;
    private Double sqmPrice;
    private Double sqmPriceRangeHigh;
    private Double sqmPriceRangeLow;
    private Integer numberOfReferences;
    private String propertyId;

    public BooliEstimate() {
        this.id = UUIDUtils.toTinkUUID(UUID.randomUUID());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getResidenceType() {
        return residenceType;
    }

    public void setResidenceType(String residenceType) {
        this.residenceType = residenceType;
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

    public void setPlotArea(Double plotArea) {
        this.plotArea = plotArea;
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

    public Double getBiddingAveragePrediction() {
        return biddingAveragePrediction;
    }

    public void setBiddingAveragePrediction(Double biddingAveragePrediction) {
        this.biddingAveragePrediction = biddingAveragePrediction;
    }

    public Double getBiddingAverageWeight() {
        return biddingAverageWeight;
    }

    public void setBiddingAverageWeight(Double biddingAverageWeight) {
        this.biddingAverageWeight = biddingAverageWeight;
    }

    public Double getDifferenceAverage() {
        return differenceAverage;
    }

    public void setDifferenceAverage(Double differenceAverage) {
        this.differenceAverage = differenceAverage;
    }

    public Double getDifferenceCv() {
        return differenceCv;
    }

    public void setDifferenceCv(Double differenceCv) {
        this.differenceCv = differenceCv;
    }

    public Double getKnnPrediction() {
        return knnPrediction;
    }

    public void setKnnPrediction(Double knnPrediction) {
        this.knnPrediction = knnPrediction;
    }

    public Double getKnnWeight() {
        return knnWeight;
    }

    public void setKnnWeight(Double knnWeight) {
        this.knnWeight = knnWeight;
    }

    public Date getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(Date predictionDate) {
        this.predictionDate = predictionDate;
    }

    public String getPredictor() {
        return predictor;
    }

    public void setPredictor(String predictor) {
        this.predictor = predictor;
    }

    public Double getPreviousSalePrediction() {
        return previousSalePrediction;
    }

    public void setPreviousSalePrediction(Double previousSalePrediction) {
        this.previousSalePrediction = previousSalePrediction;
    }

    public Double getPreviousSaleWeight() {
        return previousSaleWeight;
    }

    public void setPreviousSaleWeight(Double previousSaleWeight) {
        this.previousSaleWeight = previousSaleWeight;
    }

    public Double getPriceCv() {
        return priceCv;
    }

    public void setPriceCv(Double priceCv) {
        this.priceCv = priceCv;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPriceRangeHigh(Integer priceRangeHigh) {
        this.priceRangeHigh = priceRangeHigh;
    }

    public Integer getPriceRangeHigh() {
        return priceRangeHigh;
    }

    public void setPriceRangeLow(Integer priceRangeLow) {
        this.priceRangeLow = priceRangeLow;
    }

    public Integer getPriceRangeLow() {
        return priceRangeLow;
    }

    public void setSqmPrice(Double sqmPrice) {
        this.sqmPrice = sqmPrice;
    }

    public Double getSqmPrice() {
        return sqmPrice;
    }

    public void setSqmPriceRangeHigh(Double sqmPriceRangeHigh) {
        this.sqmPriceRangeHigh = sqmPriceRangeHigh;
    }

    public Double getSqmPriceRangeHigh() {
        return sqmPriceRangeHigh;
    }

    public void setSqmPriceRangeLow(Double sqmPriceRangeLow) {
        this.sqmPriceRangeLow = sqmPriceRangeLow;
    }

    public Double getSqmPriceRangeLow() {
        return sqmPriceRangeLow;
    }

    public void setNumberOfReferences(Integer numberOfReferences) {
        this.numberOfReferences = numberOfReferences;
    }

    public Integer getNumberOfReferences() {
        return numberOfReferences;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("price", price)
                .add("accuracy", accuracy)
                .add("recommendation", recommendation)
                .add("priceRangeHigh", priceRangeHigh)
                .add("priceRangeLow", priceRangeLow)
                .add("sqmPrice", sqmPrice)
                .add("sqmPriceRangeHigh", sqmPriceRangeHigh)
                .add("sqmPriceRangeLow", sqmPriceRangeLow)
                .add("numberOfReferences", numberOfReferences)
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
                .add("residenceType", residenceType)
                .add("operatingCost", operatingCost)
                .add("operatingCostPerSqm", operatingCostPerSqm)
                .add("patio", patio)
                .add("plotArea", plotArea)
                .add("rent", rent)
                .add("rentPerSqm", rentPerSqm)
                .add("rooms", rooms)
                .add("streetAddress", streetAddress)
                .add("biddingAveragePrediction", biddingAveragePrediction)
                .add("biddingAverageWeight", biddingAverageWeight)
                .add("differenceAverage", differenceAverage)
                .add("differenceCv", differenceCv)
                .add("knnPrediction", knnPrediction)
                .add("knnWeight", knnWeight)
                .add("predictionDate", predictionDate)
                .add("predictor", predictor)
                .add("previousSalePrediction", previousSalePrediction)
                .add("previousSaleWeight", previousSaleWeight)
                .add("priceCv", priceCv)
                .add("propertyId", propertyId)
                .toString();
    }
}
