
package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="restaurantId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="latitude" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="longitude" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="login" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "restaurantId",
    "latitude",
    "longitude",
    "login",
    "password"
})
@XmlRootElement(name = "GetRestaurantDetails")
public class GetRestaurantDetails {

    protected int restaurantId;
    protected long latitude;
    protected long longitude;
    protected String login;
    protected String password;

    /**
     * Gets the value of the restaurantId property.
     * 
     */
    public int getRestaurantId() {
        return restaurantId;
    }

    /**
     * Sets the value of the restaurantId property.
     * 
     */
    public void setRestaurantId(int value) {
        this.restaurantId = value;
    }

    /**
     * Gets the value of the latitude property.
     * 
     */
    public long getLatitude() {
        return latitude;
    }

    /**
     * Sets the value of the latitude property.
     * 
     */
    public void setLatitude(long value) {
        this.latitude = value;
    }

    /**
     * Gets the value of the longitude property.
     * 
     */
    public long getLongitude() {
        return longitude;
    }

    /**
     * Sets the value of the longitude property.
     * 
     */
    public void setLongitude(long value) {
        this.longitude = value;
    }

    /**
     * Gets the value of the login property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the value of the login property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogin(String value) {
        this.login = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

}
