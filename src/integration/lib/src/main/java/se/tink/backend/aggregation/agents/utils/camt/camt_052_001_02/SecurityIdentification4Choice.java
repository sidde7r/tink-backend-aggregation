//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.03.20 at 11:40:02 AM CET
//

package se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Java class for SecurityIdentification4Choice complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SecurityIdentification4Choice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="ISIN" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ISINIdentifier"/>
 *           &lt;element name="Prtry" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}AlternateSecurityIdentification2"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "SecurityIdentification4Choice",
        propOrder = {"isin", "prtry"})
public class SecurityIdentification4Choice {

    @XmlElement(name = "ISIN")
    protected String isin;

    @XmlElement(name = "Prtry")
    protected AlternateSecurityIdentification2 prtry;

    /**
     * Gets the value of the isin property.
     *
     * @return possible object is {@link String }
     */
    public String getISIN() {
        return isin;
    }

    /**
     * Sets the value of the isin property.
     *
     * @param value allowed object is {@link String }
     */
    public void setISIN(String value) {
        this.isin = value;
    }

    /**
     * Gets the value of the prtry property.
     *
     * @return possible object is {@link AlternateSecurityIdentification2 }
     */
    public AlternateSecurityIdentification2 getPrtry() {
        return prtry;
    }

    /**
     * Sets the value of the prtry property.
     *
     * @param value allowed object is {@link AlternateSecurityIdentification2 }
     */
    public void setPrtry(AlternateSecurityIdentification2 value) {
        this.prtry = value;
    }
}
