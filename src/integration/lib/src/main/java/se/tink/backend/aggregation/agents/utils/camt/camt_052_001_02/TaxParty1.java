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
 * Java class for TaxParty1 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TaxParty1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TaxId" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}Max35Text" minOccurs="0"/>
 *         &lt;element name="RegnId" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}Max35Text" minOccurs="0"/>
 *         &lt;element name="TaxTp" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}Max35Text" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "TaxParty1",
        propOrder = {"taxId", "regnId", "taxTp"})
public class TaxParty1 {

    @XmlElement(name = "TaxId")
    protected String taxId;

    @XmlElement(name = "RegnId")
    protected String regnId;

    @XmlElement(name = "TaxTp")
    protected String taxTp;

    /**
     * Gets the value of the taxId property.
     *
     * @return possible object is {@link String }
     */
    public String getTaxId() {
        return taxId;
    }

    /**
     * Sets the value of the taxId property.
     *
     * @param value allowed object is {@link String }
     */
    public void setTaxId(String value) {
        this.taxId = value;
    }

    /**
     * Gets the value of the regnId property.
     *
     * @return possible object is {@link String }
     */
    public String getRegnId() {
        return regnId;
    }

    /**
     * Sets the value of the regnId property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRegnId(String value) {
        this.regnId = value;
    }

    /**
     * Gets the value of the taxTp property.
     *
     * @return possible object is {@link String }
     */
    public String getTaxTp() {
        return taxTp;
    }

    /**
     * Sets the value of the taxTp property.
     *
     * @param value allowed object is {@link String }
     */
    public void setTaxTp(String value) {
        this.taxTp = value;
    }
}
