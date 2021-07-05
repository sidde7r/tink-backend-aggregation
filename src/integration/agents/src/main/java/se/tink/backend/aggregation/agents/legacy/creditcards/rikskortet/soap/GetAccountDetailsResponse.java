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
 *         &lt;element name="GetAccountDetailsResult" type="{http://edenred.se/}AccountDetails" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"getAccountDetailsResult"})
@XmlRootElement(name = "GetAccountDetailsResponse")
public class GetAccountDetailsResponse {

    @XmlElement(name = "GetAccountDetailsResult")
    protected AccountDetails getAccountDetailsResult;

    /**
     * Gets the value of the getAccountDetailsResult property.
     *
     * @return possible object is {@link AccountDetails }
     */
    public AccountDetails getGetAccountDetailsResult() {
        return getAccountDetailsResult;
    }

    /**
     * Sets the value of the getAccountDetailsResult property.
     *
     * @param value allowed object is {@link AccountDetails }
     */
    public void setGetAccountDetailsResult(AccountDetails value) {
        this.getAccountDetailsResult = value;
    }
}
