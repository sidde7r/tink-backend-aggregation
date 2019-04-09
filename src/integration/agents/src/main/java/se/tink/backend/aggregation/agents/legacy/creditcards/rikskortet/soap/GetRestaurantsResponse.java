package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="GetRestaurantsResult" type="{http://edenred.se/}ArrayOfRestaurantInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"getRestaurantsResult"})
@XmlRootElement(name = "GetRestaurantsResponse")
public class GetRestaurantsResponse {

    @XmlElement(name = "GetRestaurantsResult")
    protected ArrayOfRestaurantInfo getRestaurantsResult;

    /**
     * Gets the value of the getRestaurantsResult property.
     *
     * @return possible object is {@link ArrayOfRestaurantInfo }
     */
    public ArrayOfRestaurantInfo getGetRestaurantsResult() {
        return getRestaurantsResult;
    }

    /**
     * Sets the value of the getRestaurantsResult property.
     *
     * @param value allowed object is {@link ArrayOfRestaurantInfo }
     */
    public void setGetRestaurantsResult(ArrayOfRestaurantInfo value) {
        this.getRestaurantsResult = value;
    }
}
