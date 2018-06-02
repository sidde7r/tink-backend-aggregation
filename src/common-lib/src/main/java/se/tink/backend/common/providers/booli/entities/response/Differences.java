package se.tink.backend.common.providers.booli.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Differences {
    @JsonProperty("additional_and_living_area")
    private Double additionalAndLivingArea;
    @JsonProperty("additional_area")
    private Double additionalArea;
    @JsonProperty("construction_year")
    private Double constructionYear;
    @JsonProperty("distance_km")
    private Double distanceKm;
    @JsonProperty("living_area")
    private Double livingArea;
    @JsonProperty("object_type")
    private Double objectType;
    @JsonProperty("operating_cost_per_sqm")
    private Double operatingCostPerSqm;
    private Integer ownShore;
    private Double rent;
    private Double rooms;
    private Integer sewer;
    private Integer water;
    @JsonProperty("years_ago")
    private Double yearsAgo;

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

    public Double getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(Double constructionYear) {
        this.constructionYear = constructionYear;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getLivingArea() {
        return livingArea;
    }

    public void setLivingArea(Double livingArea) {
        this.livingArea = livingArea;
    }

    public Double getObjectType() {
        return objectType;
    }

    public void setObjectType(Double objectType) {
        this.objectType = objectType;
    }

    public Double getOperatingCostPerSqm() {
        return operatingCostPerSqm;
    }

    public void setOperatingCostPerSqm(Double operatingCostPerSqm) {
        this.operatingCostPerSqm = operatingCostPerSqm;
    }

    public Integer getOwnShore() {
        return ownShore;
    }

    public void setOwnShore(Integer ownShore) {
        this.ownShore = ownShore;
    }

    public Double getRent() {
        return rent;
    }

    public void setRent(Double rent) {
        this.rent = rent;
    }

    public Double getRooms() {
        return rooms;
    }

    public void setRooms(Double rooms) {
        this.rooms = rooms;
    }

    public Integer getSewer() {
        return sewer;
    }

    public void setSewer(Integer sewer) {
        this.sewer = sewer;
    }

    public Integer getWater() {
        return water;
    }

    public void setWater(Integer water) {
        this.water = water;
    }

    public Double getYearsAgo() {
        return yearsAgo;
    }

    public void setYearsAgo(Double yearsAgo) {
        this.yearsAgo = yearsAgo;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("additional_and_living_area", additionalAndLivingArea)
                .add("additional_area", additionalArea)
                .add("construction_year", constructionYear)
                .add("distance_km", distanceKm)
                .add("living_area", livingArea)
                .add("object_type", objectType)
                .add("operating_cost_per_sqm", operatingCostPerSqm)
                .add("ownShore", ownShore)
                .add("rent", rent)
                .add("rooms", rooms)
                .add("sewer", sewer)
                .add("water", water)
                .add("yearsAgo", yearsAgo)
                .toString();
    }
}
