
package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RestaurantDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RestaurantDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IsFavourite" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="ImageUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Restaurant" type="{http://edenred.se/}RestaurantInfo" minOccurs="0"/>
 *         &lt;element name="Week1" type="{http://edenred.se/}ArrayOfMenuDetails" minOccurs="0"/>
 *         &lt;element name="Week2" type="{http://edenred.se/}ArrayOfMenuDetails" minOccurs="0"/>
 *         &lt;element name="Week3" type="{http://edenred.se/}ArrayOfMenuDetails" minOccurs="0"/>
 *         &lt;element name="Week4" type="{http://edenred.se/}ArrayOfMenuDetails" minOccurs="0"/>
 *         &lt;element name="Offers" type="{http://edenred.se/}ArrayOfOfferDetails" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RestaurantDetails", propOrder = {
    "isFavourite",
    "imageUrl",
    "restaurant",
    "week1",
    "week2",
    "week3",
    "week4",
    "offers"
})
public class RestaurantDetails {

    @XmlElement(name = "IsFavourite")
    protected boolean isFavourite;
    @XmlElement(name = "ImageUrl")
    protected String imageUrl;
    @XmlElement(name = "Restaurant")
    protected RestaurantInfo restaurant;
    @XmlElement(name = "Week1")
    protected ArrayOfMenuDetails week1;
    @XmlElement(name = "Week2")
    protected ArrayOfMenuDetails week2;
    @XmlElement(name = "Week3")
    protected ArrayOfMenuDetails week3;
    @XmlElement(name = "Week4")
    protected ArrayOfMenuDetails week4;
    @XmlElement(name = "Offers")
    protected ArrayOfOfferDetails offers;

    /**
     * Gets the value of the isFavourite property.
     * 
     */
    public boolean isIsFavourite() {
        return isFavourite;
    }

    /**
     * Sets the value of the isFavourite property.
     * 
     */
    public void setIsFavourite(boolean value) {
        this.isFavourite = value;
    }

    /**
     * Gets the value of the imageUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the value of the imageUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageUrl(String value) {
        this.imageUrl = value;
    }

    /**
     * Gets the value of the restaurant property.
     * 
     * @return
     *     possible object is
     *     {@link RestaurantInfo }
     *     
     */
    public RestaurantInfo getRestaurant() {
        return restaurant;
    }

    /**
     * Sets the value of the restaurant property.
     * 
     * @param value
     *     allowed object is
     *     {@link RestaurantInfo }
     *     
     */
    public void setRestaurant(RestaurantInfo value) {
        this.restaurant = value;
    }

    /**
     * Gets the value of the week1 property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMenuDetails }
     *     
     */
    public ArrayOfMenuDetails getWeek1() {
        return week1;
    }

    /**
     * Sets the value of the week1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMenuDetails }
     *     
     */
    public void setWeek1(ArrayOfMenuDetails value) {
        this.week1 = value;
    }

    /**
     * Gets the value of the week2 property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMenuDetails }
     *     
     */
    public ArrayOfMenuDetails getWeek2() {
        return week2;
    }

    /**
     * Sets the value of the week2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMenuDetails }
     *     
     */
    public void setWeek2(ArrayOfMenuDetails value) {
        this.week2 = value;
    }

    /**
     * Gets the value of the week3 property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMenuDetails }
     *     
     */
    public ArrayOfMenuDetails getWeek3() {
        return week3;
    }

    /**
     * Sets the value of the week3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMenuDetails }
     *     
     */
    public void setWeek3(ArrayOfMenuDetails value) {
        this.week3 = value;
    }

    /**
     * Gets the value of the week4 property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMenuDetails }
     *     
     */
    public ArrayOfMenuDetails getWeek4() {
        return week4;
    }

    /**
     * Sets the value of the week4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMenuDetails }
     *     
     */
    public void setWeek4(ArrayOfMenuDetails value) {
        this.week4 = value;
    }

    /**
     * Gets the value of the offers property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfOfferDetails }
     *     
     */
    public ArrayOfOfferDetails getOffers() {
        return offers;
    }

    /**
     * Sets the value of the offers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfOfferDetails }
     *     
     */
    public void setOffers(ArrayOfOfferDetails value) {
        this.offers = value;
    }

}
