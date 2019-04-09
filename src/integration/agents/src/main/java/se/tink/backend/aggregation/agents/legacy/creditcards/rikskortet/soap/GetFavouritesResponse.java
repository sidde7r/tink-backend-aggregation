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
 *         &lt;element name="GetFavouritesResult" type="{http://edenred.se/}ArrayOfRestaurantInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"getFavouritesResult"})
@XmlRootElement(name = "GetFavouritesResponse")
public class GetFavouritesResponse {

    @XmlElement(name = "GetFavouritesResult")
    protected ArrayOfRestaurantInfo getFavouritesResult;

    /**
     * Gets the value of the getFavouritesResult property.
     *
     * @return possible object is {@link ArrayOfRestaurantInfo }
     */
    public ArrayOfRestaurantInfo getGetFavouritesResult() {
        return getFavouritesResult;
    }

    /**
     * Sets the value of the getFavouritesResult property.
     *
     * @param value allowed object is {@link ArrayOfRestaurantInfo }
     */
    public void setGetFavouritesResult(ArrayOfRestaurantInfo value) {
        this.getFavouritesResult = value;
    }
}
