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
 *         &lt;element name="UseOfferResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"useOfferResult"})
@XmlRootElement(name = "UseOfferResponse")
public class UseOfferResponse {

    @XmlElement(name = "UseOfferResult")
    protected boolean useOfferResult;

    /** Gets the value of the useOfferResult property. */
    public boolean isUseOfferResult() {
        return useOfferResult;
    }

    /** Sets the value of the useOfferResult property. */
    public void setUseOfferResult(boolean value) {
        this.useOfferResult = value;
    }
}
