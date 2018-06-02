package se.tink.backend.common.providers.booli.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reference {
    private Double difference;
    private Double distanceMeters;
    private Double floor;
    private Double indexAdjustedSoldPrice;
    private Double latitude;
    private Double livingArea;
    private Double longitude;
    private String objectType;
    private Integer operatingCost;
    private Double plotArea;
    private Integer rent;
    private Integer rooms;
    private Double similarity;
    private String soldDate;
    private Integer soldId;
    private Integer soldPrice;
    private Double soldSqmPrice;
    private String streetAddress;
    private String url;
    private Differences differences;

    public Double getDifference() {
        return difference;
    }

    public void setDifference(Double difference) {
        this.difference = difference;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Double getFloor() {
        return floor;
    }

    public void setFloor(Double floor) {
        this.floor = floor;
    }

    public Double getIndexAdjustedSoldPrice() {
        return indexAdjustedSoldPrice;
    }

    public void setIndexAdjustedSoldPrice(Double indexAdjustedSoldPrice) {
        this.indexAdjustedSoldPrice = indexAdjustedSoldPrice;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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

    public Integer getOperatingCost() {
        return operatingCost;
    }

    public void setOperatingCost(Integer operatingCost) {
        this.operatingCost = operatingCost;
    }

    public Double getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(Double plotArea) {
        this.plotArea = plotArea;
    }

    public Integer getRent() {
        return rent;
    }

    public void setRent(Integer rent) {
        this.rent = rent;
    }

    public Integer getRooms() {
        return rooms;
    }

    public void setRooms(Integer rooms) {
        this.rooms = rooms;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public String getSoldDate() {
        return soldDate;
    }

    public void setSoldDate(String soldDate) {
        this.soldDate = soldDate;
    }

    public Integer getSoldId() {
        return soldId;
    }

    public void setSoldId(Integer soldId) {
        this.soldId = soldId;
    }

    public Integer getSoldPrice() {
        return soldPrice;
    }

    public void setSoldPrice(Integer soldPrice) {
        this.soldPrice = soldPrice;
    }

    public Double getSoldSqmPrice() {
        return soldSqmPrice;
    }

    public void setSoldSqmPrice(Double soldSqmPrice) {
        this.soldSqmPrice = soldSqmPrice;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Differences getDifferences() {
        return differences;
    }

    public void setDifferences(Differences differences) {
        this.differences = differences;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("difference", difference)
                .add("distanceMeters", distanceMeters)
                .add("floor", floor)
                .add("indexAdjustedSoldPrice", indexAdjustedSoldPrice)
                .add("latitude", latitude)
                .add("livingArea", livingArea)
                .add("longitude", longitude)
                .add("objectType", objectType)
                .add("operatingCost", operatingCost)
                .add("plotArea", plotArea)
                .add("rent", rent)
                .add("rooms", rooms)
                .add("similarity", similarity)
                .add("soldDate", soldDate)
                .add("soldId", soldId)
                .add("soldPrice", soldPrice)
                .add("soldSqmPrice", soldSqmPrice)
                .add("streetAddress", streetAddress)
                .add("url", url)
                .add("differences", differences)
                .toString();
    }

}
