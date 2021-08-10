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
 *         &lt;element name="GetOffersResult" type="{http://edenred.se/}ArrayOfOfferDetails" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"getOffersResult"})
@XmlRootElement(name = "GetOffersResponse")
public class GetOffersResponse {

    @XmlElement(name = "GetOffersResult")
    protected ArrayOfOfferDetails getOffersResult;

    /**
     * Gets the value of the getOffersResult property.
     *
     * @return possible object is {@link ArrayOfOfferDetails }
     */
    public ArrayOfOfferDetails getGetOffersResult() {
        return getOffersResult;
    }

    /**
     * Sets the value of the getOffersResult property.
     *
     * @param value allowed object is {@link ArrayOfOfferDetails }
     */
    public void setGetOffersResult(ArrayOfOfferDetails value) {
        this.getOffersResult = value;
    }
}
