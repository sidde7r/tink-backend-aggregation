package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Java class for ArrayOfRestaurantInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ArrayOfRestaurantInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RestaurantInfo" type="{http://edenred.se/}RestaurantInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ArrayOfRestaurantInfo",
        propOrder = {"restaurantInfo"})
public class ArrayOfRestaurantInfo {

    @XmlElement(name = "RestaurantInfo", nillable = true)
    protected List<RestaurantInfo> restaurantInfo;

    /**
     * Gets the value of the restaurantInfo property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the restaurantInfo property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getRestaurantInfo().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link RestaurantInfo }
     */
    public List<RestaurantInfo> getRestaurantInfo() {
        if (restaurantInfo == null) {
            restaurantInfo = new ArrayList<RestaurantInfo>();
        }
        return this.restaurantInfo;
    }
}
