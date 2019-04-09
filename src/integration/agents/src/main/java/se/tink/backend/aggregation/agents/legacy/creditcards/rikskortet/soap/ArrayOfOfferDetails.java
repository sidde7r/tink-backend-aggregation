package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for ArrayOfOfferDetails complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ArrayOfOfferDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OfferDetails" type="{http://edenred.se/}OfferDetails" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ArrayOfOfferDetails",
        propOrder = {"offerDetails"})
public class ArrayOfOfferDetails {

    @XmlElement(name = "OfferDetails", nillable = true)
    protected List<OfferDetails> offerDetails;

    /**
     * Gets the value of the offerDetails property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the offerDetails property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getOfferDetails().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link OfferDetails }
     */
    public List<OfferDetails> getOfferDetails() {
        if (offerDetails == null) {
            offerDetails = new ArrayList<OfferDetails>();
        }
        return this.offerDetails;
    }
}
