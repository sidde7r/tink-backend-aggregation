
package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetRestaurantDetailsResult" type="{http://edenred.se/}RestaurantDetails" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getRestaurantDetailsResult"
})
@XmlRootElement(name = "GetRestaurantDetailsResponse")
public class GetRestaurantDetailsResponse {

    @XmlElement(name = "GetRestaurantDetailsResult")
    protected RestaurantDetails getRestaurantDetailsResult;

    /**
     * Gets the value of the getRestaurantDetailsResult property.
     * 
     * @return
     *     possible object is
     *     {@link RestaurantDetails }
     *     
     */
    public RestaurantDetails getGetRestaurantDetailsResult() {
        return getRestaurantDetailsResult;
    }

    /**
     * Sets the value of the getRestaurantDetailsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link RestaurantDetails }
     *     
     */
    public void setGetRestaurantDetailsResult(RestaurantDetails value) {
        this.getRestaurantDetailsResult = value;
    }

}
