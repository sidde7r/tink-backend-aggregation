package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.joda.time.DateTime;
import se.tink.backend.serialization.TypeReferences;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BooliEstimateRequest {
    private static final Joiner.MapJoiner QUERY_MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");

    // Required
    @JsonProperty("location.position.latitude")
    private Double latitude;                             // Latitude koordinater
    @JsonProperty("location.position.longitude")
    private Double longitude;                            // Longitude koordinater
    @JsonProperty("objectType")
    private ResidenceType residenceType;                // Bostadstyp
    @JsonProperty("livingArea")
    private Double livingArea;                           // Boarea

    // Recommended
    @JsonProperty("location.address.streetAddress")
    private String streetAddress;                       // Gatuadress
    @JsonProperty("additionalArea")
    private Double additionalArea;                       // Biarea
    @JsonProperty("plotArea")
    private Double plotArea;                             // Tomtarea
    @JsonProperty("constructionYear")
    private Integer constructionYear;                   // Byggår
    @JsonProperty("rooms")
    private Double rooms;                                // Antal rum
    @JsonProperty("rent")
    private Double rent;                                 // Avgift per månad
    @JsonProperty("operatingCost")
    private Double operatingCost;                        // Driftskostnad per månad
    @JsonProperty("apartmentNumber")
    private String apartmentNumber;                     // SKVs lägenhetsnummer, '1001'
    @JsonProperty("assessmentValue")
    private Double assessmentValue;                      // Taxeringsvärde
    @JsonProperty("assessmentYear")
    private Double assessmentYear;                       // Taxeringsår
    @JsonProperty("assessmentPoints")
    private Double assessmentPoints;                     // Standardpoäng
    @JsonProperty("water")
    private WaterSupplyType waterSupplyType;            // Tillgång till vatten
    @JsonProperty("sewer")
    private SewerType sewerType;                        // Avloppstyp
    @JsonProperty("ownShore")
    private Integer ownShore;                           // Egen strand
    @JsonProperty("distanceToWater")
    private Double distanceToWater;                      // Avstånd till närmsta vatten

    // Optional
    @JsonProperty("estimate_to_date")
    private String estimateToDate;                      // Datum att värdera till
    @JsonProperty("listPrice")
    private Double listPrice;                            // Utropspris
    @JsonProperty("condition.kitchen")
    private Integer kitchenCondition;                   // Skick kök ( 1 - 5 )
    @JsonProperty("condition.bathroom")
    private Integer bathroomCondition;                  // Skick badrum  ( 1 - 5 )
    @JsonProperty("fireplace")
    private FireplaceType fireplace;                    // Kakelugn / Öppen spis
    @JsonProperty("vista")
    private VistaType vista;                            // Utsikt
    @JsonProperty("floor")
    private Integer floor;                              // Våning
    @JsonProperty("ceilingHeight")
    private Double ceilingHeight;                        // Takhöjd i meter
    @JsonProperty("buildingHasElevator")
    private Integer elevator;                           // Hiss finns
    @JsonProperty("balcony")
    private BalconyType balcony;                        // Balkong
    @JsonProperty("patio")
    private VistaType patio;                            // Uteplats (same values as vista)
    @JsonProperty("hasBasement")
    private Integer basement;                           // Källare finns
    @JsonProperty("parking")
    private ParkingType parking;                        // Parkeringsmöjligheter
    @JsonProperty("lastGroundDrainage")
    private MaintenanceIntervalType lastGroundDrainage; // Senaste dränering
    @JsonProperty("lastRoofRenovation")
    private MaintenanceIntervalType lastRoofRenovation; // Senaste takrenovering

    public BooliEstimateRequest(Double latitude, Double longitude, ResidenceType residenceType, Double livingArea) {
        setLatitude(latitude);
        setLongitude(longitude);
        setResidenceType(residenceType);
        setLivingArea(livingArea);
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        Preconditions.checkNotNull(latitude);
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        Preconditions.checkNotNull(longitude);
        this.longitude = longitude;
    }

    public ResidenceType getResidenceType() {
        return residenceType;
    }

    public void setResidenceType(ResidenceType residenceType) {
        Preconditions.checkNotNull(residenceType);
        this.residenceType = residenceType;
    }

    public Double getLivingArea() {
        return livingArea;
    }

    public void setLivingArea(Double livingArea) {
        Preconditions.checkNotNull(livingArea);
        this.livingArea = livingArea;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public Double getAdditionalArea() {
        return additionalArea;
    }

    public void setAdditionalArea(Double additionalArea) {
        this.additionalArea = additionalArea;
    }

    public Double getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(Double plotArea) {
        this.plotArea = plotArea;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(Integer constructionYear) {
        this.constructionYear = constructionYear;
    }

    public Double getRooms() {
        return rooms;
    }

    public void setRooms(Double rooms) {
        this.rooms = rooms;
    }

    public Double getRent() {
        return rent;
    }

    public void setRent(Double rent) {
        this.rent = rent;
    }

    public Double getOperatingCost() {
        return operatingCost;
    }

    public void setOperatingCost(Double operatingCost) {
        this.operatingCost = operatingCost;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public Double getAssessmentValue() {
        return assessmentValue;
    }

    public void setAssessmentValue(Double assessmentValue) {
        this.assessmentValue = assessmentValue;
    }

    public Double getAssessmentYear() {
        return assessmentYear;
    }

    public void setAssessmentYear(Double assessmentYear) {
        this.assessmentYear = assessmentYear;
    }

    public Double getAssessmentPoints() {
        return assessmentPoints;
    }

    public void setAssessmentPoints(Double assessmentPoints) {
        this.assessmentPoints = assessmentPoints;
    }

    public WaterSupplyType getWaterSupplyType() {
        return waterSupplyType;
    }

    public void setWaterSupplyType(WaterSupplyType waterSupplyType) {
        this.waterSupplyType = waterSupplyType;
    }

    public SewerType getSewerType() {
        return sewerType;
    }

    public void setSewerType(SewerType sewerType) {
        this.sewerType = sewerType;
    }

    public Integer getOwnShore() {
        return ownShore;
    }

    public void setOwnShore(Boolean ownShore) {
        if (ownShore == null) {
            this.ownShore = null;
            return;
        }

        this.ownShore = ownShore ? 1 : 0;
    }

    public Double getDistanceToWater() {
        return distanceToWater;
    }

    public void setDistanceToWater(Double distanceToWater) {
        this.distanceToWater = distanceToWater;
    }

    public String getEstimateToDate() {
        return estimateToDate;
    }

    public void setEstimateToDate(DateTime estimateToDate) {
        if (estimateToDate == null) {
            this.estimateToDate = null;
            return;
        }

        this.estimateToDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(estimateToDate.toDate());
    }

    public Double getListPrice() {
        return listPrice;
    }

    public void setListPrice(Double listPrice) {
        this.listPrice = listPrice;
    }

    public Integer getKitchenCondition() {
        return kitchenCondition;
    }

    public void setKitchenCondition(Integer kitchenCondition) {
        Preconditions.checkState(kitchenCondition == null || Range.closed(1, 5).contains(kitchenCondition),
                String.format("Kitchen condition must be a value between 1 & 5, given: %s", kitchenCondition));

        this.kitchenCondition = kitchenCondition;
    }

    public Integer getBathroomCondition() {
        return bathroomCondition;
    }

    public void setBathroomCondition(Integer bathroomCondition) {
        Preconditions.checkState(bathroomCondition == null || Range.closed(1, 5).contains(bathroomCondition),
                String.format("Bathroom condition must be a value between 1 & 5, given: %s", bathroomCondition));

        this.bathroomCondition = bathroomCondition;
    }

    public FireplaceType getFireplace() {
        return fireplace;
    }

    public void setFireplace(FireplaceType fireplace) {
        this.fireplace = fireplace;
    }

    public VistaType getVista() {
        return vista;
    }

    public void setVista(VistaType vista) {
        this.vista = vista;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Double getCeilingHeight() {
        return ceilingHeight;
    }

    public void setCeilingHeight(Double ceilingHeight) {
        this.ceilingHeight = ceilingHeight;
    }

    public Integer getElevator() {
        return elevator;
    }

    public void setElevator(Boolean elevator) {
        if (elevator == null) {
            this.elevator = null;
            return;
        }

        this.elevator = elevator ? 1 : 0;
    }

    public BalconyType getBalcony() {
        return balcony;
    }

    public void setBalcony(BalconyType balcony) {
        this.balcony = balcony;
    }

    public VistaType getPatio() {
        return patio;
    }

    public void setPatio(VistaType patio) {
        this.patio = patio;
    }

    public Integer getBasement() {
        return basement;
    }

    public void setBasement(Boolean basement) {
        if (basement == null) {
            this.basement = null;
            return;
        }

        this.basement = basement ? 1 : 0;
    }

    public ParkingType getParking() {
        return parking;
    }

    public void setParking(ParkingType parking) {
        this.parking = parking;
    }

    public MaintenanceIntervalType getLastGroundDrainage() {
        return lastGroundDrainage;
    }

    public void setLastGroundDrainage(MaintenanceIntervalType lastGroundDrainage) {
        this.lastGroundDrainage = lastGroundDrainage;
    }

    public MaintenanceIntervalType getLastRoofRenovation() {
        return lastRoofRenovation;
    }

    public void setLastRoofRenovation(MaintenanceIntervalType lastRoofRenovation) {
        this.lastRoofRenovation = lastRoofRenovation;
    }

    @JsonIgnore
    public String toQueryParameters() {
        switch (residenceType) {
        case APARTMENT:
            Preconditions.checkState(assessmentValue == null, "Apartments doesn't have assessmentValue");
            Preconditions.checkState(assessmentYear == null, "Apartments doesn't have assessmentYear");
            Preconditions.checkState(assessmentPoints == null, "Apartments doesn't have assessmentPoints");
            Preconditions.checkState(waterSupplyType == null, "Apartments can't specify waterSupplyType");
            Preconditions.checkState(sewerType == null, "Apartments can't specify sewerType");
            Preconditions.checkState(ownShore == null, "Apartments can't specify ownShore");
            Preconditions.checkState(distanceToWater == null, "Apartments can't specify distanceToWater");
            Preconditions.checkState(patio == null, "Apartments can't specify patio");
            Preconditions.checkState(basement == null, "Apartments can't specify basement");
            Preconditions.checkState(parking == null, "Apartments can't specify parking");
            Preconditions.checkState(lastGroundDrainage == null, "Apartments can't specify lastGroundDrainage");
            Preconditions.checkState(lastRoofRenovation == null, "Apartments can't specify lastRoofRenovation");
            break;
        case HOUSE:
        case TERRACED:
        case CHAIN_TERRACED:
        case SEMI_DETACHED:
            Preconditions.checkState(apartmentNumber == null, "Only apartments are allowed to specify apartmentNumber");
            Preconditions.checkState(floor == null, "Only apartments are allowed to specify floor");
            Preconditions.checkState(ceilingHeight == null, "Only apartments are allowed to specify ceilingHeight");
            Preconditions.checkState(elevator == null, "Only apartments are allowed to specify elevator");
            Preconditions.checkState(balcony == null, "Only apartments are allowed to specify balcony");
            break;
        }

        Map<String, String> objectAsMap = new ObjectMapper().convertValue(this, TypeReferences.MAP_OF_STRING_STRING);

        // URL encode all values since it's a query string
        for (String key : objectAsMap.keySet()) {
            objectAsMap.put(key, urlEncode(objectAsMap.get(key)));
        }

        return QUERY_MAP_JOINER.join(objectAsMap);
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Couldn't url encode value: " + value);
        }
    }
}
