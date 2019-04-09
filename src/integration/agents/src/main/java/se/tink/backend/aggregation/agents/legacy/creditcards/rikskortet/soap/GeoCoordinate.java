package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for GeoCoordinate complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GeoCoordinate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Latitude" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="Longitude" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "GeoCoordinate",
        propOrder = {"latitude", "longitude"})
public class GeoCoordinate {

    @XmlElement(name = "Latitude")
    protected double latitude;

    @XmlElement(name = "Longitude")
    protected double longitude;

    /** Gets the value of the latitude property. */
    public double getLatitude() {
        return latitude;
    }

    /** Sets the value of the latitude property. */
    public void setLatitude(double value) {
        this.latitude = value;
    }

    /** Gets the value of the longitude property. */
    public double getLongitude() {
        return longitude;
    }

    /** Sets the value of the longitude property. */
    public void setLongitude(double value) {
        this.longitude = value;
    }
}
