
package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="GetAccountDetailsResult" type="{http://edenred.se/}AccountDetails" minOccurs="0"/>
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
    "getAccountDetailsResult"
})
@XmlRootElement(name = "GetAccountDetailsResponse")
public class GetAccountDetailsResponse {

    @XmlElement(name = "GetAccountDetailsResult")
    protected AccountDetails getAccountDetailsResult;

    /**
     * Gets the value of the getAccountDetailsResult property.
     * 
     * @return
     *     possible object is
     *     {@link AccountDetails }
     *     
     */
    public AccountDetails getGetAccountDetailsResult() {
        return getAccountDetailsResult;
    }

    /**
     * Sets the value of the getAccountDetailsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountDetails }
     *     
     */
    public void setGetAccountDetailsResult(AccountDetails value) {
        this.getAccountDetailsResult = value;
    }

}
