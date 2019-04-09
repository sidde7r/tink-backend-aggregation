package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for RestaurantInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RestaurantInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Address" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ZipCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="City" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Network" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OrgNr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Phone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Distance" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GeoCoordinate" type="{http://edenred.se/}GeoCoordinate" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "RestaurantInfo",
        propOrder = {
            "id",
            "name",
            "address",
            "zipCode",
            "city",
            "network",
            "orgNr",
            "phone",
            "distance",
            "geoCoordinate"
        })
public class RestaurantInfo {

    @XmlElement(name = "Id")
    protected int id;

    @XmlElement(name = "Name")
    protected String name;

    @XmlElement(name = "Address")
    protected String address;

    @XmlElement(name = "ZipCode")
    protected String zipCode;

    @XmlElement(name = "City")
    protected String city;

    @XmlElement(name = "Network")
    protected String network;

    @XmlElement(name = "OrgNr")
    protected String orgNr;

    @XmlElement(name = "Phone")
    protected String phone;

    @XmlElement(name = "Distance")
    protected String distance;

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
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the address property.
     *
     * @return possible object is {@link String }
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * Gets the value of the zipCode property.
     *
     * @return possible object is {@link String }
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Sets the value of the zipCode property.
     *
     * @param value allowed object is {@link String }
     */
    public void setZipCode(String value) {
        this.zipCode = value;
    }

    /**
     * Gets the value of the city property.
     *
     * @return possible object is {@link String }
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the value of the city property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCity(String value) {
        this.city = value;
    }

    /**
     * Gets the value of the network property.
     *
     * @return possible object is {@link String }
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Sets the value of the network property.
     *
     * @param value allowed object is {@link String }
     */
    public void setNetwork(String value) {
        this.network = value;
    }

    /**
     * Gets the value of the orgNr property.
     *
     * @return possible object is {@link String }
     */
    public String getOrgNr() {
        return orgNr;
    }

    /**
     * Sets the value of the orgNr property.
     *
     * @param value allowed object is {@link String }
     */
    public void setOrgNr(String value) {
        this.orgNr = value;
    }

    /**
     * Gets the value of the phone property.
     *
     * @return possible object is {@link String }
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPhone(String value) {
        this.phone = value;
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
