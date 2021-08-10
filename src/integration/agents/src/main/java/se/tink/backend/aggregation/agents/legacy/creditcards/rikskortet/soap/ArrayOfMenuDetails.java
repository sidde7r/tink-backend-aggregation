package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Java class for ArrayOfMenuDetails complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ArrayOfMenuDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MenuDetails" type="{http://edenred.se/}MenuDetails" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ArrayOfMenuDetails",
        propOrder = {"menuDetails"})
public class ArrayOfMenuDetails {

    @XmlElement(name = "MenuDetails", nillable = true)
    protected List<MenuDetails> menuDetails;

    /**
     * Gets the value of the menuDetails property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the menuDetails property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getMenuDetails().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link MenuDetails }
     */
    public List<MenuDetails> getMenuDetails() {
        if (menuDetails == null) {
            menuDetails = new ArrayList<MenuDetails>();
        }
        return this.menuDetails;
    }
}
