package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for OfferDetails complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="OfferDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="RestaurantName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RestaurantId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Details" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Distance" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DaysLeft" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="GeoCoordinate" type="{http://edenred.se/}GeoCoordinate" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "OfferDetails",
        propOrder = {
            "id",
            "restaurantName",
            "restaurantId",
            "title",
            "details",
            "distance",
            "daysLeft",
            "geoCoordinate"
        })
public class OfferDetails {

    @XmlElement(name = "Id")
    protected int id;

    @XmlElement(name = "RestaurantName")
    protected String restaurantName;

    @XmlElement(name = "RestaurantId")
    protected int restaurantId;

    @XmlElement(name = "Title")
    protected String title;

    @XmlElement(name = "Details")
    protected String details;

    @XmlElement(name = "Distance")
    protected String distance;

    @XmlElement(name = "DaysLeft")
    protected int daysLeft;

    @XmlElement(name = "GeoCoordinate")
    protected GeoCoordinate geoCoordinate;

    /** Gets the value of the id property. */
    public int getId() {
        return id;
    }

    /** Sets the value of the id property. */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the restaurantName property.
     *
     * @return possible object is {@link String }
     */
    public String getRestaurantName() {
        return restaurantName;
    }

    /**
     * Sets the value of the restaurantName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRestaurantName(String value) {
        this.restaurantName = value;
    }

    /** Gets the value of the restaurantId property. */
    public int getRestaurantId() {
        return restaurantId;
    }

    /** Sets the value of the restaurantId property. */
    public void setRestaurantId(int value) {
        this.restaurantId = value;
    }

    /**
     * Gets the value of the title property.
     *
     * @return possible object is {@link String }
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     *
     * @param value allowed object is {@link String }
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the details property.
     *
     * @return possible object is {@link String }
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the value of the details property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDetails(String value) {
        this.details = value;
    }

    /**
     * Gets the value of the distance property.
     *
     * @return possible object is {@link String }
     */
    public String getDistance() {
        return distance;
    }

    /**
     * Sets the value of the distance property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDistance(String value) {
        this.distance = value;
    }

    /** Gets the value of the daysLeft property. */
    public int getDaysLeft() {
        return daysLeft;
    }

    /** Sets the value of the daysLeft property. */
    public void setDaysLeft(int value) {
        this.daysLeft = value;
    }

    /**
     * Gets the value of the geoCoordinate property.
     *
     * @return possible object is {@link GeoCoordinate }
     */
    public GeoCoordinate getGeoCoordinate() {
        return geoCoordinate;
    }

    /**
     * Sets the value of the geoCoordinate property.
     *
     * @param value allowed object is {@link GeoCoordinate }
     */
    public void setGeoCoordinate(GeoCoordinate value) {
        this.geoCoordinate = value;
    }
}
