package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportPropertyEstimate implements DefaultSetter {

    private String additionalAndLivingArea;
    private String additionalArea;
    private String apartmentNumber;
    private String balcony;
    private String bathroomCondition;
    private String buildingHasElevator;
    private String canParkCar;
    private String ceilingHeight;
    private String constructionEra;
    private String constructionYear;
    private String fireplace;
    private String floor;
    private String hasBasement;
    private String kitchenCondition;
    private String knowledge;
    private String lastGroundDrainage;
    private String lastRoofRenovation;
    private String latitude;
    private String listPrice;
    private String livingArea;
    private String longitude;
    private String residenceType;
    private String operatingCost;
    private String operatingCostPerSqm;
    private String patio;
    private String plotArea;
    private String rent;
    private String rentPerSqm;
    private String rooms;
    private String streetAddress;
    private String biddingAveragePrediction;
    private String biddingAverageWeight;
    private String differenceAverage;
    private String differenceCv;
    private String knnPrediction;
    private String knnWeight;
    private String predictionDate;
    private String predictor;
    private String previousSalePrediction;
    private String previousSaleWeight;
    private String priceCv;
    private String recommendation;
    private String accuracy;
    private String price;
    private String priceRangeHigh;
    private String priceRangeLow;
    private String sqmPrice;
    private String sqmPriceRangeHigh;
    private String sqmPriceRangeLow;
    private String numberOfReferences;

    public ExportPropertyEstimate(Double additionalAndLivingArea, Double additionalArea, String apartmentNumber,
            Boolean balcony, Integer bathroomCondition, Boolean buildingHasElevator, Boolean canParkCar,
            Double ceilingHeight, String constructionEra, Integer constructionYear, String fireplace,
            Integer floor, Boolean hasBasement, Integer kitchenCondition, Double knowledge,
            String lastGroundDrainage, String lastRoofRenovation, Double latitude, Integer listPrice,
            Double livingArea, Double longitude, String residenceType, Double operatingCost,
            Double operatingCostPerSqm, String patio, Double plotArea, Double rent, Double rentPerSqm,
            Integer rooms, String streetAddress, Double biddingAveragePrediction, Double biddingAverageWeight,
            Double differenceAverage, Double differenceCv, Double knnPrediction, Double knnWeight,
            Date predictionDate, String predictor, Double previousSalePrediction, Double previousSaleWeight,
            Double priceCv, String recommendation, Double accuracy, Integer price, Integer priceRangeHigh,
            Integer priceRangeLow, Double sqmPrice, Double sqmPriceRangeHigh, Double sqmPriceRangeLow,
            Integer numberOfReferences) {
        this.additionalAndLivingArea = notNull(additionalAndLivingArea);
        this.additionalArea = notNull(additionalArea);
        this.apartmentNumber = notNull(apartmentNumber);
        this.balcony = notNull(balcony);
        this.bathroomCondition = notNull(bathroomCondition);
        this.buildingHasElevator = notNull(buildingHasElevator);
        this.canParkCar = notNull(canParkCar);
        this.ceilingHeight = notNull(ceilingHeight);
        this.constructionEra = notNull(constructionEra);
        this.constructionYear = notNull(constructionYear);
        this.fireplace = notNull(fireplace);
        this.floor = notNull(floor);
        this.hasBasement = notNull(hasBasement);
        this.kitchenCondition = notNull(kitchenCondition);
        this.knowledge = notNull(knowledge);
        this.lastGroundDrainage = notNull(lastGroundDrainage);
        this.lastRoofRenovation = notNull(lastRoofRenovation);
        this.latitude = notNull(latitude);
        this.listPrice = notNull(listPrice);
        this.livingArea = notNull(livingArea);
        this.longitude = notNull(longitude);
        this.residenceType = notNull(residenceType);
        this.operatingCost = notNull(operatingCost);
        this.operatingCostPerSqm = notNull(operatingCostPerSqm);
        this.patio = notNull(patio);
        this.plotArea = notNull(plotArea);
        this.rent = notNull(rent);
        this.rentPerSqm = notNull(rentPerSqm);
        this.rooms = notNull(rooms);
        this.streetAddress = notNull(streetAddress);
        this.biddingAveragePrediction = notNull(biddingAveragePrediction);
        this.biddingAverageWeight = notNull(biddingAverageWeight);
        this.differenceAverage = notNull(differenceAverage);
        this.differenceCv = notNull(differenceCv);
        this.knnPrediction = notNull(knnPrediction);
        this.knnWeight = notNull(knnWeight);
        this.predictionDate = notNull(predictionDate);
        this.predictor = notNull(predictor);
        this.previousSalePrediction = notNull(previousSalePrediction);
        this.previousSaleWeight = notNull(previousSaleWeight);
        this.priceCv = notNull(priceCv);
        this.recommendation = notNull(recommendation);
        this.accuracy = notNull(accuracy);
        this.price = notNull(price);
        this.priceRangeHigh = notNull(priceRangeHigh);
        this.priceRangeLow = notNull(priceRangeLow);
        this.sqmPrice = notNull(sqmPrice);
        this.sqmPriceRangeHigh = notNull(sqmPriceRangeHigh);
        this.sqmPriceRangeLow = notNull(sqmPriceRangeLow);
        this.numberOfReferences = notNull(numberOfReferences);
    }

    public String getAdditionalAndLivingArea() {
        return additionalAndLivingArea;
    }

    public String getAdditionalArea() {
        return additionalArea;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public String getBalcony() {
        return balcony;
    }

    public String getBathroomCondition() {
        return bathroomCondition;
    }

    public String getBuildingHasElevator() {
        return buildingHasElevator;
    }

    public String getCanParkCar() {
        return canParkCar;
    }

    public String getCeilingHeight() {
        return ceilingHeight;
    }

    public String getConstructionEra() {
        return constructionEra;
    }

    public String getConstructionYear() {
        return constructionYear;
    }

    public String getFireplace() {
        return fireplace;
    }

    public String getFloor() {
        return floor;
    }

    public String getHasBasement() {
        return hasBasement;
    }

    public String getKitchenCondition() {
        return kitchenCondition;
    }

    public String getKnowledge() {
        return knowledge;
    }

    public String getLastGroundDrainage() {
        return lastGroundDrainage;
    }

    public String getLastRoofRenovation() {
        return lastRoofRenovation;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getListPrice() {
        return listPrice;
    }

    public String getLivingArea() {
        return livingArea;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getResidenceType() {
        return residenceType;
    }

    public String getOperatingCost() {
        return operatingCost;
    }

    public String getOperatingCostPerSqm() {
        return operatingCostPerSqm;
    }

    public String getPatio() {
        return patio;
    }

    public String getPlotArea() {
        return plotArea;
    }

    public String getRent() {
        return rent;
    }

    public String getRentPerSqm() {
        return rentPerSqm;
    }

    public String getRooms() {
        return rooms;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getBiddingAveragePrediction() {
        return biddingAveragePrediction;
    }

    public String getBiddingAverageWeight() {
        return biddingAverageWeight;
    }

    public String getDifferenceAverage() {
        return differenceAverage;
    }

    public String getDifferenceCv() {
        return differenceCv;
    }

    public String getKnnPrediction() {
        return knnPrediction;
    }

    public String getKnnWeight() {
        return knnWeight;
    }

    public String getPredictionDate() {
        return predictionDate;
    }

    public String getPredictor() {
        return predictor;
    }

    public String getPreviousSalePrediction() {
        return previousSalePrediction;
    }

    public String getPreviousSaleWeight() {
        return previousSaleWeight;
    }

    public String getPriceCv() {
        return priceCv;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public String getPrice() {
        return price;
    }

    public String getPriceRangeHigh() {
        return priceRangeHigh;
    }

    public String getPriceRangeLow() {
        return priceRangeLow;
    }

    public String getSqmPrice() {
        return sqmPrice;
    }

    public String getSqmPriceRangeHigh() {
        return sqmPriceRangeHigh;
    }

    public String getSqmPriceRangeLow() {
        return sqmPriceRangeLow;
    }

    public String getNumberOfReferences() {
        return numberOfReferences;
    }
}
