package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

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
 *         &lt;element name="GetRandomRestaurantResult" type="{http://edenred.se/}RestaurantInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"getRandomRestaurantResult"})
@XmlRootElement(name = "GetRandomRestaurantResponse")
public class GetRandomRestaurantResponse {

    @XmlElement(name = "GetRandomRestaurantResult")
    protected RestaurantInfo getRandomRestaurantResult;

    /**
     * Gets the value of the getRandomRestaurantResult property.
     *
     * @return possible object is {@link RestaurantInfo }
     */
    public RestaurantInfo getGetRandomRestaurantResult() {
        return getRandomRestaurantResult;
    }

    /**
     * Sets the value of the getRandomRestaurantResult property.
     *
     * @param value allowed object is {@link RestaurantInfo }
     */
    public void setGetRandomRestaurantResult(RestaurantInfo value) {
        this.getRandomRestaurantResult = value;
    }
}
