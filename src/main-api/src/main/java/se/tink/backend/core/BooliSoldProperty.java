package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Entity
@Table(name = "booli_sold_properties")
public class BooliSoldProperty {
    @Id
    private String id;
    private Double floor;
    private Double indexAdjustedFloatPrice;
    private Double latitude;
    private Double longitude;
    private Double livingArea;
    private String residenceType;
    private Integer operatingCost;
    private Double plotArea;
    private Integer rent;
    private Integer rooms;
    private Date soldDate;
    private Integer booliId;
    private Integer soldPrice;
    private Double soldSqmPrice;
    private String streetAddress;
    private String booliUrl;

    public BooliSoldProperty() {
        this.id = UUIDUtils.toTinkUUID(UUID.randomUUID());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getFloor() {
        return floor;
    }

    public void setFloor(Double floor) {
        this.floor = floor;
    }

    public Double getIndexAdjustedFloatPrice() {
        return indexAdjustedFloatPrice;
    }

    public void setIndexAdjustedFloatPrice(Double indexAdjustedFloatPrice) {
        this.indexAdjustedFloatPrice = indexAdjustedFloatPrice;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLivingArea() {
        return livingArea;
    }

    public void setLivingArea(Double livingArea) {
        this.livingArea = livingArea;
    }

    public String getResidenceType() {
        return residenceType;
    }

    public void setResidenceType(String residenceType) {
        this.residenceType = residenceType;
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

    public Date getSoldDate() {
        return soldDate;
    }

    public void setSoldDate(Date soldDate) {
        this.soldDate = soldDate;
    }

    public Integer getBooliId() {
        return booliId;
    }

    public void setBooliId(Integer booliId) {
        this.booliId = booliId;
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

    public String getBooliUrl() {
        return booliUrl;
    }

    public void setBooliUrl(String booliUrl) {
        this.booliUrl = booliUrl;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("floor", floor)
                .add("indexAdjustedFloatPrice", indexAdjustedFloatPrice)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("livingArea", livingArea)
                .add("residenceType", residenceType)
                .add("operatingCost", operatingCost)
                .add("plotArea", plotArea)
                .add("rent", rent)
                .add("rooms", rooms)
                .add("soldDate", soldDate)
                .add("booliId", booliId)
                .add("soldPrice", soldPrice)
                .add("soldSqmPrice", soldSqmPrice)
                .add("streetAddress", streetAddress)
                .add("booliUrl", booliUrl)
                .toString();
    }
}
