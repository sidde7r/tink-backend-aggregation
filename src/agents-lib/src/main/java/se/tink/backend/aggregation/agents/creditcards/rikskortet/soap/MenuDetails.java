
package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MenuDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MenuDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RestaurantId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Day" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Dish" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DishDesc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DiscountPrice" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Price" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MenuDetails", propOrder = {
    "restaurantId",
    "day",
    "dish",
    "dishDesc",
    "discountPrice",
    "price"
})
public class MenuDetails {

    @XmlElement(name = "RestaurantId")
    protected String restaurantId;
    @XmlElement(name = "Day")
    protected int day;
    @XmlElement(name = "Dish")
    protected String dish;
    @XmlElement(name = "DishDesc")
    protected String dishDesc;
    @XmlElement(name = "DiscountPrice")
    protected String discountPrice;
    @XmlElement(name = "Price")
    protected String price;

    /**
     * Gets the value of the restaurantId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRestaurantId() {
        return restaurantId;
    }

    /**
     * Sets the value of the restaurantId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRestaurantId(String value) {
        this.restaurantId = value;
    }

    /**
     * Gets the value of the day property.
     * 
     */
    public int getDay() {
        return day;
    }

    /**
     * Sets the value of the day property.
     * 
     */
    public void setDay(int value) {
        this.day = value;
    }

    /**
     * Gets the value of the dish property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDish() {
        return dish;
    }

    /**
     * Sets the value of the dish property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDish(String value) {
        this.dish = value;
    }

    /**
     * Gets the value of the dishDesc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDishDesc() {
        return dishDesc;
    }

    /**
     * Sets the value of the dishDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDishDesc(String value) {
        this.dishDesc = value;
    }

    /**
     * Gets the value of the discountPrice property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDiscountPrice() {
        return discountPrice;
    }

    /**
     * Sets the value of the discountPrice property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDiscountPrice(String value) {
        this.discountPrice = value;
    }

    /**
     * Gets the value of the price property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrice(String value) {
        this.price = value;
    }

}
