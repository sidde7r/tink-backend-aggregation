package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="latitude" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="longitude" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="distance" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"latitude", "longitude", "distance"})
@XmlRootElement(name = "GetRestaurants")
public class GetRestaurants {

    protected long latitude;
    protected long longitude;
    protected long distance;

    /** Gets the value of the latitude property. */
    public long getLatitude() {
        return latitude;
    }

    /** Sets the value of the latitude property. */
    public void setLatitude(long value) {
        this.latitude = value;
    }

    /** Gets the value of the longitude property. */
    public long getLongitude() {
        return longitude;
    }

    /** Sets the value of the longitude property. */
    public void setLongitude(long value) {
        this.longitude = value;
    }

    /** Gets the value of the distance property. */
    public long getDistance() {
        return distance;
    }

    /** Sets the value of the distance property. */
    public void setDistance(long value) {
        this.distance = value;
    }
}
